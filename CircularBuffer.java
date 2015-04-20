import com.jsyn.*;
import com.jsyn.data.*;
import com.jsyn.unitgen.*;
import com.jsyn.util.*;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;


/**
 * Implementation for a Circular buffer.
 * Allows you to populate a circular buffer of doubles, then read / write from it, 
 * whilst internally keeping tracking of the read and write pointers. 
 * Also allows a second read pointer
 * Specifically for the use in synthesis with the Karplus-Strong algorithm, so must specify desired delay length 
 * Has two one input and two outputs (one for each read pointer)
 */
 
public class CircularBuffer extends UnitFilter
{
  
  /**************************************************************************************************/
  //
  /* Instance variables 
  //
  /**************************************************************************************************/

  private double[] circularBuffer; // The actual array for the buffer
  private int readPointer;         // Read pointer
  private int writePointer;        // Write pointer
  private int bufferSize;          // Size of the buffer
  private int secondReadPointer;   // Second read pointer
  
  // Ports
  public UnitOutputPort outputB;  // Second output for second read pointer
  public UnitInputPort delayPointer1;  // First pointer delay
  public UnitInputPort delayPointer2;  // Second pointer delay
  int currentDelay;
  
  
  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/
  /**
   * Class constructor
   * @param size int the size of the circular buffer
   * @param delayLength int the distance from the read to the write pointer initially
   * @param delayOfSecondReadPointer int the distance from an optional second read pointer to the write pointer initially
   */
  public CircularBuffer(int bufferSize, int delayLength, int delayOfSecondReadPointer)
  {
    super();
    
    // Check parameters 
    if(bufferSize == 0)
    {
      throw new IllegalArgumentException("Cannot create a buffer of size 0!");
    }
    if(delayLength > bufferSize || delayOfSecondReadPointer > bufferSize)
    {
      throw new IllegalArgumentException("Delay cannot be larger than the buffer size!");
    }
    
    this.bufferSize = bufferSize;
    circularBuffer = new double[bufferSize];
    writePointer = 0;
    readPointer = (bufferSize - delayLength) % bufferSize;
    secondReadPointer = (bufferSize - delayOfSecondReadPointer) % bufferSize;
    
    // Add ports
    addPort(outputB = new UnitOutputPort("OutputB"));
    addPort(delayPointer1 = new UnitInputPort("Pointer1", delayLength));
    addPort(delayPointer2 = new UnitInputPort("Pointer2", delayOfSecondReadPointer));
  }
  
  
  /**************************************************************************************************/
  //
  /* Allocate 
  //
  /**************************************************************************************************/
  /**
   * Sets a new empty buffer
   * @param delayLength int the distance from the read to the write pointer
   * @param delayOfSecondReadPointer int the distance from an optional second read pointer to the write pointer
   */
  
  public void allocate(int delayLength, int delayOfSecondReadPointer)
  { 
    // Re initialise the array, so that values are set to 0;
    circularBuffer = new double[bufferSize];
    
    // Set the pointers to the new position
    writePointer = 0;
    readPointer = (bufferSize - delayLength) % bufferSize;
    secondReadPointer = (bufferSize - delayOfSecondReadPointer) % bufferSize;
  }
  
  
  /**************************************************************************************************/
  //
  /* Generate  
  //
  /**************************************************************************************************/
  /**
   * Process one sample through the delay line
   * @param int start
   * @param int limit
   */

  public void generate(int start, int limit)
  {
    // Get inputs from ports
    double[] inputs = input.getValues();
    double[] outputAs = output.getValues();
    double[] outputBs = outputB.getValues();

    for(int i=start; i<limit; i++)
    {
      // Read from the buffer
      double[] samples = read();
      
      // Write input
      write(inputs[i]);
      
      // Output values to each port
      outputAs[i] = samples[0];  // Pointer 1
      outputBs[i] = samples[1];  // Pointer 2
    }
  }
  
  
  /**************************************************************************************************/
  //
  /* Read  
  //
  /**************************************************************************************************/
  /**
   * Read the next value from the circular buffer, returning the value and moving the pointer on
   * @return double[] the values of the first and second read pointer from the buffer
   */

   private double[] read()
   {     
     // FIRST READ POINTER
     
     // Get the delay pointer 1 length from input and make sure this is correctly set
     readPointer = (writePointer - (int)delayPointer1.getValue()) % bufferSize; 
     // Make sure non-negative
     if(readPointer < 0)
     {
       readPointer += bufferSize;
     }
     
     // Get value at current pointer position
     double pointer1 = circularBuffer[readPointer]; 
     
     // Advance the read pointer
     readPointer = (readPointer + 1) % bufferSize;
     
     // SECOND READ POINTER
     
     // Get the delay pointer 1 length from input and make sure this is correctly set
     secondReadPointer = (writePointer - (int)delayPointer2.getValue()) % bufferSize;     
     // Make sure non-negative
     if(secondReadPointer < 0)
     {
       secondReadPointer += bufferSize;
     }
     
     // Get value at current pointer position
     double pointer2 = circularBuffer[secondReadPointer];
     
     // Advance the second read pointer
     secondReadPointer = (secondReadPointer + 1) % bufferSize;
     
     return new double[]{pointer1, pointer2};
   }
   
  /**************************************************************************************************/
  //
  /* Write  
  //
  /**************************************************************************************************/
  /**
   * Write the next value to the circular buffer, the moving on the write pointer
   * @param value double the value to write to the buffer
   */
   
   private void write(double value)
   {
     // Write value at current position
     circularBuffer[writePointer] = value;
     
     // Advance the write pointer
     writePointer = (writePointer + 1) % bufferSize;
   }
   
}
