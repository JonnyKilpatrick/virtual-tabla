import processing.core.*;
import com.jsyn.*;
import com.jsyn.data.*;
import com.jsyn.unitgen.*;
import com.jsyn.util.*;
import java.io.File;
import java.io.IOException;

/**
 * Class for a TablaSynthesiser, synthesises tabla sounds given MidiMessages
 */

public class TablaSynthesiser implements IAudioPlayer
{ 
  /**************************************************************************************************/
  //
  /* Constants 
  //
  /**************************************************************************************************/
  
  // Samples
  private final String HIGH_TABLA = "Samples/High/21_14_01.aif";
  private final String LOW_TABLA = "Samples/Low/21_15_09.aif";
  private final String HIGH_RIM = "Samples/High/21_14_11.aif";
  private final String LOW_RIM = "Samples/Low/21_15_07.aif";
  
  // Frequencies
  private final double HIGHEST_FREQUENCY_HI = 430;
  private final double LOWEST_FREQUENCY_HI = 400;
  private final double HIGHEST_FREQUENCY_LOW = 120;
  private final double LOWEST_FREQUENCY_LOW = 100;
  
  // Durations
  private final double LONGEST_DURATION = 8;
  private final double SHORTEST_DURATION = 0.1;
  
  
  /**************************************************************************************************/
  //
  /* Instance variables 
  //
  /**************************************************************************************************/

  // JSyn Unit Generators
  private Synthesizer synth;     // JSyn synthesizer
  private LineOut lineOut;       // Output

  private BandedWaveguideNote hiCenterSynth;
  private BandedWaveguideNote lowCenterSynth;
  
  private double hiFrequencyRange;
  private double lowFrequencyRange;
  private double durationRange;
  

  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/

  /**
   * Class constructor
   * @param parent PApplet the sketch the return the sketch path for files
   */
  public TablaSynthesiser(PApplet parent) throws IOException
  {
    // Initialise and start synthesizer with a line out
    synth = JSyn.createSynthesizer();
    //synth.getAudioDeviceManager().setSuggestedOutputLatency( 0.04 );
    synth.start();
    synth.add(lineOut = new LineOut());

    // Initialise drum synthesisers
    hiCenterSynth = new BandedWaveguideNote(synth, lineOut, 5);
    lowCenterSynth = new BandedWaveguideNote(synth, lineOut, 5);
    
    // Work out frequency ranges
    hiFrequencyRange = HIGHEST_FREQUENCY_HI - LOWEST_FREQUENCY_HI;
    lowFrequencyRange = HIGHEST_FREQUENCY_LOW - LOWEST_FREQUENCY_LOW;
    
    // Work out duration range
    durationRange = LONGEST_DURATION - SHORTEST_DURATION;
  }


  /**************************************************************************************************/
  //
  /* playSample
  //
  /**************************************************************************************************/

  /**
   * Implements playSample method from IAudioPlayer interface, using the MIDI message to read which 
   * drum was hit and then call the relevant private method
   * @param midiMessage MidiMessage the message containing velocity, note and right/left drum 
   */
  public void playSample(MidiMessage midi)
  {
    // If left drum, trigger the left drum sampler
    if (midi.getDrum() == TablaDrum.LEFT)
    {  
      // Trigger the sound
      playLowDrum(midi);
    }

    // Else if right drum, trigger the right drum sampler
    else if (midi.getDrum() == TablaDrum.RIGHT)
    {
      // Trigger the sound
      playHighDrum(midi);
    }
  }  


  /**
   * Plays the left bigger drum samples given the note 
   * @param midi MidiMessage the midi message containing velocity 0-127 and 0-127 from the center of the drum to the rim 
   */

