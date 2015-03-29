import org.jtransforms.fft.DoubleFFT_1D;
import java.util.Arrays;

/**
 * Computes Spectrogram of the given audio samples, returning array of frequency contents over time
 */
 
public class Spectrogram
{
  /**************************************************************************************************/
  //
  /* Instance variables 
  //
  /**************************************************************************************************/ 
  
  private DoubleFFT_1D fft;        // The JTransforms library Fast Fourier Transform implementation
  private int windowSize;          // Size of FFT data
  private int stepSamples;         // Number of samples to move the window along the whole data
  
  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/
  /**
   * Class constructor
   * @param windowSize int the size of the window which is then put through hanning window and used to compute fft
   * @param stepSamples int the number of samples to move the window along
   */
  
  public Spectrogram(int windowSize, int stepSamples)
  {
    this.windowSize = windowSize;
    this.stepSamples = stepSamples;
    fft = new DoubleFFT_1D(windowSize);
  }
  
  /**************************************************************************************************/
  //
  /* spectrogram 
  //
  /**************************************************************************************************/
  /**
   * Computes the spectrogram for the given audio data
   * @param samples double[] the audio data samples to analyse
   */
   
  public double[][] spectrogram(double[] samples)
  {
    // Check the inputs
    if(samples.length < 1 || windowSize < 1 || stepSamples < 1)
    {
      throw new IllegalArgumentException("Invalid arguments, window size must be less than sample length");
    }
    
    // Create array to store spectrogram
    double[][] results = new double[samples.length / stepSamples][windowSize / 2];
    
    // For each window along the samples, put through windowing function, compute fft, convert to power spectrum and add to array
      
    for(int i=0; i<samples.length; i += stepSamples)
    {
      // If current window falls off the end of the samples, end the loop
      if((i + windowSize - 1) >= samples.length)
      {
        break;
      }
      
      // Get the sample in this current window
      double[] window = Arrays.copyOfRange(samples, i, i+windowSize);
      
      // Put the samples through Hanning function
      window = hanningFunction(window);
       
      // Convert the window data into Complex form, real + imaginary with the imaginary parts set to 0
      double[] complexData = new double[window.length * 2];
      
      for(int j=0; j<window.length; j++)
      {
        complexData[j * 2] = window[j];  // Real
        complexData[(j * 2) + 1] = 0;    // Imaginary
      }
      
      // Now compute the fft
      fft.complexForward(complexData);
      
      // Compute the power spectrum by combining the real / complex results, updating the results array
      // Only need the first half as the second half is just a reflection
      
      // Set the 0Hz bin to 0 as is useless data
      results[i / windowSize][0] = 0;
      
      for(int j=1; j<window.length / 2; j++)
      {
        results[i / windowSize][j] = Math.sqrt(Math.pow(complexData[j * 2], 2) + Math.pow(complexData[(j * 2) + 1], 2));
      }
    }
    
    return results;
  }

  
  /**************************************************************************************************/
  //
  /* hanningWindow 
  //
  /**************************************************************************************************/
  /**
   * Multiples the given samples by the hann windowing function
   * @param samples double[] the audio data to apply the windowing function
   */
   
  private double[] hanningFunction(double[] samples)
  {
    // Get size of data
    int numSamples = samples.length;
    
    // For each sample, multiply by the hanning function
    for(int n=0; n<numSamples; n++)
    {
      samples[n] = 0.5 * (1 - Math.cos((2 * Math.PI * samples[n]) / numSamples-1));
    }
    
    // Return updated array
    return samples;
  }
  
  
  /**************************************************************************************************/
  //
  /* distance 
  //
  /**************************************************************************************************/
  /**
   * Computes the average difference between two spectrograms, given as a 2d array, time vs frequency
   * @param spectrogram1 double[][] the first spectrogram
   * @param spectrogram2 double[][] the second spectrogram
   */
   
  public static double distance(double[][] spectrogram1, double[][] spectrogram2)
  {
    // For each window (frequency spectrum), compute the average distance then average all of the averages
    
    double totalDistance = 0;
    
    for(int i=0; i<spectrogram1.length; i++)
    {
      double totalDistanceN = 0;
      
      for(int j=0; j<spectrogram1[i].length; j++)
      {
        // Total distance of one spectrum
        totalDistanceN += (Math.abs(spectrogram1[i][j] - spectrogram2[i][j]));
      }
      
      // Add one spectrum average distance to the totalDistance
      totalDistance += totalDistanceN / spectrogram1[i].length;
    }
    
    // Return the average distance across the different windows in time
    return totalDistance / spectrogram1.length;
  }
  
}
