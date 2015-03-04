import com.jsyn.*;
import com.jsyn.data.*;
import com.jsyn.unitgen.*;
import com.jsyn.util.*;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.unitgen.UnitGenerator;

/**
 * Implementation for an LowPass filter with given shortening / stretching factors.
 * Has one input and one output, so extends UnitFilter
 */
 
public class LowpassFilter extends UnitFilter
{
  
  /**************************************************************************************************/
  //
  /* Instance variables 
  //
  /**************************************************************************************************/
  
  private double shorteningFactor;
  private double stretchingFactor;
  private double stretchingFactorTakeOne;
  private double lastInput;
  
  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/
  /**
   * Class constructor
   * @param coeffient double the coeffient for the allpass filter
   */
   
  public LowpassFilter(double shorteningFactor, double stretchingFactor)
  {
    super();
    this.shorteningFactor = shorteningFactor;
    this.stretchingFactor = stretchingFactor;
    stretchingFactorTakeOne = 1 - stretchingFactor;
    lastInput = 0;
  }
  
  
  /**************************************************************************************************/
  //
  /* Generate  
  //
  /**************************************************************************************************/
  /**
   * Process one sample through the filter, overwridden from UnitGenerator
   * @param start int
   * @param limit int 
   */
   
   @Override
   public void generate(int start, int limit)
   { 
     // Get input from ports
     double[] inputs = input.getValues();
     double[] outputs = output.getValues();
     
     for(int i=start; i<limit; i++)
     {
       double sample = inputs[i];
       double newSample = shorteningFactor * ((stretchingFactorTakeOne * sample) + (stretchingFactor * lastInput));
       lastInput = sample;
       outputs[i] = newSample;
     }
   }
   
  /**************************************************************************************************/
  //
  /* SetParameters  
  //
  /**************************************************************************************************/
  /**
   * Update the parameters of the filter
   * @param shorteningFactor double the new shortening factor
   * @param stretchingFactor double the new stretching factor
   */
   
   public void setParameters(double shorteningFactor, double stretchingFactor)
   {
     this.shorteningFactor = shorteningFactor;
     this.stretchingFactor = stretchingFactor;
     stretchingFactorTakeOne = 1 - stretchingFactor;
   }
  
}
