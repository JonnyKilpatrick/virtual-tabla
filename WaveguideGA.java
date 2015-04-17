import org.uncommons.maths.binary.BitString;
import org.uncommons.watchmaker.framework.*;
import org.uncommons.watchmaker.framework.factories.BitStringFactory;
import org.uncommons.watchmaker.framework.operators.BitStringMutation;
import org.uncommons.watchmaker.framework.operators.BitStringCrossover;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.watchmaker.framework.termination.Stagnation;
import org.uncommons.watchmaker.framework.termination.GenerationCount;
import org.uncommons.maths.random.Probability;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.unitgen.LineOut;

import java.util.List;
import java.util.LinkedList;
import java.util.Random;
import java.io.IOException;
import java.io.PrintWriter;

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
  private static final int TOTAL_VOLUME_LENGTH = 14;


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
    CandidateFactory<BitString> factory = new BitStringFactory((TOTAL_STRING_LENGTH * numWaveguides) + TOTAL_VOLUME_LENGTH);
    
    // Create operators for mutation and evolution
    List<EvolutionaryOperator<BitString>> operators = new LinkedList<EvolutionaryOperator<BitString>>();
    operators.add(new BitStringMutation(new Probability(0.01)));
    operators.add(new BitStringCrossover());
    EvolutionaryOperator<BitString> pipeline = new EvolutionPipeline<BitString>(operators);
    
    // Initialise the fitness evaluator created for this problem
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
    
    engine.addEvolutionObserver(new EvolutionObserver<BitString>()
    {
      public void populationUpdate(PopulationData<? extends BitString> data)
      {
          System.out.printf("Generation %d: %s\n",
                            data.getGenerationNumber(),
                            data.getBestCandidate());
      }
    });
      
    // Run the algorithm
    ((GenerationalEvolutionEngine<BitString>)engine).setSingleThreaded(true);
    BitString result = engine.evolve(70, 1, new TerminationCondition[] {new Stagnation(20, false), new GenerationCount(200)});
    
    WaveguideParameters[] p = ((WaveguideFitnessEvaluator)fitnessEvaluator).convertToParameters(result.toString());
    
    for(int i=0; i<numWaveguides; i++)
    {
      System.out.println(p[i].getCenterFrequency() + ", " +p[i].getAmplitude()+ " ," +p[i].getQ()+ ", " +p[i].getGain());
    }
    
    double gain = ((WaveguideFitnessEvaluator)fitnessEvaluator).convertToOverallGain(result.toString());
    System.out.println("Gain: " + gain);
    
    Synthesizer synth = JSyn.createSynthesizer();
    synth.setRealTime(false);
    synth.start();
    CaptureOutput lineOut = new CaptureOutput(49280);
    synth.add(lineOut);
    BandedWaveguideNote note = new BandedWaveguideNote(synth, lineOut, numWaveguides);
   
    double fundimentalFreq = p[0].getCenterFrequency();

    for(int i=1; i<numWaveguides; i++)
    {
      double freq = p[i].getCenterFrequency(); 
      if(freq < fundimentalFreq)
      {
        fundimentalFreq = freq;
      }
    }
    lineOut.resetData();
    note.playNote(p, fundimentalFreq, gain);
    try {
      synth.sleepFor(49280 / 44100);
    } 
    catch (InterruptedException e) 
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    lineOut.stop();
    
    double[] output = lineOut.getData();
    lineOut.resetData();
    
    PrintWriter writer = new PrintWriter("outputTime.csv", "UTF-8");

    for(double d : output)
    {
      writer.print(d + ", ");
    }
    writer.println();
    
    writer.close();
    
    writer = new PrintWriter("outputSpectrum.csv", "UTF-8");

    Spectrogram spectrogram = new Spectrogram(512, 128);
    double[][] spectrum = spectrogram.spectrogram(output);
    
    for(int i=0; i<spectrum.length; i++)
    {
      for(int j=0; j<spectrum[i].length; j++)
      {
        writer.print(spectrum[i][j] + ", ");
      }
      writer.println();
    }
    writer.println();
    
    writer.close();
    
    
    synth = JSyn.createSynthesizer();
    synth.start();
    LineOut line = new LineOut();
    synth.add(line);
    note = new BandedWaveguideNote(synth, line, numWaveguides);
    
    for(int i=0; i<35; i++)
    {
      note.playNote(p, fundimentalFreq, gain);
      try 
      {
        Thread.sleep(1500);
      } 
      catch (InterruptedException e) 
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    return null;   
  }
  
  
  public static void main(String[] args)
  {
    try 
    {
      WaveguideGA.runGeneticAlgorithm(5, "C:/Users/Jon/Documents/Computer Science/Third Year/Dissertation/Code/VirtualTablaSynthesiser/Samples/High/21_14_01.AIF");
    } 
    catch (IOException e) 
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }  
      System.out.println("Finished...");
  }
  

  
}

