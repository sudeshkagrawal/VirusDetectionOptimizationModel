package analysis;

import com.opencsv.CSVWriter;
import dataTypes.McNemarsOutput;
import dataTypes.algorithmOutput;
import dataTypes.parameters;
import dataTypes.solverOutput;
import helper.commonMethods;
import network.graph;
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
 * Last Updated: September 22, 2020.
 */
public class McNemarsProcedure
{
	/**
	 * A map from {@code parameters} to {@code statisticalOutput}.
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
							"CI lower bound", "CI upper bound", "UTC"};
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
			String[] line = new String[13];
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
			line[12] = now;
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
	 * @throws Exception throw if 0<{@code alpha}<1 does not hold,
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
		
		for (parameters param: heuristicOutputs.keySet())
		{
			if (!optimizationOutputs.containsKey(param))
			{
				System.out.println(param.toString()+" does not exist in solver output, skipping!");
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
			Sextet<String, String, Integer, Integer, Double, Double> key =
														new Sextet<>(modelName, networkName, t_0, sampleSize, r, p);
			List<List<Integer>> virusSpreadSamples =
					observations.getMapModelNetworkT0RunsFalseNegativeToSimulationRuns().get(key);
			List<List<Integer>> virtualDetectionSamples =
					observations.getMapModelNetworkT0RunsFalseNegativeToVirtualDetections().get(key);
			List<List<Integer>> successfulDetectMatrix;
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
				}
				else
				{
					successfulDetectMatrix = commonMethods.elementwiseMultiplyMatrix(
							Collections.unmodifiableList(virusSpreadSamples),
							Collections.unmodifiableList(virtualDetectionSamples));
				}
			}
			else
			{
				successfulDetectMatrix = new ArrayList<>(virusSpreadSamples);
			}
			List<Integer> heuristicPots = heuristicOutputs.get(param).getHoneypots();
			List<Integer> optimalPots = optimizationOutputs.get(param).getHoneypots();
			Map<String, Integer> table = getContingencyTable(successfulDetectMatrix, heuristicPots, optimalPots);
		}
	}
	/**
	 * Finds n11, n12, n21, and n22:
	 <dl>
	 *     <dt>n11</dt> <dd>number of rows in {@code arr} where a node
	 *              from both {@code nodes1} and {@code nodes2} are present</dd>
	 *     <dt>n12</dt> <dd>number of rows in {@code arr} where a node
	 * 	 *          from {@code nodes1} is present, but no nodes from {@code nodes2} are present</dd>
	 *     <dt>n21</dt> <dd>number of rows in {@code arr} where no nodes
	 * 	 *          from {@code nodes1} are present, but a node from {@code nodes2} is present</dd>
	 *     <dt>n22</dt> <dd>number of rows in {@code arr} where no nodes
	 * 	 *          from either {@code nodes1} or {@code nodes2} are present.</dd>
	 * </dl>
	 *
	 * @param arr a list of lists of integers.
	 * @param nodes1 a list of integers.
	 * @param nodes2 a list of integers.
	 * @return a map from {n11, n12, n21, and n22} to corresponding value.
	 */
	private Map<String, Integer> getContingencyTable(List<List<Integer>> arr,
	                                                 List<Integer> nodes1, List<Integer> nodes2)
	{
		int n11 = 0;
		int n12 = 0;
		int n21 = 0;
		int n22 = 0;
		for (List<Integer> row: arr)
		{
			boolean onePresent = false;
			boolean twoPresent = false;
			for (Integer columnElement: row)
			{
				for (Integer e1: nodes1)
				{
					if (e1.equals(columnElement))
					{
						onePresent = true;
						break;
					}
					
				}
				for (Integer e2: nodes2)
				{
					if (e2.equals(columnElement))
					{
						twoPresent = true;
						break;
					}
					
				}
				if (onePresent && twoPresent)
					break;
			}
			if (onePresent && twoPresent)
				n11++;
			else
			{
				if ((!onePresent) && (!twoPresent))
					n22++;
				else
				{
					if (!onePresent)
						n21++;
					else
						n12++;
				}
			}
		}
		Map<String, Integer> output = new HashMap<>();
		output.put("n11", n11);
		output.put("n12", n12);
		output.put("n21", n21);
		output.put("n22", n22);
		return output;
	}
}
