package heuristic;

import network.graph;
import org.javatuples.Quintet;
import org.javatuples.Sextet;
import org.jgrapht.alg.util.Triple;
import simulation.simulationRuns;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Sudesh Agrawal (sudesh@utexas.edu)
 * Last Updated: Aug 29, 2020.
 * Class for heuristics.
 */
public class nodeInMaxRowsGreedyHeuristic
{
	// Model (TN11C, RAEPC, etc.); Network name; t_0; repetitions; false negative probability; number of honeypots
	Map<Sextet<String, String, Integer, Integer, Double, Integer>, Double> mapToObjectiveValue;
	Map<Sextet<String, String, Integer, Integer, Double, Integer>, List<Integer>> mapToHoneypots;
	Map<Sextet<String, String, Integer, Integer, Double, Integer>, Double> mapToWallTime;
	
	public nodeInMaxRowsGreedyHeuristic()
	{
		this(new HashMap<>(), new HashMap<>(), new HashMap<>());
	}
	
	public nodeInMaxRowsGreedyHeuristic(Map<Sextet<String, String, Integer, Integer, Double, Integer>, Double> mapToObjectiveValue,
	                                    Map<Sextet<String, String, Integer, Integer, Double, Integer>, List<Integer>> mapToHoneypots,
	                                    Map<Sextet<String, String, Integer, Integer, Double, Integer>, Double> mapToWallTime)
	{
		this.mapToObjectiveValue = mapToObjectiveValue;
		this.mapToHoneypots = mapToHoneypots;
		this.mapToWallTime = mapToWallTime;
	}
	
	public Map<Sextet<String, String, Integer, Integer, Double, Integer>, Double> getMapToObjectiveValue()
	{
		return mapToObjectiveValue;
	}
	
	public Map<Sextet<String, String, Integer, Integer, Double, Integer>, List<Integer>> getMapToHoneypots()
	{
		return mapToHoneypots;
	}
	
	public Map<Sextet<String, String, Integer, Integer, Double, Integer>, Double> getMapToWallTime()
	{
		return mapToWallTime;
	}
	
	/**
	 *
	 * @param modelName
	 * @param g
	 * @param simulationResults
	 * @param k_t0_runs
	 * @param r
	 * @throws Exception
	 */
	public void runSAAUsingHeuristic(String modelName, graph g, simulationRuns simulationResults, List<Triple<Integer, Integer, Integer>> k_t0_runs, double r) throws Exception
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
			Quintet<String, String, Integer, Integer, Double> key = new Quintet<>(modelName, g.getNetworkName(), t_0, run, r);
			Sextet<String, String, Integer, Integer, Double, Integer> fullKey = new Sextet<>(modelName, g.getNetworkName(), t_0, run, r, k);
			
