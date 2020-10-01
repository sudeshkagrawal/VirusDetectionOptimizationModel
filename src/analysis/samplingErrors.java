package analysis;

import com.opencsv.CSVWriter;
import dataTypes.algorithmOutput;
import dataTypes.parameters;
import dataTypes.samplingErrorsOutput;
import dataTypes.solverOutput;
import helper.commonMethods;
import network.graph;
import org.apache.commons.math3.distribution.NormalDistribution;
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
 * Represents statistical results of evaluating honeypots on a larger independent sample.
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: October 1, 2020.
 */
public class samplingErrors
{
	/**
	 * A map from {@code parameters} to {@code samplingErrorsOutput}.
	 * Basically, stores the outputs for different input parameters.
	 * <p>
	 *     Parameters: (model (TN11C, RAEPC, etc.), network name, time step, repetitions,
	 *     false negative probability, transmissability (p), number of honeypots).
	 * </p>
	 * <p>
	 *     Sampling Errors output: point estimate, std. err., half-width, etc.
	 * </p>
	 */
	Map<parameters, samplingErrorsOutput> outputMap;
	
	/**
	 * Constructor.
	 */
	public samplingErrors()
	{
		this(new HashMap<>());
	}
	
	/**
	 * Constructor.
	 *
	 * @param outputMap an instance of {@code outputMap}.
	 */
	public samplingErrors(Map<parameters, samplingErrorsOutput> outputMap)
	{
		this.outputMap = outputMap;
	}
	
