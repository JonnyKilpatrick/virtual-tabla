import com.leapmotion.leap.*;
import processing.core.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Class to recognise gestures in the Leap motion sensor for each finger 
 */
 
public class GestureRecogniser
{
  /**************************************************************************************************/
  //
  /* Instance variables 
  //
  /**************************************************************************************************/
  
  // Map of current gestures being tracked, key = fingerId, value = Gesture object
  private Map<Integer, Gesture> gestures;
  
  // Threshold for deciding whether the speed of a movement is fast enough to be considered gesture
  private float velocityThreshold;
  
  // Threshold for the minimum number of frames a guesture must last to be a valid gesture
  private float timeThreshold;
 
  // Threshold for the minimum length of finger movement for a gesture to be valid
  private float lengthThreshold; 
  
  // Time to wait after a sound is triggered before a second sound is triggered in the same area 
  // of the drum
  private long restTimeBetweenHits;
  
  // After a sound is triggered the distance between the point and new points in which to pause 
  // new hits (in x and y only)
  private float distanceToPauseHits;
  
  // Map of the positions of recently played sounds, and their timestamp of time triggered
  // Used to pause new hits being triggered in the same position just after
  private Map<Vector, Long> pausedPositions;
  
  // The last position of a valid gesture, used for pausing the last if it resulted in triggering a sound
  private Vector lastGesturePosition;
  
  
  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/
  
  /**
   * Constructor for the class
   * @param velocityThreshold flaot the minimum velcity in the negative Y direction to be considered 
   * a gesture
   * @param timeThreshold float the minimum number of frames a gesture must be at the required 
   * velocity to be considered a gesture
   * @param lengthThreshold float the mimimum length in Y that the gesture must move
   * @param restTimeBetweenHits long the number of frames to wait before a new gesture can be 
   * triggered in the same region - in microseconds
   * @param distanceToPauseHits float the distance in which to stop any other gestures being 
   * recognised in the set restTimeBetweenHits 
   */
   
  public GestureRecogniser(float velocityThreshold, float timeThreshold, float lengthThreshold, 
    long restTimeBetweenHits, float distanceToPauseHits)
  {
    // Initialise map of current gestures
    gestures = new HashMap<Integer, Gesture>();
    
    // Initialise map of currently paused positions
    pausedPositions = new HashMap<Vector, Long>();
    
    // Initialise thresholds
    this.velocityThreshold = velocityThreshold;
    this.timeThreshold = timeThreshold;
    this.lengthThreshold = lengthThreshold;
    this.restTimeBetweenHits = restTimeBetweenHits;
    this.distanceToPauseHits = distanceToPauseHits;
  }
  
  
  /**************************************************************************************************/
  //
  /* extractGestures - method to track a single finger 
  //
  /**************************************************************************************************/
  
  /**
   * Checks whether a valid gesture has occured in a given finger from the leap software, or track for 
   * future gestures
   * @param finger Finger the finger to check whether a gesture has occured / track for future gestures 
   * @return Gesture the gesture found in that finger, if no valid gesture returns null
   */
   
  public Gesture checkForGestures(Finger finger) throws Exception
  {
    try
    {
      // Get the finger id, velocity, position and velocity
      int id = finger.id();
      float velocity = finger.tipVelocity().getY();
      lastGesturePosition = finger.tipPosition();
      
      // if this finger already has a current gesture
      
      if (gestures.containsKey(id))
      {
        // If current velocity is still greater than the threshold, update the gesture object
        
        Gesture gesture = gestures.get(id);
        
        if(velocity <= velocityThreshold)
        {
          // Update gesture
          gesture.addNoFramesSeen();
          
          // If current velocity is faster than previous fastest, update fastest
          if ((velocity * -1) > gesture.getFastestVelocity())
          {
            gesture.setFastestVelocity(velocity);
          }
          
        }
        
        // Else finger has slowed down, if valid gesture in time active and distance moved, and 
        // the position is not currently paused because another gesture has just been triggered here,
        // trigger the gesture
        
        else if (gesture.framesVisable() >= timeThreshold && 
                 gesture.getLength(lastGesturePosition) >= lengthThreshold && 
                 positionPaused(lastGesturePosition) == false)
        { 
          // Update end position
          gesture.setEndPosition(lastGesturePosition);
          
          // Remove gesture as sound will be triggered and gesture is over
          gestures.remove(id);
  
          return gesture;
        }
        
        // Else gesture is not valid so remove from map
        
        else
        {
          gestures.remove(id);
        }      
      }
    
      // Else the finger is not part of a current gesture, so if velocity is greater than threshold, create new gesture
      
      else if (velocity <= velocityThreshold)
      {
        // Create new gesture and initialise fastest velocity
        Gesture gesture = new Gesture(id, 0, lastGesturePosition);
        gestures.put(id, gesture);
        gesture.setFastestVelocity(velocity);
      }
      
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
      throw new Exception("Error: Failed to check for drum hit gestures");
    }
      
    // Return null as no sound should be triggered if we've reached this point
    return null;
    
  }
  
  
  /**************************************************************************************************/
  //
  /* Pause gesture recognition
  //
  /**************************************************************************************************/
  
