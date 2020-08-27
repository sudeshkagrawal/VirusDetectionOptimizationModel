package network;

class graphTest
{
	
	@org.junit.jupiter.api.Test
	void initializeCompleteGraph() throws Exception
	{
		int size = 4;
		graph network = new graph();
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
		graph network = new graph();
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
		graph network = new graph();
		network.buildGraphFromFile("./files/networks/testNetworkFile.txt");
		
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
}