package heuristic;

import com.opencsv.CSVWriter;
import network.graph;
import org.javatuples.Septet;
import org.javatuples.Sextet;
import org.jgrapht.alg.util.Triple;
import simulation.simulationRuns;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents results of heuristic on {@code simulationRuns}.
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: September 2, 2020.
 */
public class nodeInMaxRowsGreedyHeuristic
{
	/**
	 * A map from a 7-tuple to the objective value for a given solution.
	 * 7-tuple: (model (TN11C, RAEPC, etc.), network name, time step, repetitions,
	 * false negative probability, transmissability (p), number of honeypots)
	 */
	Map<Septet<String, String, Integer, Integer, Double, Double, Integer>, Double> mapToObjectiveValue;
	/**
	 * A map from a 7-tuple to the list of honeypots in a given solution.
	 * 7-tuple: (model (TN11C, RAEPC, etc.), network name, time step, repetitions,
	 * false negative probability, transmissability (p), number of honeypots)
	 */
	Map<Septet<String, String, Integer, Integer, Double, Double, Integer>, List<Integer>> mapToHoneypots;
	/**
	 * A map from a 7-tuple to the CPU time it took to find a given solution.
	 * 7-tuple: (model (TN11C, RAEPC, etc.), network name, time step, repetitions,
	 * false negative probability, transmissability (p), number of honeypots)
	 */
	Map<Septet<String, String, Integer, Integer, Double, Double, Integer>, Double> mapToWallTime;
	
	/**
	 * Constructor.
	 */
	public nodeInMaxRowsGreedyHeuristic()
	{
		this(new HashMap<>(), new HashMap<>(), new HashMap<>());
	}
	
	/**
	 * Constructor.
	 *
	 * @param mapToObjectiveValue value of objective function for a given solution
	 * @param mapToHoneypots list of honeypots in a given solution
	 * @param mapToWallTime CPU time taken to find a given solution.
	 */
	public nodeInMaxRowsGreedyHeuristic(
			Map<Septet<String, String, Integer, Integer, Double, Double, Integer>, Double> mapToObjectiveValue,
			Map<Septet<String, String, Integer, Integer, Double, Double, Integer>, List<Integer>> mapToHoneypots,
			Map<Septet<String, String, Integer, Integer, Double, Double, Integer>, Double> mapToWallTime)
	{
		this.mapToObjectiveValue = mapToObjectiveValue;
		this.mapToHoneypots = mapToHoneypots;
		this.mapToWallTime = mapToWallTime;
	}
	
	/**
	 * Getter.
	 *
	 * @return returns {@code mapToObjectiveValue}.
	 */
	public Map<Septet<String, String, Integer, Integer, Double, Double, Integer>, Double> getMapToObjectiveValue()
	{
		return mapToObjectiveValue;
	}
	
	/**
	 * Getter.
	 *
	 * @return returns {@code mapToHoneypots}.
	 */
	public Map<Septet<String, String, Integer, Integer, Double, Double, Integer>, List<Integer>> getMapToHoneypots()
	{
		return mapToHoneypots;
	}
	
	/**
	 * Getter.
	 *
	 * @return returns {@code mapToWallTime}.
	 */
	public Map<Septet<String, String, Integer, Integer, Double, Double, Integer>, Double> getMapToWallTime()
	{
		return mapToWallTime;
	}
	
