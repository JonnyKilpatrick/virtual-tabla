import com.leapmotion.leap.*;
import processing.core.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

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
  
  
  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/
  
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
  
  public boolean checkForGestures(Finger finger)
  {
    // Get the finger id, velocity, position and velocity
    int id = finger.id();
    float velocity = finger.tipVelocity().getY();
    Vector position = finger.tipPosition();
    
    // if this finger already has a current gesture
    
    if (gestures.containsKey(id))
    {
      // If current velocity is greater than the threshold, update the gesture object
      
      Gesture gesture = gestures.get(id);
      
      if(velocity <= velocityThreshold)
      {
        // Update gesture
        gesture.addNoFramesSeen();
      }
      
      // Else if valid gesture in time and movement length play sound
      
      else if (gesture.framesVisable() >= timeThreshold && 
               gesture.getLength(position) >= lengthThreshold && 
               positionPaused(position) == false)
      {
        // Add rest period for this position
        pausedPositions.put(position, finger.frame().timestamp() + restTimeBetweenHits);
        
        // Remove gesture as sound will be triggered and gesture is over
        gestures.remove(id);
        
        // Return true to signify that sound should be played
        return true;
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
      // Create new gesture
      gestures.put(id, new Gesture(id, 0, position));
    }
    
    return false;
    
  }
  
  
  /**************************************************************************************************/
  //
  /* cleanUpFingerMap - method to remove any fingers lost between frames that didn't get removed 
  // because the velocity reduced
  //
  /**************************************************************************************************/
  
  public void cleanUpFingerMap(Frame frame)
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
  
  /**************************************************************************************************/
  //
  /* positionPaused - returns whether the given position is paused determining if a sound has 
  // recently been played near this position
  //
  /**************************************************************************************************/
  
  private boolean positionPaused(Vector position)
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
    // Else return false
    return false;
  }
  
  
  
  
}
