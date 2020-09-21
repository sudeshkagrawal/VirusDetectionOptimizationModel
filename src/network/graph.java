package network;

import com.opencsv.CSVWriter;
import org.javatuples.Pair;
import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.Graphs;
import org.jgrapht.alg.scoring.Coreness;
import org.jgrapht.alg.shortestpath.DijkstraManyToManyShortestPaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.util.SupplierUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents a network graph.
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: September 17, 2020.
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
	 * @param filename path to file to be read
	 * @param separator character that separates source node and target node.
	 * @throws Exception exception thrown if vertex set of {@code g} is not empty.
	 */
	public void buildGraphFromFile(String filename, String separator) throws Exception
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
					String[] tokens = data.split(separator);
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
	 * Adds a vertex to {@code NursingHome}.
	 *
	 * @param v vertex (node) to be added to the network.
	 */
	public void addVertex(Integer v)
	{
		g.addVertex(v);
	}
	
	/**
	 * Adds an edge to {@code NursingHome}.
	 *
	 * @param s source of the edge to be added
	 * @param t target (destination) of the edge to be added.
	 */
	public void addEdge(Integer s, Integer t)
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
	
	/**
	 * Checks whether the graph {@code g} has self-loops.
	 *
	 * @return returns true if graph {@code g} has self-loops, false otherwise.
	 */
	public boolean hasSelfLoops()
	{
		return GraphTests.hasSelfLoops(g);
	}
	
	/**
	 * Finds the distances between all pairs of nodes in the graph {@code g}.
	 *
	 * @return a the distances between all pairs of nodes in the graph {@code g}.
	 */
	public Map<Pair<Integer, Integer>, Double> findDistancesBetweenNodes()
	{
		Map<Pair<Integer, Integer>, Double> output = new HashMap<>();
		DijkstraManyToManyShortestPaths paths = new DijkstraManyToManyShortestPaths(g);
		
		for (Integer source: getVertexSet())
		{
			for (Integer sink: getVertexSet())
			{
				Pair<Integer, Integer> edge1 = new Pair<>(source, sink);
				Pair<Integer, Integer> edge2 = new Pair<>(sink, source);
				if (!output.containsKey(edge1))
				{
					double distance;
					if (paths.getPath(source, sink) != null)
						distance = paths.getPath(source, sink).getWeight();
					else
						distance = Double.POSITIVE_INFINITY;
					output.put(edge1, distance);
					output.put(edge2, distance);
				}
			}
		}
		
		return output;
	}
	
	/**
	 * Finds the distance between {@code node1} and {@code node2} in the graph {@code g}.
	 *
	 * @param node1 a node in the graph {@code g}
	 * @param node2 a node in the graph {@code g}.
	 * @return the distance between {@code node1} and {@code node2} in the graph {@code g}.
	 */
	public double findDistanceBetweenNodes(int node1, int node2)
	{
		DijkstraShortestPath path = new DijkstraShortestPath(g);
		return path.getPathWeight(node1, node2);
	}
	
	/**
	 * Finds the maximum distance between node pairs.
	 *
	 * @return the maximum distance between node pairs.
	 */
	public double findMaxDistanceBetweenNodes()
	{
		Map<Pair<Integer, Integer>, Double> distances = findDistancesBetweenNodes();
		return distances.values().stream().mapToDouble(e -> e).filter(e -> e >= 0.0).max().orElse(0.0);
	}
	
	/**
	 * Finds the average distance between node pairs.
	 *
	 * @return the average distance between node pairs.
	 */
	public double findAverageDistanceBetweenNodes()
	{
		Map<Pair<Integer, Integer>, Double> distances = findDistancesBetweenNodes();
		// remove self-pairs
		for (Integer node: getVertexSet())
			distances.remove(new Pair<>(node, node));
		return distances.values().stream().mapToDouble(e -> e).reduce(0, Double::sum)/distances.size();
	}
	
	/**
	 * Returns the degree of the specified node.
	 *
	 * @param node node whose degree is to be calculated.
	 * @return the degree of the specified node.
	 */
	public int getDegreeOfNode(int node)
	{
		return g.degreeOf(node);
	}
	
	/**
	 * Returns a map from nodes to their degrees.
	 *
	 * @return a map from nodes to their degrees.
	 */
	public Map<Integer, Integer> getDegrees()
	{
		return g.vertexSet().stream().collect(Collectors.toMap(e -> e, this::getDegreeOfNode, (a, b) -> b));
	}
	
	/**
	 * Calculates the average degree of nodes in the graph {@code g}.
	 *
	 * @return the average degree of nodes in the graph {@code g}.
	 */
	public double findAverageDegreeOfNodes()
	{
		Map<Integer, Integer> degrees = getDegrees();
		int sum = degrees.keySet().stream().map(degrees::get).reduce(0, Integer::sum);
		return 1.0*sum/g.vertexSet().size();
	}
	
	/**
	 * Finds the maximum degree.
	 *
	 * @return the maximum degree.
	 */
	public int findMaxDegreeOfNodes()
	{
		Map<Integer, Integer> degrees = getDegrees();
		return Objects.requireNonNull(degrees.entrySet().stream().max(Map.Entry.comparingByValue())
						.orElse(null)).getValue();
	}
	
	/**
	 * Returns a set of nodes contained in the graph {@code g}.
	 *
	 * @return a set of nodes contained in the graph {@code g}.
	 */
	public Set<Integer> getVertexSet()
	{
		return g.vertexSet();
	}
	
	/**
	 * Returns the set of edges contained in the graph {@code g}.
	 *
	 * @return a set of the edges contained in the graph {@code g}.
	 */
	public Set<DefaultEdge> getEdgeSet()
	{
		return g.edgeSet();
	}
	
	/**
	 * Removes nodes in {@code nodesToBeRemoved} in graph {@code g}.
	 *
	 * @param nodesToBeRemoved nodes to be removed from the graph {@code g}.
	 */
	public void removeAllVertices(Set<Integer> nodesToBeRemoved)
	{
		g.removeAllVertices(nodesToBeRemoved);
	}
	
	/**
	 * Returns the minimum node label.
	 *
	 * @return the minimum node label.
	 */
	public int findMinimumNodeLabel()
	{
		return getVertexSet().stream().mapToInt(v -> v).min().orElseThrow(NoSuchElementException::new);
	}
	
	/**
	 * Get a map from nodes to their neighbors.
	 *
	 * @return a map from nodes to their neighbors.
	 */
	public Map<Integer, List<Integer>> getNeighbors()
	{
		Set<Integer> nodes = getVertexSet();
		return nodes.stream().collect(Collectors.toMap(v -> v, v -> Graphs.neighborListOf(g, v), (a, b) -> b));
	}
	
	/**
	 * Get the neighbors of {@code node}.
	 *
	 * @param node the node whose neighbors are to be fetched.
	 * @return the neighbors of {@code node}.
	 */
	public List<Integer> getNeighborOfNode(int node)
	{
		return Graphs.neighborListOf(g, node);
	}
	
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
	 * Get the core number of {@code node} in the graph {@code g}.
	 *
	 * @param node the node.
	 * @return the core number.
	 */
	public int getCoreNumber(int node)
	{
		Coreness<Integer, DefaultEdge> c = new Coreness<>(g);
		return c.getVertexScore(node);
	}
	
	/**
	 * Modifies the graph {@code g} to its k-core subgraph.
	 *
	 * @param k the order of the core.
	 * @throws Exception exception thrown if graph contains self-loops.
	 */
	public void dokCoreDecomposition(int k) throws Exception
	{
		g = getkCore(k);
		changeGraphToLargestConnectedComponent();
	}
	
	/**
	 * Returns the k-core of {@code g}.
	 * A k-core is a maximal subgraph that contains nodes of degree k or more.
	 *
	 * @param k the order of the core.
	 * @return the k-core subgraph.
	 * @throws Exception exception thrown if graph contains self-loops.
	 */
	public Graph<Integer, DefaultEdge> getkCore(int k) throws Exception
	{
		Map<Integer, Integer> core = getCoreNumbers();
		//int k = core.entrySet().stream()
		//			.max((e1, e2) -> e1.getValue().compareTo(e2.getValue()))
		//			.orElse(null).getValue();
		
		// find nodes in the k-core
		Set<Integer> nodes	= new HashSet<>();
		for (Integer v: core.keySet())
		{
			if (core.get(v) >= k)
				nodes.add(v);
		}
		// get subgraph with only nodes
		return new AsSubgraph<>(g, nodes);
	}
	
	/**
	 * Returns the core number for each vertex.
	 * A k-core is a maximal subgraph that contains nodes (vertices) of degree k or more.
	 * The core number of a node is the largest value k of a k-core containing that node.
	 * <p>
	 * References:
	 * </p>
	 * <p>
	 * [1] An O(m) Algorithm for Cores Decomposition of Networks
	 * Vladimir Batagelj and Matjaz Zaversnik, 2003.
	 * <a href="https://arxiv.org/abs/cs.DS/0310049", target="_blank">https://arxiv.org/abs/cs.DS/0310049</a>
	 * </p>
	 * <p>
	 * [2] <a href="https://networkx.github.io/documentation/stable/_modules/networkx/algorithms/core.html#core_number",
	 *      target="_blank">
	 *      https://networkx.github.io/documentation/stable/_modules/networkx/algorithms/core.html#core_number
	 *     </a>
	 * </p>
	 *
	 * @return returns the core number for each vertex.
	 * @throws Exception exception thrown if graph contains self-loops.
	 */
	private Map<Integer, Integer> getCoreNumbers() throws Exception
	{
		if (GraphTests.hasSelfLoops(g))
			throw new Exception("Graph has self loops which is not permitted; " +
					"consider using removeSelfLoops()");
		// Find the degree of each vertex
		Map<Integer, Integer> degrees = g.vertexSet()
				.stream().collect(Collectors.toMap(vertex -> vertex, vertex -> g.degreeOf(vertex), (a, b) -> b));
		// Sort nodes by degree (ascending order)
		List<Integer> nodes = degrees.entrySet().stream()
										.sorted(Map.Entry.comparingByValue())
										.map(Map.Entry::getKey).collect(Collectors.toList());
		/*
			Bin boundaries contain for each possible degree the position of the first vertex of that degree
			in the list nodes.
		 */
		List<Integer> binBoundaries = new ArrayList<>();
		binBoundaries.add(0);
		int currentDegree = 0;
		for (int i=0; i<nodes.size(); i++)
		{
			int degreeOfV = degrees.get(nodes.get(i));
			if (degreeOfV>currentDegree)
			{
				for (int j=0; j<(degreeOfV-currentDegree); j++)
					binBoundaries.add(i);
				currentDegree = degreeOfV;
			}
		}
		Map<Integer, Integer> nodePosition = IntStream.range(0, nodes.size()).boxed().collect(Collectors
												.toMap(nodes::get, i -> i, (a, b) -> b));
		// The initial guess for the core number of a node is its degree.
		Map<Integer, Integer> core = new HashMap<>(degrees);
		Map<Integer, List<Integer>> neighbors = getNeighbors();
		int pos, binStart;
		for (Integer v: nodes)
		{
			for (Integer u: neighbors.get(v))
			{
				if (core.get(u)>core.get(v))
				{
					neighbors.get(u).remove(v);
					pos = nodePosition.get(u);
					binStart = binBoundaries.get(core.get(u));
					nodePosition.put(u, binStart);
					nodePosition.put(nodes.get(binStart), pos);
					// swapping
					int tmp = nodes.get(pos);
					nodes.set(pos, nodes.get(binStart));
					nodes.set(binStart, tmp);
					
					binBoundaries.set(core.get(u), binBoundaries.get(core.get(u))+1);
					core.put(u, core.get(u)-1);
				}
			}
		}
		return core;
	}
	
	/**
	 * Writes network information (max degree, avg. distance, etc.) to csv file.
	 *
	 * @param filename path to output file
	 * @param append true, if you wish to append to existing file; false, otherwise.
	 * @throws Exception thrown if error in input-output operation.
	 */
	public void writeNetworkInfoToCSV(String filename, boolean append) throws Exception
	{
		File fileObj = new File(filename);
		String[] header = {"Network", "#nodes", "#edges", "avg. degree", "max degree",
				"avg. distance", "max distance", "UTC"};
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
		String[] line = new String[8];
		line[0] = getNetworkName();
		line[1] = String.valueOf(getVertexSet().size());
		line[2] = String.valueOf(getEdgeSet().size());
		line[3] = String.valueOf(findAverageDegreeOfNodes());
		line[4] = String.valueOf(findMaxDegreeOfNodes());
		line[5] = String.valueOf(findAverageDistanceBetweenNodes());
		line[6] = String.valueOf(findMaxDistanceBetweenNodes());
		line[7] = Instant.now().toString();
		writer.writeNext(line);
		writer.flush();
		writer.close();
		System.out.println("Network info successfully written to \""+filename+"\".");
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
	 * Uses lombok.
	 *
	 * @return returns a integer value representing the hash code for an object.
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(g, networkName);
	}
}