	/**
	 * Solves the sample-average approximation model using a greedy heuristic.
	 * See model 4.6 in Lee, Jinho. Stochastic optimization models for rapid detection of viruses in cellphone networks. Diss. 2012.
	 *
	 * @param modelName name of virus spread model (TN11C, RAEPC, etc.)
	 * @param g network graph
	 * @param simulationResults results of simulation as an instance of {@code simulationRuns}
	 * @param k_t0_runs list of a 3-tuple of (k, t0, runs),
	 *                  where k is number of honeypots, t0 is simulation time,
	 *                  and runs is number of repetitions of simulation
	 * @param r false negative probability
	 * @param p transmissability probability.
	 * @throws Exception exception thrown in node labels are negative integers.
	 */
	public void runSAAUsingHeuristic(String modelName, graph g, simulationRuns simulationResults,
	                                 List<Triple<Integer, Integer, Integer>> k_t0_runs, double r, double p) throws Exception
	{
		// Remove self-loops if any from the graph
		g.removeSelfLoops();
		System.out.print("Removed self-loops (if any) from the graph: ");
		final int n = g.getG().vertexSet().size();
		System.out.println("(new) network has "+n+" nodes and "+g.getG().edgeSet().size()+" edges.");
		
		// minimum label of vertex
		boolean zeroNode = false;
		int minNode = g.getG().vertexSet().stream().mapToInt(v -> v).min().orElseThrow(NoSuchElementException::new);
		if (minNode==0)
			zeroNode = true;
		else
			if (minNode<0)
				throw new Exception("Node labels are negative integers! Terminating...");
		
		for (Triple<Integer, Integer, Integer> v : k_t0_runs)
		{
			int k = v.getFirst();
			int t_0 = v.getSecond();
			int run = v.getThird();
			Sextet<String, String, Integer, Integer, Double, Double> key =
														new Sextet<>(modelName, g.getNetworkName(), t_0, run, r, p);
			Septet<String, String, Integer, Integer, Double, Double, Integer> fullKey =
														new Septet<>(modelName, g.getNetworkName(), t_0, run, r, p, k);
			
			System.out.println("Using greedy heuristic: "+modelName+" spread model on "+g.getNetworkName()
								+"network; "+k+" honeypots; "+t_0+" time step(s); "
								+run+" samples; false negative probability="+r+"; transmissability (p)="+p);
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
					successfulDetectMatrix = elementwiseMultiplyMatrix(
							Collections.unmodifiableList(newVirusSpreadSamples),
							Collections.unmodifiableList(virtualDetectionSamples));
					candidates = g.getG().vertexSet().stream().map(e -> e+1).collect(Collectors.toSet());
				}
				else
				{
					successfulDetectMatrix = elementwiseMultiplyMatrix(
												Collections.unmodifiableList(virusSpreadSamples),
												Collections.unmodifiableList(virtualDetectionSamples));
					candidates = new HashSet<>(g.getG().vertexSet());
				}
			}
			else
			{
				successfulDetectMatrix = new ArrayList<>(virusSpreadSamples);
				candidates = new HashSet<>(g.getG().vertexSet());
			}
			 // System.out.println("Successful detection matrix: \n"+successfulDetectMatrix.toString()+"\n---------------------------");
			 // System.out.println("Candidate nodes: \n"+candidates.toString()+"\n---------------------------");
			
			List<Integer> honeypots = new ArrayList<>();
			int numberOfHoneypotsFound = 0;
			Set<Integer> indicesOfSamplesToBeConsidered = IntStream.range(0, run).boxed().collect(Collectors.toSet());
			Set<Integer> indicesOfSamplesToBeRemoved;
			Set<Integer> indicesOfSamplesCovered = new HashSet<>(indicesOfSamplesToBeConsidered);
			
			Instant tic = Instant.now();
			while (numberOfHoneypotsFound<k)
			{
				List<List<Integer>> samplesToBeConsidered = IntStream.range(0, successfulDetectMatrix.size())
						.filter(indicesOfSamplesToBeConsidered::contains)
						.mapToObj(successfulDetectMatrix::get).collect(Collectors.toList());
				int currentCandidate = findMaxRowFrequencyNode(Collections.unmodifiableList(samplesToBeConsidered),
										List.copyOf(candidates));
				// System.out.println("Current candidate: "+currentCandidate);
				honeypots.add(currentCandidate);
				numberOfHoneypotsFound++;
				candidates.remove(currentCandidate);
				indicesOfSamplesToBeRemoved = new HashSet<>(
						findRowOccurrenceIndices(Collections.unmodifiableList(successfulDetectMatrix), currentCandidate));
				indicesOfSamplesToBeConsidered.removeAll(indicesOfSamplesToBeRemoved);
				// TODO: What if k > number of nodes?
				// TODO: What if current set of honeypots cover all sample paths?
			}
			Instant toc = Instant.now();
			if ((r>0) && (zeroNode))
				honeypots = honeypots.stream().map(e -> e - 1).collect(Collectors.toList());
			// System.out.println("Honeypots: \n"+honeypots.toString()+"\n---------------------------");
			indicesOfSamplesCovered.removeAll(indicesOfSamplesToBeConsidered);
			double objectiveValue = indicesOfSamplesCovered.size()*1.0/run;
			System.out.println("Objective value = "+objectiveValue);
			double wallTimeInSeconds = 1.0*Duration.between(tic, toc).toMillis()/1000;
			System.out.println("Wall time (second) = "+ wallTimeInSeconds);
			
