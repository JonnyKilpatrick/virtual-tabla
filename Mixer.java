import com.jsyn.*;
import com.jsyn.data.*;
import com.jsyn.unitgen.*;
import com.jsyn.util.*;
import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.unitgen.UnitGenerator;

/**
 * Mixes multiple mono inputs down to one output by summing the inputs and dividing by the number of inputs
 * This reduces the volume of the inputs so compression could be used later to boost the overall gain
 */
 
public class Mixer extends UnitGenerator
{
  
  /**************************************************************************************************/
  //
  /* Instance variables 
  //
  /**************************************************************************************************/
  
  public UnitInputPort input;
  public UnitOutputPort output;
  
  /**************************************************************************************************/
  //
  /* Constructor 
  //
  /**************************************************************************************************/
  /**
   * Class constructor
   * @param numInputs the number of mono inputs to mix
   */
   
  public Mixer(int numInputs)
  {
    super();
    addPort(input = new UnitInputPort(numInputs, "Input"));
    addPort(output = new UnitOutputPort());
  }
  
  
  /**************************************************************************************************/
  //
  /* Generate  
  //
  /**************************************************************************************************/
  /**
   * Process one set of inputs and produce an output
   * @param start int
   * @param limit int 
   */
   
   @Override
   public void generate(int start, int limit)
   { 
     // Get number of inputs
     int numInputs = input.getNumParts();
     
     double[] outputs = output.getValues();
     
     for(int i=start; i<limit; i++)
     {
       double total = 0;
       for(int n=0; n<numInputs; n++)
       {
         double inputs[] = input.getValues(n);
         total += inputs[i];
       }
       if(total>1)
       {
         outputs[i] = 0.99;
       }
       else if(total<-1)
       {
         outputs[i] = -0.99;
       }
       else
       {
         outputs[i] = total;
       }
     }
   }
  
}
