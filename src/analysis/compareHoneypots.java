package analysis;

import algorithm.nodeInMaxRowsGreedyHeuristic;
import com.opencsv.CSVWriter;
import dataTypes.parameters;
import helper.commonMethods;
import network.graph;
import org.javatuples.Pair;
import org.javatuples.Sextet;
import org.javatuples.Triplet;
import simulation.simulationRuns;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Compares two sets of honeypot solutions on a larger independent sample.
 *
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: February 11, 2021.
 */
public class compareHoneypots
{
	/**
	 * A map from a {@code Triplet} of a pair of {@link parameters} and the out-of-sample size to evolve the two sets of
	 * honeypots on, to objective values of the two sets of honeypots evaluated on this test sample.
	 */
	Map<Triplet<parameters, parameters, Integer>, Pair<Double, Double>> mapParamsPairToObjectiveValues;
	
	/**
	 * Constructor.
	 */
	public compareHoneypots()
	{
		this(new HashMap<>());
	}
	
	/**
	 * Constructor.
	 *
	 * @param mapParamsPairToObjectiveValues an instance of {@link compareHoneypots#mapParamsPairToObjectiveValues}.
	 */
	public compareHoneypots(Map<Triplet<parameters, parameters, Integer>,
							Pair<Double, Double>> mapParamsPairToObjectiveValues)
	{
		this.mapParamsPairToObjectiveValues = mapParamsPairToObjectiveValues;
	}
	
	/**
	 * Getter.
	 *
	 * @return the instance of {@link compareHoneypots#mapParamsPairToObjectiveValues}.
	 */
	public Map<Triplet<parameters, parameters, Integer>,
				Pair<Double, Double>> getMapParamsPairToObjectiveValues()
	{
		return mapParamsPairToObjectiveValues;
	}
	
