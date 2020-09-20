package helper;

import network.graph;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Contains methods required across several classes.
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: September 19, 2020.
 */
public class commonMethods
{
	/**
	 * Element-wise multiplication of two list of lists.
	 * Only integer lists allowed.
	 *
	 * @param a the first list of lists
	 * @param b the second list of lists.
	 * @return a list of lists.
	 * @throws Exception thrown if outer lists {@code a} and {@code b} not of same size.
	 *          Corresponding inner lists should also be of same size,
	 *          but that exception is not thrown since the check would be expensive.
	 */
	public static List<List<Integer>> elementwiseMultiplyMatrix(List<List<Integer>> a, List<List<Integer>> b) throws Exception
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
	 * Finds a node in {@code nodes} which is present in the most rows of {@code arr}.
	 *
	 * @param arr a list of lists
	 * @param nodes a list of integers (nodes).
	 * @return a node as {@code int}.
	 */
	public static int findMaxRowFrequencyNode(List<List<Integer>> arr, List<Integer> nodes)
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
	 * Slower and older version of {@code findMaxRowFrequencyNode}.
	 *
	 * @param arr a list of lists
	 * @param nodes a list of integers (nodes).
	 * @return a node as {@code int}.
	 */
	@Deprecated
	public static int findMaxRowFrequencyNodeOld(List<List<Integer>> arr, List<Integer> nodes)
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
	 * Finds indices of rows where {@code node} occurs in {@code arr}.
	 *
	 * @param arr a list of lists
	 * @param node a list of integers (nodes).
	 * @return a list of indices.
	 */
	public static List<Integer> findRowOccurrenceIndices(List<List<Integer>> arr, int node)
	{
		return IntStream.range(0, arr.size())
				.filter(i -> IntStream.range(0, arr.get(i).size())
						.anyMatch(j -> arr.get(i).get(j) == node)).boxed().collect(Collectors.toList());
	}
	
	/**
	 * Calculates the sum of the marginal function values for the top {@code k} nodes
	 * that fail to be in the solution.
	 * Let us call it the delta value.
	 * Refer section 2.4.1 in
	 * Lee, Jinho. Stochastic optimization models for rapid detection of viruses in cellphone networks. Diss. 2012.
	 *
	 * @param g network graph
	 * @param simulationResults sample paths of a simulation
	 * @param honeypots list of nodes in the solution
	 * @param honeypotsFrequency number of sample paths covered by the honeypots in the solution.
	 * @return the delta value.
	 */
	public static double calculateDelta(graph g, List<List<Integer>> simulationResults, List<Integer> honeypots,
	                                    int honeypotsFrequency)
	{
		int k = honeypots.size();
		
		// failedVertices: vertices not in honeypot
		Set<Integer> failedVertices = new HashSet<>(g.getG().vertexSet());
		failedVertices.removeAll(honeypots);
		//System.out.println("Failed nodes: "+failedVertices.toString());
		
		// find rows not covered by honeypots in the heuristic solution
		List<List<Integer>> uncoveredSamplePaths = simulationResults.stream().filter(samplePath -> samplePath.stream()
				.noneMatch(honeypots::contains)).map(ArrayList::new)
				.collect(Collectors
						.toCollection(() -> new ArrayList<>(simulationResults.size())));
		//System.out.println("Uncovered sample paths: "+uncoveredSamplePaths.toString());
		
		// find frequency of failedVertices in uncoveredSamplePaths
		Map<Integer, Integer> frequency = new HashMap<>();
		failedVertices.forEach(node -> uncoveredSamplePaths.stream()
				.filter(samplePath -> samplePath.contains(node))
				.forEach(samplePath -> frequency.put(node, frequency.getOrDefault(node, 0) + 1)));
		//System.out.println("Frequency: "+frequency.toString());
		
		// choose top k nodes based on their frequency
		PriorityQueue<Integer> topKNodes = new PriorityQueue<>(k, (o1, o2) -> Integer.compare(frequency.get(o1),
				frequency.get(o2)));
		for (Integer key: frequency.keySet())
		{
			//System.out.println("\t Key "+key);
			if (topKNodes.size() < k)
				topKNodes.add(key);
			else
			{
				if (frequency.get(topKNodes.peek()) < frequency.get(key))
				{
					topKNodes.poll();
					topKNodes.add(key);
				}
			}
			//System.out.println("\t Top nodes: "+topKNodes.toString());
		}
		//System.out.println("Top k nodes: "+topKNodes.toString());
		// find delta for the top k nodes
		//Map<Integer, Double> deltaFunction = new HashMap<>();
		double commonDenominator = 1.0/simulationResults.size();
		double objectiveValue = commonDenominator* honeypotsFrequency;
		double output = 0.0;
		for (Integer node: topKNodes)
		{
			double value = commonDenominator * (honeypotsFrequency +frequency.get(node));
			value -= objectiveValue;
			//deltaFunction.put(node, value);
			output += value;
		}
		return output;
	}
}
