import com.jsyn.*;
import com.jsyn.data.*;
import com.jsyn.unitgen.*;
import com.jsyn.util.*;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.unitgen.UnitGenerator;

/**
 * Applies overall gain volume to it's input
 */
 
public class OutputGain extends UnitFilter
{
  
  /**************************************************************************************************/
  //
  /* Instance variables 
  //
  /**************************************************************************************************/
  
  public UnitInputPort gain;
  
  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/
  /**
   * Class constructor
   */
   
  public OutputGain()
  {
    super();
    addPort(gain = new UnitInputPort("Gain"));
    
    // Set default value for gain
    gain.set(1);
  }
  
  
  /**************************************************************************************************/
  //
  /* Generate  
  //
  /**************************************************************************************************/
  /**
   * Process one set of inputs and produce an output
   * @param start int
   * @param limit int 
   */
   
   @Override
   public void generate(int start, int limit)
   { 
     // Get number of inputs
     double[] inputs = input.getValues();
     double[] outputs = output.getValues();
     double[] gains = gain.getValues();
     
     for(int i=start; i<limit; i++)
     {
       // Multiply input by gain
       outputs[i] = inputs[i] * gains[i]; 
     }
   }
  
}