			System.out.println("Using greedy heuristic (false negative prob = "+r+") for "+k+" honeypots and "+run+" samples...");
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
					List<List<Integer>> newVirusSpreadSamples = virusSpreadSamples.stream().map(virusSpreadSample -> virusSpreadSample.stream()
																.map(integer -> integer + 1)
																.collect(Collectors.toCollection(() -> new ArrayList<>(t_0 + 1))))
																.collect(Collectors.toCollection(() -> new ArrayList<>(run)));
					successfulDetectMatrix = elementwiseMultiplyMatrix(Collections.unmodifiableList(newVirusSpreadSamples),
																		Collections.unmodifiableList(virtualDetectionSamples));
					candidates = g.getG().vertexSet().stream().map(e -> e+1).collect(Collectors.toSet());
				}
				else
				{
					successfulDetectMatrix = elementwiseMultiplyMatrix(Collections.unmodifiableList(virusSpreadSamples),
																		Collections.unmodifiableList(virtualDetectionSamples));
					candidates = g.getG().vertexSet();
				}
			}
			else
			{
				successfulDetectMatrix = new ArrayList<>(virusSpreadSamples);
				candidates = g.getG().vertexSet();
			}
			// System.out.println("Successful detection matrix: \n"+successfulDetectMatrix.toString()+"\n---------------------------");
			// System.out.println("Candidate nodes: \n"+candidates.toString()+"\n---------------------------");
			
			List<Integer> honeypots = new ArrayList<>();
			int numberOfHoneypotsFound = 0;
			Set<Integer> indicesOfSamplesToBeConsidered = IntStream.range(0, run).boxed().collect(Collectors.toSet());
			Set<Integer> indicesOfSamplesToBeRemoved = new HashSet<>();
			Set<Integer> indicesOfSamplesCovered = new HashSet<>(indicesOfSamplesToBeConsidered);
			
			Instant tic = Instant.now();
			while (numberOfHoneypotsFound<k)
			{
				List<List<Integer>> samplesToBeConsidered = IntStream.range(0, successfulDetectMatrix.size())
						.filter(indicesOfSamplesToBeConsidered::contains).mapToObj(successfulDetectMatrix::get).collect(Collectors.toList());
				int currentCandidate = findMaxRowFrequencyNode(Collections.unmodifiableList(samplesToBeConsidered), List.copyOf(candidates));
				honeypots.add(currentCandidate);
				numberOfHoneypotsFound++;
				candidates.remove(currentCandidate);
				indicesOfSamplesToBeRemoved = new HashSet<>(findRowOccurrenceIndices(Collections.unmodifiableList(successfulDetectMatrix), currentCandidate));
				indicesOfSamplesToBeConsidered.removeAll(indicesOfSamplesToBeRemoved);
			}
			Instant toc = Instant.now();
			if ((r>0) && (zeroNode))
				honeypots = honeypots.stream().map(e -> e - 1).collect(Collectors.toList());
			System.out.println("Honeypots: \n"+honeypots.toString()+"\n---------------------------");
			indicesOfSamplesCovered.removeAll(indicesOfSamplesToBeConsidered);
			double objectiveValue = indicesOfSamplesCovered.size()*1.0/run;
			System.out.println("Objective value = "+objectiveValue);
			double wallTimeinSeconds = 1.0*Duration.between(tic, toc).toMillis()/1000;
			System.out.println("Wall time (second) = "+wallTimeinSeconds);
			
			mapToObjectiveValue.put(fullKey, objectiveValue);
			mapToHoneypots.put(fullKey, honeypots);
			mapToWallTime.put(fullKey, wallTimeinSeconds);
		}
		
		
	}
	
	private List<List<Integer>> elementwiseMultiplyMatrix(List<List<Integer>> a, List<List<Integer>> b) throws Exception
	{
		List<List<Integer>> output = new ArrayList<>(a.size());
		if (a.size()!=b.size())
			throw new Exception("Inputs are not of the same size!");
			
		for (int i=0; i<a.size(); i++)
		{
			if (a.get(i).size() != b.get(i).size())
				throw new Exception("Inputs are not of the same size for index "+i+"!");
			List<Integer> colList = new ArrayList<>(a.get(i).size());
			for (int j=0; j<a.get(0).size(); j++)
				colList.add(a.get(i).get(j) * b.get(i).get(j));
			output.add(colList);
		}
		return output;
	}
	
	private int findMaxRowFrequencyNodeOld(List<List<Integer>> arr, List<Integer> nodes)
	{
		int maxNode = nodes.get(0);
		int maxNodeFrequency = 0;
		for (Integer node : nodes)
		{
			int count;
			int currentNode = node;
			count = (int) IntStream.range(0, arr.size()).parallel()
					.filter(i -> IntStream.range(0, arr.get(i).size()).anyMatch(j -> arr.get(i).get(j) == currentNode)).count();
			if (count > maxNodeFrequency)
			{
				maxNode = currentNode;
				maxNodeFrequency = count;
			}
		}
		return maxNode;
	}
	
	private int findMaxRowFrequencyNode(List<List<Integer>> arr, List<Integer> nodes)
	{
		Map<Integer, Integer> rowCount = nodes.stream().collect(Collectors.toMap(node -> node, node -> 0, (a, b) -> b, () -> new HashMap<>(nodes.size())));
		for (List<Integer> row : arr)
			for (int key : row)
				if (rowCount.containsKey(key))
					rowCount.put(key, rowCount.get(key) + 1);
		return rowCount.entrySet().stream().max((e1, e2) -> e1.getValue()-e2.getValue()).get().getKey();
	}
	
	private List<Integer> findRowOccurrenceIndices(List<List<Integer>> arr, int node)
	{
		List<Integer> nodeIndices = IntStream.range(0, arr.size())
				.filter(i -> IntStream.range(0, arr.get(i).size()).anyMatch(j -> arr.get(i).get(j) == node)).boxed().collect(Collectors.toList());
		return nodeIndices;
	}
}
