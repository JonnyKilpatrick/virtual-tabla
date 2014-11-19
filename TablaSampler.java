import processing.core.*;
import com.jsyn.*;
import com.jsyn.data.*;
import com.jsyn.unitgen.*;
import java.io.File;
import com.jsyn.util.*;

/**
 * Class for a TablaSampler, plays tabla samples given MidiMessages
 */

public class TablaSampler implements IAudioPlayer
{ 
  /**************************************************************************************************/
  //
  /* Instance variables 
  //
  /**************************************************************************************************/
  
  PApplet parent;        // PApplet the parent to use the processing library
  
  String sourceFile;     // Tabla sample
  
  Synthesizer synth;     // JSyn synthesizer
  
  FloatSample sample;    // Drum sample
  
  FixedRateMonoReader leftSamplePlayer;   // Sample player
  FixedRateMonoReader rightSamplePlayer;   // Sample player
  
  LineOut lineOut;       // Output
  
  
  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/
  
  /**
   * Class constructor, takes the parent PApplet, sets up the JSyn unit generators to play tabla samples
   * @param parent PApplet the PApplet processing parent
   */
  public TablaSampler(PApplet parent)
  {
    // Initialise the PApplet parent
    this.parent = parent;
    
    // Setup
    try
    {
      // Initialise and start synthesizer with a line out
      synth = JSyn.createSynthesizer();
      synth.start();
      synth.add(lineOut = new LineOut());
      lineOut.start();
      
      // Set up left drum sample player
      leftSamplePlayer = new FixedRateMonoReader();
      // Add to synthesizer
      synth.add(leftSamplePlayer);
      // Connect output to line out
      leftSamplePlayer.output.connect(lineOut.input);
      // Start sample player
      leftSamplePlayer.start();
      
      // Set up right drum sample player
      rightSamplePlayer = new FixedRateMonoReader();
      // Add to synthesizer
      synth.add(rightSamplePlayer);
      // Connect output to line out
      rightSamplePlayer.output.connect(lineOut.input);
      // Start sample player
      rightSamplePlayer.start();
      
      // Set up sample file
      sourceFile = parent.sketchPath("") + "DrumSamples/na.wav";
      File sampleFile = new File(sourceFile);
      sample = SampleLoader.loadFloatSample(sampleFile);

    }
    // Handle file not found
    catch (Exception ex)
    {
      System.out.println("Unable to load sample: '" + sourceFile + "'");
    }
  }
  
  
  /**************************************************************************************************/
  //
  /* playSample
  //
  /**************************************************************************************************/
  
  /**
   * Class constructor, takes the parent PApplet, sets up the JSyn unit generators to play tabla samples
   * @param midiMessage MidiMessage the message containing velocity, note and right/left drum 
   */
  public void playSample(MidiMessage midi)  
  {
    
    // If left drum, trigger the left drum sampler
    if (midi.getDrum() == TablaDrum.LEFT)
    {
      // Set the amplitude
      leftSamplePlayer.amplitude.set((double) midi.getVelocity() /127);
      
      // Trigger the sound
      leftSamplePlayer.dataQueue.clear();
      leftSamplePlayer.dataQueue.queue(sample, 0, sample.getNumFrames());
    }
    
    // Else if right drum, trigger the right drum sampler
    else if (midi.getDrum() == TablaDrum.RIGHT)
    {
      // Set the amplitude
      rightSamplePlayer.amplitude.set((double) midi.getVelocity() /127);
      
      // Trigger the sound
      rightSamplePlayer.dataQueue.clear();
      rightSamplePlayer.dataQueue.queue(sample, 0, sample.getNumFrames());
    }
    
  }  
}


























//
//
//import processing.core.*;
//import beads.*;
//
//public class TablaSampler implements IAudioPlayer
//{
//  
//  /**************************************************************************************************/
//  //
//  /* Instance variables 
//  //
//  /**************************************************************************************************/
//  
//  PApplet parent;        // PApplet the parent to use the processing library
//  
//  AudioContext audio;    // Audio context hardware connection
//  SamplePlayer sampler;  // SamplePlayer to play tabla drum samples
//  Gain gain;             // Gain for the audio
//  
//  String sourceFile;     // Tabla sample
//  
//  
//  /**************************************************************************************************/
//  //
//  /* Constructor 
//  //
//  /**************************************************************************************************/
//  
//  public TablaSampler(PApplet parent)
//  {
//    // Initialise the PApplet parent
//    this.parent = parent;
//    
//    // Initialise the BEADS audio context, to connect to send audio data to the speakers
//    audio = new AudioContext();
//    
//    // Initialise SamplePlayer
//    try
//    {
//      sourceFile = parent.sketchPath("") + "DrumSamples/na.wav";     // Source File
//      sampler = new SamplePlayer(audio, new Sample(sourceFile));
//      sampler.setKillOnEnd(false);
//    }
//    // Handle file not found
//    catch (Exception ex)
//    {
//      System.out.println("Unable to load sample: '" + sourceFile + "'");
//    }
//    
//    // Set the Gain
//    gain = new Gain(audio, 1, 0.2f);
//    
//    // Connect sampler to gain
//    gain.addInput(sampler);
//    
//    // Connect gain to AudioContext
//    audio.out.addInput(gain);
//    
//    // Set to start
//    sampler.setToLoopStart();
//    
//    // Start audio processing
//    audio.start();
//  }
//  
//  
//  /**************************************************************************************************/
//  //
//  /* playSample
//  //
//  /**************************************************************************************************/
//  
//  public void playSample()  
//  {
//    // Trigger the sound
//    sampler.start();
//    // Set to start
//    sampler.setToLoopStart();
//  }  
//}
