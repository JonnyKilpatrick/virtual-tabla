import org.uncommons.maths.binary.BitString;
import org.uncommons.watchmaker.framework.*;
import org.uncommons.watchmaker.framework.factories.BitStringFactory;
import org.uncommons.watchmaker.framework.operators.BitStringMutation;
import org.uncommons.watchmaker.framework.operators.BitStringCrossover;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.watchmaker.framework.termination.TargetFitness;
import org.uncommons.maths.random.Probability;
import java.util.List;
import java.util.LinkedList;
import java.util.Random;
import java.io.IOException;

/**
 * Runs the Watchmaker Genetic Algorithm to find the best parameters for the banded waveguide
 */
 
public class WaveguideGA
{
  /**************************************************************************************************/
  //
  /* Constants 
  //
  /**************************************************************************************************/
  
  // Bit Strings
  private static final int TOTAL_STRING_LENGTH = 72;


  /**************************************************************************************************/
  //
  /* runGeneticAlgorithm  
  //
  /**************************************************************************************************/
  /**
   * Simply states that the fitness function returns lower scores for 'fitter' solution
   * @param numWaveguides int the number of waveguides used in the banded waveguide
   * @param targetAudioFilePath the target audio file path that the algorithm with try to fit the parameters to
   * @return WaveguideParameters[] the optimal parameters found
   */
   
  public static WaveguideParameters[] runGeneticAlgorithm(int numWaveguides, String targetAudioFilePath) throws IOException
  {
    // Initialise candidate factory - the initial population
    CandidateFactory<BitString> factory = new BitStringFactory(TOTAL_STRING_LENGTH * numWaveguides);
    
    // Create operators for mutation and evolution
    List<EvolutionaryOperator<BitString>> operators = new LinkedList<EvolutionaryOperator<BitString>>();
    operators.add(new BitStringMutation(new Probability(0.02)));
    operators.add(new BitStringCrossover());
    EvolutionaryOperator<BitString> pipeline = new EvolutionPipeline<BitString>(operators);
    
    // Initialise the fitness evauluator created for this problem
    FitnessEvaluator<BitString> fitnessEvaluator = new WaveguideFitnessEvaluator(numWaveguides, targetAudioFilePath);
    
    // Set up selection strategy for selecting the fittest candidates given the fitness scores
    SelectionStrategy<Object> selection = new RouletteWheelSelection();
    
    // Choose the watchmaker random number generator
    Random random = new MersenneTwisterRNG();
    
    // Set up the EvolutionEngine to run the algorithm
    EvolutionEngine<BitString> engine = new GenerationalEvolutionEngine<BitString>(
      factory, 
      pipeline, 
      fitnessEvaluator,
      selection,
      random);
      
    // Run the algorithm
    BitString result = engine.evolve(100, 5, new TargetFitness(2.0, false));
    
    System.out.println(result);
    
    return null;   
  }
  

  
}
