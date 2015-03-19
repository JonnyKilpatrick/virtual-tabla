/**
 * Simple Class for a set of parameters required to set up a single banded waveguide, 
 * including bandpass parameters (center frequency, gain, q). Center frequency is also frequency 
 * of delay line so can work out integer number delay length, and also the allpass coefficients from
 * this.
 */
 

public class WaveguideParameters
{
  
  /**************************************************************************************************/
  //
  /* Instance variables 
  //
  /**************************************************************************************************/

  private double centerFrequency;
  private double amplitude;
  private double q;
  private double gain;

  
  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/
  
  /**
   * Class constructor
   * @param centerFrequency double the center frequency of the bandpass
   * @param amplitude double the amplitude of this frequency initially
   * @param bandwidth double the bandwidth of the bandpass
   * @param gain double the gain for the bandpass
   */
  public WaveguideParameters(double centerFrequency, double amplitude, double bandwidth, double gain)
  {
    this.centerFrequency = centerFrequency;
    this.amplitude = amplitude;
    this.q = centerFrequency / bandwidth;
    this.gain = gain;;
  }
  
  /**************************************************************************************************/
  //
  /* Accessor methods 
  //
  /**************************************************************************************************/

  /**
   * @return double the center frequency of the band pass filter
   */
  public double getCenterFrequency()
  {
    return centerFrequency;
  }  
  
  /**
   * @return double the initial amplitude
   */
  public double getAmplitude()
  {
    return amplitude;
  }   

  /**
   * @return double q the bandwidth of the band pass filter
   */
  public double getQ()
  {
    return q;
  }  

  /**
   * @return double the gain of the bandpass
   */
  public double getGain()
  {
    return gain;
  }  
  
  
  /**************************************************************************************************/
  //
  /* Mutator methods 
  //
  /**************************************************************************************************  

  /**
   * @param centerFrequency double the center frequency of the band pass filter
   */
  public void setCenterFrequency(double centerFrequency)
  {
    this.centerFrequency = centerFrequency;
  }  
  
  /**
   * @param double the initial amplitude
   */
  public void setAmplitude(double amplitde)
  {
    this.amplitude = amplitude;
  } 

  /**
   * @param q double the bandwidth of the band pass filter
   */
  public void setBandWidth(double bandwidth)
  {
    this.q = centerFrequency / bandwidth;
  }  

  /**
   * @param gain double the gain of the bandpass
   */
  public void setGain(double gain)
  {
    this.gain = gain;
  }  
}
