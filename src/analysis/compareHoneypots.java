package analysis;

import algorithm.nodeInMaxRowsGreedyHeuristic;
import com.opencsv.CSVWriter;
import dataTypes.parameters;
import helper.commonMethods;
import network.graph;
import org.apache.commons.math3.distribution.NormalDistribution;
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
import java.util.stream.IntStream;

/**
 * Compares two sets of honeypot solutions on a larger independent sample.
 *
 * @author Sudesh Agrawal.
 */
public class compareHoneypots
{
	/**
	 * A map from a {@code Triplet} of a pair of {@link parameters} and the out-of-sample size to evaluate the two sets
	 * of honeypots on, to the objective function values of the two sets of honeypots evaluated on this test sample.
	 */
	Map<Triplet<parameters, parameters, Integer>, Pair<Double, Double>> mapParamsPairToObjectiveValues;
	
	/**
	 * A map from a {@code Triplet} of a pair of {@link parameters} and the out-of-sample size to evaluate the two sets
	 * of honeypots on, to a pair of alpha value and the half-width of corresponding confidence interval on the
	 * difference in the objective values.
	 *
	 * The first value in the pair is alpha, and the second value is the half-width corresponding to that alpha.
	 */
	Map<Triplet<parameters, parameters, Integer>, Pair<Double, Double>> mapParamsPairToHalfWidthOfDifference;
	
	/**
	 * A map from a {@code Triplet} of a pair of {@link parameters} and the out-of-sample size to evolve the two sets of
	 * honeypots on, to the semi-hamming distance.
	 */
	Map<Triplet<parameters, parameters, Integer>, Integer> mapParamsPairToSemiHammingDistance;
	
	/**
	 * Constructor.
	 */
	public compareHoneypots()
	{
		this.mapParamsPairToObjectiveValues = new HashMap<>();
		this.mapParamsPairToSemiHammingDistance = new HashMap<>();
		this.mapParamsPairToHalfWidthOfDifference = new HashMap<>();
	}
	
	/**
	 * Constructor.
	 *
	 * @param mapParamsPairToObjectiveValues an instance of {@link compareHoneypots#mapParamsPairToObjectiveValues}
	 * @param mapParamsPairToSemiHammingDistance an instance of
	 *  {@link compareHoneypots#mapParamsPairToSemiHammingDistance}.
	 */
	public compareHoneypots(Map<Triplet<parameters, parameters, Integer>,
							Pair<Double, Double>> mapParamsPairToObjectiveValues,
	                        Map<Triplet<parameters, parameters, Integer>, Integer> mapParamsPairToSemiHammingDistance,
	                        Map<Triplet<parameters, parameters, Integer>,
	                        Pair<Double, Double>> mapParamsPairToHalfWidthOfDifference)
	{
		this.mapParamsPairToObjectiveValues = mapParamsPairToObjectiveValues;
		this.mapParamsPairToSemiHammingDistance = mapParamsPairToSemiHammingDistance;
		this.mapParamsPairToHalfWidthOfDifference = mapParamsPairToHalfWidthOfDifference;
	}
	
	/**
	 * Getter.
	 *
	 * @return {@link compareHoneypots#mapParamsPairToObjectiveValues}.
	 */
	public Map<Triplet<parameters, parameters, Integer>,
				Pair<Double, Double>> getMapParamsPairToObjectiveValues()
	{
		return mapParamsPairToObjectiveValues;
	}
	
	/**
	 * Getter.
	 *
	 * @return {@link compareHoneypots#mapParamsPairToSemiHammingDistance}.
	 */
	public Map<Triplet<parameters, parameters, Integer>, Integer> getMapParamsPairToSemiHammingDistance()
	{
		return mapParamsPairToSemiHammingDistance;
	}
	
