import org.uncommons.maths.binary.BitString;
import org.uncommons.watchmaker.framework.FitnessEvaluator;
import java.util.List;
import java.util.Random;
import java.io.File;
import java.io.IOException;
import com.jsyn.*;
import com.jsyn.data.*;
import com.jsyn.unitgen.*;
import com.jsyn.util.*;

/**
 * Implementation of the FitnessEvaluator for the Genetic algorithm, given a possible solution, 
 * returns a fitness, calculated by comparing the fft spectrum of the target and produced sound
 */
 
public class WaveguideFitnessEvaluator implements FitnessEvaluator<BitString>
{
  /**************************************************************************************************/
  //
  /* Constants 
  //
  /**************************************************************************************************/
  
  private static final int TOTAL_STRING_LENGTH = 72;
  private static final int FREQUENCY_INT_BITS = 13;
  private static final int FREQUENCY_FRAC_BITS = 8;
  private static final int Q_INT_BITS = 11;
  private static final int Q_FRAC_BITS = 8;
  private static final int AMP_FRAC_BITS = 16;
  private static final int GAIN_FRAC_BITS = 16;
  
  
  /**************************************************************************************************/
  //
  /* Instance variables 
  //
  /**************************************************************************************************/

  private int numWaveguides;
  private double[] targetSamples;
  
  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/
  /**
   * Class constructor
   * @param coeffient double the coeffient for the allpass filter
   */
   
  public WaveguideFitnessEvaluator(int numWaveguides, String targetAudioFilePath) throws IOException
  {
    this.numWaveguides = numWaveguides;
    this.targetSamples = readAudioFromFile(targetAudioFilePath);
  }
  
  public static void main(String[] args)
  {
    WaveguideFitnessEvaluator e = new WaveguideFitnessEvaluator(1, "C:/Users/Jon/Documents/Computer Science/Third Year/Dissertation/Code/VirtualTablaSynthesiser/Samples/High/21_14_01.AIF");
    e.getFitness(new BitString(72, new Random()), null);
  }
  
  
  /**************************************************************************************************/
  //
  /* getFitness  
  //
  /**************************************************************************************************/
  /**
   * Process one sample through the filter
   * @param candidate String the current candidate to evaluate
   * @param population List<> the entire population (not needed for this implementation)
   * @return the fitness score
   */

  public double getFitness(BitString candidate, List<? extends BitString> population)
  {
    // Convert binary representation to the parameters needed
    WaveguideParameters[] parameters = convertToParameters(candidate.toString());
    
    for(int i=0; i<numWaveguides; i++)
    {
      System.out.println(parameters[i].getCenterFrequency() + ", " + parameters[i].getAmplitude() + ", " + parameters[i].getQ() + ", " + parameters[i].getGain());
    }
    return 0;
  }
   
  /**************************************************************************************************/
  //
  /* isNatural  
  //
  /**************************************************************************************************/
  /**
   * Simply states that the fitness function returns lower scores for 'fitter' solution
   * @return boolean
   */
   
  public boolean isNatural()
  {
    return false;   
  }
  
  /**************************************************************************************************/
  //
  /* convertToParameters  
  //
  /**************************************************************************************************/
  /**
   * Converts binary string to the 4 parameters of a banded waveguide
   * @return WaveguideParameters[] an array of the parameters for each waveguide in the banded waveguides
   */
   
  private WaveguideParameters[] convertToParameters(String bitString)
  {
    // Initialise array
    WaveguideParameters[] params = new WaveguideParameters[numWaveguides];
    
    // For each waveguide, convert to the 4 parameters and add to array
    for(int i=0; i<numWaveguides; i++)
    {
      
      // Get the corresponding set of bits for this waveguide from the whole string
      String bits = bitString.substring((i*TOTAL_STRING_LENGTH), ((i+1)*TOTAL_STRING_LENGTH)-1);
      
      // Get centre frequency
      double frequency = fixedPointBinaryToDouble(
        bits.substring(0, FREQUENCY_INT_BITS + FREQUENCY_FRAC_BITS - 1), 
        FREQUENCY_INT_BITS, 
        FREQUENCY_FRAC_BITS);
      
      // Get bandwidth
      double bandwidth = fixedPointBinaryToDouble(
        bits.substring(FREQUENCY_INT_BITS + FREQUENCY_FRAC_BITS, FREQUENCY_INT_BITS + FREQUENCY_FRAC_BITS + Q_INT_BITS + Q_FRAC_BITS - 1), 
        Q_INT_BITS, 
        Q_FRAC_BITS);
      
      double amplitude = fixedPointBinaryToDouble(
        bits.substring(FREQUENCY_INT_BITS + FREQUENCY_FRAC_BITS + Q_INT_BITS + Q_FRAC_BITS, FREQUENCY_INT_BITS + FREQUENCY_FRAC_BITS + Q_INT_BITS + Q_FRAC_BITS + AMP_FRAC_BITS - 1), 
        0, 
        AMP_FRAC_BITS);
        
      double gain = fixedPointBinaryToDouble(
        bits.substring(FREQUENCY_INT_BITS + FREQUENCY_FRAC_BITS + Q_INT_BITS + Q_FRAC_BITS + AMP_FRAC_BITS, FREQUENCY_INT_BITS + FREQUENCY_FRAC_BITS + Q_INT_BITS + Q_FRAC_BITS + AMP_FRAC_BITS + GAIN_FRAC_BITS - 1), 
        0, 
        GAIN_FRAC_BITS);
        
      // Add parameters to array
      params[i] = new WaveguideParameters(frequency, amplitude, bandwidth, gain);      
    }
    
    return params;
  }
  
  /**************************************************************************************************/
  //
  /* fixedPointBinaryToDouble
  //
  /**************************************************************************************************/
  /**
   * Converts fixed point binary string to double
   * @param bitString String the bit string to convert
   * @param numIntBits int the number of fixed integer bits
   * @param numFracBits int the number of fixed fractional bits
   * @return double the decimal value
   */
   
  private double fixedPointBinaryToDouble(String bitString, int numIntBits, int numFracBits)
  { 
    double result = 0;
    
    // Get int part
    if(numIntBits > 0)
    {
      result = (double) Integer.parseInt(bitString.substring(0,numIntBits-1), 2);
    }
    
    if(numFracBits > 0)
    {
      // Add the fractional part
      String fracBits = bitString.substring(numIntBits, numIntBits + numFracBits - 1);
      
      for(int i=-1; i>(-1-fracBits.length()); i--)
      {
        result += (Character.getNumericValue(bitString.charAt(Math.abs(i)-1)) * Math.pow(2, i));
      }
    }
    
    return result;
  }
  
  
  /**************************************************************************************************/
  //
  /* readAudioFromFile
  //
  /**************************************************************************************************/
  /**
   * Converts fixed point binary string to double
   * @param filePath String the path of the target audio file sample
   * @return double[] the sample data from the file
   */
   
  private double[] readAudioFromFile(String filePath) throws IOException
  { 
    File file = new File(filePath);
    FloatSample sample = SampleLoader.loadFloatSample(file);
    
    // Return the sample as a double array
    float[] data = new float[sample.getNumFrames()];
    
    sample.read(0, data, 0, data.length);
    
    // Convert to double
    double[] result = new double[data.length];
    for(int i=0; i<data.length; i++)
    {
      result[i] = (double) data[i];
      System.out.println(result[i] + " : " + data[i]);
    }
    
    return data;
  }
  
}
