import com.leapmotion.leap.*;
import processing.core.*;

public class MapLeapPointsToScreen
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
  
  private Vector leapRange;
  private Vector screenRange;
  
  
  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/
  
  public MapLeapPointsToScreen(Vector leapMin, Vector leapMax, Vector screenMin, Vector screenMax)
  {
    
    // Instansiate space sizes
    this.leapMin = leapMin;
    this.leapMax = leapMax;
    this.screenMin = screenMin;
    this.screenMax = screenMax;
    
    this.leapRange = this.leapMax.minus(this.leapMin);
    this.screenRange = this.screenMax.minus(this.screenMin);
  }
  
  /**************************************************************************************************/
  //
  /* convert method, converts a 3D set of points from the real space to screen space 
  //
  /**************************************************************************************************/
  
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
}
