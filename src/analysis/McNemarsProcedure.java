package analysis;

import com.opencsv.CSVWriter;
import dataTypes.McNemarsOutput;
import dataTypes.algorithmOutput;
import dataTypes.parameters;
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
 * Represents results of the McNemar's procedure.
 * <p>
 *     Reference:
 * </p>
 * <p>
 *     [1] McNemar, Quinn.
 *     "Note on the sampling error of the difference between correlated proportions or percentages."
 *     <i>Psychometrika</i>
 *     12.2 (1947): 153&ndash;157.
 * </p>
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: October 1, 2020.
 */
public class McNemarsProcedure
{
	/**
	 * A map from {@code parameters} to {@code McNemarsOutput}.
	 * Basically, stores the outputs for different input parameters.
	 * <p>
	 *     Parameters: (model (TN11C, RAEPC, etc.), network name, time step, repetitions,
	 *     false negative probability, transmissability (p), number of honeypots).
	 * </p>
	 * <p>
	 *     McNemar's Output: sample std. err., confidence interval, etc.
	 * </p>
	 */
	Map<parameters, McNemarsOutput> outputMap;
	
	/**
	 * Constructor.
	 */
	public McNemarsProcedure()
	{
		this(new HashMap<>());
	}
	
	/**
	 * Constructor.
	 *
	 * @param outputMap a map from {@code parameters} to {@code McNemarsOutput}.
	 */
	public McNemarsProcedure(Map<parameters, McNemarsOutput> outputMap)
	{
		this.outputMap = outputMap;
	}
	
	/**
	 * Getter.
	 *
	 * @return {@code outputMap}.
	 */
	public Map<parameters, McNemarsOutput> getOutputMap()
	{
		return outputMap;
	}
	
