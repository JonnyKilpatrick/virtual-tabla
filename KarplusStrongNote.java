import processing.core.*;
import com.jsyn.*;
import com.jsyn.data.*;
import com.jsyn.unitgen.*;
import com.jsyn.util.*;

/**
 * Class for a KarplusStrongNote, synthesises tabla sounds given MidiMessages
 */

public class KarplusStrongNote
{ 
  /**************************************************************************************************/
  //
  /* Instance variables 
  //
  /**************************************************************************************************/
  
  // JSyn Unit Generators  
  private Synthesizer synth;                           // JSyn synthesizer
  private LineOut lineOut;                             // Output
  private FixedRateMonoReader initialInput;            // Evaluator to initialise the delay line with values
  private CircularBuffer buffer;                       // Circular buffer to store the values
  private float[] initialData;  
  // Filters
  private AllpassFilter allpassFilterReader1;
  private AllpassFilter allpassFilterReader2;
  private LowpassFilter lowpassFilterReader1;
  private LowpassFilter lowpassFilterReader2;
  // Pitch bend controller
  private PitchBendController pitchBendController;
  
  // Karplus-Strong parameters
  private double frequencyPointer1;
  private int samplingRate;
  
  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/
  
  /**
   * Class constructor, sets up the JSyn unit generators to synthesise drum sounds
   * @param synth Synthesiser
   * @param lineOut LineOut
   * @param initialData double[] initial samples to use to populate the wavetable, set to null to initialise with random values
   */
  public KarplusStrongNote(Synthesizer synth, LineOut lineOut, float[] initialData)
  {
    
    // Setup
    try
    {       
      this.synth = synth;
      this.lineOut = lineOut;
      this.initialData = initialData;

      lineOut.stop();
      
      // Initialise Filters
      allpassFilterReader1 = new AllpassFilter(1);
      allpassFilterReader2 = new AllpassFilter(1);
      lowpassFilterReader1 = new LowpassFilter(1.0, 0.5);
      lowpassFilterReader2 = new LowpassFilter(1.0, 0.5);
      
      // Create new reader to output the initial values
      initialInput = new FixedRateMonoReader();
      
      // Initialise circular buffer with empty fields
      buffer = new CircularBuffer(1, 1, 1);
      
      // Initialise pitch bend controller
      pitchBendController = new PitchBendController(samplingRate, buffer, allpassFilterReader1, allpassFilterReader2);
      
      // Add Unit Gens to Synth
      synth.add(allpassFilterReader1);
      synth.add(allpassFilterReader2);
      synth.add(lowpassFilterReader1);
      synth.add(lowpassFilterReader2);
      synth.add(initialInput);
      synth.add(pitchBendController);
      synth.add(buffer);
      
      // Connect units together
      initialInput.output.connect(0, lineOut.input, 0);                      // Send initial values to speakers while delay line is filled
      initialInput.output.connect(0, lineOut.input, 1);                      // Send initial values to speakers while delay line is filled
      initialInput.output.connect(0, buffer.input, 0);                       // Connect initial values to fill the delay line
      buffer.output.connect(0, allpassFilterReader1.input, 0);              // First read pointer output into the first allpass filter
      buffer.outputB.connect(0, allpassFilterReader2.input, 0);              // First read pointer output into the first allpass filter
      allpassFilterReader1.output.connect(0, lowpassFilterReader1.input, 0); // Output of first allpass filter into the first lowpass filter
      allpassFilterReader2.output.connect(0, lowpassFilterReader2.input, 0); // Output of second allpass filter into the second lowpass filter
      lowpassFilterReader1.output.connect(0, pitchBendController.inputA, 0); // Output of the first lowpass filter into the first input of the pitch bend controller
      lowpassFilterReader2.output.connect(0, pitchBendController.inputB, 0); // Output of the second lowpass filter into the second input of the pitch bend controller
      pitchBendController.output.connect(0, buffer.input, 0);                // Feed the output back into the delay line for the feedback loop
      pitchBendController.output.connect(0, lineOut.input, 0);               // Output from controller values to speaker
      pitchBendController.output.connect(0, lineOut.input, 1);               // Output from controller values to speaker
      
      // Get the current default sampling rate, for Karplus-Strong calculations
      samplingRate = synth.getFrameRate();
      
      // Initialise fundimental frequency 
      frequencyPointer1 = 1000;
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
   * @param frequencyPointer1 double the frequency of the note / hit
   * @param amplitude double the peak amplitude of the hit in dB
   * @param duration double the duration of the note / hit decay in seconds
   * @param q double the desired decay in dB
   */
   
   public void playNote(double frequencyPointer1, double amplitude, double duration, double q)
   {
     lineOut.stop();
     
     this.frequencyPointer1 = frequencyPointer1;
     
     // Work out p, the number of samples needed in the buffer for the chosen frequency
     double loop = samplingRate / frequencyPointer1;
     int numSamples = (int) loop;
     
     // Create the circular buffer and give it the initial data samples
     buffer.allocate(numSamples, numSamples);

     // Set initial values for decay stretching / shortening and all pass filter
     double shorteningFactor = 1.0;
     double stretchingFactor = 0.5;
     
     // Calculate G(f), the loop gain required at any frequency for the decay of q
     double gF = Math.pow(10.0, (-q/(20.0 * frequencyPointer1 * duration)));
     
     // Calculate G norm, the gain of the lowpass filter at the frequency
     double g = Math.cos((Math.PI * frequencyPointer1) / samplingRate);
     
     // Compare gF and g
     
     // If gF <= g, decay shortening is needed
     if(gF <= g)
     {
       shorteningFactor = gF / g;
     }
     // Else decay stretching is needed
     else
     {
       // Solve quadratic formula
       
       double cosFundimental = Math.cos((2.0 * Math.PI * frequencyPointer1) / samplingRate);
       double a = 2.0 - (2.0 * cosFundimental);
       double b = (2.0 * cosFundimental) - 2.0;
       double c = 1.0 - (gF * gF);
       double d = Math.sqrt((b * b) - (4.0 * a * c));
       double a2 = 2.0 * a;
       double stretching1 = (-b + d) / a2;
       double stretching2 = (-b - d) / a2;
       
       // Pick the value for stretchingFactor that stretches 
       if (stretching1 > 0 && stretching1 <= 0.5)
       {
         stretchingFactor = stretching1;
       }  
       else
       {
         stretchingFactor = stretching2;
       }
     }
     
     // Set parameters in the low pass filter
     lowpassFilterReader1.setParameters(shorteningFactor, stretchingFactor);
     lowpassFilterReader2.setParameters(shorteningFactor, stretchingFactor);
     
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
     
     // Set c1 and c2 initial values in the filters
     double fracPart = loop - numSamples;
     double coefficient = (1-fracPart)/(1+fracPart);
     allpassFilterReader1.setCoefficient(coefficient);
     allpassFilterReader2.setCoefficient(coefficient); 
     
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
     pitchBendController.startBend(frequencyPointer1, frequencyPointer2, slideDuration);
   }
}

