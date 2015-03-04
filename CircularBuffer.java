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
  //public UnitInputPort input;
 // public UnitOutputPort outputA;
  public UnitOutputPort outputB;
  
  
  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/
  /**
   * Class constructor
   * @param size int the size of the circular buffer
   * @param delayLength int the distance from the read to the write pointer
   * @param delayOfSecondReadPointer int the distance from an optional second read pointer to the write pointer
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
    //addPort(input = new UnitInputPort("Input"));
    //addPort(outputA = new UnitOutputPort("OutputA"));
    addPort(outputB = new UnitOutputPort("OutputB"));
  }
  
  public void allocate(int bufferSize, int delayLength, int delayOfSecondReadPointer)
  {
    this.bufferSize = bufferSize;
    circularBuffer = new double[bufferSize];
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
      // Write input
      write(inputs[i]);
      
      // Read from the buffer
      double[] samples = read();
      
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
     // Get value at current pointer position
     double pointer1 = circularBuffer[readPointer]; 
     
     // Advance the read pointer
     readPointer = (readPointer + 1) % bufferSize;
     
     // SECOND READ POINTER
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
   
   
  /**************************************************************************************************/
  //
  /* setPointer1Delay  
  //
  /**************************************************************************************************/
  /**
   * Update the position of pointer1
   * @param delay int the number of samples deleay (distance from pointer to the write pointer)
   */
   
   public void setPointer1Delay(int delay)
   { 
     readPointer = (writePointer - delay) % bufferSize;
     
     // Make sure non-negative
     if(readPointer < 0)
     {
       readPointer += bufferSize;
     }
   }
   
   
  /**************************************************************************************************/
  //
  /* setPointer2Delay  
  //
  /**************************************************************************************************/
  /**
   * Update the position of pointer2
   * @param delay int the number of samples deleay (distance from pointer to the write pointer)
   */
   
   public void setPointer2Delay(int delay)
   { 
     secondReadPointer = (writePointer - delay) % bufferSize;
     
     // Make sure non-negative
     if(secondReadPointer < 0)
     {
       secondReadPointer += bufferSize;
     }
   }
  
}
