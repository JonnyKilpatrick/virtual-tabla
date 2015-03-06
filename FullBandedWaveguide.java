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
  private MixerMono mixer;
  
  // Set up and input and output
  public UnitInputPort input;
  public PassThrough passThrough;
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
    
    add(passThrough = new PassThrough());
    addPort(input = passThrough.input);
    
    // Create a mixer to mix all delay lines down to, with the correct number of inputs
    mixer = new MixerMono(numSingleWaveguides);
    // Output of circuit is the output of the mixer
    output = mixer.output;
    
    // Create desired number of Single banded waveguides
    waveguides = new SingleBandedWaveguide[numSingleWaveguides];
    for(int i=0; i<numSingleWaveguides; i++)
    {
      // Also add to circuit
      add(waveguides[i] = new SingleBandedWaveguide(samplingRate, maxBufferSize));
      
      // Connect input of this circuit to the input of each of the single banded waveguides
      passThrough.output.connect(waveguides[i].input);
      
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
    for(SingleBandedWaveguide s : waveguides)
    {
      s.pitchBend(f1, f2, duration);
    }
  }

}
