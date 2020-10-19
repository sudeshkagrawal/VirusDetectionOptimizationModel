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
 * Last Updated: October 19, 2020.
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
	 * Initialize {@link graph#g}, assumed to be empty, with a complete graph of given size.
	 *
	 * @param size number of vertices (nodes) in the complete graph
	 * @param startingNodeLabel node numbering to start from.
	 * @throws Exception thrown if {@code size<0},
	 * or if {@code startingNodeLabel<0},
	 * or if {@code startingNodeLabel>=size},
	 * or if vertex set of {@link graph#g} is not empty.
	 */
	public void initializeAsCompleteGraph(int size, int startingNodeLabel) throws Exception
	{
		if (size<0)
			throw new Exception("Size cannot be negative!");
		if (startingNodeLabel<0)
			throw new Exception("Node labels should be non-negative integers!");
		if (startingNodeLabel>=size)
			throw new Exception("'startingNodeLabel<size' should hold!");
		if ((!this.g.vertexSet().isEmpty()) && (this.g.vertexSet().size()>0))
			throw new Exception("Graph is not empty!");
		
		Supplier<Integer> vertexSupplier = new Supplier<>()
		{
			private int id = startingNodeLabel;
			
			@Override
			public Integer get()
			{
				return id++;
			}
		};
		this.g = new DefaultUndirectedGraph<>(vertexSupplier, SupplierUtil.createDefaultEdgeSupplier(),
				false);
		CompleteGraphGenerator<Integer, DefaultEdge> completeGraphGenerator =
				new CompleteGraphGenerator<>(size);
		completeGraphGenerator.generateGraph(this.g);
	}
	
	/**
	 * Initialize an empty graph {@link graph#g} with a circulant graph of given size.
	 * <br>
	 * A circulant graph is a graph of {@code n (= size)} vertices in which the {@code i}<sup>th</sup> vertex is
	 * adjacent to the {@code (i+j)}<sup>th</sup> and the {@code (i-j)}<sup>th</sup> vertices for each {@code j} in the
	 * array offsets.
	 *
	 * @param size number of vertices (nodes) in the circulant graph
	 * @param offsets defines the list of all distances in any edge
	 * @param startingNodeLabel node numbering to start from.
	 * @throws Exception thrown if {@code size<0},
	 * or if {@code startingNodeLabel<0},
	 * or if {@code startingNodeLabel>=size},
	 * or if vertex set of {@link graph#g} is not empty,
	 * of if {@code offsets} has invalid values.
	 */
	public void initializeAsCirculantGraph(int size, int[] offsets, int startingNodeLabel) throws Exception
	{
		if (size<0)
			throw new Exception("Size cannot be negative!");
		if (startingNodeLabel<0)
			throw new Exception("Node labels should be non-negative integers!");
		if (startingNodeLabel>=size)
			throw new Exception("'startingNodeLabel<size' should hold!");
		if ((!this.g.vertexSet().isEmpty()) && (this.g.vertexSet().size()>0))
			throw new Exception("Graph is not empty!");
		if (Arrays.stream(offsets).min().orElse(1)<0)
			throw new Exception("Offset values cannot be negative!");
		if (Arrays.stream(offsets).max().orElse(size-1)>size)
			throw new Exception("Offset values cannot be larger than size of the network!");
		
		// add vertices
		for (int i=startingNodeLabel; i<(size+startingNodeLabel); i++)
			this.g.addVertex(i);
		// add edges
		for (int i=startingNodeLabel; i<(size+startingNodeLabel); i++)
		{
			for (int offset : offsets)
			{
				this.g.addEdge(i, i-offset>=startingNodeLabel ? i-offset : (i-offset+size));
				this.g.addEdge(i, i+offset<(size+startingNodeLabel) ? i+offset : (i+offset)%size);
			}
		}
	}
	
	/**
	 * Build network from a text file.
	 * Each line in the text file is an edge, where the vertices are separated by commas.
	 *
	 * @param filename path to file to be read
	 * @param separator character that separates source node and target node.
	 * @throws Exception thrown if vertex set of {@code g} is not empty.
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
	 * @return the graph {@code g}.
	 */
	public Graph<Integer, DefaultEdge> getG()
	{
		return g;
	}
	
	/**
	 * Getter for {@code networkName}.
	 *
	 * @return the network name as String.
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
	 * Find the source node of an edge.
	 *
	 * @param e an edge in the network.
	 * @return the source node of the edge.
	 */
	public Integer getEdgeSource(DefaultEdge e)
	{
		return this.getG().getEdgeSource(e);
	}
	
	/**
	 * Find the target node of an edge.
	 *
	 * @param e an edge in the network.
	 * @return the target node of the edge.
	 */
	public Integer getEdgeTarget(DefaultEdge e)
	{
		return this.getG().getEdgeTarget(e);
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
	 * @return {@code true} if graph {@code g} has self-loops, {@code false} otherwise.
	 */
	public boolean hasSelfLoops()
	{
		return GraphTests.hasSelfLoops(g);
	}
	
	/**
	 * Finds the distances between all pairs of nodes in the graph {@code g}.
	 *
	 * @return the distances between all pairs of nodes in the graph {@code g}.
	 */
	public Map<Pair<Integer, Integer>, Double> findDistancesBetweenNodes()
	{
		Map<Pair<Integer, Integer>, Double> output = new HashMap<>();
		DijkstraManyToManyShortestPaths<Integer, DefaultEdge> paths = new DijkstraManyToManyShortestPaths<>(g);
		
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
		DijkstraShortestPath<Integer, DefaultEdge> path = new DijkstraShortestPath<>(g);
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
	 * @throws Exception thrown if graph {@code g} does not have a vertex.
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
			BreadthFirstIterator<Integer, DefaultEdge> iter = new BreadthFirstIterator<>(g, startNode);
			List<Integer> connectedComponent = new ArrayList<>();
			iter.forEachRemaining(connectedComponent::add);
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
	 * Modifies the graph {@code g} to the largest connected component of its k-core subgraph.
	 *
	 * @param k the order of the core.
	 * @throws Exception thrown if graph contains self-loops.
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
	 * @throws Exception thrown if graph contains self-loops.
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
	 * <a href="https://arxiv.org/abs/cs.DS/0310049" target="_blank">https://arxiv.org/abs/cs.DS/0310049</a>
	 * </p>
	 * <p>
	 * [2]
	 * <a href="https://networkx.github.io/documentation/stable/_modules/networkx/algorithms/core.html#core_number" target="_blank">
	 *      https://networkx.github.io/documentation/stable/_modules/networkx/algorithms/core.html#core_number
	 *     </a>
	 * </p>
	 *
	 * @return the core number for each vertex.
	 * @throws Exception thrown if graph contains self-loops.
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
	 * @param append {@code true}, if you wish to append to existing file; {@code false}, otherwise.
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
	 * Returns a string representation of the object.
	 *
	 * @return a string representation of the object.
	 */
	@Override
	public String toString()
	{
		return networkName+":g<"+g.vertexSet()+", "+g.edgeSet()+">";
	}
	
	/**
	 * Indicates whether some other object is "equal to" this one.
	 * Used guidelines at <a href="http://www.technofundo.com/tech/java/equalhash.html" target="_blank">
	 *     "Equals and Hash Code"</a>.
	 *
	 * @param o the reference object with which to compare.
	 * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
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
	 * Returns a hash code value for the object.
	 *
	 * @return a hash code value for this object.
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(g, networkName);
	}
}
