import processing.core.*;
import com.jsyn.*;
import com.jsyn.data.*;
import com.jsyn.unitgen.*;
import java.io.File;
import com.jsyn.util.*;
import java.io.FileNotFoundException;

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
  
  Synthesizer synth;     // JSyn synthesizer
  
  // SAMPLES
  FloatSample lowCenter1; // Drum sample
  FloatSample lowCenter2; // Drum sample
  FloatSample lowMid1;    // Drum sample
  FloatSample lowMid2;    // Drum sample
  FloatSample lowRim1;    // Drum sample
  FloatSample lowRim2;    // Drum sample
  
  FloatSample hiCenter1; // Drum sample
  FloatSample hiCenter2; // Drum sample
  FloatSample hiMid1;    // Drum sample
  FloatSample hiMid2;    // Drum sample
  FloatSample hiRim1;    // Drum sample
  FloatSample hiRim2;    // Drum sample
  
  // SAMPLE PLAYERS
  FixedRateMonoReader lowCenter;  // Sample player
  FixedRateMonoReader lowMid;     // Sample player
  FixedRateMonoReader lowRim;     // Sample player
  FixedRateMonoReader hiCenter;   // Sample player
  FixedRateMonoReader hiMid;      // Sample player
  FixedRateMonoReader hiRim;      // Sample player
  
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
  public TablaSampler(PApplet parent) throws FileNotFoundException
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
      
      /**************************************************************************************************/
      /* Left drum sample players 
      /**************************************************************************************************/
      
      // Left center
      lowCenter = new FixedRateMonoReader();
      // Add to synthesizer
      synth.add(lowCenter);
      // Connect output to line out
      lowCenter.output.connect(0, lineOut.input, 0);
      lowCenter.output.connect(0, lineOut.input, 1);
      // Start sample player
      lowCenter.start();
      
      // Left mid
      lowMid = new FixedRateMonoReader();
      // Add to synthesizer
      synth.add(lowMid);
      // Connect output to line out
      lowMid.output.connect(0, lineOut.input, 0);
      lowMid.output.connect(0, lineOut.input, 1);
      // Start sample player
      lowMid.start();
      
      // Left rim
      lowRim = new FixedRateMonoReader();
      // Add to synthesizer
      synth.add(lowRim);
      // Connect output to line out
      lowRim.output.connect(0, lineOut.input, 0);
      lowRim.output.connect(0, lineOut.input, 1);
      // Start sample player
      lowRim.start();
      
      /**************************************************************************************************/
      /* Right drum sample players
      /**************************************************************************************************/
      
      // Right center
      hiCenter = new FixedRateMonoReader();
      // Add to synthesizer
      synth.add(hiCenter);
      // Connect output to line out
      hiCenter.output.connect(0, lineOut.input, 0);
      hiCenter.output.connect(0, lineOut.input, 1);
      // Start sample player
      hiCenter.start();
      
      // Right mid
      hiMid = new FixedRateMonoReader();
      // Add to synthesizer
      synth.add(hiMid);
      // Connect output to line out
      hiMid.output.connect(0, lineOut.input, 0);
      hiMid.output.connect(0, lineOut.input, 1);
      // Start sample player
      hiMid.start();
      
      // Right rim
      hiRim = new FixedRateMonoReader();
      // Add to synthesizer
      synth.add(hiRim);
      // Connect output to line out
      hiRim.output.connect(0, lineOut.input, 0);
      hiRim.output.connect(0, lineOut.input, 1);
      // Start sample player
      hiRim.start();
      
      /**************************************************************************************************/
      /* Left drum sample's
      /**************************************************************************************************/

      lowCenter1 = SampleLoader.loadFloatSample(new File(parent.sketchPath("") + "Samples/Low/21_15_10.aif"));
      lowCenter2 = SampleLoader.loadFloatSample(new File(parent.sketchPath("") + "Samples/Low/21_15_09.aif"));
      lowMid1 = SampleLoader.loadFloatSample(new File(parent.sketchPath("") + "Samples/Low/21_15_06.aif"));
      lowMid2 = SampleLoader.loadFloatSample(new File(parent.sketchPath("") + "Samples/Low/21_15_07.aif"));
      lowRim1 = SampleLoader.loadFloatSample(new File(parent.sketchPath("") + "Samples/Low/21_15_03.aif"));
      lowRim2 = SampleLoader.loadFloatSample(new File(parent.sketchPath("") + "Samples/Low/21_15_02.aif"));
      
      /**************************************************************************************************/
      /* Right drum sample's
      /**************************************************************************************************/

      hiCenter1 = SampleLoader.loadFloatSample(new File(parent.sketchPath("") + "Samples/High/21_14_01.aif"));
      hiCenter2 = SampleLoader.loadFloatSample(new File(parent.sketchPath("") + "Samples/High/21_14_02.aif"));
      hiMid1 = SampleLoader.loadFloatSample(new File(parent.sketchPath("") + "Samples/High/21_14_12.aif"));
      hiMid2 = SampleLoader.loadFloatSample(new File(parent.sketchPath("") + "Samples/High/21_14_15.aif"));
      hiRim1 = SampleLoader.loadFloatSample(new File(parent.sketchPath("") + "Samples/High/21_14_04.aif"));
      hiRim2 = SampleLoader.loadFloatSample(new File(parent.sketchPath("") + "Samples/High/21_14_11.aif"));

    }
    // Handle file not found
    catch (Exception ex)
    {
      ex.printStackTrace();
      throw new FileNotFoundException("Error: Unable to load drum samples");
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
    
      // Center
      if (note<55)
      {
        // Set the amplitude
        lowCenter.amplitude.set((double) midi.getVelocity() /127);
        
        if (note <35)
        {
          lowCenter.dataQueue.clear();
          lowCenter.dataQueue.queue(lowCenter1, 0, lowCenter1.getNumFrames());
        }
        else
        {
          lowCenter.dataQueue.clear();
          lowCenter.dataQueue.queue(lowCenter2, 0, lowCenter2.getNumFrames());
        }
      }
      
      // Mid
      else if (note < 110)
      {
        // Set the amplitude
        lowMid.amplitude.set((double) midi.getVelocity() /127);
        
        if (note <85)
        {
          lowMid.dataQueue.clear();
          lowMid.dataQueue.queue(lowMid1, 0, lowMid1.getNumFrames());
        }
        else
        {
          lowMid.dataQueue.clear();
          lowMid.dataQueue.queue(lowMid2, 0, lowMid2.getNumFrames());
        }
      }
      else
      {
        // Set the amplitude
        lowRim.amplitude.set((double) midi.getVelocity() /127);
        
        if (note <120)
        {
          lowRim.dataQueue.clear();
          lowRim.dataQueue.queue(lowRim1, 0, lowRim1.getNumFrames());
        }
        else
        {
          lowRim.dataQueue.clear();
          lowRim.dataQueue.queue(lowRim2, 0, lowRim2.getNumFrames());
        }
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
      
      // Center
      if (note<55)
      {
        // Set the amplitude
        hiCenter.amplitude.set((double) midi.getVelocity() /127);
        
        if (note <35)
        {
          hiCenter.dataQueue.clear();
          hiCenter.dataQueue.queue(hiCenter1, 0, hiCenter1.getNumFrames());
        }
        else
        {
          hiCenter.dataQueue.clear();
          hiCenter.dataQueue.queue(hiCenter2, 0, hiCenter2.getNumFrames());
        }
      }
      
      // Mid
      else if (note < 110)
      {
        // Set the amplitude
        hiMid.amplitude.set((double) midi.getVelocity() /127);
        
        if (note <85)
        {
          hiMid.dataQueue.clear();
          hiMid.dataQueue.queue(hiMid1, 0, hiMid1.getNumFrames());
        }
        else
        {
          hiMid.dataQueue.clear();
          hiMid.dataQueue.queue(hiMid2, 0, hiMid2.getNumFrames());
        }
      }
      else
      {
        // Set the amplitude
        hiRim.amplitude.set((double) midi.getVelocity() /127);
        
        if (note <120)
        {
          hiRim.dataQueue.clear();
          hiRim.dataQueue.queue(hiRim1, 0, hiRim1.getNumFrames());
        }
        else
        {
          hiRim.dataQueue.clear();
          hiRim.dataQueue.queue(hiRim2, 0, hiRim2.getNumFrames());
        }
      }
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
      throw new ArithmeticException("Error: Failed to convert Midi number to amplitude");
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
