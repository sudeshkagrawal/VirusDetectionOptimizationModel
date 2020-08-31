package simulation;

import network.graph;
import org.javatuples.Pair;
import org.javatuples.Quintet;
import org.jgrapht.Graphs;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Sudesh Agrawal (sudesh@utexas.edu)
 * Last Updated: Aug 31, 2020.
 * Class for simulation.
 */
public class simulationRuns
{
	// Model (TN11C, RAEPC, etc.); Network name; t_0; repetitions; false negative probability
	Map<Quintet<String, String, Integer, Integer, Double>, List<List<Integer>>>
																	mapModelNetworkT0RunsFalseNegativeToSimulationRuns;
	Map<Quintet<String, String, Integer, Integer, Double>, List<List<Integer>>>
																mapModelNetworkT0RunsFalseNegativeToVirtualDetections;
	
	public simulationRuns(Map<Quintet<String, String, Integer, Integer, Double>, List<List<Integer>>>
			                      mapModelNetworkT0RunsFalseNegativeToSimulationRuns,
	                      Map<Quintet<String, String, Integer, Integer, Double>, List<List<Integer>>>
			                      mapModelNetworkT0RunsFalseNegativeToVirtualDetections)
	{
		this.mapModelNetworkT0RunsFalseNegativeToSimulationRuns = mapModelNetworkT0RunsFalseNegativeToSimulationRuns;
		this.mapModelNetworkT0RunsFalseNegativeToVirtualDetections =
																mapModelNetworkT0RunsFalseNegativeToVirtualDetections;
	}
	public simulationRuns()
	{
		mapModelNetworkT0RunsFalseNegativeToSimulationRuns = new HashMap<>();
		mapModelNetworkT0RunsFalseNegativeToVirtualDetections = new HashMap<>();
	}
	
	/**
	 * Getter for {@code mapModelNetworkT0RunsFalseNegativeToSimulationRuns}.
	 * @return returns {@code mapModelNetworkT0RunsFalseNegativeToSimulationRuns}.
	 */
	public Map<Quintet<String, String, Integer, Integer, Double>, List<List<Integer>>>
																getMapModelNetworkT0RunsFalseNegativeToSimulationRuns()
	{
		return mapModelNetworkT0RunsFalseNegativeToSimulationRuns;
	}
	
	/**
	 * Getter for {@code mapModelNetworkT0RunsFalseNegativeToVirtualDetections}.
	 * @return returns {@code mapModelNetworkT0RunsFalseNegativeToVirtualDetections}.
	 */
	public Map<Quintet<String, String, Integer, Integer, Double>, List<List<Integer>>>
															getMapModelNetworkT0RunsFalseNegativeToVirtualDetections()
	{
		return mapModelNetworkT0RunsFalseNegativeToVirtualDetections;
	}
	
