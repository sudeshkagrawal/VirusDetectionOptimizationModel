package network;

import org.javatuples.Pair;
import org.jgrapht.Graphs;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for {@code graph}.
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: September 19, 2020.
 */
class graphTest
{
	
	@org.junit.jupiter.api.Test
	void initializeCompleteGraph() throws Exception
	{
		int size = 4;
		graph network = new graph("CompleteGraph_size"+size);
		network.initializeCompleteGraph(size);
		for (int i=0; i<size; i++)
			for (int j = 0; j < size; j++)
				assert i == j || (network.getG().containsEdge(i, j));
	}
	
	@org.junit.jupiter.api.Test
	void initializeCirculantGraph() throws Exception
	{
		int size = 7;
		int[] offsets = {2, 4};
		graph network = new graph("CirculantGraph_size"+size+"_offsets_"+Arrays.toString(offsets));
		network.initializeCirculantGraph(size, offsets);
		assert network.getG().containsEdge(0, 5);
		assert network.getG().containsEdge(0, 2);
		assert network.getG().containsEdge(0, 3);
		assert network.getG().containsEdge(0, 4);
		assert !network.getG().containsEdge(0, 0);
		assert !network.getG().containsEdge(0, 1);
		assert !network.getG().containsEdge(0, 6);
		
		assert network.getG().containsEdge(1, 6);
		assert network.getG().containsEdge(1, 3);
		assert network.getG().containsEdge(1, 4);
		assert network.getG().containsEdge(1, 5);
		assert !network.getG().containsEdge(1, 1);
		assert !network.getG().containsEdge(1, 2);
		
		assert network.getG().containsEdge(2, 4);
		assert network.getG().containsEdge(2, 5);
		assert network.getG().containsEdge(2, 6);
		assert !network.getG().containsEdge(2, 2);
		assert !network.getG().containsEdge(2, 3);
		
		assert network.getG().containsEdge(3, 5);
		assert network.getG().containsEdge(3, 6);
		assert !network.getG().containsEdge(3, 3);
		assert !network.getG().containsEdge(3, 4);
		
		assert network.getG().containsEdge(4, 6);
		assert !network.getG().containsEdge(4, 4);
		assert !network.getG().containsEdge(4, 5);
		
		assert !network.getG().containsEdge(5, 5);
		assert !network.getG().containsEdge(5, 6);
		
		assert !network.getG().containsEdge(6, 6);
	}
	
	@org.junit.jupiter.api.Test
	void buildGraphFromFile() throws Exception
	{
		graph network = new graph("testNetwork");
		String separator = ",";
		network.buildGraphFromFile("./test/resources/networks/testNetworkFile.txt", separator);
		
		// check vertex set
		assert network.getG().vertexSet().size()==7;
		for (int i=1; i<=7; i++)
			assert network.getG().containsVertex(i);
		
		// check edge set
		assert network.getG().edgeSet().size()==6;
		assert network.getG().containsEdge(1, 2);
		assert network.getG().containsEdge(2, 3);
		assert network.getG().containsEdge(3, 4);
		assert network.getG().containsEdge(4, 5);
		assert network.getG().containsEdge(5, 6);
		assert network.getG().containsEdge(6, 7);
	}
	
	@Test
	void setNetworkName() throws Exception
	{
		String networkName = "testnetwork1";
		graph network = new graph(networkName);
		String separator = ",";
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		assert network.getNetworkName().equals("testnetwork1");
		network.setNetworkName("randomname");
		assert network.getNetworkName().equals("randomname");
	}
	
	@Test
	void changeGraphToLargestConnectedComponent()
	{
	}
	
	@Test
	void dokCoreDecomposition()
	{
	}
	
	@Test
	void getkCore()
	{
	}
	
	@Test
	void testEquals()
	{
	}
	
	@Test
	void testHashCode()
	{
	}
	
	@Test
	void removeSelfLoops() throws Exception
	{
		String networkName = "testnetwork8";
		graph network = new graph(networkName);
		String separator = ",";
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		assert !network.hasSelfLoops();
		network.removeSelfLoops();
		assert !network.hasSelfLoops();
		
		networkName = "testnetwork10_selfLoop";
		network = new graph(networkName);
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		assert network.hasSelfLoops();
		network.removeSelfLoops();
		assert !network.hasSelfLoops();
	}
	
