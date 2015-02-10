import processing.core.*;
import com.jsyn.*;
import com.jsyn.data.*;
import com.jsyn.unitgen.*;
import com.jsyn.util.*;

/**
 * Class for a DrumSynthNote, synthesises tabla sounds given MidiMessages
 */

public class DrumSynthNote
{ 
  /**************************************************************************************************/
  //
  /* Instance variables 
  //
  /**************************************************************************************************/
  
  // JSyn Unit Generators
  
  Synthesizer synth;                           // JSyn synthesizer
  LineOut lineOut;                             // Output
  FixedRateMonoReader initialEvaluator;        // Mono Reader to output the initial values from the table into the delay line 
  FunctionEvaluator karplusStrongEvaluator;    // Evaluator to output the new value of the sample from the Karplus-Strong algorithm
  Delay delayLine;                             // DelayLine for the Karplus-Strong algorithm
  
  // Karplus-Strong parameters
  int samplingRate;
  double shorteningFactor;
  double stretchingFactor;
  double stretchingFactorTakeOne;
  
  
  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/
  
  /**
   * Class constructor, sets up the JSyn unit generators to synthesise drum sounds
   */
  public DrumSynthNote(Synthesizer synth, LineOut lineOut)
  {
    
    // Setup
    try
    {  
      this.synth = synth;
      this.lineOut = lineOut;
      
      // Create initial mono reader to play the initial values in the wave table and feed them into the delay line
      initialEvaluator = new FixedRateMonoReader();
      synth.add(initialEvaluator);

      // Create the actual delay line
      delayLine = new Delay();   
      // Allocate initial number of samples
      delayLine.allocate(1);
      synth.add(delayLine);
      
      // Create new function evaluator to evaluate the values from the table and update the table
      karplusStrongEvaluator = new FunctionEvaluator();
      synth.add(karplusStrongEvaluator);
       
      // Connect the initial mono reader to the line out to play initial samples
      // and to the input of the delay line to fill it with samples
      initialEvaluator.output.connect(0, lineOut.input, 0);
      initialEvaluator.output.connect(0, delayLine.input, 0);
      
      // Connect output of the delay line to the function evaluator to run the karplus strong algorithm
      delayLine.output.connect(0, karplusStrongEvaluator.input, 0);
       
      // Connect the output of the function evaluator to the line out
      // and back into the delay line to update the values from the table
      karplusStrongEvaluator.output.connect(0, lineOut.input, 0);
      karplusStrongEvaluator.output.connect(0, delayLine.input, 0);
      
      // Get the current default sampling rate, for Karplus-Strong calculations
      samplingRate = synth.getFrameRate();
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
   * @param fundimentalFrequency double the frequency of the note / hit
   * @param amplitude double the peak amplitude of the hit in dB
   * @param duration double the duration of the note / hit decay in seconds
   * @param q double the desired decay in dB
   */
   
   public void playNote(double fundimentalFrequency, double amplitude, double duration, double q)
   {
     lineOut.stop();
     // Work out p, the number of samples needed in the buffer for the chosen frequency
     double loop = samplingRate / fundimentalFrequency;
     int numSamples = (int) Math.round(loop);
     
     // Allocate a delay line of the right length
     delayLine.allocate(numSamples);
     
     // Set initial values for decay stretching / shortening and all pass filter
     shorteningFactor = 1.0;
     stretchingFactor = 0.5;
     double coefficient = 0.999;
     
     // Calculate G(f), the loop gain required at any frequency for the decay of q
     double gF = Math.pow(10.0, (-q/(20.0 * fundimentalFrequency * duration)));
     
     // Calculate G norm, the gain of the lowpass filter at the frequency
     double g = Math.cos((Math.PI * fundimentalFrequency) / samplingRate);
     
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
       
       double cosFundimental = Math.cos((2.0 * Math.PI * fundimentalFrequency) / samplingRate);
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
     
     // Work out coefficient for allpass filter approach for fractional delay line
     double delay = numSamples + stretchingFactor;
     if(delay > loop)
     {
       delay = (numSamples - 1) + stretchingFactor;
     }
     // Store 1-S for forumla
     stretchingFactorTakeOne = 1 - stretchingFactor;
     
     double d = loop - delay;
     coefficient = (1 - d) / (1 + d);
     
     // Create delay line of the correct number of samples for chosen frequency, 
     // initialised with random numbers in the range -1 to 1.
     float[] initialWaveTable = generateRandomNumbers(numSamples, -1, 1);
     
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
     
     // Create function for the Karplus-Strong algorithm
     Function karplusStrong = new Function()
     {
       private double lastSample = 0;
       private double newSample = 0;
       
       public double evaluate(double sample)
       {
         newSample = shorteningFactor * ((stretchingFactorTakeOne * sample) + (stretchingFactor * lastSample));
         lastSample = sample;
         return newSample;
       }
     };
     // Add the function to the evaluator
     karplusStrongEvaluator.function.set(karplusStrong);
     
     // Load a float table into the function evaluator to play the initial samples
     FloatSample waveTable = new FloatSample(initialWaveTable);
     
     // Queue the initial values into the initial evaluator to fill the delay line
     initialEvaluator.dataQueue.clear();
     initialEvaluator.dataQueue.queue(waveTable, 0, numSamples);

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
   
}
