//import processing.core.*;
//import com.jsyn.*;
//import com.jsyn.data.*;
//import com.jsyn.unitgen.*;
//import com.jsyn.util.*;
//
///**
// * Class for a DrumSynthNote, synthesises tabla sounds given MidiMessages
// */
//
//public class DrumSynthNote
//{ 
//  /**************************************************************************************************/
//  //
//  /* Instance variables 
//  //
//  /**************************************************************************************************/
//  
//  // JSyn Unit Generators
//  
//  private Synthesizer synth;                           // JSyn synthesizer
//  private LineOut lineOut;                             // Output
//  private FunctionEvaluator karplusStrongEvaluator;    // Evaluator to output the new value of the sample from the Karplus-Strong algorithm
//  private CircularBuffer buffer;                       // Circular buffer to store the values
//  
//  // Karplus-Strong parameters
//  private double frequencyPointer1;
//  private int samplingRate;
//  private double shorteningFactor;
//  private double stretchingFactor;
//  private double stretchingFactorTakeOne;
//  private double c1;
//  private double c2;
//  
//  // Pitch bend parameters
//  private double frequencyPointer2;
//  private int pitchBendTotalSamples;
//  private double frequencyPitchBendStep;
//  private boolean pitchBend;
//  
//  // Extra karplus strong variables
//  private int count16 = 0;
//  private int totalCount = 0;
//  private boolean currentPointer = false;
//  
//  private double lastSample = 0;
//  private double lastFirstFilter1 = 0;
//  private double lastSecondFilter1 = 0;
//  
//  private double lastSample2 = 0;
//  private double lastFirstFilter2 = 0;
//  private double lastSecondFilter2 = 0;
//  
//  private double blendFactor = 0; 
//  
//  /**************************************************************************************************/
//  //
//  /* Constructor 
//  //
//  /**************************************************************************************************/
//  
//  /**
//   * Class constructor, sets up the JSyn unit generators to synthesise drum sounds
//   */
//  public DrumSynthNote(Synthesizer synth, LineOut lineOut)
//  {
//    
//    // Setup
//    try
//    {  
//      this.synth = synth;
//      this.lineOut = lineOut;
//      
//      // Create new function evaluator to evaluate the values from the buffer and update it
//      karplusStrongEvaluator = new FunctionEvaluator();
//      synth.add(karplusStrongEvaluator);
//      
//      // Initialise circular buffer with empty fields
//      buffer = new CircularBuffer(1, 1, 1, new double[1]);
//
//      // Create function for the Karplus-Strong algorithm
//      Function karplusStrong = new Function()
//      {     
//        public double evaluate(double x)
//        {
//          // Read samples from the buffer
//          double[] samples = buffer.read();
//          
//          // Process samples
//          return processSample(samples[0], samples[1]);
//        }
//      };
//      // Add the function to the evaluator
//      karplusStrongEvaluator.function.set(karplusStrong);
//       
//      // Connect the output of the function evaluator to the line out
//      karplusStrongEvaluator.output.connect(0, lineOut.input, 0);
//      
//      // Get the current default sampling rate, for Karplus-Strong calculations
//      samplingRate = synth.getFrameRate();
//      
//      // Initialise fundimental frequency 
//      frequencyPointer1 = 1000;
//      
//      // Initialise the pitch bend parameters
//      pitchBend = false;
//    }
//    // Handle errors
//    catch (Exception ex)
//    {
//      ex.printStackTrace();
//      //// ***** TODO throw new FileNotFoundException("Error: Unable to initialise drum synthesis");
//    }
//  }
//  
//  
//  /**************************************************************************************************/
//  //
//  /* playNote 
//  //
//  /**************************************************************************************************/
//  
//  /**
//   * Plays a single drum hit, given the paramaters to control the Karplus-Strong algorithm
//   * @param frequencyPointer1 double the frequency of the note / hit
//   * @param amplitude double the peak amplitude of the hit in dB
//   * @param duration double the duration of the note / hit decay in seconds
//   * @param q double the desired decay in dB
//   */
//   
//   public void playNote(double frequencyPointer1, double amplitude, double duration, double q)
//   {
//     lineOut.stop();
//     
//     this.frequencyPointer1 = frequencyPointer1;
//     
//     // Work out p, the number of samples needed in the buffer for the chosen frequency
//     double loop = samplingRate / frequencyPointer1;
//     int numSamples = (int) Math.round(loop);
//     
//     // Set initial values for decay stretching / shortening and all pass filter
//     shorteningFactor = 1.0;
//     stretchingFactor = 0.5;
//     double coefficient = 0.999;
//     
//     // Calculate G(f), the loop gain required at any frequency for the decay of q
//     double gF = Math.pow(10.0, (-q/(20.0 * frequencyPointer1 * duration)));
//     
//     // Calculate G norm, the gain of the lowpass filter at the frequency
//     double g = Math.cos((Math.PI * frequencyPointer1) / samplingRate);
//     
//     // Compare gF and g
//     
//     // If gF <= g, decay shortening is needed
//     if(gF <= g)
//     {
//       shorteningFactor = gF / g;
//     }
//     // Else decay stretching is needed
//     else
//     {
//       // Solve quadratic formula
//       
//       double cosFundimental = Math.cos((2.0 * Math.PI * frequencyPointer1) / samplingRate);
//       double a = 2.0 - (2.0 * cosFundimental);
//       double b = (2.0 * cosFundimental) - 2.0;
//       double c = 1.0 - (gF * gF);
//       double d = Math.sqrt((b * b) - (4.0 * a * c));
//       double a2 = 2.0 * a;
//       double stretching1 = (-b + d) / a2;
//       double stretching2 = (-b - d) / a2;
//       
//       // Pick the value for stretchingFactor that stretches 
//       if (stretching1 > 0 && stretching1 <= 0.5)
//       {
//         stretchingFactor = stretching1;
//       }  
//       else
//       {
//         stretchingFactor = stretching2;
//       }
//     }
//     
//     // Work out coefficient for allpass filter approach for fractional delay line
//     double delay = numSamples + stretchingFactor;
//     if(delay > loop)
//     {
//       delay = (numSamples - 1) + stretchingFactor;
//     }
//     // Store 1-S for forumla
//     stretchingFactorTakeOne = 1 - stretchingFactor;
//     
//     double d = loop - delay;
//     coefficient = (1 - d) / (1 + d);
//     
//     // Create delay line of the correct number of samples for chosen frequency, 
//     // initialised with random numbers in the range -1 to 1.
//     double[] initialWaveTable = generateRandomNumbers(numSamples, -1, 1);
//     
//     // Compute the average of the random numbers
//     double average = computeAverage(initialWaveTable);
//     
//     // Take away the average from each number to ensure the values will have a 0 mean
//     // This ensures the sound eventually dies away 
//     // Also scale the random numbers for the chosen amplitude
//     
//     for (int i=0; i<numSamples; i++)
//     {
//       initialWaveTable[i] -= average;
//       initialWaveTable[i] *= amplitude;
//     }
//     
//     // Set c1 and c2 initial values
//     double frac1 = frequencyPointer1 - loop;
//     c1 = (1-frac1)/(1+frac1);
//     c2 = c1;
//     
//     
//     // Create the circular buffer and give it the initial data samples
//     buffer = new CircularBuffer(600, numSamples, numSamples, initialWaveTable);
//
//     // Start the processing to play sound
//     lineOut.start();
//   }
//   
//   /**
//   * Compute the average of an array of floats
//   * @param randomValues double[] the array of values
//   * @retrun double the average of the array values
//   */
//   
//   private double computeAverage(double[] randomValues)
//   {  
//     double total = 0;
//     double arrayLength = (double) randomValues.length;
//     
//     for(int i=0; i<arrayLength; i++)
//     {
//       total += randomValues[i];  
//     }
//     
//     return total / arrayLength;
//   }
//   
//   
//   /**
//   * Return array of random double values between the given limits 
//   * @param size int the number of random numbers to generate
//   * @param min int the minimum boundary for the random numbers
//   * @param max int the maxumum boundary for the random numbers
//   * @retrun double[] the array of random numbers
//   */
//   
//   private double[] generateRandomNumbers(int size, int min, int max)
//   {
//     int range = max - min;
//     double[] randomNumbers = new double[size];
//     
//     for(int i=0; i<size; i++)
//     {
//       randomNumbers[i] = min + (range * Math.random());
//     }
//     return randomNumbers;
//   }
//   
//  /**************************************************************************************************/
//  //
//  /* pitchBend 
//  //
//  /**************************************************************************************************/
//   
//  /**
//  * Return array of random double values between the given limits 
//  * @param frequencyPointer2 double the new frequency to pitch bend (slide) to
//  * @param slideDuration double the duration of the pitch bend in seconds   
//  */
//   
//   public void pitchBend(double frequencyPointer2, double slideDuration)
//   {
//     this.frequencyPointer2 = frequencyPointer2;
//     
//     // Work out the number of legato crossfades possible in the time
//     // (16 samples between sending each new fractional delay length value to the readers)
//     // Round down to an integer
//     pitchBendTotalSamples = (int) Math.floor(samplingRate * slideDuration);
//     int numSteps = (int) (pitchBendTotalSamples / 16); 
//     
//     // Work out what the resulting change in frequency is for each step
//     frequencyPitchBendStep = 2 * ((frequencyPointer2 - frequencyPointer1) / numSteps);
//     
//     // Set the frequencyPointer2 to be one step away in the right direction from the first frequencyPointer
//     this.frequencyPointer2 = frequencyPointer1 + (0.5 * frequencyPitchBendStep);
//
//     int intPart = (int) (samplingRate / frequencyPointer2);
//     buffer.setPointer2Delay(intPart);
//     
//     // Set the flag for pitch bend to true, to be carried out by the function evaluator
//     pitchBend = true;
//   }
//   
//   
//  /**************************************************************************************************/
//  //
//  /* processSample 
//  //
//  /**************************************************************************************************/
//  
//  /**
//  * Process one sample from the buffer, overwriting the buffer and returning the output sample to play 
//  * @param sample1 double sample from the buffer at the first read pointer
//  * @param sample2 double sample from the buffer at the second read pointer
//  * @return double the new sample to play  
//  */
//  
//  private double processSample(double sample1, double sample2)
//  { 
//
//    // If pitch bend flag is set, move the read pointers
//    if(pitchBend == true)
//    {
//      // Flip current pointer
//      currentPointer = !currentPointer;
//        
//      // If count is 0, 16 samples have passed to alter delay length
//      if(count16 == 0)
//      {              
//        // Update the frequency of the current pointer, update corresponding read pointer, update coefficient for allpass filter
//        if(currentPointer == false)
//        {
//          frequencyPointer2 += frequencyPitchBendStep;
//          int intPart = (int) (samplingRate / frequencyPointer2);
//          buffer.setPointer2Delay(intPart);
//          c2 = frequencyPointer2 - intPart;
//        }
//        else
//        {
//          frequencyPointer1 += frequencyPitchBendStep;
//          int intPart = (int) (samplingRate / frequencyPointer1);
//          buffer.setPointer1Delay(intPart);
//          c1 = frequencyPointer1 - intPart;
//        }
//          
//        // reset blendFactor
//        blendFactor = 0;
//      }
//        
//      // If count is > 4 then we've waited 5 samples so can start cross fading between the two pointers
//      if(count16 > 4)
//      {
//        blendFactor = (count16 - 5) / 11;
//      }
//        
//      // Increment counts
//      count16 = (count16 + 1) % 16;
//      totalCount++;
//        
//      // If totalCount is same as pitchBendTotalSamples, pitch bend is finished so reset variables
//      if(totalCount >= pitchBendTotalSamples)
//      {
//        pitchBend = false;
//        count16 = 0;
//        totalCount = 0;
//        blendFactor = 0;
//      }
//    }
//      
//    double newSample = shorteningFactor * ((stretchingFactorTakeOne * sample1) + (stretchingFactor * lastSample));
//    lastSample = sample1;
//    double f2newSample = (c1*newSample) + lastFirstFilter1 - (c1*lastSecondFilter1);
//    lastFirstFilter1 = newSample;
//    lastSecondFilter1 = f2newSample;
//      
//    double newSample2 = shorteningFactor * ((stretchingFactorTakeOne * sample2) + (stretchingFactor * lastSample2));
//    lastSample2 = sample2;
//    double f2newSample2 = (c2*newSample2) + lastFirstFilter2 - (c2*lastSecondFilter2);
//    lastFirstFilter2 = newSample2;
//    lastSecondFilter2 = f2newSample2;
//      
//    // Set the current pointer sample to interpolate from for the legato crossfade
//    double firstSample = 0;
//    double secondSample = 0;
//    if(currentPointer == false)
//    {
//      firstSample = newSample;
//      secondSample = newSample2;
//    }
//    else
//    {
//      firstSample = newSample2;
//      secondSample = newSample;
//    }
//      
//    // Work out range between 2 samples
//    double range = Math.abs(newSample - newSample2); 
//      
//    double result = 0;
//      
//    if(firstSample < secondSample)
//    {
//      result = firstSample + (blendFactor * range);
//    }
//    else
//    {
//      result = firstSample - (blendFactor * range);
//    }
//     
//    buffer.write(result);
//    
//    return result;
//  }
//   
//}




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
  /* Constants
  //
  /**************************************************************************************************/
  
  private final double LOWEST_FREQUENCY = 100.0;
  
  
  /**************************************************************************************************/
  //
  /* Instance variables 
  //
  /**************************************************************************************************/
  
  // JSyn Unit Generators  
  private Synthesizer synth;                           // JSyn synthesizer
  private LineOut lineOut;                             // Output
  private FunctionEvaluator karplusStrongEvaluator;    // Evaluator to output the new value of the sample from the Karplus-Strong algorithm
  private CircularBuffer buffer;                       // Circular buffer to store the values
  private double[] initialData;
  
  // Filters
  private AllpassFilter allpassFilterReader1;
  private AllpassFilter allpassFilterReader2;
  private LowpassFilter lowpassFilterReader1;
  private LowpassFilter lowpassFilterReader2;
  
  // Karplus-Strong parameters
  private double frequencyPointer1;
  private int samplingRate;
  private double shorteningFactor;
  private double stretchingFactor;
  
  // Pitch bend parameters
  private double frequencyPointer2;
  private int pitchBendTotalSamples;
  private double frequencyPitchBendStep;
  private boolean pitchBend;
  
  // Extra karplus strong variables
  private int count16 = 0;
  private int totalCount = 0;
  private boolean currentPointer = false;
  
  private double lastSample = 0;
  private double lastFirstFilter1 = 0;
  private double lastSecondFilter1 = 0;
  
  private double lastSample2 = 0;
  private double lastFirstFilter2 = 0;
  private double lastSecondFilter2 = 0;
  
  private double blendFactor = 0; 
  
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
  public DrumSynthNote(Synthesizer synth, LineOut lineOut, double[] initialData)
  {
    
    // Setup
    try
    {  
      this.synth = synth;
      this.lineOut = lineOut;
      this.initialData = initialData;
      
      allpassFilterReader1 = new AllpassFilter(1);
      allpassFilterReader2 = new AllpassFilter(1);
      lowpassFilterReader1 = new LowpassFilter(1.0, 0.5);
      lowpassFilterReader2 = new LowpassFilter(1.0, 0.5);
      
      // Create new function evaluator to evaluate the values from the buffer and update it
      karplusStrongEvaluator = new FunctionEvaluator();
      synth.add(karplusStrongEvaluator);
      
      // Initialise circular buffer with empty fields
      buffer = new CircularBuffer(1, 1, 1, new double[1]);

      // Create function for the Karplus-Strong algorithm
      Function karplusStrong = new Function()
      {     
        public double evaluate(double x)
        {
          // Read samples from the buffer
          double[] samples = buffer.read();
          
          // Process samples
          return processSample(samples[0], samples[1]);
        }
      };
      // Add the function to the evaluator
      karplusStrongEvaluator.function.set(karplusStrong);
       
      // Connect the output of the function evaluator to the line out
      karplusStrongEvaluator.output.connect(0, lineOut.input, 0);
      karplusStrongEvaluator.output.connect(0, lineOut.input, 1);
      
      // Get the current default sampling rate, for Karplus-Strong calculations
      samplingRate = synth.getFrameRate();
      
      // Initialise fundimental frequency 
      frequencyPointer1 = 1000;
      
      // Initialise the pitch bend parameters
      pitchBend = false;
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
     int numSamples = (int) Math.floor(loop);
     
     // Set initial values for decay stretching / shortening and all pass filter
     shorteningFactor = 1.0;
     stretchingFactor = 0.5;
     
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
     double[] initialWaveTable;
     if(initialData == null)
     {
       // Generate random numbers in the range -1 to 1.
       initialWaveTable = generateRandomNumbers(numSamples, -1, 1);
       
       // Compute the average of the random numbers
       double average = computeAverage(initialWaveTable);
       
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
       initialWaveTable = new double[numSamples];
       
       for(int i=0; i<numSamples; i++)
       {
         initialWaveTable[i] = initialData[i] * 100 * amplitude;
       }
     }
     
     // Set c1 and c2 initial values in the filters
     double fracPart = loop - numSamples;
     double coefficient = (1-fracPart)/(1+fracPart);
     allpassFilterReader1.setCoefficient(coefficient);
     allpassFilterReader2.setCoefficient(coefficient);
     
     
     // Create the circular buffer and give it the initial data samples
     buffer = new CircularBuffer(600, numSamples, numSamples, initialWaveTable);

     // Start the processing to play sound
     lineOut.start();
   }
   
   /**
   * Compute the average of an array of floats
   * @param randomValues double[] the array of values
   * @retrun double the average of the array values
   */
   
   private double computeAverage(double[] randomValues)
   {  
     double total = 0;
     double arrayLength = (double) randomValues.length;
     
     for(int i=0; i<arrayLength; i++)
     {
       total += randomValues[i];  
     }
     
     return total / arrayLength;
   }
   
   
   /**
   * Return array of random double values between the given limits 
   * @param size int the number of random numbers to generate
   * @param min int the minimum boundary for the random numbers
   * @param max int the maxumum boundary for the random numbers
   * @retrun double[] the array of random numbers
   */
   
   private double[] generateRandomNumbers(int size, int min, int max)
   {
     int range = max - min;
     double[] randomNumbers = new double[size];
     
     for(int i=0; i<size; i++)
     {
       randomNumbers[i] = min + (range * Math.random());
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
     this.frequencyPointer2 = frequencyPointer2;
     
     // Work out the number of legato crossfades possible in the time
     // (16 samples between sending each new fractional delay length value to the readers)
     // Round down to an integer
     pitchBendTotalSamples = (int) Math.floor(samplingRate * slideDuration);
     int numSteps = (int) (pitchBendTotalSamples / 16); 
     
     // Work out what the resulting change in frequency is for each step
     frequencyPitchBendStep = 2 * ((this.frequencyPointer2 - frequencyPointer1) / numSteps);
     
     // Set the frequencyPointer2 to be one step away in the right direction from the first frequencyPointer
     this.frequencyPointer2 = frequencyPointer1 + (0.5 * frequencyPitchBendStep);

     double delay = samplingRate / this.frequencyPointer2;
     int intPart = (int) delay;
     buffer.setPointer2Delay(intPart);
     
     // Set the coefficient of the second allpass interpolated readpointer
     double fracPart = delay - intPart;
     allpassFilterReader2.setCoefficient((1-fracPart)/(1+fracPart));
     
     // Set the flag for pitch bend to true, to be carried out by the function evaluator
     pitchBend = true;
   }
   
   
  /**************************************************************************************************/
  //
  /* processSample 
  //
  /**************************************************************************************************/
  
  /**
  * Process one sample from the buffer, overwriting the buffer and returning the output sample to play 
  * @param sample1 double sample from the buffer at the first read pointer
  * @param sample2 double sample from the buffer at the second read pointer
  * @return double the new sample to play  
  */
  
  private double processSample(double sample1, double sample2)
  { 

    // If pitch bend flag is set, move the read pointers
    if(pitchBend == true)
    {
      // Flip current pointer
      currentPointer = !currentPointer;
        
      // If count is 0, 16 samples have passed to alter delay length
      if(count16 == 0)
      {              
        // Update the frequency of the current pointer, update corresponding read pointer, update coefficient for allpass filter
        if(currentPointer == false)
        {
          frequencyPointer2 += frequencyPitchBendStep;
          double delay = samplingRate / frequencyPointer2;
          int intPart = (int) delay;
          buffer.setPointer2Delay(intPart);
          double fracPart = delay - intPart;
          allpassFilterReader2.setCoefficient((1-fracPart)/(1+fracPart));
        }
        else
        {
          frequencyPointer1 += frequencyPitchBendStep;
          double delay = samplingRate / frequencyPointer1;
          int intPart = (int) delay;
          buffer.setPointer1Delay(intPart);
          double fracPart = delay - intPart;
          allpassFilterReader1.setCoefficient((1-fracPart)/(1+fracPart));
        }
          
        // reset blendFactor
        blendFactor = 0;
      }
        
      // If count is > 4 then we've waited 5 samples so can start cross fading between the two pointers
      if(count16 > 4)
      {
        blendFactor = (count16 - 5) / 11;
      }
        
      // Increment counts
      count16 = (count16 + 1) % 16;
      totalCount++;
        
      // If totalCount is same as pitchBendTotalSamples, pitch bend is finished so reset variables
      if(totalCount >= pitchBendTotalSamples)
      {
        pitchBend = false;
        count16 = 0;
        totalCount = 0;
        blendFactor = 0;
      }
    }
      
    // Pass values through all pass filter for fractionalDelay
    double allpass1 = allpassFilterReader1.processSample(sample1);
    double allpass2 = allpassFilterReader2.processSample(sample2);
    
    // Pass values through the low pass filter for Karplus-Strong effect
    double newSample1 = lowpassFilterReader1.processSample(allpass1);
    double newSample2 = lowpassFilterReader2.processSample(allpass2);
      
    // Set the current pointer sample to interpolate from for the legato crossfade
    double firstSample = 0;
    double secondSample = 0;
    if(currentPointer == false)
    {
      firstSample = newSample1;
      secondSample = newSample2;
    }
    else
    {
      firstSample = newSample2;
      secondSample = newSample1;
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
     
    buffer.write(result);
    
    return result;
  }
   
}