	/**
	 * Function to simulate several runs of TN11C spread models where detectors are completely reliable.
	 * See Lee, Jinho. Stochastic optimization models for rapid detection of viruses in cellphone networks. Diss. 2012.
	 * Each detector has a reliability of 1-r, where r is false negative probability of the detectors.
	 *
	 * @param g network graph
	 * @param t0_runs list of a pair of (t0, runs), where t0 is simulation time,
	 *                   and runs is number of repetitions of simulation
	 * @param r false negative probability
	 * @param seed an array of length 2 (for r=0) or 3 (for r>0);
	 *             the first seed is for the initial random location of the virus,
	 *             the second is for random choice of neighbor while spreading,
	 *             and the third is for virtual detections.
	 * @throws Exception exception thrown if length of {@code seed} is not {2, 3}.
	 */
	public void simulateTN11CRuns(graph g, List<Pair<Integer, Integer>> t0_runs, double r, int[] seed) throws Exception
	{
		// Remove self-loops if any from the graph
		g.removeSelfLoops();
		System.out.print("Removed self-loops (if any) from the graph: ");
		final int n = g.getG().vertexSet().size();
		System.out.println("(new) network has "+n+" nodes and "+g.getG().edgeSet().size()+" edges.");
		
		if ((seed.length!=2) && (seed.length!=3))
			throw new Exception("Seed array should either be of length 2 (for r=0) or of length 3 (for r>0)!");
		
		for (Pair<Integer, Integer> v: t0_runs)
		{
			int time0 = v.getValue0();
			int rep = v.getValue1();
			SplittableRandom initialLocationGenChoice = new SplittableRandom(seed[0]+time0+rep);
			SplittableRandom neighborGenChoice = new SplittableRandom(seed[1]+time0+rep);
			int[] initialLocationRuns = getInitialLocationRuns(g, initialLocationGenChoice, rep);
			
			List<List<Integer>> samplePathRuns = new ArrayList<>(rep);
			
			System.out.println("Starting "+rep+" runs of simulating TN11C spread upto "+time0
			+" time step for each run on the \""+g.getNetworkName()+"\" network...");
			for (int x=0; x<rep; x++)
			{
				//System.out.println("\t Simulation run "+(x+1));
				int initialLocation = initialLocationRuns[x];
				//System.out.println("\t Initial location of virus: "+initialLocation);
				List<Integer> infected = new ArrayList<>(time0);
				
				// time step 0
				int currentInfected = initialLocation;
				infected.add(currentInfected);
				
				for (int t=1; t<=time0; t++)
				{
					//System.out.println("\t\t Time: "+t);
					currentInfected = getRandomInfectedNeighbor(g, neighborGenChoice, currentInfected);
					infected.add(currentInfected);
					//System.out.println("\t\t Current infected node: "+currentInfected);
				}
				samplePathRuns.add(infected);
			}
			System.out.println("Ending "+rep+" runs of simulating TN11C spread upto "+time0
					+" time step for each run on the \""+g.getNetworkName()+"\" network...");
			Quintet<String, String, Integer, Integer, Double> key =
													new Quintet<>("TN11C", g.getNetworkName(), time0, rep, r);
			mapModelNetworkT0RunsFalseNegativeToSimulationRuns.put(key, samplePathRuns);
			//virtual detections
			SplittableRandom reliabilityGenChoice = new SplittableRandom(seed[2]+time0+rep);
			mapModelNetworkT0RunsFalseNegativeToVirtualDetections.put(key, new ArrayList<>(rep));
			IntStream.range(0, rep).mapToObj(i -> IntStream.range(0, (time0 + 1))
					.mapToObj(j -> reliabilityGenChoice.nextDouble() < r ? 0 : 1)
					.collect(Collectors.toCollection(() -> new ArrayList<>(time0 + 1))))
					.forEach(timeList -> mapModelNetworkT0RunsFalseNegativeToVirtualDetections.get(key)
					.add(new ArrayList<>(timeList)));
		}
	}
	
	/**
	 * Get the random initial locations of the virus.
	 * @param g network graph
	 * @param initialLocationGenChoice an instance of {@code SplittableRandom}
	 *                                    to randomly choose the initial location of the virus
	 * @param size length of output array.
	 * @return returns an array of the initial locations of the virus.
	 */
	private int[] getInitialLocationRuns(graph g, SplittableRandom initialLocationGenChoice, int size)
	{
		List<Integer> vertices = new ArrayList<>(g.getG().vertexSet());
		int[] initialLocationRunsIndices = initialLocationGenChoice.ints(size, 0, g.getG()
																	.vertexSet().size()).toArray();
		return Arrays.stream(initialLocationRunsIndices).map(vertices::get).toArray();
	}
	
	/**
	 * Chooses a neighbor uniformly at random to infect.
	 * @param g network graph
	 * @param neighborGenChoice an instance of SplittableRandom
	 * @param currentInfected the current infected node  (one of its neighboring vertex is randomly infected).
	 * @return returns the vertex that is infected.
	 */
	private int getRandomInfectedNeighbor(graph g, SplittableRandom neighborGenChoice, int currentInfected)
	{
		List<Integer> currentNeighbors = Graphs.neighborListOf(g.getG(), currentInfected);
		int rnd = neighborGenChoice.nextInt(currentNeighbors.size());
		currentInfected = currentNeighbors.get(rnd);
		return currentInfected;
	}
	
