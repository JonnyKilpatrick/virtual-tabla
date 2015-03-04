import com.jsyn.*;
import com.jsyn.data.*;
import com.jsyn.unitgen.*;
import com.jsyn.util.*;

/**
 * Implementation for an Allpass filter with a given coefficient.
 s* Has one input and one output, so extends UnitFilter
 */
 
public class AllpassFilter extends UnitFilter
{
  
  /**************************************************************************************************/
  //
  /* Instance variables 
  //
  /**************************************************************************************************/
  
  private double coefficient;
  private double lastInput;
  private double lastOutput;
  
  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/
  /**
   * Class constructor
   * @param coeffient double the coeffient for the allpass filter
   */
   
  public AllpassFilter(double coefficient)
  {
    super();
    this.coefficient = coefficient;
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
     
     for(int i=start; i<limit; i++)
     {
       double sample = inputs[i];
       double newSample = (coefficient*sample) + lastInput - (coefficient*lastOutput);
       lastInput = sample;
       lastOutput = newSample;
       outputs[i] = newSample;
     }
   }
   
  /**************************************************************************************************/
  //
  /* SetCoefficient  
  //
  /**************************************************************************************************/
  /**
   * Update the coeffieient of the filter
   * @param sample coefficient the new coefficient value
   */
   
   public void setCoefficient(double coefficient)
   {
     this.coefficient = coefficient;
   }
  
}
