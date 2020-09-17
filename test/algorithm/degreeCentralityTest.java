package algorithm;

import network.graph;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

/**
 * Unit test for {@code degreeCentrality}.
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: September 17, 2020.
 */
class degreeCentralityTest
{
	
	/**
	 * A test for {@code degreeCentrality.getKHighestDegreeNodes}.
	 * Uses graph in 'testnetwork8.txt' for the test.
	 *
	 * @throws Exception exception thrown if there is error while building a graph from 'testnetwork8.txt'.
	 */
	@Test
	void getKHighestDegreeNodes() throws Exception
	{
		// build graph
		String networkName = "testnetwork8";
		graph network = new graph(networkName);
		String separator = ",";
		network.buildGraphFromFile("./files/networks/"+networkName+".txt", separator);
		
		Map<Integer, Integer> degreesOfNodes = network.getVertexSet().stream()
												.collect(Collectors.toMap(
														node -> node, network::getDegreeOfNode, (a, b) -> b));
		assert degreesOfNodes.get(1) == 1;
		assert degreesOfNodes.get(2) == 5;
		assert degreesOfNodes.get(3) == 4;
		assert degreesOfNodes.get(4) == 2;
		assert degreesOfNodes.get(5) == 2;
		assert degreesOfNodes.get(6) == 5;
		assert degreesOfNodes.get(7) == 1;
		assert degreesOfNodes.get(8) == 1;
		assert degreesOfNodes.get(9) == 1;
		assert degreesOfNodes.get(10) == 1;
		assert degreesOfNodes.get(11) == 1;
		assert degreesOfNodes.get(12) == 1;
		assert degreesOfNodes.get(13) == 1;
		
		degreeCentrality degreeCentralityResults = new degreeCentrality();
		PriorityQueue<Integer> topKDegreeNodes = degreeCentralityResults.getKHighestDegreeNodes(degreesOfNodes, 1);
		assert (topKDegreeNodes.contains(2) || topKDegreeNodes.contains(6));
		topKDegreeNodes = degreeCentralityResults.getKHighestDegreeNodes(degreesOfNodes, 2);
		assert (topKDegreeNodes.contains(2) && topKDegreeNodes.contains(6));
		topKDegreeNodes = degreeCentralityResults.getKHighestDegreeNodes(degreesOfNodes, 3);
		assert topKDegreeNodes.contains(3);
		topKDegreeNodes = degreeCentralityResults.getKHighestDegreeNodes(degreesOfNodes, 4);
		assert (topKDegreeNodes.contains(5) || topKDegreeNodes.contains(4));
		topKDegreeNodes = degreeCentralityResults.getKHighestDegreeNodes(degreesOfNodes, 5);
		assert (topKDegreeNodes.contains(5) && topKDegreeNodes.contains(4));
	}
}