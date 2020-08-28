package simulation;

import network.graph;
import org.javatuples.Pair;
import org.javatuples.Quintet;
import org.jgrapht.Graphs;

import java.io.*;
import java.util.*;

public class simulationRuns
{
	// Model (TN11C, RAEPC, etc.); Network name; t_0; repetitions; false negative probability
	Map<Quintet<String, String, Integer, Integer, Double>, List<List<Integer>>> dictModelNetworkT0RunsFalseNegative;
	
	public simulationRuns(Map<Quintet<String, String, Integer, Integer, Double>, List<List<Integer>>> dictModelNetworkT0RunsFalseNegative)
	{
		this.dictModelNetworkT0RunsFalseNegative = dictModelNetworkT0RunsFalseNegative;
	}
	public simulationRuns()
	{
		dictModelNetworkT0RunsFalseNegative = new HashMap<>();
	}
	
	public Map<Quintet<String, String, Integer, Integer, Double>, List<List<Integer>>> getDictModelNetworkT0RunsFalseNegative()
	{
		return dictModelNetworkT0RunsFalseNegative;
	}
	
	/**
	 * Function to simulate several runs of TN11C spread models where detectors are completely reliable.
	 * See Lee, Jinho. Stochastic optimization models for rapid detection of viruses in cellphone networks. Diss. 2012.
	 *
	 * @param g network graph
	 * @param t0_runs array of a pair of (t0, runs), where t0 is simulation time, and runs is number of repetitions of simulation
	 * @param seed an array of length 2; the first seed is for the initial random location of the virus,
	 *             and the second is for random choice of neighbor while spreading
	 * @throws Exception exception thrown if length of seed[] is not 2
	 */
	public void simulateTN11CRuns(graph g, List<Pair<Integer, Integer>> t0_runs, int[] seed) throws Exception
	{
		// Remove self-loops if any from the graph
		g.removeSelfLoops();
		System.out.print("Removed self-loops (if any) from the graph: ");
		final int n = g.getG().vertexSet().size();
		final int minNode = g.getG().vertexSet().stream().mapToInt(v->v).min().orElseThrow(NoSuchElementException::new);
		final int maxNode = g.getG().vertexSet().stream().mapToInt(v->v).max().orElseThrow(NoSuchElementException::new);
		System.out.println("(new) network has "+n+" nodes and "+g.getG().edgeSet().size()+" edges.");
		
		if (seed.length!=2)
			throw new Exception("Seed array should be of length 2!");
		
		
		for (Pair<Integer, Integer> v: t0_runs)
		{
			int time0 = v.getValue0();
			int rep = v.getValue1();
			SplittableRandom initialLocationGenChoice = new SplittableRandom(seed[0]+time0+rep);
			SplittableRandom neighborGenChoice = new SplittableRandom(seed[1]+time0+rep);
			int[] initialLocationRuns = initialLocationGenChoice.ints(rep, minNode, maxNode+1).toArray();
			
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
			dictModelNetworkT0RunsFalseNegative.put(new Quintet<>("TN11C", g.getNetworkName(), time0, rep, 0.0), samplePathRuns);
		}
	}
	
	/**
	 * Chooses a neighbor uniformly at random to infect.
	 * @param g network graph
	 * @param neighborGenChoice an instance of SplittableRandom
	 * @param currentInfected the current infected node  (one of its neighboring vertex is randomly infected)
	 * @return returns the vertex that is infected
	 */
	private int getRandomInfectedNeighbor(graph g, SplittableRandom neighborGenChoice, int currentInfected)
	{
		List<Integer> currentNeighbors = Graphs.neighborListOf(g.getG(), currentInfected);
		int rnd = neighborGenChoice.nextInt(currentNeighbors.size());
		currentInfected = currentNeighbors.get(rnd);
		return currentInfected;
	}
	
	/**
	 *
	 * @param g network graph
	 * @param t0_runs array of a pair of (t0, runs), where t0 is simulation time, and runs is number of repetitions of simulation
	 * @param seed an array of length 2; the first seed is for the initial random location of the virus,
	 *              and the second is for random choice of neighbor while spreading
	 * @throws Exception exception thrown if length of seed[] is not 2
	 */
	public void simulateOnlyNecessaryTN11CRuns(graph g, List<Pair<Integer, Integer>> t0_runs, int[] seed) throws Exception
	{
		List<Pair<Integer, Integer>> new_t0_runs = new ArrayList<>();
		for (Pair<Integer, Integer> time0_run : t0_runs)
		{
			Quintet<String, String, Integer, Integer, Double> newKey;
			newKey = new Quintet<>("TN11C", g.getNetworkName(), time0_run.getValue0(), time0_run.getValue1(), 0.0);
			if (!dictModelNetworkT0RunsFalseNegative.containsKey(newKey))
				new_t0_runs.add(time0_run);
		}
		if (new_t0_runs.size() > 0)
		{
			System.out.println("Running more simulations for: "+new_t0_runs.toString());
			simulateTN11CRuns(g, new_t0_runs, seed);
		}
	}
	
	/**
	 * Loads any simulation runs from serialized object in file.
	 * @param serialFilename path of the file where the serialized object is stored
	 */
	public void loadTN11CRunsFromFile(String serialFilename)
	{
		try
		{
			FileInputStream fin = new FileInputStream(serialFilename);
			BufferedInputStream bin = new BufferedInputStream(fin);
			ObjectInputStream objin = new ObjectInputStream(bin);
			dictModelNetworkT0RunsFalseNegative = (Map) objin.readObject();
			objin.close();
			bin.close();
			fin.close();
			System.out.println("Using simulation results in \""+serialFilename+"\".");
			// dictModelNetworkT0RunsFalseNegative.entrySet().stream().forEach(System.out::println);
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
	 * Serializes dictModelNetworkT0RunsFalseNegative
	 * @param serialFilename path of the file where the serialized object is to be stored
	 */
	public void serializeSimulationRuns(String serialFilename)
	{
		try
		{
			FileOutputStream fout = new FileOutputStream(serialFilename);
			BufferedOutputStream bout = new BufferedOutputStream(fout);
			ObjectOutputStream objout = new ObjectOutputStream(bout);
			objout.writeObject(dictModelNetworkT0RunsFalseNegative);
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
	
	/**
	 * Function to simulate several runs of TN11C spread models where each detector has a reliability of 1-r, where r is false negative probability of detector.
	 *
	 * @param dataFolder path of the folder where the input and/or pickle file are/is
	 * @param filename input filename with extension
	 * @param g network graph
	 * @param t0_runs array of a pair of (t0, runs), where t0 is simulation time, and runs is number of repetitions of simulation
	 * @param r array of false negative probabilities
	 * @param pickleFilename filename (without extension) of serialized file (output)
	 */
	public void simulateTN11CFalseNegativeRuns(String dataFolder, String filename, graph g,
	                                           Pair<Integer, Integer>[] t0_runs, int[] r, String pickleFilename)
	{
		System.out.println("Soon to go under construction!");
	}
	
}
