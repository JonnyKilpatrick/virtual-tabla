import processing.core.*;

/**
 * Simple class for a droplet to display a drum hit
 */
 
public class Droplet
{
  /**************************************************************************************************/
  //
  /* Instance variables 
  //
  /**************************************************************************************************/
  
  private PApplet parent;       // Parent sketch 
  private float radius;         // Radius of the droplet (ellipse)
  private float x;              // X position of the droplet center
  private float y;              // Y position of the droplet center
  private float changeInRadius; // Change in radius size each frame
  
  
  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/
  /**
   * Constructor for the class
   * @param parent PApplet the parent sketch to call ellipse method
   * @param x float the x position of the droplet center
   * @param y float the y position of the droplet center
   * @param startingRadius float the starting radius of the droplet
   * @param changeInRadius flost the increase of decrease in radius each frame
   */
   
  public Droplet(PApplet parent, float x, float y, float startingRadius, float changeInRadius)
  {
    this.parent = parent;
    this.x = x;
    this.y = y;
    radius = startingRadius;
    this.changeInRadius = changeInRadius;
  }


  /**************************************************************************************************/
  //
  /* updateAndDisplay
  //
  /**************************************************************************************************/
  /**
   * Updates the size of the droplet, and displays it
   */
   
  public void updateAndDisplay()
  {
    radius += changeInRadius;
    parent.ellipse(x, y, radius, radius);
  }
  
  /**************************************************************************************************/
  //
  /* getRadius
  //
  /**************************************************************************************************/
  /**
   * Get the current radius
   */
   
  public float getRadius()
  {
    return radius;
  }
 
}