	@Test
	void hasSelfLoops() throws Exception
	{
		String networkName = "testnetwork8";
		graph network = new graph(networkName);
		String separator = ",";
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		assert !network.hasSelfLoops();
		
		networkName = "testnetwork10_selfLoop";
		network = new graph(networkName);
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		assert network.hasSelfLoops();
	}
	
	@Test
	void getDegreeOfNode() throws Exception
	{
		String networkName = "testnetwork8";
		graph network = new graph(networkName);
		String separator = ",";
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		assert network.getDegreeOfNode(1)==1;
		assert network.getDegreeOfNode(2)==5;
		assert network.getDegreeOfNode(3)==4;
		assert network.getDegreeOfNode(4)==2;
		assert network.getDegreeOfNode(5)==2;
		assert network.getDegreeOfNode(6)==5;
		assert network.getDegreeOfNode(7)==1;
		assert network.getDegreeOfNode(8)==1;
		assert network.getDegreeOfNode(9)==1;
		assert network.getDegreeOfNode(10)==1;
		assert network.getDegreeOfNode(11)==1;
		assert network.getDegreeOfNode(12)==1;
		assert network.getDegreeOfNode(13)==1;
	}
	
	@Test
	void getDegrees() throws Exception
	{
		String networkName = "testnetwork8";
		graph network = new graph(networkName);
		String separator = ",";
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		Map<Integer, Integer> degrees = network.getDegrees();
		assert degrees.get(1)==1;
		assert degrees.get(2)==5;
		assert degrees.get(3)==4;
		assert degrees.get(4)==2;
		assert degrees.get(5)==2;
		assert degrees.get(6)==5;
		assert degrees.get(7)==1;
		assert degrees.get(8)==1;
		assert degrees.get(9)==1;
		assert degrees.get(10)==1;
		assert degrees.get(11)==1;
		assert degrees.get(12)==1;
		assert degrees.get(13)==1;
	}
	
	@Test
	void findAverageDegreeOfNodes() throws Exception
	{
		// Test 1
		String networkName = "testnetwork8";
		graph network = new graph(networkName);
		String separator = ",";
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		double avgDegree = network.findAverageDegreeOfNodes();
		assert Math.abs(avgDegree-2)<0.00000001;
		
		// Test 2
		networkName = "testnetwork6withUnconnectedComponents";
		network = new graph(networkName);
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		avgDegree = network.findAverageDegreeOfNodes();
		assert Math.abs(avgDegree-(28.0/12.0))<0.00000001;
	}
	@Test
	void findMaxDegreeOfNodes() throws Exception
	{
		// Test 1
		String networkName = "testnetwork8";
		graph network = new graph(networkName);
		String separator = ",";
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		int maxDegree = network.findMaxDegreeOfNodes();
		assert maxDegree==5;
		
		// Test 2
		networkName = "testnetwork6withUnconnectedComponents";
		network = new graph(networkName);
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		maxDegree = network.findMaxDegreeOfNodes();
		assert maxDegree==4;
	}
	
	@Test
	void getVertexSet() throws Exception
	{
		// Test 1
		String networkName = "testnetwork8";
		graph network = new graph(networkName);
		String separator = ",";
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		Set<Integer> nodes = network.getVertexSet();
		for (int i=1; i<=13; i++)
			assert nodes.contains(i);
		assert nodes.size()==13;
		
		// Test 2
		networkName = "testnetwork6withUnconnectedComponents";
		network = new graph(networkName);
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		nodes = network.getVertexSet();
		for (int i=1; i<=12; i++)
			assert nodes.contains(i);
		assert nodes.size()==12;
		
		// Test 3
		networkName = "testnetwork9";
		network = new graph(networkName);
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		nodes = network.getVertexSet();
		assert nodes.contains(1);
		assert !nodes.contains(2);
		assert nodes.contains(3);
		for (int i=4; i<=9; i++)
			assert nodes.contains(i);
		assert !nodes.contains(10);
		assert nodes.contains(11);
		assert !nodes.contains(12);
		assert nodes.contains(13);
		
		assert nodes.size()==11;
	}
	
	@Test
	void getEdgeSet()
	{
	
	}
	
