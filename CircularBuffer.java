/**
 * Implementation for a Circular buffer.
 * Allows you to populate a circular buffer of doubles, then read / write from it, 
 * whilst internally keeping tracking of the read and write pointers. 
 * Also allows a second read pointer
 * Specifically for the use in synthesis with the Karplus-Strong algorithm, so must specify desired delay length 
 */
 
public class CircularBuffer
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
   * @param initialData double[] the array of initial data to populate the circular buffer,
   *   does not have to fill the entire circular buffer
   */
  public CircularBuffer(int bufferSize, int delayLength, int delayOfSecondReadPointer, double[] initialData)
  {
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
    readPointer = 0;
    writePointer = delayLength % bufferSize;
    secondReadPointer = ((writePointer - delayOfSecondReadPointer) % bufferSize);
   
    // Make sure pointer is not negative 
    if(secondReadPointer < 0)
    {
      secondReadPointer += bufferSize;
    }
    
    // Populate circular buffer with initial data
    for(int i=0; i<initialData.length; i++)
    {
      circularBuffer[i] = initialData[i];
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
   
   public double[] read()
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
   
   public void write(double value)
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
     secondReadPointer = Math.abs((writePointer - delay) % bufferSize);
     
     // Make sure non-negative
     if(secondReadPointer < 0)
     {
       secondReadPointer += bufferSize;
     }
   }
   
   
   
   
   public void print()
   {
     String print = "";
     for(double value: circularBuffer)
     {
       print += value + ", ";
     }
     System.out.println(print);
   }
   
   public static void main(String[] args)
   {
     CircularBuffer buffer = new CircularBuffer(5, 5, 5, new double[]{4f, 2f, 6f, 4f, 7f});
     System.out.println("Read: " + buffer.read());
     System.out.println("Write: 1");
     buffer.write(1);
     
     System.out.println("Read: " + buffer.read());
     System.out.println("Write: 1");
     buffer.write(1);
     
     System.out.println("Read: " + buffer.read());
     System.out.println("Write: 1");
     buffer.write(1);
     
     System.out.println("Read: " + buffer.read());
     System.out.println("Write: 1");
     buffer.write(1);
     
     System.out.println("Read: " + buffer.read());
     System.out.println("Write: 1");
     buffer.write(1);
     buffer.print();
     
     
     System.out.println("Read: " + buffer.read());
     buffer.write(8);
     System.out.println("Read: " + buffer.read());
     buffer.write(7);
     System.out.println("Read: " + buffer.read());
     buffer.write(6);
     System.out.println("Read: " + buffer.read());
     buffer.write(5);
     System.out.println("Read: " + buffer.read());
     buffer.write(4);
     
     buffer.print();
     
     
   }
  
}
