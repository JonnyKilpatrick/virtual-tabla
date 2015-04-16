import com.jsyn.*;
import com.jsyn.data.*;
import com.jsyn.unitgen.*;
import com.jsyn.util.*;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;


/**
 * Circuit for a FullBandedWaveguide, to group together a number of SingleBandedWaveguides
 */
 
public class FullBandedWaveguide extends Circuit
{
  
  /**************************************************************************************************/
  //
  /* Instance variables 
  //
  /**************************************************************************************************/
  
  // UnitGenerators
  private SingleBandedWaveguide[] waveguides;
  private Mixer mixer;
  
  // Set up and input and output
  public UnitInputPort[] inputs;    // Array of inputs for each initial evaluator / delay line
  public UnitOutputPort output;
  
  
  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/
  /**
   * Class constructor
   * @param samplingRate double the sampling rate of the Synth
   * @param maxBufferSize int the size of the circular buffer
   * @param n int the number of single banded waveguides in the full waveguide
   */
   
  public FullBandedWaveguide(double samplingRate, int maxBufferSize, int numSingleWaveguides)
  {
    super();
    
    // Create a mixer to mix all delay lines down to, with the correct number of inputs
    mixer = new Mixer(numSingleWaveguides);
    // Output of circuit is the output of the mixer
    output = mixer.output;
    
    // Create an input for each waveguide
    inputs = new UnitInputPort[numSingleWaveguides];
    
    // Create desired number of Single banded waveguides
    waveguides = new SingleBandedWaveguide[numSingleWaveguides];
    
    for(int i=0; i<numSingleWaveguides; i++)
    {
      // Add port for this waveguide
      addPort(inputs[i] = new UnitInputPort("Intput"));
      
      // Add waveguide to circuit
      add(waveguides[i] = new SingleBandedWaveguide(samplingRate, maxBufferSize));
      
      // Make each input correspond to the input of each waveguide
      inputs[i] = waveguides[i].input;
      
      // Connect output of each single banded waveguide to the input of the mixer
      waveguides[i].output.connect(0, mixer.input, i); 
    } 
  }
  
  /**************************************************************************************************/
  //
  /* PlayNote 
  //
  /**************************************************************************************************/
  /**
   * Update the parameters of the delay line, filters for new notes
   * @param waveguideParameters WaveguideParameters[] the array of parameters for each of the SingleBandedWaveguides
   */
   
  public void playNote(WaveguideParameters[] waveguideParameters)
  {
    // For each single banded waveguide, set up the required parameters
    for(int i=0; i<waveguides.length; i++)
    {
      waveguides[i].playNote(waveguideParameters[i]);
    }    
  }
  
  
  
  /**************************************************************************************************/
  //
  /* PitchBend 
  //
  /**************************************************************************************************/
  /**
   * Set the pitch bend controller to start bending
   * @param f1 double initial frequency
   * @param f2 double the frequency to pitch bend to
   * @param duration double the length of the pitch bend in time
   */
   
  public void pitchBend(double f1, double f2, double duration)
  {
    // Get the distance between the two frequencies
    double frequencyChange = f1 - f2;
    
    for(SingleBandedWaveguide s : waveguides)
    {
      s.pitchBend(frequencyChange, duration);
    }
  }

}
