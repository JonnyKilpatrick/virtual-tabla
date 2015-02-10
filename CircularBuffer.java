/**
 * Implementation for a Circular buffer.
 * Allows you to populate a circular buffer of a chosen type, then read / write from it, 
 * whilst internally keeping tracking of the read and write pointers. 
 * Specifically for the use in synthesis with the Karplus-Strong algorithm, so must specify desired delay length 
 */
 
public class CircularBuffer<T>
{
  
  /**************************************************************************************************/
  //
  /* Instance variables 
  //
  /**************************************************************************************************/

  private T[] circularBuffer;  // The actual array for the buffer of type T
  private int readPointer;     // Read pointer
  private int writePointer;    // Write pointer
  private int bufferSize;      // Size of the buffer
  
  
  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/
  /**
   * Class constructor
   * @param size int the size of the circular buffer
   * @param delayLength int the distance from the read to the write pointer
   * @param initialData T[] the array of initial data to populate the circular buffer,
   *   does not have to fill the entire circular buffer
   */
  public CircularBuffer(int bufferSize, int delayLength, T[] initialData)
  {
    this.bufferSize = bufferSize;
    circularBuffer = (T[]) new Object[bufferSize];
    readPointer = 0;
    writePointer = delayLength % bufferSize;
    
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
   */
   
   public T read()
   {
     // Get value at current pointer position
     T value = circularBuffer[readPointer];
     
     // Advance the read pointer
     readPointer = (readPointer + 1) % bufferSize;
     
     return value;
   }
   
  /**************************************************************************************************/
  //
  /* Write  
  //
  /**************************************************************************************************/
  /**
   * Write the next value to the circular buffer, the moving on the write pointer
   */
   
   public void write(T value)
   {
     // Write value at current position
     circularBuffer[writePointer] = value;
     
     // Advance the write pointer
     writePointer = (writePointer + 1) % bufferSize;
   }
   
   public void print()
   {
     String print = "";
     for(T value: circularBuffer)
     {
       print += value + ", ";
     }
     System.out.println(print);
   }


   
   
   public static void main(String[] args)
   {
     CircularBuffer<Integer> buffer = new CircularBuffer<Integer>(5, 5, new Integer[]{4, 2, 6, 4, 7});
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