	/**
	 * Getter.
	 *
	 * @return {@code outputMap}.
	 */
	public Map<parameters, samplingErrorsOutput> getOutputMap()
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
		final StringBuilder sb = new StringBuilder("Point estimates with sampling errors:");
		for(Map.Entry<parameters, samplingErrorsOutput> e: outputMap.entrySet())
		{
			sb.append("\n\t<").append(e.getKey().toString()).append(", ");
			sb.append("\n\t\t sample size =\n\t\t\t").append(e.getValue().getSampleSize());
			sb.append("\n\t\t point estimate =\n\t\t\t").append(e.getValue().getPointEstimate());
			sb.append("\n\t\t std. err. = \n\t\t\t").append(e.getValue().getStandardError());
			sb.append("\n\t\t half-width (alpha = ").append(e.getValue().getAlpha());
			sb.append(") = ").append(e.getValue().getHalfWidth());
			sb.append("\n\t>");
		}
		return sb.toString();
	}
	
	/**
	 * Writes point estimates and sampling error results to csv file.
	 *
	 * @param filename path to output file
	 * @param append {@code true}, if you wish to append to existing file; {@code false}, otherwise.
	 * @throws IOException thrown if error in input-output operation.
	 */
	public void writeToCSV(String filename, boolean append) throws IOException
	{
		File fileObj = new File(filename);
		String[] header = {"Model", "Network", "t_0", "Simulation repetitions", "FN probability",
							"transmissability (p)", "no. of honeypots", "point estimate", "std. err.", "alpha",
							"half-width", "CI lower bound", "CI upper bound", "sample size", "UTC"};
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
		for (Map.Entry<parameters, samplingErrorsOutput> e: outputMap.entrySet())
		{
			String[] line = new String[15];
			line[0] = e.getKey().getSpreadModelName();
			line[1] = e.getKey().getNetworkName();
			line[2] = String.valueOf(e.getKey().getTimeStep());
			line[3] = String.valueOf(e.getKey().getNumberOfSimulationRepetitions());
			line[4] = String.valueOf(e.getKey().getFalseNegativeProbability());
			line[5] = String.valueOf(e.getKey().getTransmissability());
			line[6] = String.valueOf(e.getKey().getNumberOfHoneypots());
			double mean = e.getValue().getPointEstimate();
			line[7] = String.valueOf(mean);
			line[8] = String.valueOf(e.getValue().getStandardError());
			line[9] = String.valueOf(e.getValue().getAlpha());
			double hw = e.getValue().getHalfWidth();
			line[10] = String.valueOf(hw);
			line[11] = String.valueOf(mean-hw);
			line[12] = String.valueOf(mean+hw);
			line[13] = String.valueOf(e.getValue().getSampleSize());
			line[14] = now;
			writer.writeNext(line);
		}
		writer.flush();
		writer.close();
		System.out.println("Point estimates and sampling errors results successfully written to \""+filename+"\".");
	}
	
	/**
	 * Evaluates honeypots from an algorithm statistically, using larger independent samples.
	 *
	 * @param g network graph
	 * @param algorithmOutputs results of an algorithm (greedy heuristic, for example)
	 * @param alpha alpha value for confidence interval
	 * @param sampleSize sample size.
	 * @throws Exception thrown if {@code 0<alpha<1} does not hold,
	 *  if the label of a node in {@code g} is a negative integer,
	 *  or if the network name in one of the parameters and the network name stored in the graph {@code g}
	 *      do not match.
	 */
	public void getPointEstimatesAndErrorsForAlgos(graph g, Map<parameters, algorithmOutput> algorithmOutputs,
	                                               double alpha, int sampleSize) throws Exception
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
		
		NormalDistribution mynormdist = new NormalDistribution(0, 1);
		double zValue = mynormdist.inverseCumulativeProbability(1-0.5*alpha);
		for (Map.Entry<parameters, algorithmOutput> candidate: algorithmOutputs.entrySet())
		{
			int hashcode = candidate.getKey().hashCode();
			String modelName = candidate.getKey().getSpreadModelName();
			String networkName = candidate.getKey().getNetworkName();
			if (!networkName.equals(g.getNetworkName()))
				throw new Exception("Parameters are for a different network than that has been provided as input!");
			int t_0 = candidate.getKey().getTimeStep();
			double r = candidate.getKey().getFalseNegativeProbability();
			double p = candidate.getKey().getTransmissability();
			System.out.println("Getting point estimates (algorithm) and errors: "+candidate.getKey().toString()
									+";\n\t"+sampleSize+" samples for procedure");
			simulationRuns observations = new simulationRuns();
			List<Pair<Integer, Integer>> t0_runs = new ArrayList<>();
			t0_runs.add(new Pair<>(t_0, sampleSize));
			if (modelName.equals("TN11C"))
			{
				int[]  seed = {2507+hashcode, 2101+hashcode, 3567+hashcode};
				observations.simulateTN11CRuns(g, t0_runs, r, seed);
			}
			else
			{
				if (modelName.equals("RA1PC"))
				{
					int[] seed = {2507+hashcode, 2101+hashcode, 2101+hashcode, 3567+hashcode};
					observations.simulateRA1PCRuns(g, t0_runs, r, p, seed);
				}
				else if (modelName.equals("RAEPC"))
				{
					int[]  seed = {2507+hashcode, 2101+hashcode, 3567+hashcode};
					observations.simulateRAEPCRuns(g, t0_runs, r, p, seed);
				}
			}
			Sextet<String, String, Integer, Integer, Double, Double> key = new Sextet<>(modelName,
																					networkName, t_0, sampleSize, r, p);
			List<List<Integer>> virusSpreadSamples =
									observations.getMapModelNetworkT0RunsFalseNegativeToSimulationRuns().get(key);
			List<List<Integer>> virtualDetectionSamples =
									observations.getMapModelNetworkT0RunsFalseNegativeToVirtualDetections().get(key);
			List<List<Integer>> successfulDetectMatrix;
			List<Integer> candidateHoneypots = candidate.getValue().getHoneypots();
			Set<Integer> mappedCandidateHoneypots;
			if (r>0)
			{
				if (zeroNode)
				{
					List<List<Integer>> newVirusSpreadSamples = virusSpreadSamples.stream()
												.map(virusSpreadSample -> virusSpreadSample.stream()
												.map(integer -> integer + 1)
												.collect(Collectors.toCollection(ArrayList::new)))
												.collect(Collectors.toCollection(() -> new ArrayList<>(sampleSize)));
					successfulDetectMatrix = commonMethods.elementwiseMultiplyMatrix(
												Collections.unmodifiableList(newVirusSpreadSamples),
												Collections.unmodifiableList(virtualDetectionSamples));
					mappedCandidateHoneypots = candidateHoneypots.stream().map(e -> e+1).collect(Collectors.toSet());
				}
				else
				{
					successfulDetectMatrix = commonMethods.elementwiseMultiplyMatrix(
												Collections.unmodifiableList(virusSpreadSamples),
												Collections.unmodifiableList(virtualDetectionSamples));
					mappedCandidateHoneypots = new HashSet<>(candidateHoneypots);
				}
			}
			else
			{
				successfulDetectMatrix = new ArrayList<>(virusSpreadSamples);
				mappedCandidateHoneypots = new HashSet<>(candidateHoneypots);
			}
			int frequency = (int) successfulDetectMatrix.stream()
								.filter(samplePath -> mappedCandidateHoneypots.stream()
								.anyMatch(samplePath::contains)).count();
			double estimate = 1.0*frequency/sampleSize;
			double err = Math.sqrt(estimate*(1-estimate)/sampleSize);
			double hw = zValue*err;
			outputMap.put(candidate.getKey(), new samplingErrorsOutput(estimate, err, alpha, hw, sampleSize));
		}
	}
	
	/**
	 * Evaluates honeypots from an optimization model statistically, using larger independent samples.
	 *
	 * @param g network graph
	 * @param solverOutputs results of SAA using a solver.
	 * @param alpha alpha value for confidence interval
	 * @param sampleSize sample size.
	 * @throws Exception thrown if {@code 0<alpha<1} does not hold,
	 *  if the label of a node in {@code g} is a negative integer,
	 *  or if the network name in one of the parameters and the network name stored in the graph {@code g}
	 *      do not match.
	 */
	public void getPointEstimatesAndErrorsForMIP(graph g, Map<parameters, solverOutput> solverOutputs,
	                                               double alpha, int sampleSize) throws Exception
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
		
		NormalDistribution mynormdist = new NormalDistribution(0, 1);
		double zValue = mynormdist.inverseCumulativeProbability(1-0.5*alpha);
		for (Map.Entry<parameters, solverOutput> candidate: solverOutputs.entrySet())
		{
			int hashcode = candidate.getKey().hashCode();
			String modelName = candidate.getKey().getSpreadModelName();
			String networkName = candidate.getKey().getNetworkName();
			if (!networkName.equals(g.getNetworkName()))
				throw new Exception("Parameters are for a different network than that has been provided as input!");
			int t_0 = candidate.getKey().getTimeStep();
			double r = candidate.getKey().getFalseNegativeProbability();
			double p = candidate.getKey().getTransmissability();
			System.out.println("Getting point estimates (MIP) and errors: "+candidate.getKey().toString()
					+";\n\t"+sampleSize+" samples for procedure");
			simulationRuns observations = new simulationRuns();
			List<Pair<Integer, Integer>> t0_runs = new ArrayList<>();
			t0_runs.add(new Pair<>(t_0, sampleSize));
			if (modelName.equals("TN11C"))
			{
				int[]  seed = {2507+hashcode, 2101+hashcode, 3567+hashcode};
				observations.simulateTN11CRuns(g, t0_runs, r, seed);
			}
			else
			{
				if (modelName.equals("RA1PC"))
				{
					int[] seed = {2507+hashcode, 2101+hashcode, 2101+hashcode, 3567+hashcode};
					observations.simulateRA1PCRuns(g, t0_runs, r, p, seed);
				}
				else if (modelName.equals("RAEPC"))
				{
					int[]  seed = {2507+hashcode, 2101+hashcode, 3567+hashcode};
					observations.simulateRAEPCRuns(g, t0_runs, r, p, seed);
				}
			}
			Sextet<String, String, Integer, Integer, Double, Double> key = new Sextet<>(modelName,
					networkName, t_0, sampleSize, r, p);
			List<List<Integer>> virusSpreadSamples =
					observations.getMapModelNetworkT0RunsFalseNegativeToSimulationRuns().get(key);
			List<List<Integer>> virtualDetectionSamples =
					observations.getMapModelNetworkT0RunsFalseNegativeToVirtualDetections().get(key);
			List<List<Integer>> successfulDetectMatrix;
			List<Integer> candidateHoneypots = candidate.getValue().getHoneypots();
			Set<Integer> mappedCandidateHoneypots;
			if (r>0)
			{
				if (zeroNode)
				{
					List<List<Integer>> newVirusSpreadSamples = virusSpreadSamples.stream()
							.map(virusSpreadSample -> virusSpreadSample.stream()
									.map(integer -> integer + 1)
									.collect(Collectors.toCollection(ArrayList::new)))
							.collect(Collectors.toCollection(() -> new ArrayList<>(sampleSize)));
					successfulDetectMatrix = commonMethods.elementwiseMultiplyMatrix(
							Collections.unmodifiableList(newVirusSpreadSamples),
							Collections.unmodifiableList(virtualDetectionSamples));
					mappedCandidateHoneypots = candidateHoneypots.stream().map(e -> e+1).collect(Collectors.toSet());
				}
				else
				{
					successfulDetectMatrix = commonMethods.elementwiseMultiplyMatrix(
							Collections.unmodifiableList(virusSpreadSamples),
							Collections.unmodifiableList(virtualDetectionSamples));
					mappedCandidateHoneypots = new HashSet<>(candidateHoneypots);
				}
			}
			else
			{
				successfulDetectMatrix = new ArrayList<>(virusSpreadSamples);
				mappedCandidateHoneypots = new HashSet<>(candidateHoneypots);
			}
			int frequency = (int) successfulDetectMatrix.stream()
					.filter(samplePath -> mappedCandidateHoneypots.stream()
							.anyMatch(samplePath::contains)).count();
			double estimate = 1.0*frequency/sampleSize;
			double err = Math.sqrt(estimate*(1-estimate)/sampleSize);
			double hw = zValue*err;
			
			outputMap.put(candidate.getKey(), new samplingErrorsOutput(estimate, err, alpha, hw, sampleSize));
		}
	}
}
