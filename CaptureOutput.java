import java.util.Arrays;

import com.jsyn.*;
import com.jsyn.data.*;
import com.jsyn.unitgen.*;
import com.jsyn.util.*;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.unitgen.UnitGenerator;

/**
 * Captures the input data to an array
 */
 
public class CaptureOutput extends UnitGenerator
{
  
  /**************************************************************************************************/
  //
  /* Instance variables 
  //
  /**************************************************************************************************/
  
  public UnitInputPort input;      // The input
  private double[] incomingData;   // The incoming sample data
  private boolean isDataCaptured;  // Whether all the data has been captured
  private int count;               // Number of samples captured so far
  
  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/
  /**
   * Class constructor
   * @param outputSize int the number of samples to capture
   */
   
  public CaptureOutput(int outputSize)
  {
    super();
    addPort(input = new UnitInputPort("Input"));
    incomingData = new double[outputSize];
    isDataCaptured = false;
    count = 0;
  }
  
  
  /**************************************************************************************************/
  //
  /* Generate  
  //
  /**************************************************************************************************/
  /**
   * Process one set of input
   * @param start int
   * @param limit int 
   */
   
   @Override
   public void generate(int start, int limit)
   { 
     // Get input values
     double[] inputs = input.getValues();
     
     for(int i=start; i<limit; i++)
     {
       incomingData[count] = inputs[i];
       count++;
     }
   }
   
  
  /**************************************************************************************************/
  //
  /* getData  
  //
  /**************************************************************************************************/
  /**
   * Return the array of samples that have been captured
   * @return double[] array of data captured
   */

  public double[] getData()
  {      
    // Set the isDataCaptured boolean to false as we have now read the latest data
    isDataCaptured = false;
    
    return incomingData; 
  }
  
  /**************************************************************************************************/
  //
  /* resetData  
  //
  /**************************************************************************************************/
  /**
   * Reset the data
   */

  public void resetData()
  { 
    count = 0; 
    incomingData = new double[incomingData.length];
    isDataCaptured = false;
  }
  
}

