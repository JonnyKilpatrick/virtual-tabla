import com.leapmotion.leap.*;
import java.util.Iterator;
import controlP5.*;

/**************************************************************************************************/
//
/* Constants
//
/**************************************************************************************************/

// Size of finger circles
final float ELLIPSE_MIN = 5;
final float ELLIPSE_MAX = 16;
final float DROPLET_STARTING_RADIUS = 1;
final float MAX_DROPLET_RADIUS = 100;
final float DROPLET_INCREASE_PER_FRAME = 3;

// Thresholds for gesture recognition
final float VELOCITY_THRESHOLD = -200;
final float TIME_THRESHOLD = 0;
final float LENGTH_THRESHOLD = 0;
final long REST_PERIOD = 100000;          // microseconds
final float DISTANCE_TO_PAUSE_HITS = 150;
final long PITCH_SLIDE_REST = 100000;
final float HEIGHT_FOR_SLIDE = 100;

// For Midi conversion
final float VELOCITY_MAXIMUM = -2000;

// Image widths
final float SCALER_BAYAN = 0.91;
final float SCALER_DAYAN = 0.78;

// Sensitivity min and max
final float SENSITIVE_NORMAL = 5;
final float SENSITIVE_MIN = 0;
final float SENSITIVE_MAX = 10;
final float VELOCITY_MIN = -300;
final float VELOCITY_MAX = -100;

/**************************************************************************************************/
//
/* Instance variables
//
/**************************************************************************************************/

com.leapmotion.leap.Controller leap;       // LEAP controller
MapLeapPoints mapToScreen;                 // Class to map the real co-ordinates to screen space
IAudioPlayer audioPlayer;                  // IAudioPlayer for creating/playing sound
GestureRecogniser gestureRecogniser;       // For recognising gestures

Frame frame;                               // The most recent frame

// Leap Motion interaction space
Vector leapMinimum = new Vector(-200, -180, 10);
Vector leapMaximum = new Vector(200, 140, 250);

// Virtual Screen space
Vector screenMinimum = new Vector(0, 0, 0);
Vector screenMaximum;

float zRange;
float ellipseRange = ELLIPSE_MAX - ELLIPSE_MIN;

// Background image of wood texture
PImage woodTexture;

// Images of Tabla drums
PImage bayan;
PImage dayan;

// Image properties
Vector leftDrumCenter;
float leftDrumWidth;
float leftDrumHeight;
Vector rightDrumCenter;
float rightDrumWidth;
float rightDrumHeight;

// List of circles for hits
ArrayList<Droplet> droplets;

// GUI controls 
ControlP5 controlP5;
float sensitivityValue = 10;


/**************************************************************************************************/
//
/* Setup method
//
/**************************************************************************************************/

void setup() 
{

  // Set up window 

  frameRate(200);

  size(displayWidth, displayHeight, P3D);
  
  // Set up display
  // Initialise images
  woodTexture = loadImage("wood-texture.png");
  bayan = loadImage("Bayan.png");
  dayan = loadImage("Dayan.png");
  
  // Sizes of images
  leftDrumCenter = new Vector(width * (3.25/ 12.0), height/2.0, 0);
  leftDrumWidth = bayan.width * SCALER_BAYAN;
  leftDrumHeight = bayan.height * SCALER_BAYAN;
  rightDrumCenter = new Vector(width * (9.25/12.0), height/2.0, 0);
  rightDrumWidth = dayan.width * SCALER_DAYAN;
  rightDrumHeight = dayan.height * SCALER_DAYAN;
  
  // Set correct sizes
  woodTexture.resize(displayWidth, displayHeight);
  bayan.resize((int)leftDrumWidth, (int)leftDrumHeight);
  dayan.resize((int)rightDrumWidth, (int)rightDrumHeight);
  
  // Initialise array list of droplets
  droplets = new ArrayList<Droplet>();

  // Connect to the LEAP sensor
  leap = new com.leapmotion.leap.Controller();

  // Set up class to map real co-ordinates to screen space in real-time
  screenMaximum = new Vector(width, height, 500);
  mapToScreen = 
    new MapLeapPoints(leapMinimum, leapMaximum, screenMinimum, screenMaximum, 
  VELOCITY_THRESHOLD, VELOCITY_MAXIMUM, 
  leftDrumCenter, leftDrumWidth/2.0, rightDrumCenter, rightDrumWidth/2.0);

  // Set up points to work out size of finger circle
  zRange = (screenMaximum.getZ() - screenMinimum.getZ());

  // Set up gesture recogniser
  gestureRecogniser = 
    new GestureRecogniser(VELOCITY_THRESHOLD, TIME_THRESHOLD, LENGTH_THRESHOLD, REST_PERIOD, 
  DISTANCE_TO_PAUSE_HITS, PITCH_SLIDE_REST, HEIGHT_FOR_SLIDE);
  
  // Set up gui controls
  controlP5 = new ControlP5(this);
  // Create new slider for sensitivity
  controlP5.addSlider("sensitivity", SENSITIVE_MIN, SENSITIVE_MAX, SENSITIVE_NORMAL, (int)(width * (8.0/10.0)), 
    (int)(height * (0.3/10.0)), 200, 30).setSliderMode(Slider.FLEXIBLE).setNumberOfTickMarks((int)SENSITIVE_MAX + 1).showTickMarks(false);
  // Create drop down list for samples / synthesis choice
  DropdownList dropdown = controlP5.addDropdownList("Sound Generator", (int)(width * (8.0/10.0)), (int)(height * (1.0/10.0)), 200, 100);
  
  dropdown.addItem("Tabla Sampler", 0);
  dropdown.addItem("Tabla Synthesizer", 1);
  dropdown.setValue(1);
  dropdown.setItemHeight(40);
  

  // Set up Audio Player
  try
  {
    //audioPlayer = new TablaSampler(this);
    audioPlayer = new TablaSynthesiser(this);
  }
  catch(Exception ex)
  {  
    ex.printStackTrace();
    System.out.println(ex.getMessage());
  }
}


