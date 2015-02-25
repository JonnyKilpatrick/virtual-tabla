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
  
  private final String TABLA_SAMPLE = "Samples/High/21_14_01.aif";
  
  
  /**************************************************************************************************/
  //
  /* Instance variables 
   //
  /**************************************************************************************************/

  // JSyn Unit Generators

  Synthesizer synth;     // JSyn synthesizer
  LineOut lineOut;       // Output
  DrumSynthNote drumSynth;
  DrumSynthNote drumSynthOne;

  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/

  /**
   * Class constructor
   */
  public TablaSynthesiser(PApplet parent) throws IOException
  {
    // Initialise and start synthesizer with a line out
    synth = JSyn.createSynthesizer();
    synth.start();
    synth.add(lineOut = new LineOut());

    // New drum synth
    drumSynth = new DrumSynthNote(synth, lineOut, readSamplesFromFile(parent.sketchPath("") + TABLA_SAMPLE));
    drumSynthOne = new DrumSynthNote(synth, lineOut, null);
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
      double amplitude = midi.getVelocity() / 127.0;
      drumSynthOne.playNote(100, amplitude, 150, 40);
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
      double amplitude = midi.getVelocity() / 127.0;
      drumSynth.playNote(500, amplitude, 3, 40);
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
      throw new ArithmeticException("Error: Failed to convert Midi number to amplitude");
    }
  }
  
  /**
   * Reads data from a file to initialise the wave table, returning a double array of the data samples 
   * @param filePath String the url of the file
   */
   
  private double[] readSamplesFromFile(String filePath) throws IOException
  {
    File file = new File(filePath);
    FloatSample sample = SampleLoader.loadFloatSample(file);
    
    // Return the sample as a double array
    double[] doubleTable = new double[sample.getNumFrames()];
    
    for(int i=0; i<sample.getNumFrames(); i++)
    {
      doubleTable[i] = sample.readDouble(i);
    }
    return doubleTable;
  }
}