	/**
	 * Returns a string representation of the object.
	 *
	 * @return a string representation of the object.
	 */
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder("McNemar's procedure:");
		for(Map.Entry<parameters, McNemarsOutput> e: outputMap.entrySet())
		{
			sb.append("\n\t<").append(e.getKey().toString()).append(", ");
			sb.append("\n\t\t sample size =\n\t\t\t").append(e.getValue().getSampleSize());
			sb.append("\n\t\t dhat =\n\t\t\t").append(e.getValue().getdHat());
			sb.append("\n\t\t sample std. err. = \n\t\t\t").append(e.getValue().getSampleStandardError());
			sb.append("\n\t\t CI (alpha = ").append(e.getValue().getAlpha());
			sb.append("): [").append(e.getValue().getLowerCI());
			sb.append(", ").append(e.getValue().getUpperCI()).append("]");
			sb.append("\n\t>");
		}
		return sb.toString();
	}
	
	/**
	 * Writes McNemar's procedure results to csv file.
	 *
	 * @param filename path to output file
	 * @param append {@code true}, if you wish to append to existing file; {@code false}, otherwise.
	 * @throws IOException thrown if error in input-output operation.
	 */
	public void writeToCSV(String filename, boolean append) throws IOException
	{
		File fileObj = new File(filename);
		String[] header = {"Model", "Network", "t_0", "Simulation repetitions", "FN probability",
							"transmissability (p)", "no. of honeypots", "dhat", "sample std. err.", "alpha",
							"CI lower bound", "CI upper bound", "sample size", "UTC"};
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
		for (Map.Entry<parameters, McNemarsOutput> e: outputMap.entrySet())
		{
			String[] line = new String[14];
			line[0] = e.getKey().getSpreadModelName();
			line[1] = e.getKey().getNetworkName();
			line[2] = String.valueOf(e.getKey().getTimeStep());
			line[3] = String.valueOf(e.getKey().getNumberOfSimulationRepetitions());
			line[4] = String.valueOf(e.getKey().getFalseNegativeProbability());
			line[5] = String.valueOf(e.getKey().getTransmissability());
			line[6] = String.valueOf(e.getKey().getNumberOfHoneypots());
			line[7] = String.valueOf(e.getValue().getdHat());
			line[8] = String.valueOf(e.getValue().getSampleStandardError());
			line[9] = String.valueOf(e.getValue().getAlpha());
			line[10] = String.valueOf(e.getValue().getLowerCI());
			line[11] = String.valueOf(e.getValue().getUpperCI());
			line[12] = String.valueOf(e.getValue().getSampleSize());
			line[13] = now;
			writer.writeNext(line);
		}
		writer.flush();
		writer.close();
		System.out.println("McNemar's procedure results successfully written to \""+filename+"\".");
	}
	
	/**
	 * Compares the results of MIP and greedy heuristic using McNemar's procedure.
	 *
	 * @param g network graph
	 * @param heuristicOutputs results of greedy heuristic
	 * @param optimizationOutputs results of optimization model
	 * @param alpha alpha value for confidence interval
	 * @param sampleSize sample size.
	 * @throws Exception thrown if {@code 0<alpha<1} does not hold,
	 *  or if the label of a node in {@code g} is a negative integer,
	 *  or if the network name in one of the parameters and the network name stored in the graph {@code g}
	 *      do not match.
	 */
	public void compareMIPAndHeuristic(graph g, Map<parameters, algorithmOutput> heuristicOutputs,
	                                      Map<parameters, solverOutput> optimizationOutputs,
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
		System.out.println("Z-value used in McNemar's procedure = "+zValue);
		for (parameters param: heuristicOutputs.keySet())
		{
			if (!optimizationOutputs.containsKey(param))
			{
				System.out.println(param.toString()+" does not exist in optimization output, skipping!");
				continue;
			}
			int hashcode = param.hashCode();
			String modelName = param.getSpreadModelName();
			String networkName = param.getNetworkName();
			if (!networkName.equals(g.getNetworkName()))
				throw new Exception("Parameters are for a different network than that has been provided as input!");
			int t_0 = param.getTimeStep();
			double r = param.getFalseNegativeProbability();
			double p = param.getTransmissability();
			System.out.println("Using McNemar's procedure: "+param.toString()+";\n\t"+sampleSize
									+" samples for procedure");
			simulationRuns observations = new simulationRuns();
			List<Pair<Integer, Integer>> t0_runs = new ArrayList<>();
			t0_runs.add(new Pair<>(t_0, sampleSize));
			if (modelName.equals("TN11C"))
			{
				int[]  seed = {5072+hashcode, 1012+hashcode, 5673+hashcode};
				observations.simulateTN11CRuns(g, t0_runs, r, seed);
			}
			else
			{
				if (modelName.equals("RA1PC"))
				{
					int[] seed = {5072+hashcode, 1012+hashcode, 1013+hashcode, 5673+hashcode};
					observations.simulateRA1PCRuns(g, t0_runs, r, p, seed);
				}
				else if (modelName.equals("RAEPC"))
				{
					int[]  seed = {5072+hashcode, 1012+hashcode, 5673+hashcode};
					observations.simulateRAEPCRuns(g, t0_runs, r, p, seed);
				}
			}
			Sextet<String, String, Integer, Integer, Double, Double> key =
														new Sextet<>(modelName, networkName, t_0, sampleSize, r, p);
			List<List<Integer>> virusSpreadSamples =
									observations.getMapModelNetworkT0RunsFalseNegativeToSimulationRuns().get(key);
			List<List<Integer>> virtualDetectionSamples =
									observations.getMapModelNetworkT0RunsFalseNegativeToVirtualDetections().get(key);
			List<List<Integer>> successfulDetectMatrix;
			List<Integer> heuristicPots = heuristicOutputs.get(param).getHoneypots();
			List<Integer> optimalPots = optimizationOutputs.get(param).getHoneypots();
			Set<Integer> mappedHeuristicPots, mappedOptimalPots;
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
					mappedHeuristicPots = heuristicPots.stream().map(e -> e+1).collect(Collectors.toSet());
					mappedOptimalPots = optimalPots.stream().map(e -> e+1).collect(Collectors.toSet());
				}
				else
				{
					successfulDetectMatrix = commonMethods.elementwiseMultiplyMatrix(
												Collections.unmodifiableList(virusSpreadSamples),
												Collections.unmodifiableList(virtualDetectionSamples));
					mappedHeuristicPots = new HashSet<>(heuristicPots);
					mappedOptimalPots = new HashSet<>(optimalPots);
				}
			}
			else
			{
				successfulDetectMatrix = new ArrayList<>(virusSpreadSamples);
				mappedHeuristicPots = new HashSet<>(heuristicPots);
				mappedOptimalPots = new HashSet<>(optimalPots);
			}
			Map<String, Integer> table = commonMethods.getContingencyTable(successfulDetectMatrix,
																			new ArrayList<>(mappedHeuristicPots),
																			new ArrayList<>(mappedOptimalPots));
			double commonDenominator = 1.0/sampleSize;
			double dhat = (table.get("n12")-table.get("n21"))*commonDenominator;
			double n12term = commonDenominator*table.get("n12");
			double n21term = commonDenominator*table.get("n21");
			double sampleVarFirst = n12term*(1.0-n12term);
			double sampleVarSecond = n21term*(1.0-n21term);
			double sampleVarThird = 2.0*n12term*n21term;
			double sampleVar = commonDenominator*(sampleVarFirst+sampleVarSecond+sampleVarThird);
			double sampleStDev = Math.sqrt(sampleVar);
			double width = zValue * sampleStDev;
			
			outputMap.put(param, new McNemarsOutput(dhat, sampleStDev,
										alpha, dhat - width, dhat + width, sampleSize));
		}
	}
}
