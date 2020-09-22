package analysis;

import algorithm.nodeInMaxRowsGreedyHeuristic;
import com.opencsv.CSVWriter;
import dataTypes.algorithmOutput;
import dataTypes.parameters;
import dataTypes.statisticalOutput;
import helper.commonMethods;
import network.graph;
import org.apache.commons.math3.distribution.TDistribution;
import org.javatuples.Pair;
import org.javatuples.Sextet;
import simulation.simulationRuns;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents results of the multiple replications procedure (<b>MRP</b>).
 * <p>
 *     Reference:
 * </p>
 * <p>
 *     [1] Bayraksan, G&uuml;zin, and David P. Morton.
 *     "Assessing solution quality in stochastic programs." <i>Mathematical Programming</i>
 *     108.2&ndash;3 (2006): 495&ndash;514.
 * </p>
 *
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: September 22, 2020.
 */
public class multipleReplicationsProcedure
{
	/**
	 * A map from {@code parameters} to {@code statisticalOutput}.
	 * Basically, stores the outputs for different input parameters.
	 * <p>
	 *     Parameters: (model (TN11C, RAEPC, etc.), network name, time step, repetitions,
	 *     false negative probability, transmissability (p), number of honeypots).
	 * </p>
	 * <p>
	 *     Statistical output: mean, standard deviation, CI width, etc.
	 * </p>
	 */
	Map<parameters, statisticalOutput> outputMap;
	
	/**
	 * Constructor.
	 */
	public multipleReplicationsProcedure()
	{
		this(new HashMap<>());
	}
	
	/**
	 * Constructor.
	 *
	 * @param outputMap an instance of {@code outputMap}.
	 */
	public multipleReplicationsProcedure(Map<parameters, statisticalOutput> outputMap)
	{
		this.outputMap = outputMap;
	}
	
	/**
	 * Getter.
	 *
	 * @return the instance of {@code outputMap}.
	 */
	public Map<parameters, statisticalOutput> getOutputMap()
	{
		return outputMap;
	}
	
