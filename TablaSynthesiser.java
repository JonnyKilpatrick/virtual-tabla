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
  
  // Drum synthesisers
//  private KarplusStrongNote hiCenterSynth;
//  private KarplusStrongNote hiRimSynth;
//  private KarplusStrongNote lowCenterSynth;
//  private KarplusStrongNote lowRimSynth;

  private BandedWaveguideNote hiCenterSynth;
  
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
    synth.getAudioDeviceManager().setSuggestedOutputLatency( 0.04 );
    synth.start();
    synth.add(lineOut = new LineOut());

    // Initialise drum synthesisers
    hiCenterSynth = new BandedWaveguideNote(synth, lineOut, 3, null);//readSamplesFromFile(parent.sketchPath("") + HIGH_TABLA));
    //lowCenterSynth = new KarplusStrongNote(synth, lineOut, null);//readSamplesFromFile(parent.sketchPath("") + LOW_TABLA));
    //hiRimSynth = new KarplusStrongNote(synth, lineOut, null);//readSamplesFromFile(parent.sketchPath("") + HIGH_RIM));
    //lowRimSynth = new KarplusStrongNote(synth, lineOut, null);//readSamplesFromFile(parent.sketchPath("") + LOW_RIM));
    
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
      
      // If the rim is hit
      if(note > 110)
      {
//        lowRimSynth.playNote(180, amplitude, 10, 20);
      }
      else
      {
        double frequency = LOWEST_FREQUENCY_LOW + (lowFrequencyRange * ((double) note/127));
        double duration = LONGEST_DURATION - (durationRange * ((double) note/127));
        //lowCenterSynth.playNote(frequency, amplitude, duration, 60);
      }
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
      
      // If the rim is hit
      if(note > 110)
      {
//        hiRimSynth.playNote(430, amplitude, 10, 20);
      }
      else
      {
        double frequency = LOWEST_FREQUENCY_HI + (hiFrequencyRange * ((double) note/127));
        double duration = LONGEST_DURATION - (durationRange * ((double) note/127));
        //hiCenterSynth.playNote(frequency, amplitude, duration, 60);
        hiCenterSynth.playNote(
          new WaveguideParameters[] {
            new WaveguideParameters(940, 0.67, 4, 1.0),
            new WaveguideParameters(940, 0.54, 2, 0.8),
            new WaveguideParameters(940, 0.5, 2, 0.7)
          },
          940,
          amplitude
        );
      }
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
      doubleTable[i] = (float) sample.readDouble(i);
    }
    return doubleTable;
  }
}
