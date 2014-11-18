import com.leapmotion.leap.*;
import beads.*;

/**************************************************************************************************/
//
/* Constants
 //
/**************************************************************************************************/

// Size of finger circles
final float ELLIPSE_MIN = 10;
final float ELLIPSE_MAX = 30;

// Thresholds for gesture recognition
final float VELOCITY_THRESHOLD = -150;
final float TIME_THRESHOLD = 0;
final float LENGTH_THRESHOLD = 0;
final long REST_PERIOD = 100000;
final float DISTANCE_TO_PAUSE_HITS = 150;//100000;

// For Midi conversion
final float VELOCITY_MAXIMUM = -300;

/**************************************************************************************************/
//
/* Instance variables
 //
/**************************************************************************************************/

Controller leap;       // LEAP controller
MapLeapPoints mapToScreen; // Class to map the real co-ordinates to screen space
IAudioPlayer audioPlayer; // IAudioPlayer for creating/playing sound
GestureRecogniser gestureRecogniser; // For recognising gestures

Frame frame; // The most recent frame


/**************************************************************************************************/
//
/* Class variables
 //
/**************************************************************************************************/

// Leap Motion interaction space
Vector leapMinimum = new Vector(-200, -180, 30);
Vector leapMaximum = new Vector(200, 140, 250);

// Virtual Screen space
Vector screenMinimum = new Vector(0, 0, 0);
Vector screenMaximum;

float zRange;
float ellipseRange = ELLIPSE_MAX - ELLIPSE_MIN;

// Circle properties
Vector leftCircleCenter;
float leftCircleRadius;
Vector rightCircleCenter;
float rightCircleRadius;


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
  noStroke();

  // Set up display
  ellipseMode(RADIUS);
  leftCircleCenter = new Vector(width * (3.5/ 12.0), height/2.0, 0);
  leftCircleRadius = width * (5.0/24.0);
  rightCircleCenter = new Vector(width * (9.0/12.0), height/2.0, 0);
  rightCircleRadius = width/6.0;

  // Connect to the LEAP sensor
  leap = new Controller();

  // Set up class to map real co-ordinates to screen space in real-time
  screenMaximum = new Vector(width, height, 100);
  mapToScreen = 
    new MapLeapPoints(leapMinimum, leapMaximum, screenMinimum, screenMaximum, 
  VELOCITY_THRESHOLD, VELOCITY_MAXIMUM, 
  leftCircleCenter, leftCircleRadius, rightCircleCenter, rightCircleRadius);

  // Set up points to work out size of finger circle
  zRange = (screenMaximum.getZ() - screenMinimum.getZ()) * (3.0/4.0);

  // Set up gesture recogniser
  gestureRecogniser = 
    new GestureRecogniser(VELOCITY_THRESHOLD, TIME_THRESHOLD, LENGTH_THRESHOLD, REST_PERIOD, 
  DISTANCE_TO_PAUSE_HITS);

  // Set up Audio Player
  audioPlayer = new TablaSampler(this);
}


/**************************************************************************************************/
//
/* Draw Method (Looped)
 //
/**************************************************************************************************/

void draw() 
{
  background(166, 128, 100);
  strokeWeight(4);
  stroke(32, 24, 18);

  // Larger Drum
  fill(247, 232, 179);
  ellipse(leftCircleCenter.getX(), leftCircleCenter.getY(), leftCircleRadius, leftCircleRadius);
  //  // Syahi in middle
  //  fill(32, 24, 18);
  //  ellipse(width * (3.5/ 12.0), height/2.0, width * (1.0 /8.0), width * (1.0/8.0));

  // Smaller Drum
  fill(247, 232, 179);
  ellipse(rightCircleCenter.getX(), rightCircleCenter.getY(), rightCircleRadius, rightCircleRadius);
  //  // Syahi in middle
  //  fill(32, 24, 18);
  //  ellipse(width * (9.0/12.0), height/2.0, width * (1.0/8.0), width * (1.0/8.0));

  // ...

  // Set hand/ finger/ tool colour
  stroke(55);

  // Get the most recent frame
  frame = leap.frame();

  /*************************************************************/
  /* For each hand                      
  /*************************************************************/

  for (Hand hand : frame.hands ()) 
  {

    /*************************************************************/
    /* For each finger                      
    /*************************************************************/

    for (Finger finger : hand.fingers ()) 
    {
      // Work out if sound has been triggered
      Gesture gesture = gestureRecogniser.checkForGestures(finger);

      // If sound should be triggered play sound
      if (gesture != null)
      { 
        MidiMessage midi = mapToScreen.convertToMidiMessage(gesture); 
        if (midi !=null)
        {
          audioPlayer.playSample();
          println("Velocity: "+midi.getVelocity()+" Note:"+midi.getNote()+" Drum: "+midi.getDrum());
        }
      }

      // Clean up the map storing gestures
      gestureRecogniser.cleanUpFingerMap(frame);


      // Get the finger position in real space
      Vector fingerPosition   = finger.tipPosition();

      // Flip y and z axis for the natural way to play a drum
      fingerPosition = new Vector(fingerPosition.getX(), fingerPosition.getZ(), fingerPosition.getY());

      // Convert to screen space
      fingerPosition = mapToScreen.convertLeapCoordinates(fingerPosition);

      // Draw finger
      fill(0);
      float ellipseDiameter = ELLIPSE_MIN + (((zRange - fingerPosition.getZ()) / zRange) * (ellipseRange));
      ellipse(fingerPosition.getX(), fingerPosition.getY(), ellipseDiameter, ellipseDiameter);
    }
  }
}

