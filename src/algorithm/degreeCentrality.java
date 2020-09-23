package algorithm;

import com.opencsv.CSVWriter;
import dataTypes.algorithmOutput;
import dataTypes.parameters;
import helper.commonMethods;
import network.graph;
import org.javatuples.Sextet;
import simulation.simulationRuns;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents results of degree centrality on {@code simulationRuns}.
 * In degree centrality we choose the k vertices with highest degrees.
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: September 23, 2020.
 */
public class degreeCentrality
{
	/**
	 * A map from {@code parameters} to {@code algorithmOutput}.
	 * Basically, stores the outputs for different input parameters.
	 * <p>
	 *     Parameters: (model (TN11C, RAEPC, etc.), network name, time step, repetitions,
	 *     false negative probability, transmissability (p), number of honeypots).
	 * </p>
	 * <p>
	 *     Algorithm output: objective value, honeypot, wall time, a priori upper bound, and posterior upper bound.
	 * </p>
	 */
	Map<parameters, algorithmOutput> outputMap;
	
	/**
	 * Constructor.
	 */
	public degreeCentrality()
	{
		this(new HashMap<>());
	}
	
	/**
	 * Constructor.
	 *
	 * @param outputMap a map from {@code parameters} to {@code algorithmOutput}.
	 */
	public degreeCentrality(Map<parameters, algorithmOutput> outputMap)
	{
		this.outputMap = outputMap;
	}
	
