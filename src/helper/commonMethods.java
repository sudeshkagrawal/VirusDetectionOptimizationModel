package helper;

import network.graph;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Contains methods required across several classes.
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: September 23, 2020.
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
	public static List<List<Integer>> elementwiseMultiplyMatrix(List<List<Integer>> a,
	                                                            List<List<Integer>> b) throws Exception
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
		PriorityQueue<Integer> topKNodes = getKHighestDegreeNodes(frequency, k);
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
	
	/**
	 * Returns the k highest degree nodes.
	 *
	 * @param degreesOfNodes map from node to its degree in the graph
	 * @param k number of top degree nodes required.
	 * @return the k highest degree nodes.
	 */
	public static PriorityQueue<Integer> getKHighestDegreeNodes(Map<Integer, Integer> degreesOfNodes, int k)
	{
		PriorityQueue<Integer> topKDegreeNodes = new PriorityQueue<>(k, Comparator.comparingInt(degreesOfNodes::get));
		for (Integer key : degreesOfNodes.keySet())
		{
			if (topKDegreeNodes.size() < k)
				topKDegreeNodes.add(key);
			else
			{
				if (degreesOfNodes.get(topKDegreeNodes.peek()) < degreesOfNodes.get(key))
				{
					topKDegreeNodes.poll();
					topKDegreeNodes.add(key);
				}
			}
		}
		return topKDegreeNodes;
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
	public static Map<String, Integer> getContingencyTable(List<List<Integer>> arr,
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