	/**
	 * Overrides {@code toString}.
	 *
	 * @return string representation of values of field(s) in the class.
	 */
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder("Compare Honeypots:");
		for (Map.Entry<Triplet<parameters, parameters, Integer>,
				Pair<Double, Double>> e: mapParamsPairToObjectiveValues.entrySet())
		{
			sb.append("\n\t<Parameter 1 - \n\t\t").append(e.getKey().getValue0().toString()).append(", ");
			sb.append("\n\t Parameter 2 - \n\t\t").append(e.getKey().getValue1().toString()).append(", ");
			sb.append("\n\t Test Sample Size = ").append(e.getKey().getValue2()).append(">, ");
			sb.append("\n\t<Objective Value for Parameter 1 = ").append(e.getValue().getValue0()).append(", ");
			sb.append("\n\t<Objective Value for Parameter 2 = ").append(e.getValue().getValue1()).append(">.");
		}
		return sb.toString();
	}
	
	/**
	 * Evaluates two sets of honeypots for the pairs of parameters provided on a larger independent sample.
	 * Assumption: False negative probability in the second parameter of the pair is the correct one.
	 *
	 * @param g network graph
	 * @param compareParams list of pairs of parameters for which the set of honeypots are to be compared
	 * @param outSampleSize number of virus spread sample paths for evaluating the two sets of honeypots.
	 * @throws Exception thrown if the label of a node in {@code g} is a negative integer.
	 */
	public void evaluateHoneypotsOnFalseNegativeModel(graph g, List<Pair<parameters, parameters>> compareParams,
	                                                  int outSampleSize) throws Exception
	{
		// minimum label of vertex
		boolean zeroNode = false;
		int minNode = g.findMinimumNodeLabel();
		if (minNode==0)
			zeroNode = true;
		else
		{
			if (minNode<0)
				throw new Exception("Node labels are negative integers!");
		}
		for (Pair<parameters, parameters> cparam : compareParams)
		{
			
			String spreadModelName1 = cparam.getValue0().getSpreadModelName();
			String spreadModelName2 = cparam.getValue1().getSpreadModelName();
			
			String networkName1 = cparam.getValue0().getNetworkName();
			String networkName2 = cparam.getValue1().getNetworkName();
			int timeStep1 = cparam.getValue0().getTimeStep();
			int timeStep2 = cparam.getValue1().getTimeStep();
			int numberOfSimulationRepetitions1 = cparam.getValue0().getNumberOfSimulationRepetitions();
			int numberOfSimulationRepetitions2 = cparam.getValue1().getNumberOfSimulationRepetitions();
			double falseNegativeProbability1 = cparam.getValue0().getFalseNegativeProbability();
			double falseNegativeProbability2 = cparam.getValue1().getFalseNegativeProbability();
			double transmissability1 = cparam.getValue0().getTransmissability();
			double transmissability2 = cparam.getValue1().getTransmissability();
			int numberOfHoneypots1 = cparam.getValue0().getNumberOfHoneypots();
			int numberOfHoneypots2 = cparam.getValue1().getNumberOfHoneypots();
			
			if (!(networkName1.equals(g.getNetworkName())))
			{
				System.out.println("Provided graph has a different network name!");
				System.out.println("Skipping parameter pair: "+cparam.toString());
				continue;
			}
			if ((!spreadModelName1.equals(spreadModelName2)) || (!networkName1.equals(networkName2))
					|| (timeStep1!=timeStep2) || numberOfSimulationRepetitions1!=numberOfSimulationRepetitions2
					|| transmissability1!=transmissability2 || numberOfHoneypots1!=numberOfHoneypots2)
			{
				System.out.println("Some mismatch exists in the parameter pairs!");
				System.out.println("Skipping parameter pair: "+cparam.toString());
				continue;
			}
			if (falseNegativeProbability2 <=0)
			{
				System.out.println("False negative for second set of params should be positive!");
				System.out.println("Skipping parameter pair: "+cparam.toString());
				continue;
			}
			
			parameters newParam = new parameters(spreadModelName1, networkName1, timeStep1,
					numberOfSimulationRepetitions1, falseNegativeProbability2, transmissability1,
					numberOfHoneypots1, 0);
			
			// for each tuple of parameters generate samples
			Pair<Integer, Integer> t0_run1 = new Pair<>(timeStep1, numberOfSimulationRepetitions1);
			Pair<Integer, Integer> t0_run2 = new Pair<>(timeStep2, numberOfSimulationRepetitions2);
			Pair<Integer, Integer> t0_run = new Pair<>(timeStep1, outSampleSize);
			
			List<Pair<Integer, Integer>> t0_runs1 = new ArrayList<>();
			t0_runs1.add(t0_run1);
			List<Pair<Integer, Integer>> t0_runs2 = new ArrayList<>();
			t0_runs2.add(t0_run2);
			List<Pair<Integer, Integer>> t0_runs = new ArrayList<>();
			t0_runs.add(t0_run);
			
			int hashCode = cparam.hashCode();       // hashcode for parameter pair
			int newHashCode = newParam.hashCode();  // hashcode for parameters of actual samples
			
			simulationRuns simulationRuns1 = new simulationRuns();      // corresponding to first pair
			simulationRuns simulationRuns2 = new simulationRuns();      // corresponding to second pair
			simulationRuns trueSimulationRuns = new simulationRuns();   // samples on which solutions evaluated
			
			switch (spreadModelName1)
			{
				case "TN11C" ->
				{
					int[] TN11Cseed = {2507+hashCode, 2101+hashCode, 3567+hashCode};
					int[] seed = {2507+newHashCode+1, 2101+newHashCode+1, 3567+newHashCode+1};
					simulationRuns1.simulateTN11CRuns(g, t0_runs1, falseNegativeProbability1, TN11Cseed);
					simulationRuns2.simulateTN11CRuns(g, t0_runs2, falseNegativeProbability2, TN11Cseed);
					trueSimulationRuns.simulateTN11CRuns(g, t0_runs, falseNegativeProbability2, seed);
				}
				case "RA1PC" ->
				{
					int[] RA1PCseed = {2507+hashCode, 2101+hashCode, 2101+hashCode, 3567+hashCode};
					int[] seed = {2507+newHashCode+1, 2101+newHashCode+1, 2101+newHashCode+1, 3567+newHashCode+1};
					simulationRuns1.simulateRA1PCRuns(g, t0_runs1, falseNegativeProbability1,
														transmissability1, RA1PCseed);
					simulationRuns2.simulateRA1PCRuns(g, t0_runs2, falseNegativeProbability2,
														transmissability2, RA1PCseed);
					trueSimulationRuns.simulateRA1PCRuns(g, t0_runs, falseNegativeProbability2, transmissability1, seed);
				}
				case "RAEPC" ->
				{
					int[] RAEPCseed = {2507+hashCode, 2101+hashCode, 3567+hashCode};
					int[] seed = {2507+newHashCode+1, 2101+newHashCode+1, 3567+newHashCode+1};
					simulationRuns1.simulateRAEPCRuns(g, t0_runs1, falseNegativeProbability1,
														transmissability1, RAEPCseed);
					simulationRuns2.simulateRAEPCRuns(g, t0_runs2, falseNegativeProbability2,
														transmissability2, RAEPCseed);
					trueSimulationRuns.simulateRAEPCRuns(g, t0_runs, falseNegativeProbability2, transmissability1, seed);
				}
				default ->
				{
					System.out.println("Invalid model name!");
					System.out.println("Skipping parameter pair: "+cparam.toString());
					continue;
				}
			}
			
			// find honeypot solutions for these samples
			nodeInMaxRowsGreedyHeuristic heuristicResults1 = new nodeInMaxRowsGreedyHeuristic();
			nodeInMaxRowsGreedyHeuristic heuristicResults2 = new nodeInMaxRowsGreedyHeuristic();
			List<parameters> listOfParams1 = new ArrayList<>();
			List<parameters> listOfParams2 = new ArrayList<>();
			listOfParams1.add(cparam.getValue0());
			listOfParams2.add(cparam.getValue1());
			heuristicResults1.runSAAUsingHeuristic(g, simulationRuns1, listOfParams1);
			heuristicResults2.runSAAUsingHeuristic(g, simulationRuns2, listOfParams2);
			
			// evaluate solutions on a larger independent set of samples
			List<Integer> honeypots1 = heuristicResults1.getOutputMap().get(cparam.getValue0()).getHoneypots();
			List<Integer> honeypots2 = heuristicResults2.getOutputMap().get(cparam.getValue1()).getHoneypots();
			
			Sextet<String, String, Integer, Integer, Double, Double> actualSamplesKey =
					new Sextet<>(spreadModelName1, networkName1, timeStep1,
									outSampleSize, falseNegativeProbability2, transmissability1);
			List<List<Integer>> virusSpreadSamples = trueSimulationRuns
										.getMapModelNetworkT0RunsFalseNegativeToSimulationRuns().get(actualSamplesKey);
			List<List<Integer>> virtualDetectionSamples = trueSimulationRuns
									.getMapModelNetworkT0RunsFalseNegativeToVirtualDetections().get(actualSamplesKey);
			List<List<Integer>> successfulDetectMatrix;
			Set<Integer> candidates1, candidates2;
			
			if (zeroNode)
			{
				List<List<Integer>> newVirusSpreadSamples = virusSpreadSamples.stream()
												.map(virusSpreadSample -> virusSpreadSample.stream()
												.map(integer -> integer + 1)
												.collect(Collectors.toCollection(ArrayList::new)))
												.collect(Collectors.toCollection(() -> new ArrayList<>(outSampleSize)));
				successfulDetectMatrix = commonMethods.elementwiseMultiplyMatrix(
														Collections.unmodifiableList(newVirusSpreadSamples),
														Collections.unmodifiableList(virtualDetectionSamples));
				candidates1 = honeypots1.stream().map(e -> e+1).collect(Collectors.toSet());
				candidates2 = honeypots2.stream().map(e -> e+1).collect(Collectors.toSet());
			}
			else
			{
				successfulDetectMatrix = commonMethods.elementwiseMultiplyMatrix(
														Collections.unmodifiableList(virusSpreadSamples),
														Collections.unmodifiableList(virtualDetectionSamples));
				candidates1 = new HashSet<>(honeypots1);
				candidates2 = new HashSet<>(honeypots2);
			}
			
			int frequency1 = (int) successfulDetectMatrix.stream()
									.filter(samplePath -> candidates1.stream().anyMatch(samplePath::contains)).count();
			int frequency2 = (int) successfulDetectMatrix.stream()
									.filter(samplePath -> candidates2.stream().anyMatch(samplePath::contains)).count();
			double candidateObjective1 = frequency1*1.0/outSampleSize;
			double candidateObjective2 = frequency2*1.0/outSampleSize;
			
			mapParamsPairToObjectiveValues.put(new Triplet<>(cparam.getValue0(), cparam.getValue1(),
							outSampleSize), new Pair<>(candidateObjective1, candidateObjective2));
		}
		
	}
	
	/**
	 * Writes results of {@link compareHoneypots#evaluateHoneypotsOnFalseNegativeModel(graph, List, int)}
	 * stored in {@link compareHoneypots#mapParamsPairToObjectiveValues} to csv file.
	 *
	 * @param filename path to output file
	 * @param append {@code true}, if you wish to append to existing file; {@code false}, otherwise.
	 * @throws IOException thrown if error in input-output operation.
	 */
	public void writeToCSV(String filename, boolean append) throws IOException
	{
		File fileObj = new File(filename);
		String[] header = {"Model", "Network", "t_0", "Simulation repetitions", "transmissability (p)",
							"no. of honeypots", "FN probability 1", "FN probability 2",  "test sample size",
							"objective for model with FN 1", "objective for model with FN 2",
							"difference (FN1-FN2)", "UTC"};
		boolean writeHeader = false;
		if (!fileObj.exists())
			writeHeader = true;
		else if (!append)
			writeHeader = true;
		CSVWriter writer = new CSVWriter(new FileWriter(filename, append));
		if (writeHeader)
		{
			writer.writeNext(header);
			writer.flush();
		}
		String now = Instant.now().toString();
		for (Map.Entry<Triplet<parameters, parameters, Integer>,
				Pair<Double, Double>> e: mapParamsPairToObjectiveValues.entrySet())
		{
			String[] line = new String[13];
			line[0] = e.getKey().getValue0().getSpreadModelName();
			line[1] = e.getKey().getValue0().getNetworkName();
			line[2] = String.valueOf(e.getKey().getValue0().getTimeStep());
			line[3] = String.valueOf(e.getKey().getValue0().getNumberOfSimulationRepetitions());
			line[4] = String.valueOf(e.getKey().getValue0().getTransmissability());
			line[5] = String.valueOf(e.getKey().getValue0().getNumberOfHoneypots());
			line[6] = String.valueOf(e.getKey().getValue0().getFalseNegativeProbability());
			line[7] = String.valueOf(e.getKey().getValue1().getFalseNegativeProbability());
			line[8] = String.valueOf(e.getKey().getValue2());
			line[9] = String.valueOf(e.getValue().getValue0());
			line[10] = String.valueOf(e.getValue().getValue1());
			line[11] = String.valueOf(e.getValue().getValue0()-e.getValue().getValue1());
			line[12] = now;
			writer.writeNext(line);
		}
		writer.flush();
		writer.close();
		System.out.println("Honeypots comparison results successfully written to \""+filename+"\".");
	}
}