	/**
	 * Getter.
	 *
	 * @return {@code outputMap}.
	 */
	public Map<parameters, algorithmOutput> getOutputMap()
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
		StringBuilder str = new StringBuilder(1000);
		str.append("degreeCentrality:");
		for(Map.Entry<parameters, algorithmOutput> e: outputMap.entrySet())
		{
			str.append("\n\t<").append(e.getKey()).append(",");
			str.append("\n\t\t Objective value:\n\t\t\t").append(e.getValue().getObjectiveValue());
			str.append("\n\t\t Honeypots:\n\t\t\t").append(e.getValue().getHoneypots());
			str.append("\n\t\t a priori UB:\n\t\t\t").append(e.getValue().getAPrioriUB());
			str.append("\n\t\t posterior UB:\n\t\t\t").append(e.getValue().getPosteriorUB());
			str.append("\n\t\t Wall time (second):\n\t\t\t").append(e.getValue().getWallTime());
			str.append("\n\t>");
		}
		return str.toString();
	}
	
	/**
	 * Writes algorithm results to csv file.
	 *
	 * @param filename path to output file
	 * @param append {@code true}, if you wish to append to existing file; {@code false}, otherwise.
	 * @throws IOException thrown if error in input-output operation.
	 */
	public void writeToCSV(String filename, boolean append) throws IOException
	{
		File fileObj = new File(filename);
		String[] header = {"Model", "Network", "t_0", "Simulation repetitions", "FN probability",
				"transmissability (p)", "no. of honeypots", "objective value", "honeypots",
				"a priori UB", "posterior UB", "Posterior Gap (%)", "Wall time (s)", "UTC"};
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
		for (Map.Entry<parameters, algorithmOutput> e: outputMap.entrySet())
		{
			String[] line = new String[14];
			line[0] = e.getKey().getSpreadModelName();
			line[1] = e.getKey().getNetworkName();
			line[2] = String.valueOf(e.getKey().getTimeStep());
			line[3] = String.valueOf(e.getKey().getNumberOfSimulationRepetitions());
			line[4] = String.valueOf(e.getKey().getFalseNegativeProbability());
			line[5] = String.valueOf(e.getKey().getTransmissability());
			line[6] = String.valueOf(e.getKey().getNumberOfHoneypots());
			double objectiveValue = e.getValue().getObjectiveValue();
			line[7] = String.valueOf(objectiveValue);
			line[8] = e.getValue().getHoneypots().toString();
			line[9] = String.valueOf(e.getValue().getAPrioriUB());
			double posteriorUB = e.getValue().getPosteriorUB();
			line[10] = String.valueOf(posteriorUB);
			line[11] = String.valueOf(
					100.0*(posteriorUB-objectiveValue)/(objectiveValue));
			line[12] = String.valueOf(e.getValue().getWallTime());
			line[13] = now;
			writer.writeNext(line);
		}
		writer.flush();
		writer.close();
		System.out.println("Degree centrality results successfully written to \""+filename+"\".");
	}
	
	/**
	 * Find the k highest degree nodes to use as honeypots
	 * and evaluates the objective value, upper bounds and execution time.
	 *
	 * @param g network graph
	 * @param simulationResults results of simulation as an instance of {@code simulationRuns}
	 * @param listOfParams list of the set of parameters used to get {@code simulationResults}.
	 * @throws Exception thrown if the graph {@code g} has self loops,
	 *  or if the label of a node in {@code g} is a negative integer,
	 *  or if the network name in one of the parameters and the network name stored in the graph {@code g}
	 *      do not match,
	 *  or if the number of nodes is less than the number of honeypots in any of the parameters in {@code listOfParams}.
	 */
	public void runSAAUsingKHighestDegreeNodes(graph g, simulationRuns simulationResults,
	                                           List<parameters> listOfParams) throws Exception
	{
		if (g.hasSelfLoops())
			throw new Exception("Graphs has self-loops!");
		
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
		
		Instant tic = Instant.now();
		Map<Integer, Integer> degreesOfNodes = g.getVertexSet()
												.stream()
												.collect(Collectors.toMap(
														node -> node, g::getDegreeOfNode, (a, b) -> b));
		Instant toc = Instant.now();
		double commonWallTimeInSeconds = 1.0*Duration.between(tic, toc).toMillis()/1000;
		for (parameters param: listOfParams)
		{
			String modelName = param.getSpreadModelName();
			String networkName = param.getNetworkName();
			if (!networkName.equals(g.getNetworkName()))
				throw new Exception("Parameters are for a different network than that has been provided as input!");
			int k = param.getNumberOfHoneypots();
			if (k>g.getVertexSet().size())
				throw new Exception("Number of honeypots cannot be greater than the number of nodes!");
			int t_0 = param.getTimeStep();
			int run = param.getNumberOfSimulationRepetitions();
			double r = param.getFalseNegativeProbability();
			double p = param.getTransmissability();
			System.out.println("Finding "+k+" highest degree nodes: "+networkName+"network; "
					+t_0+" time step(s); "
					+run+" samples; false negative probability="+r+"; transmissability (p)="+p);
			// find K highest degree nodes
			tic = Instant.now();
			PriorityQueue<Integer> topKDegreeNodes = commonMethods.getKHighestDegreeNodes(degreesOfNodes, k);
			toc = Instant.now();
			double wallTimeInSeconds = 1.0*Duration.between(tic, toc).toMillis()/1000;
			List<Integer> honeypots = new ArrayList<>(topKDegreeNodes);
			
			// find objective value
			Sextet<String, String, Integer, Integer, Double, Double> key =
															new Sextet<>(modelName, networkName, t_0, run, r, p);
			List<List<Integer>> virusSpreadSamples =
					simulationResults.getMapModelNetworkT0RunsFalseNegativeToSimulationRuns().get(key);
			List<List<Integer>> virtualDetectionSamples =
					simulationResults.getMapModelNetworkT0RunsFalseNegativeToVirtualDetections().get(key);
			// System.out.println("Virus spread samples:\n"+virusSpreadSamples+"\n"+virtualDetectionSamples);
			List<List<Integer>> successfulDetectMatrix;
			Set<Integer> candidates;
			if (r>0)
			{
				if (zeroNode)
				{
					List<List<Integer>> newVirusSpreadSamples = virusSpreadSamples.stream()
									.map(virusSpreadSample -> virusSpreadSample.stream()
									.map(integer -> integer + 1)
									.collect(Collectors.toCollection(ArrayList::new)))
									.collect(Collectors.toCollection(() -> new ArrayList<>(run)));
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
			//System.out.println("Successful detection matrix: \n"
			//					+successfulDetectMatrix.toString()+"\n---------------------------");
			//System.out.println("Candidate nodes: \n"+candidates.toString()+"\n---------------------------");
			int frequency = (int) successfulDetectMatrix.stream()
									.filter(samplePath -> candidates.stream().anyMatch(samplePath::contains)).count();
			double objectiveValue = frequency*1.0/run;
			
			// find upper bounds
			double factor = Math.exp(1)/(Math.exp(1)-1);
			double delta = commonMethods.calculateDelta(g, successfulDetectMatrix, honeypots, frequency);
			
			outputMap.put(param, new algorithmOutput(objectiveValue, honeypots,
					commonWallTimeInSeconds+wallTimeInSeconds, Math.min(factor*objectiveValue, 1),
					Math.min(objectiveValue+delta, 1)));
		}
	}
}
