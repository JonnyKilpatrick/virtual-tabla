import com.jsyn.*;
import com.jsyn.data.*;
import com.jsyn.unitgen.*;
import com.jsyn.util.*;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;


/**
 * Circuit for a SingleBandedWaveguide, to group together a Bandpass filter, delay line, 
 * 2 allpass filters, and a pitch bend controller, making it much easier to create this circuit 
 * multiple times. 
 */

public class SingleBandedWaveguide extends Circuit
{

  /**************************************************************************************************/
  //
  /* Instance variables 
   //
  /**************************************************************************************************/

  // UnitGenerators
  private FilterBandPass bandpass;
  private CircularBuffer buffer;
  private AllpassFilter allpass1;
  private AllpassFilter allpass2;
  private PitchBendController pitchBendController;

  // Set up and input and output
  public UnitInputPort input;
  public UnitOutputPort output;

  // Parameters
  private double samplingRate;
  private double frequency;


  /**************************************************************************************************/
  //
  /* Constructor 
   //
  /**************************************************************************************************/
  /**
   * Class constructor
   * @param samplingRate double the sampling rate of the Synth
   * @param maxBufferSize int the size of the circular buffer
   */

  public SingleBandedWaveguide(double samplingRate, int maxBufferSize)
  {
    super();

    this.samplingRate = samplingRate;

    // Initialise units and add to circuit
    add(bandpass = new FilterBandPass());
    add(buffer = new CircularBuffer(maxBufferSize, 1, 1));
    add(allpass1 = new AllpassFilter());
    add(allpass2 = new AllpassFilter());
    add(pitchBendController = new PitchBendController(samplingRate, buffer, allpass1, allpass2));

    // Set up input an output to be called when using the circuit
    input = bandpass.input;
    output = pitchBendController.output;

    // Connect up the units to form the unit
    bandpass.output.connect(0, buffer.input, 0);                // Output bandpass into the delay line
    buffer.output.connect(0, allpass1.input, 0);                // First read pointer output into the first allpass filter
    buffer.outputB.connect(0, allpass2.input, 0);               // First read pointer output into the first allpass filter
    allpass1.output.connect(0, pitchBendController.inputA, 0);  // Output from allpass1 into the pitchbend controller
    allpass2.output.connect(0, pitchBendController.inputB, 0);  // Output from allpass2 into the pitchbend controller
  }

  /**************************************************************************************************/
  //
  /* PlayNote 
   //
  /**************************************************************************************************/
  /**
   * Update the parameters of the delay line, filters for new notes
   * @param waveGuideParameters WaveguideParameters the parameters for the bandpass, allpass and delay line
   */

  public void playNote(WaveguideParameters waveguideParameters)
  {  
    // Work out the length of the delay line, and the allpass coefficients from the frequency 

    double frequency = waveguideParameters.getCenterFrequency();
    double loop = samplingRate / frequency;
    int delayLength = (int) loop;
    double fractionalDelay = loop - delayLength;
    double coefficient = (1-fractionalDelay)/(1+fractionalDelay);
    allpass1.coefficient.set(coefficient);
    allpass2.coefficient.set(coefficient);

    // Allocate new delay length
    buffer.allocate(delayLength, delayLength);

    // Set bandpass filter parameters
    bandpass.frequency.set(frequency);
    bandpass.Q.set(waveguideParameters.getQ());
    bandpass.amplitude.set(waveguideParameters.getGain());
  }



  /**************************************************************************************************/
  //
  /* PitchBend 
   //
  /**************************************************************************************************/
  /**
   * Set the pitch bend controller to start bending
   * @param frequencyChange double the +/- distance between the current frequency and the frequency to slide to
   * @param duration double the length of the pitch bend in time
   */

  public void pitchBend(double frequencyChange, double duration)
  {
    pitchBendController.startBend(frequency, frequency + frequencyChange, duration);
  }
}

