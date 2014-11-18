import com.leapmotion.leap.*;
import processing.core.*;

/**
 * Class for a Gesture, simple class to store the fingerid, number of frames visable, initial position, 
 * end position, and fastest velocity. 
 */
 

public class Gesture
{
  
  /**************************************************************************************************/
  //
  /* Instance variables 
  //
  /**************************************************************************************************/
  
  private int fingerId;
  private int framesVisable;
  private Vector initialPosition;
  private Vector endPosition;
  private float fastestVelocity;

  
  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/
  
  /**
   * Class constructor, takes the fingerid, initial number of frames visable, and the initial position
   * @param fingerId int the id of the finger
   * @param framesVisable int the initial number of frames visable
   * @param initialPosition Vector the initial x,y,z co-ordinate of the gesture when first seen
   */
  public Gesture(int fingerId, int framesVisable, Vector initialPosition)
  {
    this.fingerId = fingerId;
    this.framesVisable = framesVisable;
    this.initialPosition = initialPosition;
    this.endPosition = initialPosition;
    this.fastestVelocity = 0f;
  }
  
  /**************************************************************************************************/
  //
  /* Accessor methods 
  //
  /**************************************************************************************************/
  
  /**
   * Get the finger id
   * @return int the finger id
   */
  public int getId()
  {
    return fingerId;
  }
  
  /**
   * Get the number of frames visable
   * @return int the no. of frames visable
   */
  public int framesVisable()
  {
    return framesVisable;
  }
  
  /**
   * Set the end position of the gesture
   * @param endPosition Vector the endPosition of the gesture
   */
  public void setEndPosition(Vector endPosition)
  {
    this.endPosition = endPosition;
  }
  
  /**
   * Get the end position of the gesture
   * @return Vector the end position of the gesture 
   */
  public Vector getEndPosition()
  {
    return endPosition;
  }
  
  /**
   * Set the current fastest velocity of the gesture
   * @param float the current fasted velocity
   */
  public void setFastestVelocity(float fastestVelocity)
  {
    this.fastestVelocity = fastestVelocity;
  }
  
  /**
   * Get the current fastest velocity seen 
   * @return float the current fastest velocity
   */
  public float getFastestVelocity()
  {
    return fastestVelocity * -1; // return position version as tracked in negative y
  }
  

  /**************************************************************************************************/
  //
  /* getLength - length of the gesture movement in the Y axis given the endPosition (current position)  
  //
  /**************************************************************************************************/
  
  /**
   * Get the length of the gesture in the Y direction, given the endPosition calculated from the 
   * initial position
   * @param Vector endPosition the endPosition to measure against
   * @return float the distance of the gesture in Y
   */
  public float getLength(Vector endPosition)
  {
    return Math.abs(initialPosition.distanceTo(endPosition));
  }
  
  
  /**************************************************************************************************/
  //
  /* Add one to number of frames this gesture has been in motion
  //
  /**************************************************************************************************/
  
  /**
   * Increment the number of frames seen
   */
  public void addNoFramesSeen()
  {
    framesVisable++;
  }
  
  
}