/**************************************************************************************************/
//
/* Draw Method (Looped)
//
/**************************************************************************************************/

void draw() 
{
  background(0);
  imageMode(CORNER);
  image(woodTexture, 0, 0);
  
  stroke(0);

//  // Larger Drum
//  fill(247, 232, 179);
//  ellipse(leftCircleCenter.getX(), leftCircleCenter.getY(), leftCircleRadius, leftCircleRadius);
//  //  // Syahi in middle
//  fill(0, 0, 0);
//  ellipse(width * (3.5/ 12.0), height/2.0, width * (1.0 /15.0), width * (1.0/15.0));
//
//  // Smaller Drum
//  fill(247, 232, 179);
//  ellipse(rightCircleCenter.getX(), rightCircleCenter.getY(), rightCircleRadius, rightCircleRadius);
//  //  // Syahi in middle
//  fill(0, 0, 0);
//  ellipse(width * (9.0/12.0), height/2.0, width * (1.0/15.0), width * (1.0/15.0));

  // Display drum images
  imageMode(CENTER);
  image(bayan, leftDrumCenter.getX(), leftDrumCenter.getY());
  image(dayan, rightDrumCenter.getX(), rightDrumCenter.getY());

  try
  {
  
    // Get the most recent frame
    frame = leap.frame();
  
    /*************************************************************/
    /* For each hand                      
    /*************************************************************/
  
    for (Hand hand : frame.hands ()) 
    {
      
      // Keep track of triggered fingers, so that can just play the most central in this frame
      MidiMessage bestMidi = null;
      Finger bestFinger = null;
      Vector bestFingerPosition = null;
      byte bestNote = 127;
  
      /*************************************************************/
      /* For each finger                      
      /*************************************************************/
  
      for (Finger finger : hand.fingers ()) 
      {
        // Boolean for whether this finger triggered note
        boolean triggered = false;
        
        // Work out if sound has been triggered
        Gesture gesture = gestureRecogniser.checkForGestures(finger);
  
        // If gesture was detected, check to see if a note should be played
        // by converting to MIDI message
        
        boolean bestSoFar = false;  // Whether this finger is the closest to a drum center so far
        
        if (gesture != null)
        { 
          MidiMessage midi = mapToScreen.convertToMidiMessage(gesture);
         
          // If a midi message was returned, if this finger position is closer to the center of the drum than 
          // the current beft of this frame, set this as the best so far
          if (midi != null && midi.getNote() < bestNote)
          {
            bestMidi = midi;
            bestFinger = finger;
            bestNote = midi.getNote();
            bestSoFar = true;
          }
        }
  
        // Clean up the map storing gestures
        gestureRecogniser.cleanUpFingerMap(frame);
  
        // Get the finger position in real space
        Vector fingerPosition = finger.tipPosition();
  
        // Flip y and z axis for the natural way to play a drum
        fingerPosition = new Vector(fingerPosition.getX(), fingerPosition.getZ(), fingerPosition.getY());
        
        // Convert to screen space
        fingerPosition = mapToScreen.convertLeapCoordinates(fingerPosition);
        
        if(bestSoFar == true)
        {
          bestFingerPosition = fingerPosition;
        }
  
        // Draw finger
        fill(255);
        // If touching skin, show difference outline colour to indicate a slide is possible
        if(hand.palmPosition().getY() < HEIGHT_FOR_SLIDE)
        {
          fill(255, 0, 0);
        }
        else
        {
          fill(255);
        }
        float ellipseDiameter = ELLIPSE_MIN + (((zRange - fingerPosition.getZ()) / zRange) * ellipseRange);
        if(fingerPosition.getZ() > zRange)
        {
          ellipseDiameter = ELLIPSE_MIN;
        }
        ellipse(fingerPosition.getX(), fingerPosition.getY(), ellipseDiameter, ellipseDiameter);
      }
      
      // If note was play from this hand in this frame, trigger sound
      if(bestMidi != null)
      {      
        // Pause recognition for set no of frames in that position
        gestureRecogniser.pausePosition(bestFinger);
        
        // Play sound
        audioPlayer.playSound(bestMidi);
        
        // If this was the left drum hit, record it so that pitch slides cannot be triggered straight away
        if(bestMidi.getDrum() == TablaDrum.LEFT)
        {
          gestureRecogniser.recordLeftDrumHit(frame);
        }
        
        // Sound was hit, so create a droplet, and add it to array list
        droplets.add(new Droplet(this, bestFingerPosition.getX(), bestFingerPosition.getY(), DROPLET_STARTING_RADIUS, DROPLET_INCREASE_PER_FRAME));
      }
      
      // If the audio player is TablaSynthesiser, with a pitch bend, gesture recognise a pitch bend 
      
      if(audioPlayer instanceof TablaSynthesiser)
      {
      
        // Work out if the palm has been slid to control pitch
        Gesture pitchBend = gestureRecogniser.checkForPalmSlide(hand);
        
        // If a gesutre was found
        if(pitchBend != null)
        {
          // Convert gesture to midi message
          MidiMessage midiPitchBend = mapToScreen.convertToMidiMessage(pitchBend);
          
          // If a Midi message was returned, tell the synthesiser to pitch bend
          if (midiPitchBend != null)
          {
            ((TablaSynthesiser)audioPlayer).pitchBend(midiPitchBend);
          }
        }
      }
      
      // Set colour for droplets
      noFill();
      stroke(255);
      
      // Display and update all current droplets
      Iterator<Droplet> iterator = droplets.iterator();
      while(iterator.hasNext())
      {
        Droplet droplet = iterator.next();
        droplet.updateAndDisplay();
        
        // If droplet size is not at max, remove from array list
        if(droplet.getRadius() >= MAX_DROPLET_RADIUS)
        {
          iterator.remove();
        }
      }
      
    }
  }
  catch(Exception ex)
  {
    System.out.println(ex.getMessage());
    System.exit(0);
  }
}

/**************************************************************************************************/
//
/* sensitivitySlider
//
/**************************************************************************************************/
/**
 * Event when slider is clicked
 * @param sensitivity float the sensitivity value from the slider
 */

void sensitivity(float sensitivity)
{
  // Work out velocity from sensitivity value
  sensitivityValue = VELOCITY_MIN + ((sensitivity/SENSITIVE_MAX) * Math.abs(VELOCITY_MAX - VELOCITY_MIN));
  
  // Update gesture recognition value for velocity threshold
  gestureRecogniser.setVelocityThreshold(sensitivityValue);
}

/**************************************************************************************************/
//
/* controlEvent
//
/**************************************************************************************************/
/**
 * Event when drop down list is changed
 * @param event ControlEvent the event
 */

void controlEvent(ControlEvent event)
{
  // Get value
  int value = (int)event.getGroup().getValue();
  
  try
  {
    switch(value)
    {
      case 0:  audioPlayer = new TablaSampler(this);
               break;
      case 1:  audioPlayer = new TablaSynthesiser(this);
               break;
      default: audioPlayer = new TablaSynthesiser(this);
    }
  }
  catch(Exception ex)
  {
    System.out.println("Failed to change audio generator");
  }
}
