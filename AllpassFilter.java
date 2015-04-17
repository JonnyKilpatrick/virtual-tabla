import com.jsyn.*;
import com.jsyn.data.*;
import com.jsyn.unitgen.*;
import com.jsyn.util.*;
import com.jsyn.ports.UnitInputPort;

/**
 * Implementation for an Allpass filter with a given coefficient.
 * Has one input and one output, so extends UnitFilter
 */
 
public class AllpassFilter extends UnitFilter
{
  
  /**************************************************************************************************/
  //
  /* Instance variables 
  //
  /**************************************************************************************************/
  
  private double lastInput;
  private double lastOutput;
  
  // Port for coefficient changes
  public UnitInputPort coefficient;
  
  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/
  /**
   * Class constructor
   * @param coeffient double the coeffient for the allpass filter
   */
   
  public AllpassFilter()
  {
    super();
    addPort(coefficient = new UnitInputPort("Coefficient", 1.0));
    lastInput = 0;
    lastOutput = 0;
  }
  
  
  /**************************************************************************************************/
  //
  /* Generate  
  //
  /**************************************************************************************************/
  /**
   * Process one sample through the filter
   * @param int start
   * @param int limit
   */

   public void generate(int start, int limit)
   {
     // Get inputs from ports
     double[] inputs = input.getValues();
     double[] outputs = output.getValues();
     
     // Get current coefficient
     double c = coefficient.getValue();
     
     for(int i=start; i<limit; i++)
     {
       double sample = inputs[i];
       double newSample = (c*sample) + lastInput - (c*lastOutput);
       lastInput = sample;
       lastOutput = newSample;
       outputs[i] = newSample;
     }
   }
  
}