	@Test
	void checkEdgeSet() throws Exception
	{
		String networkName = "testnetwork6withUnconnectedComponents";
		graph network = new graph(networkName);
		String separator = ",";
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		
		Set<Integer> nodes = network.getVertexSet();
		Map<Integer, List<Integer>> neighbors = nodes.stream()
				.collect(Collectors
						.toMap(v -> v, v -> Graphs.neighborListOf(network.getG(), v), (a, b) -> b));
		assert neighbors.get(1).contains(2);
		assert neighbors.get(1).contains(4);
		assert neighbors.get(2).contains(3);
		assert neighbors.get(2).contains(4);
		assert neighbors.get(3).contains(4);
		assert neighbors.get(5).contains(6);
		assert neighbors.get(5).contains(7);
		assert neighbors.get(6).contains(7);
		assert neighbors.get(8).contains(9);
		assert neighbors.get(8).contains(10);
		assert neighbors.get(9).contains(10);
		assert neighbors.get(10).contains(11);
		assert neighbors.get(10).contains(12);
		assert neighbors.get(11).contains(12);
		
		assert neighbors.get(1).size()==2;
		assert neighbors.get(2).size()==3;
		assert neighbors.get(3).size()==2;
		assert neighbors.get(4).size()==3;
		assert neighbors.get(5).size()==2;
		assert neighbors.get(6).size()==2;
		assert neighbors.get(7).size()==2;
		assert neighbors.get(8).size()==2;
		assert neighbors.get(9).size()==2;
		assert neighbors.get(10).size()==4;
		assert neighbors.get(11).size()==2;
		assert neighbors.get(12).size()==2;
	}
	
	@Test
	void getNeighbors() throws Exception
	{
		String networkName = "testnetwork1";
		graph network = new graph(networkName);
		String separator = ",";
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		Map<Integer, List<Integer>> neighbors = network.getNeighbors();
		assert neighbors.get(1).contains(2);
		assert neighbors.get(1).contains(3);
		assert neighbors.get(1).contains(4);
		assert neighbors.get(2).contains(5);
		assert neighbors.get(3).contains(6);
		assert neighbors.get(4).contains(5);
		assert neighbors.get(4).contains(6);
		assert neighbors.get(5).contains(6);
		assert neighbors.get(5).contains(7);
		assert neighbors.get(6).contains(7);
		
		assert neighbors.get(1).size()==3;
		assert neighbors.get(2).size()==2;
		assert neighbors.get(3).size()==2;
		assert neighbors.get(4).size()==3;
		assert neighbors.get(5).size()==4;
		assert neighbors.get(6).size()==4;
		assert neighbors.get(7).size()==2;
	}
	
	@Test
	void getNeighborOfNode() throws Exception
	{
		String networkName = "testnetwork1";
		graph network = new graph(networkName);
		String separator = ",";
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		assert network.getNeighborOfNode(1).contains(2);
		assert network.getNeighborOfNode(1).contains(4);
		assert network.getNeighborOfNode(1).size()==3;
		assert network.getNeighborOfNode(4).contains(5);
		assert network.getNeighborOfNode(4).contains(6);
		assert network.getNeighborOfNode(4).size()==3;
		assert network.getNeighborOfNode(5).contains(2);
		assert network.getNeighborOfNode(5).contains(4);
		assert network.getNeighborOfNode(5).size()==4;
	}
	
	@Test
	void findDistancesBetweenNodes() throws Exception
	{
		// TEST 1
		String networkName = "testnetwork1";
		graph network = new graph(networkName);
		String separator = ",";
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		Map<Pair<Integer, Integer>, Double> distances = network.findDistancesBetweenNodes();
		double tol = 0.00000001;
		assert Math.abs(distances.get(new Pair<>(1, 2))-1)<tol;
		assert Math.abs(distances.get(new Pair<>(1, 3))-1)<tol;
		assert Math.abs(distances.get(new Pair<>(1, 4))-1)<tol;
		assert Math.abs(distances.get(new Pair<>(1, 5))-2)<tol;
		assert Math.abs(distances.get(new Pair<>(1, 6))-2)<tol;
		assert Math.abs(distances.get(new Pair<>(1, 7))-3)<tol;
		assert Math.abs(distances.get(new Pair<>(2, 3))-2)<tol;
		assert Math.abs(distances.get(new Pair<>(2, 4))-2)<tol;
		assert Math.abs(distances.get(new Pair<>(2, 5))-1)<tol;
		assert Math.abs(distances.get(new Pair<>(2, 6))-2)<tol;
		assert Math.abs(distances.get(new Pair<>(2, 7))-2)<tol;
		assert Math.abs(distances.get(new Pair<>(3, 4))-2)<tol;
		assert Math.abs(distances.get(new Pair<>(3, 5))-2)<tol;
		assert Math.abs(distances.get(new Pair<>(3, 6))-1)<tol;
		assert Math.abs(distances.get(new Pair<>(3, 7))-2)<tol;
		assert Math.abs(distances.get(new Pair<>(4, 5))-1)<tol;
		assert Math.abs(distances.get(new Pair<>(4, 6))-1)<tol;
		assert Math.abs(distances.get(new Pair<>(4, 7))-2)<tol;
		assert Math.abs(distances.get(new Pair<>(5, 6))-1)<tol;
		assert Math.abs(distances.get(new Pair<>(5, 7))-1)<tol;
		assert Math.abs(distances.get(new Pair<>(6, 7))-1)<tol;
		
		// TEST 2
		networkName = "testnetwork6withUnconnectedComponents";
		network = new graph(networkName);
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		distances = network.findDistancesBetweenNodes();
		assert Double.isInfinite(distances.get(new Pair<>(7, 8)));
		assert Double.isInfinite(distances.get(new Pair<>(6, 4)));
		assert Double.isInfinite(distances.get(new Pair<>(1, 11)));
	}
	
