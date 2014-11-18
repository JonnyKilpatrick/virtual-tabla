import com.leapmotion.leap.*;
import processing.core.*;

/**
 * Class to convert real world co-ordinates from the leap motion sensor to screen space, and
 * from real world to Midi messages
 */
   
public class MapLeapPoints
{
  
  /**************************************************************************************************/
  //
  /* Instance variables 
  //
  /**************************************************************************************************/
  
  private Vector leapMin;
  private Vector leapMax;
  private Vector screenMin;  
  private Vector screenMax;
  
  private float minimumVelocity;
  private float maximumVelocity;
  private Vector leftDrumCenter;
  private float leftDrumRadius;
  private Vector rightDrumCenter;
  private float rightDrumRadius;
  
  
  private Vector leapRange;
  private Vector screenRange;
  
  private float velocityRange;
  
  
  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/
  
  /**
   * Constructor for the class
   * @param leapMin Vector the minimum in x,y,z of your chosen defined real world space
   * @param leapMax Vector the maximum in x,y,z of your chosen defined real world space
   * @param screenMin Vector the minimum in x,y,z of the chosen screen space size
   * @param screenMax Vector the maximum in x,y,z of the chosen screen space size
   * @param minumumVelocity float the minimum velocity of a valid gesture
   * @param maximumVelocity float the highest point in the range of possible velocities
   * @param leftDrumCenter Vector the center of the left drum in screen space
   * @param leftDrumRadius float the radius of the left drum
   * @param rightDrumCenter Vector the center of the right drum in screen space
   * @param rightDrumRadius float the radius of the right drum
   */
   
  public MapLeapPoints(Vector leapMin, Vector leapMax, Vector screenMin, Vector screenMax,
                       float minimumVelocity, float maximumVelocity, 
                       Vector leftDrumCenter, float leftDrumRadius, Vector rightDrumCenter, float rightDrumRadius)
  {
    
    // Initialise space sizes
    this.leapMin = leapMin;
    this.leapMax = leapMax;
    this.screenMin = screenMin;
    this.screenMax = screenMax;
    
    this.leapRange = this.leapMax.minus(this.leapMin);
    this.screenRange = this.screenMax.minus(this.screenMin);
    
    // Initialise velocities and circle properties for Midi converting
    this.minimumVelocity = minimumVelocity * -1;
    this.maximumVelocity = maximumVelocity * -1;
    this.leftDrumCenter = leftDrumCenter;
    this.leftDrumRadius = leftDrumRadius;
    this.rightDrumCenter = rightDrumCenter;
    this.rightDrumRadius = rightDrumRadius;
    
    this.velocityRange = this.maximumVelocity - this.minimumVelocity;
  }
  
  /**************************************************************************************************/
  //
  /* convert method, converts a 3D set of points from the real space to screen space 
  //
  /**************************************************************************************************/
  
  /**
   * Converts the given real world co-ordinates into screen space
   * @param realCoordinates Vector the real world co-ordinates to convert
   * @retrun Vector the converted screen space co-ordinates
   */
   
  public Vector convertLeapCoordinates(Vector realCoordinates)
  {
    // Initialise the normalised vector
    float[] normalisedArray = new float[3];
    
    // Initialise the screen vector
    float[] screenArray = new float[3];
    
    for(int i=0; i<=2; i++)
    {
      // Normalise to get point between 0 and 1
      normalisedArray[i] = (realCoordinates.get(i) - leapMin.get(i)) / (leapRange.get(i));
      
      // Translate to screen space
      screenArray[i] = screenMin.get(i) + (normalisedArray[i] * screenRange.get(i)); 
    }
    
    return new Vector(screenArray[0], screenArray[1], screenArray[2]);
  }
  
  
  
  /**************************************************************************************************/
  //
  /* Convert to MidiMessage
  //
  /**************************************************************************************************/
  
  /**
   * Converts a gesture to Midi like messages for the velocity and which note
   * @param realCoordinates Vector the real world co-ordinates to convert
   * @retrun MidiMessage the converted Midi message
   */
   
  public MidiMessage convertToMidiMessage(Gesture gesture)
  {    
    /********************************************************************************/
    /* Firstly, convert the velocity into the range 0-127
    /********************************************************************************/

    int velocity = Math.round(((gesture.getFastestVelocity() - minimumVelocity) / velocityRange) * 127);

    /********************************************************************************/
    /* Secondly, convert the note between 0-127 and determine which drum it is
    /********************************************************************************/
    
    // convert to screen space
    Vector position = gesture.getEndPosition(); 
    position = new Vector(position.getX(), position.getZ(), position.getY()); // Flip y and z for this system
    position = convertLeapCoordinates(position);
    
    // Work out if the left drum has been hit
    
    float xLength = position.getX() - leftDrumCenter.getX();
    float yLength = position.getY() - leftDrumCenter.getY();
    float distance = (float) Math.sqrt((xLength * xLength) + (yLength * yLength));
    
    // If left drum has been hit
    if (distance <= leftDrumRadius)
    {
      // Calculate note between 0-127 and return whole Midi message
      int note = Math.round((distance/leftDrumRadius) * 127);
      return new MidiMessage(velocity, note, TablaDrum.LEFT);
    }
    // Else check if right drum is hit
    else
    {
      xLength = position.getX() - rightDrumCenter.getX();
      yLength = position.getY() - rightDrumCenter.getY();
      distance = (float) Math.sqrt((xLength * xLength) + (yLength * yLength));

      if (distance <= rightDrumRadius)
      {
        // Calculate note between 0-127 and return whole Midi message
        int note = Math.round((distance/rightDrumRadius) * 127);
        return new MidiMessage(velocity, note, TablaDrum.RIGHT);
      } 
    }
    
    // If reached here, no drum was hit return null
    
    return null;
  }
  
}

