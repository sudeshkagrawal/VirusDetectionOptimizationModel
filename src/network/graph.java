package network;

import org.jgrapht.Graph;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.util.SupplierUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Supplier;

/**
 * Represents a network graph.
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: September 9, 2020.
 */
public class graph
{
	/**
	 * A network graph.
	 */
	private Graph<Integer, DefaultEdge> g;
	/**
	 * Name of the network.
	 */
	private String networkName;
	
	/**
	 * Constructor to instantiate with just the network name ({@code networkName}).
	 *
	 * @param networkName name of the Network.
	 */
	public graph(String networkName)
	{
		this.g = new DefaultUndirectedGraph<>(DefaultEdge.class);
		this.networkName = networkName;
	}
	
	/**
	 * Constructor to instantiate with a network name and a graph of the network.
	 *
	 * @param g network graph
	 * @param networkName name of the network.
	 */
	public graph(Graph<Integer, DefaultEdge> g, String networkName)
	{
		this.g = g;
		this.networkName = networkName;
	}
	
	/**
	 * Initialize an empty graph g with a complete graph of given size.
	 *
	 * @param size number of vertices (nodes) in the complete graph.
	 * @throws Exception exception thrown if vertex set of {@code g} is not empty.
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
	 * Initialize an empty graph {@code g} with a circulant graph of given size.
	 * A circulant graph is a graph of n (={@code size}) vertices in which the i-th vertex is adjacent to the (i+j)-th and the (i-j)-th vertices for each j in the array offsets.
	 *
	 * @param size number of vertices (nodes) in the circulant graph
	 * @param offsets defines the list of all distances in any edge.
	 * @throws Exception exception thrown if vertex set of {@code g} is not empty.
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
	
	/**
	 * Build network from a text file.
	 * Each line in the text file is an edge, where the vertices are separated by commas.
	 *
	 * @param filename path to file to be read.
	 * @throws Exception exception thrown if vertex set of {@code g} is not empty.
	 */
	public void buildGraphFromFile(String filename) throws Exception
	{
		if (g.vertexSet().size()>0)
		{
			throw new Exception("Graph is not empty!");
		}
		else
		{
			try
			{
				File myObj = new File(filename);
				Scanner myReader = new Scanner(myObj);
				while (myReader.hasNextLine())
				{
					String data = myReader.nextLine();
					if (data.equals(""))
						continue;
					String[] tokens = data.split(",");
					int source = Integer.parseInt(tokens[0].trim());
					int destination = Integer.parseInt(tokens[1].trim());
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
		
	}
	
	/**
	 * Getter for {@code g}.
	 *
	 * @return returns the graph.
	 */
	public Graph<Integer, DefaultEdge> getG()
	{
		return g;
	}
	
	/**
	 * Getter for {@code networkName}.
	 *
	 * @return returns the network name as String.
	 */
	public String getNetworkName()
	{
		return networkName;
	}
	
	/**
	 * Setter for {@code networkName}.
	 *
	 * @param networkName network name as String.
	 */
	public void setNetworkName(String networkName)
	{
		this.networkName = networkName;
	}
	
	/**
	 * Adds a vertex to the {}@code NursingHome}.
	 *
	 * @param v vertex (node) to be added to the network.
	 */
	private void addVertex(Integer v)
	{
		g.addVertex(v);
	}
	
	/**
	 * Adds an edge to the Network
	 *
	 * @param s source of the edge to be added
	 * @param t target (destination) of the edge to be added.
	 */
	private void addEdge(Integer s, Integer t)
	{
		g.addEdge(s, t);
	}
	
	/**
	 * Removes self-loops from the graph.
	 */
	public void removeSelfLoops()
	{
		for (Integer v: g.vertexSet())
			g.removeEdge(v, v);
	}
	
	// TODO: c-core decomposition
	
	/**
	 * Changes the graph to one of its largest components.
	 *
	 * @throws Exception exception thrown if graph {@code g} does not have a vertex.
	 */
	public void changeGraphToLargestConnectedComponent() throws Exception
	{
		if (g.vertexSet().size()<1)
			throw new Exception("Graph is empty!");
		List<Integer> nodes = new ArrayList<>(g.vertexSet());
		List<List<Integer>> connectedComponents = new ArrayList<>();
		// find connected components
		while (nodes.size()>0)
		{
			int startNode = nodes.get(0);
			BreadthFirstIterator iter = new BreadthFirstIterator(g, startNode);
			List<Integer> connectedComponent = new ArrayList<>();
			iter.forEachRemaining(e -> connectedComponent.add((Integer) e));
			nodes.removeAll(connectedComponent);
			connectedComponents.add(connectedComponent);
		}
		// find index of a largest connected component
		int indexOfLargestComponent = 0;
		for (List<Integer> component: connectedComponents)
		{
			if (component.size()>connectedComponents.get(indexOfLargestComponent).size())
				indexOfLargestComponent = connectedComponents.indexOf(component);
		}
		// Remove vertices (and consequently edges) not in the largest connected component
		for (List<Integer> component: connectedComponents)
		{
			if (connectedComponents.indexOf(component)==indexOfLargestComponent)
				continue;
			g.removeAllVertices(component);
		}
	}
	
	/**
	 * Overrides {@code toString()}.
	 *
	 * @return returns a string representation of values in class.
	 */
	@Override
	public String toString()
	{
		return networkName+":g<"+g.vertexSet()+", "+g.edgeSet()+">";
	}
	
	/**
	 * Overrides {@code equals}.
	 *
	 * @param o an object.
	 * @return returns true if the values of all individual fields match; false, otherwise.
	 */
	@Override
	public boolean equals(Object o)
	{
		// this instance check
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		graph graph = (graph) o;
		return g.equals(graph.g) &&
				networkName.equals(graph.networkName);
	}
	
	/**
	 * Overrides {@code hashCode}.
	 * Uses lombok,
	 *
	 * @return returns a integer value representing the hash code for an object.
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(g, networkName);
	}
}
