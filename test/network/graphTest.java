package network;

import org.jgrapht.Graphs;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Unit test for {@code graph}.
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: September 17, 2020.
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
		network.buildGraphFromFile("./files/networks/testNetworkFile.txt", separator);
		
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
		network.buildGraphFromFile("./files/networks/"+networkName+".txt", separator);
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
		network.buildGraphFromFile("./files/networks/"+networkName+".txt", separator);
		assert !network.hasSelfLoops();
		network.removeSelfLoops();
		assert !network.hasSelfLoops();
		
		networkName = "testnetwork10_selfLoop";
		network = new graph(networkName);
		network.buildGraphFromFile("./files/networks/"+networkName+".txt", separator);
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
		network.buildGraphFromFile("./files/networks/"+networkName+".txt", separator);
		assert !network.hasSelfLoops();
		
		networkName = "testnetwork10_selfLoop";
		network = new graph(networkName);
		network.buildGraphFromFile("./files/networks/"+networkName+".txt", separator);
		assert network.hasSelfLoops();
	}
	
	@Test
	void getDegreeOfNode() throws Exception
	{
		String networkName = "testnetwork8";
		graph network = new graph(networkName);
		String separator = ",";
		network.buildGraphFromFile("./files/networks/"+networkName+".txt", separator);
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
	void findAverageDegreeOfNodes() throws Exception
	{
		// Test 1
		String networkName = "testnetwork8";
		graph network = new graph(networkName);
		String separator = ",";
		network.buildGraphFromFile("./files/networks/"+networkName+".txt", separator);
		double avgDegree = network.findAverageDegreeOfNodes();
		assert Math.abs(avgDegree-2)<0.00000001;
		
		
		// Test 2
		networkName = "testnetwork6withUnconnectedComponents";
		network = new graph(networkName);
		network.buildGraphFromFile("./files/networks/"+networkName+".txt", separator);
		avgDegree = network.findAverageDegreeOfNodes();
		assert Math.abs(avgDegree-(28.0/12.0))<0.00000001;
	}
	
	@Test
	void getVertexSet() throws Exception
	{
		// Test 1
		String networkName = "testnetwork8";
		graph network = new graph(networkName);
		String separator = ",";
		network.buildGraphFromFile("./files/networks/"+networkName+".txt", separator);
		Set<Integer> nodes = network.getVertexSet();
		for (int i=1; i<=13; i++)
			assert nodes.contains(i);
		assert nodes.size()==13;
		
		// Test 2
		networkName = "testnetwork6withUnconnectedComponents";
		network = new graph(networkName);
		network.buildGraphFromFile("./files/networks/"+networkName+".txt", separator);
		nodes = network.getVertexSet();
		for (int i=1; i<=12; i++)
			assert nodes.contains(i);
		assert nodes.size()==12;
		
		// Test 3
		networkName = "testnetwork9";
		network = new graph(networkName);
		network.buildGraphFromFile("./files/networks/"+networkName+".txt", separator);
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
		network.buildGraphFromFile("./files/networks/"+networkName+".txt", separator);
		
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
}