import com.leapmotion.leap.*;
import processing.core.*;

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
  
  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/
  
  public Gesture(int fingerId, int framesVisable, Vector initialPosition)
  {
    this.fingerId = fingerId;
    this.framesVisable = framesVisable;
    this.initialPosition = initialPosition;
  }
  
  /**************************************************************************************************/
  //
  /* Accessor methods 
  //
  /**************************************************************************************************/
  
  public int getId()
  {
    return fingerId;
  }
  
  public int framesVisable()
  {
    return framesVisable;
  }

  /**************************************************************************************************/
  //
  /* getLength - length of the gesture movement in the Y axis given the endPosition (current position)  
  //
  /**************************************************************************************************/
  
  public float getLength(Vector endPosition)
  {
    return Math.abs(initialPosition.distanceTo(endPosition));
  }
  
  
  /**************************************************************************************************/
  //
  /* Add one to number of frames this gesture has been in motion
  //
  /**************************************************************************************************/
  
  public void addNoFramesSeen()
  {
    framesVisable++;
  }
  
  
}
