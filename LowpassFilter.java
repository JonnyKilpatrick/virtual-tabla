/**
 * Implementation for an LowPass filter with given shortening / stretching factors.
 */
 
public class LowpassFilter implements IFilter
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
    this.shorteningFactor = shorteningFactor;
    this.stretchingFactor = stretchingFactor;
    stretchingFactorTakeOne = 1 - stretchingFactor;
    lastInput = 0;
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
     double newSample = shorteningFactor * ((stretchingFactorTakeOne * sample) + (stretchingFactor * lastInput));
     lastInput = sample;
     return newSample;
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