	/**
	 * Getter.
	 *
	 * @return {@link compareHoneypots#mapParamsPairToHalfWidthOfDifference}.
	 */
	public Map<Triplet<parameters, parameters, Integer>, Pair<Double, Double>> getMapParamsPairToHalfWidthOfDifference()
	{
		return mapParamsPairToHalfWidthOfDifference;
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
		int count = 1;
		for (Map.Entry<Triplet<parameters, parameters, Integer>,
				Pair<Double, Double>> e: mapParamsPairToObjectiveValues.entrySet())
		{
			sb.append("\n\tEntry ").append(count);
			sb.append("\n\t\t<Parameter 1 - \n\t\t\t").append(e.getKey().getValue0().toString()).append(", ");
			sb.append("\n\t\t Parameter 2 - \n\t\t\t").append(e.getKey().getValue1().toString()).append(", ");
			sb.append("\n\t\t Test Sample Size = ").append(e.getKey().getValue2()).append(">, ");
			sb.append("\n\t\t<Objective Value for Parameter 1 = ").append(e.getValue().getValue0()).append(", ");
			sb.append("\n\t\t Objective Value for Parameter 2 = ").append(e.getValue().getValue1()).append(", ");
			sb.append("\n\t\t alpha = ").append(this.mapParamsPairToHalfWidthOfDifference.get(e.getKey()).getValue0())
										.append(", ");
			sb.append("\n\t\t half-width of CI = ").append(this.mapParamsPairToHalfWidthOfDifference.get(e.getKey())
													.getValue1()).append(", ");
			sb.append("\n\t\t Semi-hamming distance = ")
						.append(this.mapParamsPairToSemiHammingDistance.get(e.getKey())).append(">.");
			count++;
		}
		return sb.toString();
	}
	