			mapToObjectiveValue.put(fullKey, objectiveValue);
			mapToHoneypots.put(fullKey, honeypots);
			mapToWallTime.put(fullKey, wallTimeInSeconds);
		}
	}
	
	/**
	 * Element-wise multiplication of two list of lists.
	 *
	 * @param a the first list of lists
	 * @param b the second list of lists.
	 * @return returns a list of lists.
	 * @throws Exception exception thrown if outer lists {@code a} and {@code b} not of same size.
	 *          Corresponding inner lists should also be of same size,
	 *          but that exception is not thrown since the check would be expensive.
	 */
	private List<List<Integer>> elementwiseMultiplyMatrix(List<List<Integer>> a, List<List<Integer>> b) throws Exception
	{
		List<List<Integer>> output = new ArrayList<>(a.size());
		if (a.size()!=b.size())
			throw new Exception("Inputs are not of the same size!");
		for (int i=0; i<a.size(); i++)
		{
			List<Integer> colList = new ArrayList<>(a.get(i).size());
			for (int j=0; j<a.get(i).size(); j++)
				colList.add(a.get(i).get(j) * b.get(i).get(j));
			output.add(colList);
		}
		return output;
	}
	
	/**
	 * Slower and older version of {@code findMaxRowFrequencyNode()}.
	 *
	 * @param arr a list of lists
	 * @param nodes a list of integers (nodes).
	 * @return returns a node as {@code int}.
	 */
	@Deprecated
	private int findMaxRowFrequencyNodeOld(List<List<Integer>> arr, List<Integer> nodes)
	{
		int maxNode = nodes.get(0);
		int maxNodeFrequency = 0;
		for (Integer node : nodes)
		{
			int count;
			int currentNode = node;
			count = (int) IntStream.range(0, arr.size()).parallel()
					.filter(i -> IntStream.range(0, arr.get(i).size())
							.anyMatch(j -> arr.get(i).get(j) == currentNode)).count();
			if (count > maxNodeFrequency)
			{
				maxNode = currentNode;
				maxNodeFrequency = count;
			}
		}
		return maxNode;
	}
	
	/**
	 * Finds a node (in {@code nodes}) which is present in the most rows of {@code arr}.
	 *
	 * @param arr a list of lists
	 * @param nodes a list of integers (nodes).
	 * @return returns a node as {@code int}.
	 */
	private int findMaxRowFrequencyNode(List<List<Integer>> arr, List<Integer> nodes)
	{
		Map<Integer, Integer> rowCount = nodes.stream().collect(Collectors
							.toMap(node -> node, node -> 0, (a, b) -> b, () -> new HashMap<>(nodes.size())));
		for (List<Integer> row : arr)
			for (int key : row)
				if (rowCount.containsKey(key))
					rowCount.put(key, rowCount.get(key) + 1);
		return Objects.requireNonNull(rowCount.entrySet()
				.stream().max(Comparator.comparingInt(Map.Entry::getValue)).orElse(null)).getKey();
	}
	
	/**
	 * Finds indices of rows where {@code node} occurs in {@code arr}.
	 *
	 * @param arr a list of lists
	 * @param node a list of integers (nodes).
	 * @return returns a list of indices.
	 */
	private List<Integer> findRowOccurrenceIndices(List<List<Integer>> arr, int node)
	{
		return IntStream.range(0, arr.size())
				.filter(i -> IntStream.range(0, arr.get(i).size())
						.anyMatch(j -> arr.get(i).get(j) == node)).boxed().collect(Collectors.toList());
	}
	
	/**
	 * Loads any results of heuristic from serialized object in file.
	 *
	 * @param serialFilename path of the file where the serialized object is stored.
	 */
	public void loadHeuristicResultsFromFile(String serialFilename)
	{
		try
		{
			FileInputStream fin = new FileInputStream(serialFilename);
			BufferedInputStream bin = new BufferedInputStream(fin);
			ObjectInputStream objin = new ObjectInputStream(bin);
			List<Object> serObject = (List<Object>) objin.readObject();
			mapToObjectiveValue =
					(Map<Septet<String, String, Integer, Integer, Double, Double, Integer>, Double>) serObject.get(0);
			mapToHoneypots =
					(Map<Septet<String, String, Integer, Integer, Double, Double, Integer>, List<Integer>>)
							serObject.get(1);
			mapToWallTime =
					(Map<Septet<String, String, Integer, Integer, Double, Double, Integer>, Double>) serObject.get(2);
			objin.close();
			bin.close();
			fin.close();
			System.out.println("Using results of heuristic in \""+serialFilename+"\".");
		}
		catch (FileNotFoundException e1)
		{
			System.out.println("Error, file not found!");
			System.out.println(e1.getMessage());
		}
		catch (Exception e2)
		{
			System.out.println("An exception occurred:");
			e2.printStackTrace();
			System.out.println("Exiting the program...");
			System.exit(0);
		}
	}
	/**
	 * Serializes all the fields of this class.
	 *
	 * @param serialFilename path of the file where the serialized object is to be stored.
	 */
	public void serializeResults(String serialFilename)
	{
		try
		{
			FileOutputStream fout = new FileOutputStream(serialFilename);
			BufferedOutputStream bout = new BufferedOutputStream(fout);
			ObjectOutputStream objout = new ObjectOutputStream(bout);
			List<Object> serObject = new ArrayList<>(3);
			serObject.add(mapToObjectiveValue);
			serObject.add(mapToHoneypots);
			serObject.add(mapToWallTime);
			objout.writeObject(serObject);
			objout.close();
			bout.close();
			fout.close();
			System.out.println("Heuristic results serialized at \""+ serialFilename +"\".");
		}
		catch (IOException e1)
		{
			System.out.println("Input-Output Exception:");
			e1.printStackTrace();
			System.out.print("Writing of serial file to disk failed!");
			System.out.println("Exiting the program...");
			System.exit(0);
		}
		catch (Exception e2)
		{
			System.out.println("An exception occurred:");
			e2.printStackTrace();
			System.out.print("Writing of serial file to disk failed!");
			System.out.println("Exiting the program...");
			System.exit(0);
		}
	}
	
	/**
	 * Writes heuristic results to csv file.
	 *
	 * @param filename path to output file
	 * @param append true, if you wish to append to existing file; false, otherwise.
	 * @throws IOException exception thrown if error in input-output operation.
	 */
	public void writeToCSV(String filename, boolean append) throws IOException
	{
		File fileObj = new File(filename);
		String[] header = {"Model", "Network", "t_0", "Simulation repetitions", "FN probability",
							"transmissability (p)", "no. of honeypots", "objective value", "honeypots",
							"Wall time (s)", "UTC"};
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
		for (Septet<String, String, Integer, Integer, Double, Double, Integer> key : mapToObjectiveValue.keySet())
		{
			String[] line = new String[11];
			line[0] = key.getValue0();              // Model (TN11C, RAEPC, etc.)
			line[1] = key.getValue1();              // network name
			line[2] = key.getValue2().toString();   // t_0
			line[3] = key.getValue3().toString();   // reps
			line[4] = key.getValue4().toString();   // false negative prob.
			line[5] = key.getValue5().toString();   // transmissability
			line[6] = key.getValue6().toString();   // no. of honeypots
			line[7] = mapToObjectiveValue.get(key).toString();
			line[8] = mapToHoneypots.get(key).toString();
			line[9] = mapToWallTime.get(key).toString();
			line[10] = Instant.now().toString();
			writer.writeNext(line);
		}
		writer.flush();
		writer.close();
		System.out.println("Heuristic results successfully written to \""+filename+"\".");
	}
	
	/**
	 * Overrides {@code toString()}.
	 *
	 * @return returns string representation of values in class.
	 */
	@Override
	public String toString()
	{
		return "nodeInMaxRowsGreedyHeuristic:"
				+"\n\t Objective value:\n\t\t"+mapToObjectiveValue.toString()
				+"\n\t Honeypots:\n\t\t"+mapToHoneypots.toString()
				+"\n\t Wall time (second):\n\t\t"+mapToWallTime.toString();
	}
}