	/**
	 * Runs simulation for only those {@code t0_runs}
	 * which are not already there in {@code mapModelNetworkT0RunsFalseNegativeToSimulationRuns}.
	 * @param g network graph
	 * @param t0_runs array of a pair of (t0, runs), where t0 is simulation time,
	 *                   and runs is number of repetitions of simulation
	 * @param r false negative probability
	 * @param seed an array of length 2; the first seed is for the initial random location of the virus,
	 *              and the second is for random choice of neighbor while spreading.
	 * @throws Exception exception thrown if length of {@code seed} is not {2, 3}.
	 */
	public boolean simulateOnlyNecessaryTN11CRuns(graph g, List<Pair<Integer, Integer>> t0_runs,
	                                              double r, int[] seed) throws Exception
	{
		boolean ranNewSimulations = false;
		List<Pair<Integer, Integer>> new_t0_runs = new ArrayList<>();
		for (Pair<Integer, Integer> time0_run : t0_runs)
		{
			Quintet<String, String, Integer, Integer, Double> newKey;
			newKey = new Quintet<>("TN11C", g.getNetworkName(), time0_run.getValue0(), time0_run.getValue1(), r);
			if (!mapModelNetworkT0RunsFalseNegativeToSimulationRuns.containsKey(newKey))
				new_t0_runs.add(time0_run);
		}
		if (new_t0_runs.size() > 0)
		{
			System.out.println("Running more simulations for: "+new_t0_runs.toString());
			simulateTN11CRuns(g, new_t0_runs, r, seed);
			ranNewSimulations = true;
		}
		return ranNewSimulations;
	}
	
	/**
	 * Loads any simulation runs from serialized object in file.
	 * @param serialFilename path of the file where the serialized object is stored.
	 */
	public void loadTN11CRunsFromFile(String serialFilename)
	{
		try
		{
			FileInputStream fin = new FileInputStream(serialFilename);
			BufferedInputStream bin = new BufferedInputStream(fin);
			ObjectInputStream objin = new ObjectInputStream(bin);
			List<Map<Quintet<String, String, Integer, Integer, Double>, List<List<Integer>>>> serObject =
					(List<Map<Quintet<String, String, Integer, Integer, Double>, List<List<Integer>>>>) objin.readObject();
			mapModelNetworkT0RunsFalseNegativeToSimulationRuns = serObject.get(0);
			mapModelNetworkT0RunsFalseNegativeToVirtualDetections = serObject.get(1);
			objin.close();
			bin.close();
			fin.close();
			System.out.println("Using simulation results in \""+serialFilename+"\".");
			// mapModelNetworkT0RunsFalseNegativeToSimulationRuns.entrySet().stream().forEach(System.out::println);
		}
		catch (FileNotFoundException e1)
		{
			System.out.println("Error, file not found!");
			System.out.println(e1.getMessage());
		}
		catch (Exception e2)
		{
			System.out.println("An exception occurred:");
			e2.printStackTrace();
			System.out.println("Exiting the program...");
			System.exit(0);
		}
	}
	
	/**
	 * Serializes {@code } mapModelNetworkT0RunsFalseNegativeToSimulationRuns}
	 * and {@code mapModelNetworkT0RunsFalseNegativeToVirtualDetections} as a list of two objects.
	 *
	 * @param serialFilename path of the file where the serialized object is to be stored.
	 */
	public void serializeTN11CRuns(String serialFilename)
	{
		try
		{
			FileOutputStream fout = new FileOutputStream(serialFilename);
			BufferedOutputStream bout = new BufferedOutputStream(fout);
			ObjectOutputStream objout = new ObjectOutputStream(bout);
			List<Map<Quintet<String, String, Integer, Integer, Double>, List<List<Integer>>>> serObject =
																				new ArrayList<>(2);
			serObject.add(mapModelNetworkT0RunsFalseNegativeToSimulationRuns);
			serObject.add(mapModelNetworkT0RunsFalseNegativeToVirtualDetections);
			objout.writeObject(serObject);
			objout.close();
			bout.close();
			fout.close();
			System.out.println("Simulation results serialized at \""+ serialFilename +"\".");
		}
		catch (IOException e1)
		{
			System.out.println("Input-Output Exception:");
			e1.printStackTrace();
			System.out.print("Writing of serial file to disk failed!");
			System.out.println("Exiting the program...");
			System.exit(0);
		}
		catch (Exception e2)
		{
			System.out.println("An exception occurred:");
			e2.printStackTrace();
			System.out.print("Writing of serial file to disk failed!");
			System.out.println("Exiting the program...");
			System.exit(0);
		}
	}
}
