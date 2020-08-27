package network;

import org.jgrapht.Graph;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.util.SupplierUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.function.Supplier;

/**
 * @author Sudesh Agrawal
 * Last Updated: Aug 26, 2020
 */
public class graph
{
	private Graph<Integer, DefaultEdge> g;
	
	public graph()
	{
		this.g = new DefaultUndirectedGraph<>(DefaultEdge.class);
	}
	
	/**
	 * Initialize an empty graph g with a complete graph of given size.
	 * @param size number of vertices (nodes) in the complete graph
	 * @throws Exception exception thrown if vertex set of g is not empty
	 */
	public void initializeCompleteGraph(int size) throws Exception
	{
		if ((!g.vertexSet().isEmpty()) && (g.vertexSet().size()>0))
			throw new Exception("Graph is not empty!");
		else
		{
			Supplier<Integer> vSupplier = new Supplier<>()
			{
				private int id = 0;
				
				@Override
				public Integer get()
				{
					return id++;
				}
			};
			this.g = new SimpleGraph<>(vSupplier, SupplierUtil.createDefaultEdgeSupplier(), false);
			CompleteGraphGenerator<Integer, DefaultEdge> completeGraphGenerator = new CompleteGraphGenerator<>(size);
			completeGraphGenerator.generateGraph(this.g);
		}
	}
	
	/**
	 * Initialize an empty graph g with a circulant graph of given size.
	 * A circulant graph is a graph of n (=size) vertices in which the i-th vertex is adjacent to the (i+j)-th and the (i-j)-th vertices for each j in the array offsets.
	 *
	 * @param size number of vertices (nodes) in the circulant graph
	 * @param offsets defines the list of all distances in any edge
	 * @throws Exception exception thrown if vertex set of g is not empty
	 */
	public void initializeCirculantGraph(int size, int[] offsets) throws Exception
	{
		if (g.vertexSet().size()>0)
			throw new Exception("Graph is not empty!");
		else
		{
			// add vertices
			for (int i=0; i<size; i++)
				g.addVertex(i);
			// add edges
			for (int i=0; i<size; i++)
				for (int offset : offsets)
				{
					g.addEdge(i, i - offset >= 0 ? (i - offset) % size : (i - offset + size));
					g.addEdge(i, (i + offset) % size);
				}
		}
	}
	
	public void buildGraphFromFile(String filename)
	{
		String data;
		try
		{
			File myObj = new File(filename);
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine())
			{
				data = myReader.nextLine();
				if (data.equals(""))
					continue;
				String[] tokens = data.split(",");
				int source = Integer.parseInt(tokens[0]);
				int destination = Integer.parseInt(tokens[1]);
				g.addVertex(source);
				g.addVertex(destination);
				g.addEdge(source, destination);
				
			}
		}
		catch (FileNotFoundException e)
		{
			System.out.println("An error occurred while trying to read the file \""+filename+"\":");
			e.printStackTrace();
		}
	}
	
	public Graph<Integer, DefaultEdge> getG()
	{
		return g;
	}
	
	/**
	 * Adds a vertex to the @NursingHome
	 * @param v vertex (node) to be added to the network
	 */
	private void addVertex(Integer v)
	{
		g.addVertex(v);
	}
	
	/**
	 * Adds an edge to the Network
	 * @param s source of the edge to be added
	 * @param t target (destination) of the edge to be added
	 */
	private void addEdge(Integer s, Integer t)
	{
		g.addEdge(s, t);
	}
	
	@Override
	public String toString()
	{
		return "graph:g<"+g.vertexSet()+", "+g.edgeSet()+">";
	}
}
