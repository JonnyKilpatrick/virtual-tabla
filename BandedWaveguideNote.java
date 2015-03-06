import processing.core.*;
import com.jsyn.*;
import com.jsyn.data.*;
import com.jsyn.unitgen.*;
import com.jsyn.util.*;

/**
 * Class for a BandedWaveguideNote, playes a single note on a banded waveguide
 */

public class BandedWaveguideNote
{ 
  /**************************************************************************************************/
  //
  /* Constants
  //
  /**************************************************************************************************/
  private static final int MAX_BUFFER_SIZE = 600;  
  
  /**************************************************************************************************/
  //
  /* Instance variables 
  //
  /**************************************************************************************************/
  
  // JSyn Unit Generators  
  private Synthesizer synth;                           // JSyn synthesizer
  private LineOut lineOut;                             // Output
  private FixedRateMonoReader initialInput;            // Evaluator to initialise the delay line with values
  private FullBandedWaveguide bandedWaveguide;         // BandedWaveguide
  private float[] initialData;  
  
  // Waveguide parameters
  private int samplingRate;
  private double fundimentalFrequency;
  private int numSingleWaveguides;
  
  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/
  
  /**
   * Class constructor
   * @param synth Synthesiser
   * @param lineOut LineOut
   * @param numSingleWaveguides int the number of delay lines used in the Banded waveguide
   * @param initialData double[] initial samples to input to the banded waveguide, set to null to initialise with random values
   */
  public BandedWaveguideNote(Synthesizer synth, LineOut lineOut, int numSingleWaveguides, float[] initialData)
  {
    
    // Setup
    try
    {       
      this.synth = synth;
      this.lineOut = lineOut;
      this.numSingleWaveguides = numSingleWaveguides;
      this.initialData = initialData;

      lineOut.stop();
      
      // Get the current default sampling rate, for Karplus-Strong calculations
      samplingRate = synth.getFrameRate();
      
      // Create new reader to output the initial values
      initialInput = new FixedRateMonoReader();
      
      // Create banded waveguide
      bandedWaveguide = new FullBandedWaveguide(samplingRate, MAX_BUFFER_SIZE, numSingleWaveguides);
      
      // Add units to synthesiser
      synth.add(initialInput);
      synth.add(bandedWaveguide);
      
      // Connect units together
      initialInput.output.connect(0, bandedWaveguide.input, 0);
      bandedWaveguide.output.connect(0, lineOut.input, 0);
      bandedWaveguide.output.connect(0, lineOut.input, 1);
      bandedWaveguide.output.connect(0, bandedWaveguide.input, 0);
    }
    // Handle errors
    catch (Exception ex)
    {
      ex.printStackTrace();
      //// ***** TODO throw new FileNotFoundException("Error: Unable to initialise drum synthesis");
    }
  }
  
  
  /**************************************************************************************************/
  //
  /* playNote 
  //
  /**************************************************************************************************/
  
  /**
   * Plays a single drum hit, given the paramaters to control the Karplus-Strong algorithm
   * @param params WaveguideParameters[] the parameters for each single banded waveguide
   * @param fundimentalFrequency double the fundimental for the note, used to work out length of random values / sampled values to initialise delay lines
   * @param amplitude double the peak amplitude of the hit in dB
   */
   
   public void playNote(WaveguideParameters[] params, double fundimentalFrequency, double amplitude)
   {
     // Check inputs
     if(params.length != numSingleWaveguides)
     {
       throw new IllegalArgumentException("Can't play note! Number of waveguide parameters supplied must be equal to the number of delay lines");
     }
     
     this.fundimentalFrequency = fundimentalFrequency;
     
     lineOut.stop();
     
     // Work out delay length for longest delay line
     int numSamples = (int) (samplingRate / fundimentalFrequency);
     
     // If no initialData given, use random numbers
     float[] initialWaveTable;
     if(initialData == null)
     {
       
       // Generate random numbers in the range -1 to 1.
       initialWaveTable = generateRandomNumbers(numSamples, -1, 1);
       
       // Compute the average of the random numbers
       float average = computeAverage(initialWaveTable);
       
       // Take away the average from each number to ensure the values will have a 0 mean
       // This ensures the sound eventually dies away 
       // Also scale the random numbers for the chosen amplitude
       
       for (int i=0; i<numSamples; i++)
       {
         initialWaveTable[i] -= average;
         initialWaveTable[i] *= amplitude;
       }
     }
     // Else set the initial wave table to the supplied data, only using the the first N samples
     else
     {
       initialWaveTable = new float[numSamples];
       
       for(int i=0; i<numSamples; i++)
       {
         initialWaveTable[i] = (float) (initialData[i] * 100 * amplitude);
       }
     }
     
     // Create FloatSample to initialise delay line
     FloatSample floatSample = new FloatSample(initialWaveTable);
     
     // Set up parameters in the banded waveguides
     bandedWaveguide.playNote(params);
     
     // Queue the initial values into the initial evaluator to fill the delay line
     initialInput.dataQueue.clear();
     initialInput.dataQueue.queue(floatSample, 0, numSamples);
     
     // Start the processing to play sound
     lineOut.start();
   }
   
   /**
   * Compute the average of an array of floats
   * @param randomValues float[] the array of values
   * @retrun float the average of the array values
   */
   
   private float computeAverage(float[] randomValues)
   {  
     float total = 0;
     float arrayLength = (float) randomValues.length;
     
     for(int i=0; i<arrayLength; i++)
     {
       total += randomValues[i];  
     }
     
     return total / arrayLength;
   }
   
   
   /**
   * Return array of random float values between the given limits 
   * @param size int the number of random numbers to generate
   * @param min int the minimum boundary for the random numbers
   * @param max int the maxumum boundary for the random numbers
   * @retrun float[] the array of random numbers
   */
   
   private float[] generateRandomNumbers(int size, int min, int max)
   {
     int range = max - min;
     float[] randomNumbers = new float[size];
     
     for(int i=0; i<size; i++)
     {
       randomNumbers[i] = (float) (min + (range * Math.random()));
     }
     return randomNumbers;
   }
   
  /**************************************************************************************************/
  //
  /* pitchBend 
  //
  /**************************************************************************************************/
   
  /**
  * Return array of random double values between the given limits 
  * @param frequencyPointer2 double the new frequency to pitch bend (slide) to
  * @param slideDuration double the duration of the pitch bend in seconds   
  */
   
   public void pitchBend(double frequencyPointer2, double slideDuration)
   {    
     bandedWaveguide.pitchBend(fundimentalFrequency, frequencyPointer2, slideDuration);
   }
}
