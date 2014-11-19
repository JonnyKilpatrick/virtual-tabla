import com.leapmotion.leap.*;
import processing.core.*;

/**
 * Class to store a Midi style message, including velocity and position on the drum
 */
 
public class MidiMessage
{
  /**************************************************************************************************/
  //
  /* Instance variables 
  //
  /**************************************************************************************************/
  
  private byte velocity;   // Velocity of the message
  private byte note;       // The note to play 
  private TablaDrum drum; // Which drum: left/right/neither
  
  
  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/
  
  /**
   * Constructor for the class
   * @param velocity int the veloicity for the note between 0-127
   * @param note int the note to play
   */
   
  public MidiMessage(int velocity, int note, TablaDrum drum)
  {
    if (velocity >127)
    {
      this.velocity = 127;
    }
    else if (velocity < 0)
    {
      velocity = 0;
    }
    else
    {
      this.velocity = (byte) velocity;
    }
    
    
    if (note >127)
    {
      this.note = 127;
    }
    else if (note < 0)
    {
      note = 0;
    }
    else
    {
      this.note = (byte) note;
    }
    
    this.drum = drum;

  }
  
  
  
  /**************************************************************************************************/
  //
  /* Accessor methods 
  //
  /**************************************************************************************************/
  
  /**
   * Get the velocity
   * @return int the veloicity for the note between 0-127
   */
   
  public byte getVelocity()
  {
    return velocity;
  }
  
  
  /**
   * Get the note
   * @return int the note between 0-127
   */
   
  public byte getNote()
  {
    return note;
  }
  
  /**
   * Get the Drum
   * @return TableDrum the drum that has been hit left/right/neither
   */
   
  public TablaDrum getDrum()
  {
    return drum;
  }
 
}
