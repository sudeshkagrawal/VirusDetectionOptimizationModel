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
 * Represents results of degree discount on {@code simulationRuns}.
 *
 * Reference:
 * Chen, Wei, Yajun Wang, and Siyu Yang. "Efficient influence maximization in social networks."
 * <i>Proceedings of the 15th ACM SIGKDD international conference on Knowledge discovery and data mining.</i> 2009.
 *
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: September 19, 2020.
 */
public class degreeDiscount
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
	public degreeDiscount()
	{
		this(new HashMap<>());
	}
	
	/**
	 * Constructor.
	 *
	 * @param outputMap a map from {@code parameters} to {@code algorithmOutput}.
	 */
	public degreeDiscount(Map<parameters, algorithmOutput> outputMap)
	{
		this.outputMap = outputMap;
	}
	
	/**
	 * Getter.
	 *
	 * @return returns {@code outputMap}.
	 */
	public Map<parameters, algorithmOutput> getOutputMap()
	{
		return outputMap;
	}
	
	/**
	 * Overrides {@code toString()}.
	 *
	 * @return returns string representation of values of field(s) in the class.
	 */
	@Override
	public String toString()
	{
		StringBuilder str = new StringBuilder(1000);
		str.append("degreeDiscount:");
		for(Map.Entry<parameters, algorithmOutput> e: outputMap.entrySet())
		{
			str.append("\n\t<").append(e.getKey()).append(",");
			str.append("\n\t\t Objective value:\n\t\t\t").append(e.getValue().getObjectiveValue());
			str.append("\n\t\t Honeypots:\n\t\t\t").append(e.getValue().getHoneypot());
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
	 * @param append true, if you wish to append to existing file; false, otherwise.
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
			line[8] = e.getValue().getHoneypot().toString();
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
		System.out.println("Degree discount results successfully written to \""+filename+"\".");
	}
	
	public void runSAAUsingKHighestDegreeSingleDiscountNodes(String modelName, graph g,
	                                                         simulationRuns simulationResults,
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
		double q = 0.01;   // parameter
		Instant tic = Instant.now();
		Map<Integer, List<Integer>> neighbors = g.getNeighbors();
		Instant toc = Instant.now();
		double commonWallTimeInSeconds = 1.0* Duration.between(tic, toc).toMillis()/1000;
		for (parameters param: listOfParams)
		{
			int k = param.getNumberOfHoneypots();
			if (k>g.getVertexSet().size())
				throw new Exception("Number of honeypots cannot be greater than the number of nodes!");
			int t_0 = param.getTimeStep();
			int run = param.getNumberOfSimulationRepetitions();
			double r = param.getFalseNegativeProbability();
			double p = param.getTransmissability();
			System.out.println("Finding "+k+" highest degree single discount nodes: "+g.getNetworkName()+"network; "
					+t_0+" time step(s); "
					+run+" samples; false negative probability="+r+"; transmissability (p)="+p);
			tic = Instant.now();
			List<Integer> honeypots = new ArrayList<>();
			// find the degrees of the nodes
			Map<Integer, Double> degreesOfNodes = g.getVertexSet().stream()
						.collect(Collectors.toMap(
								node -> node, e -> (double)g.getDegreeOfNode(e), (a, b) -> b));
			// initialize t_v with 0
			Map<Integer, Integer> numOfNeighborsSelected = g.getVertexSet().stream()
									.mapToInt(node -> node).boxed()
									.collect(Collectors.toMap(node -> node, node -> 0, (a, b) -> b));
			while (honeypots.size()<k)
			{
				// pick the highest discounted degree node
				int selectedNode = degreesOfNodes.keySet().stream()
						.max(Comparator.comparingDouble(degreesOfNodes::get)).orElseThrow();
				// remove it and add it to honeypot
				degreesOfNodes.remove(selectedNode);
				honeypots.add(selectedNode);
				// update degrees and t_v of neighbors not selected so far
				List<Integer> updateNodeList = new ArrayList<>(neighbors.get(selectedNode));
				updateNodeList.removeAll(honeypots);
				for (int neighbor: updateNodeList)
				{
					numOfNeighborsSelected.put(neighbor, numOfNeighborsSelected.get(neighbor)+1);
					double discount = 2*numOfNeighborsSelected.get(neighbor)
							+ q*numOfNeighborsSelected.get(neighbor)
							*(degreesOfNodes.get(neighbor)-numOfNeighborsSelected.get(neighbor));
					degreesOfNodes.put(neighbor, degreesOfNodes.get(neighbor)-discount);
				}
			}
			toc = Instant.now();
			double wallTimeInSeconds = 1.0*Duration.between(tic, toc).toMillis()/1000;
			
			// find objective value
			Sextet<String, String, Integer, Integer, Double, Double> key =
					new Sextet<>(modelName, g.getNetworkName(), t_0, run, r, p);
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
									.collect(Collectors.toCollection(() -> new ArrayList<>(t_0 + 1))))
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
