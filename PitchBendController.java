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
 
public class PitchBendController extends UnitBinaryOperator
{
  
  /**************************************************************************************************/
  //
  /* Instance variables 
  //
  /**************************************************************************************************/
  
  private double samplingRate;       // Sample rate
  
  private boolean pitchBend;         // Whether to perform pitch bend
  private double frequencyPointer1;  // Frequency of first pointer
  private double frequencyPointer2;  // Frequency of second pointer
  private double frequencyStep;      // Size of each step that the read pointers jump
  private double totalSamplesOfBend; // Total number of samples for the pitch bend
  private double numSteps;           // Total number of steps
  private double blendFactor;        // Used in the legato cross fade between pointers, blending from 0 to 1;
  private int count16;               // Counts up to 16, per the sample rate, where read pointers jump every 16 samples
  private long totalCount;            // Total number of samples into the pitch bend
  private boolean currentPointer;    // True when the current pointer being read from is pointer 1
  
  // References to other unit gens to control
  private FilterBandPass bandpass;
  private CircularBuffer buffer;
  private AllpassFilter allpassFilterReader1;
  private AllpassFilter allpassFilterReader2;

  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/
  /**
   * Class constructor
   * @param samplingRate double the sampling rate of the synthesiser, needed to work out how many samples the pitch bend will last
   * @param bandpass FilterBandPass reference to the bandpass for this delay line, needed to update the center frequency as the delay length changes
   * @param buffer CircularBuffer reference to the delay line, needed to update the delay lengths
   * @param allpassFilteredReader1 AllpassFilter reference to the first allpass filter, needed to update the coefficient
   * @param allpassFilteredReader2 AllpassFilter reference to the second allpass filter, needed to update the coefficient
   */
   
  public PitchBendController(double samplingRate, FilterBandPass bandpass, CircularBuffer buffer, AllpassFilter allpassFilterReader1, AllpassFilter allpassFilterReader2)
  {
    super();
    
    // Initialise instance variables 
    this.samplingRate = samplingRate;
    this.bandpass = bandpass;
    this.buffer = buffer;
    this.allpassFilterReader1 = allpassFilterReader1;
    this.allpassFilterReader2 = allpassFilterReader2;
    
    pitchBend = false;
    frequencyPointer1 = 0;
    frequencyPointer2 = 0;
    frequencyStep = 0;
    
    blendFactor = 0;
    count16 = 0;
    totalCount = 0;
    currentPointer = true;
  }
  
  /**************************************************************************************************/
  //
  /* StartBend  
  //
  /**************************************************************************************************/
  /**
   * Start the pitch bend
   * @param frequencyPointer1 double the delay of the first read pointer
   * @param frequencyPointer2 double the delay of the second read pointer
   * @param slideDuration double the length of the pitch bend in samples
   */

   public void startBend(double f1, double f2, double duration)
   { 
     frequencyPointer1 = f1;
     frequencyPointer2 = f2;
     
     // Work out the number of legato crossfades possible in the time
     // (16 samples between sending each new fractional delay length value to the readers)
     // Round down to an integer
     totalSamplesOfBend = (int) (samplingRate * duration);
     numSteps = (int) (totalSamplesOfBend / 16); 
     
     // Work out what the resulting change in frequency is for each step
     // * 2 because each pointer moves alternatly so must move twice the actual frequency step each time
     frequencyStep = 2 * ((frequencyPointer2 - frequencyPointer1) / numSteps);
     
     // Set the frequencyPointer2 to be half a step away in the wrong direction so that the second move will move it ahead of the first
     frequencyPointer2 = frequencyPointer1 - (0.5 * frequencyStep);
     
     // Move position of second readPointer
     double delay = samplingRate / frequencyPointer2;
     int intPart = (int) delay;
     buffer.delayPointer2.set(intPart);
     
     // Set the coefficient of the second allpass interpolated readpointer
     double fracPart = delay - intPart;
     allpassFilterReader2.coefficient.set((1-fracPart)/(1+fracPart));
     
     // Set the flag for pitch bend to true, to be carried out by the function evaluator
     pitchBend = true;
   }
  
  
  /**************************************************************************************************/
  //
  /* Generate  
  //
  /**************************************************************************************************/
  /**
   * Process one sample through the unit, overwridden from UnitGenerator
   * @param start int
   * @param limit int 
   */
   
  @Override
  public void generate(int start, int limit)
  { 
    // Get input from ports
    double[] inputAs = inputA.getValues();
    double[] inputBs = inputB.getValues();
    double[] outputs = output.getValues();
     
        
    for(int i=start; i<limit; i++)
    { 
     
      if(pitchBend == true)
      {
        // If count is 0, 16 samples have passed to alter delay length
        if(count16 == 0)
        {            
          // Update the frequency of the current pointer, update corresponding read pointer, update coefficient for allpass filter, update center frequency for bandpass filter
          if(currentPointer == true)
          {
            frequencyPointer2 += frequencyStep;
            double delay = samplingRate / frequencyPointer2;
            int intPart = (int) delay;
            buffer.delayPointer2.set(intPart);
            double fracPart = delay - intPart;
            allpassFilterReader2.coefficient.set((1-fracPart)/(1+fracPart));
            bandpass.frequency.set(frequencyPointer2);
          }
          else
          {
            frequencyPointer1 += frequencyStep;
            double delay = samplingRate / frequencyPointer1;
            int intPart = (int) delay;
            buffer.delayPointer1.set(intPart);
            double fracPart = delay - intPart;
            allpassFilterReader1.coefficient.set((1-fracPart)/(1+fracPart));
            bandpass.frequency.set(frequencyPointer1);
          }
            
          // reset blendFactor
          blendFactor = 0;
        }
          
        // If count is > 4 then we've waited 5 samples so can start cross fading between the two pointers
        if(count16 > 4)
        {
          blendFactor = (count16 - 5) / 11;
        }
          
        // If totalCount is same as pitchBendTotalSamples, pitch bend is finished so reset variables
        if(totalCount >= totalSamplesOfBend)
        {
          pitchBend = false;
          count16 = 0;
          totalCount = 0;
          blendFactor = 0;
          
          if(numSteps % 2 == 0)
          {
            currentPointer = true;
          }
          else
          {
            currentPointer = false;
          }
        }
      } 
      // Get the two input samples
      double sample1 = inputAs[i];
      double sample2 = inputBs[i];
        
      // Set the current pointer sample to interpolate from for the legato crossfade
      double firstSample = 0;
      double secondSample = 0;
      if(currentPointer == true)
      {
        firstSample = sample1;
        secondSample = sample2;
      }
      else
      {
        firstSample = sample2;
        secondSample = sample1;
      }
        
      // Work out range between 2 samples
      double range = Math.abs(firstSample - secondSample); 
        
      double result = 0;
        
      if(firstSample < secondSample)
      {
        result = firstSample + (blendFactor * range);
      }
      else
      {
        result = firstSample - (blendFactor * range);
      }
      
      // If pitchbending and time to swap pointer, swap
      if(pitchBend == true)
      { 
        if(count16 == 0)
        {
          // Flip current pointer
          currentPointer = !currentPointer;
        }
        
        // Increment counts
        count16 = (count16 + 1) % 16;
        totalCount++;
      }
       
      // Output the result
      outputs[i] = result;  
    }
  } 
  
  /**************************************************************************************************/
  //
  /* stopBend  
  //
  /**************************************************************************************************/
  /**
   * Stops the pitch bend
   */
   
  public void stopBend()
  {
    pitchBend = false;
  }
}