	@Test
	void findDistanceBetweenNodes() throws Exception
	{
		// TEST 1
		String networkName = "testnetwork1";
		graph network = new graph(networkName);
		String separator = ",";
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		double tol = 0.00000001;
		assert Math.abs(network.findDistanceBetweenNodes(1, 2)-1)<tol;
		assert Math.abs(network.findDistanceBetweenNodes(1, 6)-2)<tol;
		assert Math.abs(network.findDistanceBetweenNodes(1, 7)-3)<tol;
		assert Math.abs(network.findDistanceBetweenNodes(2, 3)-2)<tol;
		
		// TEST 2
		networkName = "testnetwork6withUnconnectedComponents";
		network = new graph(networkName);
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		assert Math.abs(network.findDistanceBetweenNodes(1, 3)-2)<tol;
		assert Math.abs(network.findDistanceBetweenNodes(8, 12)-2)<tol;
		assert Double.isInfinite(network.findDistanceBetweenNodes(7, 8));
		assert Double.isInfinite(network.findDistanceBetweenNodes(6, 4));
		assert Double.isInfinite(network.findDistanceBetweenNodes(1, 11));
	}
	
	@Test
	void findMaxDistanceBetweenNodes() throws Exception
	{
		// TEST 1
		String networkName = "testnetwork1";
		graph network = new graph(networkName);
		String separator = ",";
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		double tol = 0.00000001;
		assert Math.abs(network.findMaxDistanceBetweenNodes()-3)<tol;
		
		// TEST 2
		networkName = "testnetwork6withUnconnectedComponents";
		network = new graph(networkName);
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		assert Double.isInfinite(network.findMaxDistanceBetweenNodes());
	}
	
	@Test
	void findAverageDistanceBetweenNodes() throws Exception
	{
		// TEST 1
		String networkName = "testnetwork1";
		graph network = new graph(networkName);
		String separator = ",";
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		double tol = 0.00000001;
		assert Math.abs(network.findAverageDistanceBetweenNodes()-(33.0/21.0))<tol;
		
		// TEST 2
		networkName = "testnetwork6withUnconnectedComponents";
		network = new graph(networkName);
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		assert Double.isInfinite(network.findAverageDistanceBetweenNodes());
	}
	
	@Test
	void findMinimumNodeLabel() throws Exception
	{
		// TEST 1
		String networkName = "testnetwork11";
		graph network = new graph(networkName);
		String separator = ",";
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		assert network.findMinimumNodeLabel()==3;
		
		// TEST 2
		networkName = "testnetwork12";
		network = new graph(networkName);
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		assert network.findMinimumNodeLabel()==-3;
		
		// TEST 3
		networkName = "testnetwork13";
		network = new graph(networkName);
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		assert network.findMinimumNodeLabel()==-0;
		
		// TEST 4
		networkName = "testnetwork12";
		network = new graph(networkName);
		network.buildGraphFromFile("./test/resources/networks/"+networkName+".txt", separator);
		network.removeAllVertices(new HashSet<>(network.getVertexSet()));
		//System.out.println(network.findMinimumNodeLabel());
		graph finalNetwork = network;
		Exception exception = assertThrows(NoSuchElementException.class,
				finalNetwork::findMinimumNodeLabel);
		String actualMessage = exception.getMessage();
		assertSame(null, actualMessage);
	}
}