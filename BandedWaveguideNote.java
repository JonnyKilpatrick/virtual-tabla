import processing.core.*;
import com.jsyn.*;
import com.jsyn.data.*;
import com.jsyn.unitgen.*;
import com.jsyn.util.*;
import java.io.File;
import java.io.IOException;

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
  private UnitGenerator lineOut;                       // Output
  private FixedRateMonoReader[] initialInput;          // Array of evaluators to initialise the delay line with values
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
   * @param lineOut UnitGenerator
   * @param numSingleWaveguides int the number of delay lines used in the Banded waveguide
   * @param initialData double[] initial samples to input to the banded waveguide, set to null to initialise with random values
   */
  public BandedWaveguideNote(Synthesizer synth, UnitGenerator lineOut, int numSingleWaveguides, float[] initialData)
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
      
      // Create new initial readers, one for each delay line
      initialInput = new FixedRateMonoReader[numSingleWaveguides];
      
      // Create banded waveguide
      bandedWaveguide = new FullBandedWaveguide(samplingRate, MAX_BUFFER_SIZE, numSingleWaveguides);
      
      // Add banded waveguide to synthesiser
      synth.add(bandedWaveguide);
      
      for(int i=0; i<numSingleWaveguides; i++)
      {
        synth.add(initialInput[i] = new FixedRateMonoReader());
        
        // Connect to correct input on the banded waveguide
        initialInput[i].output.connect(0, bandedWaveguide.inputs[i], 0);
        
        // Also connect initial input to line out
        //initialInput[i].output.connect(0, lineOut.input, 0);
        //initialInput[i].output.connect(0, lineOut.input, 1);
                
        // Connect the output of the banded waveguide to every input
        bandedWaveguide.output.connect(0, bandedWaveguide.inputs[i], 0);
      }
      
      // Connect output of banded waveguides to lineout
      if(lineOut instanceof LineOut)
      { 
        bandedWaveguide.output.connect(0, ((LineOut)lineOut).input, 0);
        bandedWaveguide.output.connect(0, ((LineOut)lineOut).input, 1);
      }
      else if(lineOut instanceof CaptureOutput)
      {
        bandedWaveguide.output.connect(0, ((CaptureOutput)lineOut).input, 0);
      }
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
   */
   
   public void playNote(WaveguideParameters[] params, double fundimentalFrequency) throws IOException
   {
     // Check inputs
     if(params.length != numSingleWaveguides)
     {
       throw new IllegalArgumentException("Can't play note! Number of waveguide parameters supplied must be equal to the number of delay lines");
     }
     
     this.fundimentalFrequency = fundimentalFrequency;
     
     lineOut.stop();
     
     FloatSample[] floatSamples = new FloatSample[numSingleWaveguides]; // FloatSample for each initial input
     float[] initialWaveTable = null;   // Float version of each initial input

     // Work out delay length for the longest delay line
     int maxNumSamples = (int) (samplingRate / fundimentalFrequency);
     
     // If no initialData given, use random numbers
     if(initialData == null)
     {
       // Generate random numbers in the range -1 to 1.
       initialWaveTable = generateRandomNumbers(maxNumSamples, -1, 1);
     }
     // Else set the initial wave table to the supplied data, only using the the first N samples
     else
     {
       initialWaveTable = new float[maxNumSamples];
       
       for(int i=0; i<maxNumSamples; i++)
       {
         initialWaveTable[i] = (float) (initialData[i]);
       }
     }
     
     // Give each initial evaluator the right size of random value or sample data
     for(int i=0; i<numSingleWaveguides; i++)
     {
       // Get length of delay lines for this delay
       int numSamples = (int) (samplingRate / params[i].getCenterFrequency());
       
       // Take away the average from each number to ensure the values will have a 0 mean
       // This ensures the sound eventually dies away 
       // Also scale the random numbers for the chosen amplitude
       
       float[] data = new float[numSamples]; 
       for (int j=0; j<numSamples; j++)
       {
         data[j] = (float) (initialWaveTable[j] * params[i].getAmplitude());  // Multiply by amplitude set for this frequency (delay line)
       } 
       
       // Set as FloatSample
       floatSamples[i] = new FloatSample(data);
       
     }
     
     // Set up parameters in the banded waveguides
     bandedWaveguide.playNote(params);
     
     // Queue the initial values into the initial evaluators to fill the delay line
     
     for(int i=0; i<numSingleWaveguides; i++)
     {
       initialInput[i].dataQueue.clear();
       initialInput[i].dataQueue.queue(floatSamples[i], 0, floatSamples[i].getNumFrames());
     }
     
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