  private void playLowDrum(MidiMessage midi)
  {
    // Split up drum into three sections and call the corresponding sample player

    byte note = midi.getNote();

    try
    {
      // Set the amplitude
      double amplitude = (double) midi.getVelocity() / 127.0;

      //double frequency = LOWEST_FREQUENCY_LOW + (lowFrequencyRange * ((double) note/127));
      //double duration = LONGEST_DURATION - (durationRange * ((double) note/127));
        //lowCenterSynth.playNote(frequency, amplitude, duration, 60);
//        lowCenterSynth.playNote(
//          new WaveguideParameters[] {
//            new WaveguideParameters(124, 10.0, 20, 1.0),
//            new WaveguideParameters(558, 5.4, 20, 1.0),
//            new WaveguideParameters(1251, 4.2, 20, 1.0),
//            new WaveguideParameters(1728, 2.7, 20, 1.0),
//            new WaveguideParameters(3728, 2.8, 20, 1.0)
//          },
//          124
//        );

      lowCenterSynth.playNote(
        new WaveguideParameters[] {
          new WaveguideParameters(371.0859375, 0.71295166015625, 38.03125, 0.7628173828125),
          new WaveguideParameters(724.171875, 0.059051513671875, 799.7734375, 0.5528564453125),
          new WaveguideParameters(465.109375, 0.674530029296875, 501.484375, 0.55609130859375),
          new WaveguideParameters(2974.71875, 0.168426513671875, 140.1328125, 0.593353271484375),
          new WaveguideParameters(191.0390625, 0.786865234375, 262.25, 0.858978271484375)
        },
        191.0390625,
        31.9921875
      );
      
      //lowCenterSynth.pitchBend(350, 0.2);
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
      throw new ArithmeticException("Error: Failed to convert Midi number to amplitude");
    }
  }

  /**
   * Plays the right smaller drum samples given the note 
   * @param midi MidiMessage the midi message containing velocity 0-127 and 0-127 from the center of the drum to the rim
   */

  private void playHighDrum(MidiMessage midi)
  {
    // Split up drum into three sections and call the corresponding sample player

    byte note = midi.getNote();

    try
    {
      // Set the amplitude
      double amplitude = (double) midi.getVelocity() / 127.0;

        //double frequency = LOWEST_FREQUENCY_HI + (hiFrequencyRange * ((double) note/127));
        //double duration = LONGEST_DURATION - (durationRange * ((double) note/127));
//        hiCenterSynth.playNote(
//          new WaveguideParameters[] {
//            new WaveguideParameters(417, 0.44, 20, 0.95),
//            new WaveguideParameters(699, 1, 20, 1.0),
//            new WaveguideParameters(1299, 1, 20, 1.0),
//            new WaveguideParameters(1725, 0.94, 20, 1.0),
//            new WaveguideParameters(2563, 0.88, 20, 1.0),
//            new WaveguideParameters(3934, 0.84, 20, 1.0)
//          },
//          417,
//          30
//        );

      hiCenterSynth.playNote(
        new WaveguideParameters[] {
          new WaveguideParameters(2544.6171875, 0.56427001953125, 125.1171875, 0.967529296875),
          new WaveguideParameters(1403.3359375, 0.584136962890625, 357.34375, 0.929473876953125),
          new WaveguideParameters(704.171875, 0.84820556640625, 94.0859375, 0.99322509765625),
          new WaveguideParameters(1690.40625, 0.89715576171875, 272.265625, 0.975341796875),
          new WaveguideParameters(2040.4921875, 0.45819091796875, 413.3984375, 0.77734375)
        },
        704.171875,
        31.9921875
      );
      
      hiCenterSynth.pitchBend(310, 0.5);
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
      throw new ArithmeticException("Error: Failed to convert Midi number to amplitude");
    }
  }
  
  /**
   * Reads data from a file to initialise the wave table, returning a float array of the data samples 
   * @param filePath String the url of the file
   * @return float[] the float array of samples from the file
   */
   
  private float[] readSamplesFromFile(String filePath) throws IOException
  {
    File file = new File(filePath);
    FloatSample sample = SampleLoader.loadFloatSample(file);
    
    // Return the sample as a double array
    float[] doubleTable = new float[sample.getNumFrames()];
    
    for(int i=0; i<sample.getNumFrames(); i++)
    {
      doubleTable[i] = (float) (sample.readDouble(i) * 100);
    }
    return doubleTable;
  }
}