	/**
	 * Overrides {@code toString}.
	 *
	 * @return string representation of values of field(s) in the class.
	 */
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder("Multiple Replications Procedure:");
		for(Map.Entry<parameters, statisticalOutput> e: outputMap.entrySet())
		{
			sb.append("\n\t<").append(e.getKey().toString()).append(", ");
			sb.append("\n\t\t mean =\n\t\t\t").append(e.getValue().getMean());
			sb.append("\n\t\t std. dev = \n\t\t\t").append(e.getValue().getStDev());
			sb.append("\n\t\t CI width (alpha = ").append(e.getValue().getAlpha());
			sb.append(") = ").append(e.getValue().getCIWidth());
			sb.append("\n\t>");
		}
		return sb.toString();
	}
	
	/**
	 * Writes MRP results to csv file.
	 *
	 * @param filename path to output file
	 * @param append true, if you wish to append to existing file; false, otherwise.
	 * @throws IOException thrown if error in input-output operation.
	 */
	public void writeToCSV(String filename, boolean append) throws IOException
	{
		File fileObj = new File(filename);
		String[] header = {"Model", "Network", "t_0", "Simulation repetitions", "FN probability",
				"transmissability (p)", "no. of honeypots", "mean", "std. dev.", "alpha", "CI width", "UTC"};
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
		for (Map.Entry<parameters, statisticalOutput> e: outputMap.entrySet())
		{
			String[] line = new String[12];
			line[0] = e.getKey().getSpreadModelName();
			line[1] = e.getKey().getNetworkName();
			line[2] = String.valueOf(e.getKey().getTimeStep());
			line[3] = String.valueOf(e.getKey().getNumberOfSimulationRepetitions());
			line[4] = String.valueOf(e.getKey().getFalseNegativeProbability());
			line[5] = String.valueOf(e.getKey().getTransmissability());
			line[6] = String.valueOf(e.getKey().getNumberOfHoneypots());
			line[7] = String.valueOf(e.getValue().getMean());
			line[8] = String.valueOf(e.getValue().getStDev());
			line[9] = String.valueOf(e.getValue().getAlpha());
			line[10] = String.valueOf(e.getValue().getCIWidth());
			line[11] = now;
			writer.writeNext(line);
		}
		writer.flush();
		writer.close();
		System.out.println("MRP results successfully written to \""+filename+"\".");
	}
	
	/**
	 * Estimates gap in objective value using MRP.
	 * Gap = optimal objective - objective value of a candidate solution.
	 *
	 * @param g network graph
	 * @param algorithmOutputs list of honeypots as a candidate solution
	 * @param alpha alpha value for confidence interval
	 * @param sampleSize sample size
	 * @param replicationSize replication size
	 * @param algorithm possible values: {"greedy"}.
	 * @throws Exception throw if 0<{@code alpha}<1 does not hold,
	 *  or if the label of a node in {@code g} is a negative integer,
	 *  or if in one of the parameters of {@code algorithmOutputs}, {@code p}<=0,
	 *  or if {@code algorithm} has a value for which gap estimation calculation has not been implemented.
	 */
	public void estimateGap(graph g, Map<parameters, algorithmOutput> algorithmOutputs,
	                        double alpha, int sampleSize, int replicationSize, String algorithm) throws Exception
	{
		if ((alpha<=0) || (alpha>=1))
			throw new Exception("Invalid value of alpha!");
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
		TDistribution mytdist = new TDistribution(replicationSize-1);
		double tValue = mytdist.inverseCumulativeProbability(1-alpha);
		
		for (Map.Entry<parameters, algorithmOutput> candidate: algorithmOutputs.entrySet())
		{
			int hashCode = candidate.getKey().hashCode();
			String modelName = candidate.getKey().getSpreadModelName();
			String networkName = candidate.getKey().getNetworkName();
			int t_0 = candidate.getKey().getTimeStep();
			double r = candidate.getKey().getFalseNegativeProbability();
			double p = candidate.getKey().getTransmissability();
			int k = candidate.getKey().getNumberOfHoneypots();
			List<Double> gaps = new ArrayList<>(replicationSize);
			for (int i=1; i<=replicationSize; i++)
			{
				int newHashCode = hashCode+i;
				// sample iid observations of size sampleSize
				simulationRuns observations = new simulationRuns();
				List<Pair<Integer, Integer>> t0_runs = new ArrayList<>();
				t0_runs.add(new Pair<>(t_0, sampleSize));
				if (modelName.equals("TN11C"))
				{
					int[]  seed = {2507+newHashCode, 2101+newHashCode, 3567+newHashCode};
					observations.simulateTN11CRuns(g, t0_runs, r, seed);
				}
				else
				{
					if (modelName.equals("RA1PC"))
					{
						int[] seed = {2507+newHashCode, 2101+newHashCode, 2101+newHashCode, 3567+newHashCode};
						observations.simulateRA1PCRuns(g, t0_runs, r, p, seed);
					}
					else if (modelName.equals("RAEPC"))
					{
						int[]  seed = {2507+newHashCode, 2101+newHashCode, 3567+newHashCode};
						observations.simulateRAEPCRuns(g, t0_runs, r, p, seed);
					}
				}
				
				// TODO: Implement gap estimates for other algorithms.
				// solve using algorithm
				List<parameters> listOfParams = new ArrayList<>();
				parameters newKey = new parameters(modelName, networkName, t_0, sampleSize, r, p, k, 0);
				listOfParams.add(newKey);
				nodeInMaxRowsGreedyHeuristic algoResults;
				if (algorithm.equals("greedy"))
				{
					algoResults = new nodeInMaxRowsGreedyHeuristic();
					algoResults.runSAAUsingHeuristic(g, observations, listOfParams);
				}
				else
				{
					throw new Exception("Gap estimate calculation for other algorithms have not been implemented!");
				}
				
				// evaluate honeypots on these observations
				Sextet<String, String, Integer, Integer, Double, Double> key =
						new Sextet<>(modelName, networkName, t_0, sampleSize, r, p);
				List<List<Integer>> virusSpreadSamples =
						observations.getMapModelNetworkT0RunsFalseNegativeToSimulationRuns().get(key);
				List<List<Integer>> virtualDetectionSamples =
						observations.getMapModelNetworkT0RunsFalseNegativeToVirtualDetections().get(key);
				List<List<Integer>> successfulDetectMatrix;
				List<Integer> honeypots = candidate.getValue().getHoneypots();
				Set<Integer> candidates;
				if (r>0)
				{
					if (zeroNode)
					{
						List<List<Integer>> newVirusSpreadSamples = virusSpreadSamples.stream()
								.map(virusSpreadSample -> virusSpreadSample.stream()
										.map(integer -> integer + 1)
										.collect(Collectors.toCollection(() -> new ArrayList<>(t_0 + 1))))
								.collect(Collectors.toCollection(() -> new ArrayList<>(sampleSize)));
						successfulDetectMatrix = commonMethods.elementwiseMultiplyMatrix(
								Collections.unmodifiableList(newVirusSpreadSamples),
								Collections.unmodifiableList(virtualDetectionSamples));
						candidates = honeypots.stream().map(e -> e+1).collect(Collectors.toSet());
					}
					else
					{
						successfulDetectMatrix = commonMethods.elementwiseMultiplyMatrix(
								Collections.unmodifiableList(virusSpreadSamples),
								Collections.unmodifiableList(virtualDetectionSamples));
						candidates = new HashSet<>(honeypots);
					}
				}
				else
				{
					successfulDetectMatrix = new ArrayList<>(virusSpreadSamples);
					candidates = new HashSet<>(honeypots);
				}
				int frequency = (int) successfulDetectMatrix.stream()
						.filter(samplePath -> candidates.stream().anyMatch(samplePath::contains)).count();
				double candidateObjective = frequency*1.0/sampleSize;
				// calculate gap
				double gap = algoResults.getOutputMap().get(newKey).getObjectiveValue()
								- candidateObjective;
				gaps.add(gap);
			}
			
			// calculate gap point estimate
			double gapPointEstimate = gaps.stream().reduce(0.0, Double::sum)/replicationSize;
			// calculate variance
			double variance = gaps.stream().map(e -> Math.pow(e-gapPointEstimate, 2))
					.reduce(0.0, Double::sum)/(replicationSize-1);
			double stDev = Math.sqrt(variance);
			double CIWidth = tValue*stDev/Math.sqrt(replicationSize);
			
			outputMap.put(candidate.getKey(), new statisticalOutput(gapPointEstimate, stDev, alpha, CIWidth));
		}
	}
}