  /**
   * Pauses a position of a gesture for the set number of frames, in the set distance so no more 
  // gestures will be triggered there
   * @param frame Frame the current frame
   */
  
  public void pausePosition(Finger finger) throws Exception
  {
    // Add rest period for this position
    
    try
    {
      pausedPositions.put(lastGesturePosition, finger.frame().timestamp() + restTimeBetweenHits);
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
      throw new Exception("Error: Failed to pause the position of the latest drum hit");
    }
  }
  
  
  
  /**************************************************************************************************/
  //
  /* cleanUpFingerMap - method to remove any fingers lost between frames that didn't get removed 
  // because the velocity reduced
  //
  /**************************************************************************************************/
  
  /**
   * Cleans up the maps stored, if a position has been paused this method checks whether to start 
   * allowing gestures here, also clears up the list of current gestures removing those that have been 
   * lost. 
   * Must be called after every call to checkForGestures, but not called automatically to increase 
   * performane (play the sound first before cleaning up)
   * @param frame Frame the current frame
   */
   
  public void cleanUpFingerMap(Frame frame) throws Exception
  { 
    try
    {
      // Get the finger list in the frame
      FingerList fingerList = frame.fingers();
      
      // Get the smallest finger id that is present, as we know we can delete all gestures invloving 
      // fingers with smaller ids
      
      int smallestId = fingerList.get(0).id();
      int currentId = 0;
      
      for(int i=0; i<fingerList.count(); i++)
      {
        currentId = fingerList.get(i).id();
        if (currentId < smallestId)
        {
          smallestId = currentId;
        }
      }
      
      // Any gestures below the smallest id can be removed
      Iterator<Map.Entry<Integer, Gesture>> iterator = gestures.entrySet().iterator();
      while(iterator.hasNext())
      {
        // Remove from map if finger no longer present
        Map.Entry<Integer, Gesture> entry = iterator.next();
        
        if(entry.getKey() < smallestId)
        {
          iterator.remove();
        }
      }
      
      // Now clean up the map of currently paused positions
      // For each position in the map, if it's timestamp + the set rest period is greater than the current timestamp,
      // remove the position from the map
      
      Iterator<Map.Entry<Vector, Long>> positionIterator = pausedPositions.entrySet().iterator();
  
      while(positionIterator.hasNext())
      {
        Map.Entry<Vector, Long> entry = positionIterator.next();
        if(entry.getValue() < frame.timestamp())
        {
          positionIterator.remove(); 
        }
      }
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
      throw new Exception("Error: Failed to clean up list of current gestures and pauses");
    }
  }
  
  /**************************************************************************************************/
  //
  /* positionPaused - returns whether the given position is paused determining if a sound has 
  // recently been played near this position
  //
  /**************************************************************************************************/
  
  /**
   * Checks whether a current position is paused
   * @param position the position to check
   * @return boolean whether the position is currently paused. 
   */
   
  private boolean positionPaused(Vector position)
  {
    try
    {
      // For each position in the current paused positions map, if the given position is near enough, 
      // return false, else continue
      for(Vector p : pausedPositions.keySet())
      {
        float xLength = position.getX() - p.getX();
        float yLength = position.getY() - p.getY(); 
        float distance = (float) Math.sqrt((xLength * xLength) + (yLength * yLength));
        
        if(distance < distanceToPauseHits)
        {
          return true;
        }
      }
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
      throw new ArithmeticException("Error: Failed to check whether finger position was paused");
    }
    
    // Else return false
    return false;
  }
  
}