	/**
	 * Evaluates two sets of honeypots for the pairs of parameters provided on a larger independent sample.
	 * Assumption: False-negative probability in the second parameter of the pair is the correct one i.e., true model.
	 *
	 * @param g network graph
	 * @param compareParams list of pairs of parameters for which the set of honeypots are to be compared
	 * @param outSampleSize number of virus spread sample paths for evaluating the two sets of honeypots
	 * @param alpha significance level of the confidence interval on the difference in the objectives.
	 * @throws Exception thrown if the label of a node in {@code g} is a negative integer.
	 */
	public void evaluateHoneypotsOnFalseNegativeModel(graph g, List<Pair<parameters, parameters>> compareParams,
	                                                  int outSampleSize, double alpha) throws Exception
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
		for (Pair<parameters, parameters> cParam : compareParams)
		{
			
			String spreadModelName1 = cParam.getValue0().getSpreadModelName();
			String spreadModelName2 = cParam.getValue1().getSpreadModelName();
			String networkName1 = cParam.getValue0().getNetworkName();
			String networkName2 = cParam.getValue1().getNetworkName();
			int timeStep1 = cParam.getValue0().getTimeStep();
			int timeStep2 = cParam.getValue1().getTimeStep();
			int numberOfSimulationRepetitions1 = cParam.getValue0().getNumberOfSimulationRepetitions();
			int numberOfSimulationRepetitions2 = cParam.getValue1().getNumberOfSimulationRepetitions();
			double falseNegativeProbability1 = cParam.getValue0().getFalseNegativeProbability();
			double falseNegativeProbability2 = cParam.getValue1().getFalseNegativeProbability();
			double transmissability1 = cParam.getValue0().getTransmissability();
			double transmissability2 = cParam.getValue1().getTransmissability();
			int numberOfHoneypots1 = cParam.getValue0().getNumberOfHoneypots();
			int numberOfHoneypots2 = cParam.getValue1().getNumberOfHoneypots();
			
			if (!(networkName1.equals(g.getNetworkName())))
			{
				System.out.println("Provided graph has a different network name!");
				System.out.println("Skipping parameter pair: "+ cParam);
				continue;
			}
			if ((!spreadModelName1.equals(spreadModelName2)) || (!networkName1.equals(networkName2))
					|| (timeStep1!=timeStep2) || numberOfSimulationRepetitions1!=numberOfSimulationRepetitions2
					|| transmissability1!=transmissability2 || numberOfHoneypots1!=numberOfHoneypots2)
			{
				System.out.println("Some mismatch exists in the parameter pairs!");
				System.out.println("Skipping parameter pair: "+ cParam);
				continue;
			}
			if (falseNegativeProbability2 <=0)
			{
				System.out.println("False negative for second set of params should be positive!");
				System.out.println("Skipping parameter pair: "+ cParam);
				continue;
			}
			
			parameters newParam = new parameters(spreadModelName1, networkName1, timeStep1,
					numberOfSimulationRepetitions1, falseNegativeProbability2, transmissability1,
					numberOfHoneypots1, 0);
			
			// for each pair of parameters generate samples
			Pair<Integer, Integer> t0_run1 = new Pair<>(timeStep1, numberOfSimulationRepetitions1);
			Pair<Integer, Integer> t0_run2 = new Pair<>(timeStep2, numberOfSimulationRepetitions2);
			Pair<Integer, Integer> t0_run = new Pair<>(timeStep1, outSampleSize);
			
			List<Pair<Integer, Integer>> t0_runs1 = new ArrayList<>();
			t0_runs1.add(t0_run1);
			List<Pair<Integer, Integer>> t0_runs2 = new ArrayList<>();
			t0_runs2.add(t0_run2);
			List<Pair<Integer, Integer>> t0_runs = new ArrayList<>();
			t0_runs.add(t0_run);
			
			int hashCode = cParam.hashCode();       // hashcode for parameter pair
			int newHashCode = newParam.hashCode();  // hashcode for parameters of actual samples
			
			simulationRuns simulationRuns1 = new simulationRuns();      // corresponding to first pair
			simulationRuns simulationRuns2 = new simulationRuns();      // corresponding to second pair
			simulationRuns trueSimulationRuns = new simulationRuns();   // samples on which solutions evaluated
			
			switch (spreadModelName1)
			{
				case "TN11C" ->
				{
					int[] TN11CSeed = {2507+hashCode, 2101+hashCode, 3567+hashCode};
					int[] seed = {2507+newHashCode+1, 2101+newHashCode+1, 3567+newHashCode+1};
					simulationRuns1.simulateTN11CRuns(g, t0_runs1, falseNegativeProbability1, TN11CSeed);
					simulationRuns2.simulateTN11CRuns(g, t0_runs2, falseNegativeProbability2, TN11CSeed);
					trueSimulationRuns.simulateTN11CRuns(g, t0_runs, falseNegativeProbability2, seed);
				}
				case "RA1PC" ->
				{
					int[] RA1PCSeed = {2507+hashCode, 2101+hashCode, 2101+hashCode, 3567+hashCode};
					int[] seed = {2507+newHashCode+1, 2101+newHashCode+1, 2101+newHashCode+1, 3567+newHashCode+1};
					simulationRuns1.simulateRA1PCRuns(g, t0_runs1, falseNegativeProbability1,
														transmissability1, RA1PCSeed);
					simulationRuns2.simulateRA1PCRuns(g, t0_runs2, falseNegativeProbability2,
														transmissability2, RA1PCSeed);
					trueSimulationRuns.simulateRA1PCRuns(g, t0_runs, falseNegativeProbability2, transmissability1, seed);
				}
				case "RAEPC" ->
				{
					int[] RAEPCSeed = {2507+hashCode, 2101+hashCode, 3567+hashCode};
					int[] seed = {2507+newHashCode+1, 2101+newHashCode+1, 3567+newHashCode+1};
					simulationRuns1.simulateRAEPCRuns(g, t0_runs1, falseNegativeProbability1,
														transmissability1, RAEPCSeed);
					simulationRuns2.simulateRAEPCRuns(g, t0_runs2, falseNegativeProbability2,
														transmissability2, RAEPCSeed);
					trueSimulationRuns.simulateRAEPCRuns(g, t0_runs, falseNegativeProbability2, transmissability1, seed);
				}
				default ->
				{
					System.out.println("Invalid model name!");
					System.out.println("Skipping parameter pair: "+ cParam);
					continue;
				}
			}
			
			// find honeypot solutions for these samples
			nodeInMaxRowsGreedyHeuristic heuristicResults1 = new nodeInMaxRowsGreedyHeuristic();
			nodeInMaxRowsGreedyHeuristic heuristicResults2 = new nodeInMaxRowsGreedyHeuristic();
			List<parameters> listOfParams1 = new ArrayList<>();
			List<parameters> listOfParams2 = new ArrayList<>();
			listOfParams1.add(cParam.getValue0());
			listOfParams2.add(cParam.getValue1());
			heuristicResults1.runSAAUsingHeuristic(g, simulationRuns1, listOfParams1);
			heuristicResults2.runSAAUsingHeuristic(g, simulationRuns2, listOfParams2);
			
			// evaluate solutions on a larger independent set of samples
			List<Integer> honeypots1 = heuristicResults1.getOutputMap().get(cParam.getValue0()).getHoneypots();
			List<Integer> honeypots2 = heuristicResults2.getOutputMap().get(cParam.getValue1()).getHoneypots();
			
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
			//int n11 = (int) successfulDetectMatrix.stream()
			//						.filter(samplePath -> (candidates1.stream().anyMatch(samplePath::contains)) &&
			//								(candidates2.stream().anyMatch(samplePath::contains))).count();
			int n12 = (int) successfulDetectMatrix.stream()
									.filter(samplePath -> (candidates1.stream().anyMatch(samplePath::contains)) &&
											(candidates2.stream().noneMatch(samplePath::contains))).count();
			int n21 = (int) successfulDetectMatrix.stream()
									.filter(samplePath -> (candidates1.stream().noneMatch(samplePath::contains)) &&
											(candidates2.stream().anyMatch(samplePath::contains))).count();
			//int n22 = (int) successfulDetectMatrix.stream()
			//						.filter(samplePath -> (candidates1.stream().noneMatch(samplePath::contains)) &&
			//								(candidates2.stream().noneMatch(samplePath::contains))).count();
			
			NormalDistribution myNormDist = new NormalDistribution(0, 1);
			double zValue = myNormDist.inverseCumulativeProbability(1-0.5*alpha);
			double commonDenominator = 1.0/outSampleSize;
			//double dHat = (n12-n21)*commonDenominator;
			double dHat = candidateObjective1-candidateObjective2;
			double n12term = commonDenominator*n12;
			double n21term = commonDenominator*n21;
			double sampleVarFirst = n12term*(1.0-n12term);
			double sampleVarSecond = n21term*(1.0-n21term);
			double sampleVarThird = 2.0*n12term*n21term;
			double sampleVar = commonDenominator*(sampleVarFirst+sampleVarSecond+sampleVarThird);
			double sampleStDev = Math.sqrt(sampleVar);
			double halfWidth = zValue * sampleStDev;
			System.out.println("d^hat: "+ dHat);
			System.out.println("Half width: "+halfWidth);
			
			Triplet<parameters, parameters, Integer> key = new Triplet<>(cParam.getValue0(), cParam.getValue1(),
																			outSampleSize);
			mapParamsPairToObjectiveValues.put(key, new Pair<>(candidateObjective1, candidateObjective2));
			mapParamsPairToHalfWidthOfDifference.put(key, new Pair<>(alpha, halfWidth));
			
			// find the semi-hamming distance
			int semiHammingDistance = getSemiHammingDistance(g, honeypots1, honeypots2);
			mapParamsPairToSemiHammingDistance.put(key, semiHammingDistance);
		}
		
	}
	
	/**
	 * This method returns half of the hamming distance between the two sets of honeypots.
	 *
	 * @param g network graph
	 * @param honeypots1 the first set of honeypots
	 * @param honeypots2 the second set of honeypots.
	 * @return half of the hamming distance between the two sets.
	 */
	private int getSemiHammingDistance(graph g, List<Integer> honeypots1, List<Integer> honeypots2)
	{
		int numberOfNodes = g.getVertexSet().size();
		List<Integer> vertices = new ArrayList<>(g.getVertexSet());
		List<Integer> binaryCandidates1 = IntStream.range(0, numberOfNodes)
				.mapToObj(i -> honeypots1.contains(vertices.get(i)) ? 1 : 0)
				.collect(Collectors.toCollection(() -> new ArrayList<>(numberOfNodes)));
		List<Integer> binaryCandidates2 = IntStream.range(0, numberOfNodes)
				.mapToObj(i -> honeypots2.contains(vertices.get(i)) ? 1 : 0)
				.collect(Collectors.toCollection(() -> new ArrayList<>(numberOfNodes)));
		int hammingDistance = (int) IntStream.range(0, numberOfNodes)
				.filter(i -> binaryCandidates1.get(i) != binaryCandidates2.get(i)).count();
		return (int) (0.5*hammingDistance);
	}
	
	/**
	 * Writes results of {@link compareHoneypots#evaluateHoneypotsOnFalseNegativeModel(graph, List, int, double)}
	 * stored in {@link compareHoneypots#mapParamsPairToObjectiveValues}
	 * and {@link compareHoneypots#mapParamsPairToSemiHammingDistance} to csv file.
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
							"difference (FN1-FN2)", "alpha", "lower CI", "upper CI", "semi-hamming dist.", "UTC"};
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
			String[] line = new String[17];
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
			double diff = e.getValue().getValue0()-e.getValue().getValue1();
			line[11] = String.valueOf(diff);
			line[12] = String.valueOf(this.mapParamsPairToHalfWidthOfDifference.get(e.getKey()).getValue0());
			double halfWidth = this.mapParamsPairToHalfWidthOfDifference.get(e.getKey()).getValue1();
			line[13] = String.valueOf(diff-halfWidth);
			line[14] = String.valueOf(diff+halfWidth);
			line[15] = String.valueOf(this.mapParamsPairToSemiHammingDistance.get(e.getKey()));
			line[16] = now;
			writer.writeNext(line);
		}
		writer.flush();
		writer.close();
		System.out.println("Honeypots comparison results successfully written to \""+filename+"\".");
	}
}
