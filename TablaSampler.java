import processing.core.*;
import beads.*;

public class TablaSampler implements IAudioPlayer
{
  
  /**************************************************************************************************/
  //
  /* Instance variables 
  //
  /**************************************************************************************************/
  
  PApplet parent;        // PApplet the parent to use the processing library
  
  AudioContext audio;    // Audio context hardware connection
  SamplePlayer sampler;  // SamplePlayer to play tabla drum samples
  Gain gain;             // Gain for the audio
  
  String sourceFile;     // Tabla sample
  
  
  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/
  
  public TablaSampler(PApplet parent)
  {
    // Initialise the PApplet parent
    this.parent = parent;
    
    // Initialise the BEADS audio context, to connect to send audio data to the speakers
    audio = new AudioContext();
    
    // Initialise SamplePlayer
    try
    {
      sourceFile = parent.sketchPath("") + "DrumSamples/na.wav";     // Source File
      sampler = new SamplePlayer(audio, new Sample(sourceFile));
      sampler.setKillOnEnd(false);
    }
    // Handle file not found
    catch (Exception ex)
    {
      System.out.println("Unable to load sample: '" + sourceFile + "'");
    }
    
    // Set the Gain
    gain = new Gain(audio, 1, 0.2f);
    
    // Connect sampler to gain
    gain.addInput(sampler);
    
    // Connect gain to AudioContext
    audio.out.addInput(gain);
    
    // Set to start
    sampler.setToLoopStart();
    
    // Start audio processing
    audio.start();
  }
  
  
  /**************************************************************************************************/
  //
  /* playSample
  //
  /**************************************************************************************************/
  
  public void playSample()  
  {
    // Trigger the sound
    sampler.start();
    // Set to start
    sampler.setToLoopStart();
  }  
}
