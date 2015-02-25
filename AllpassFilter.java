/**
 * Implementation for an Allpass filter with a given coefficient.
 */
 
public class AllpassFilter implements IFilter
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
    this.coefficient = coefficient;
    lastInput = 0;
    lastOutput = 0;
  }
  
  
  /**************************************************************************************************/
  //
  /* Process  
  //
  /**************************************************************************************************/
  /**
   * Process one sample through the filter
   * @param sample double the input sample value
   * @return double the output value from the filter
   */
   
   public double processSample(double sample)
   {
     double newSample = (coefficient*sample) + lastInput - (coefficient*lastOutput);
     lastInput = sample;
     lastOutput = newSample;
     return newSample;
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
