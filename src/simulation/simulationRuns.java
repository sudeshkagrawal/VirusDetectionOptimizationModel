package simulation;

import network.graph;
import org.javatuples.Pair;
import org.jgrapht.Graphs;

import java.util.*;

public class simulationRuns
{
	Map<Pair<Integer, Integer>, List<List<Integer>>> dictT0Runs;
	
	
	/**
	 * Function to simulate several runs of TN11C spread models where detectors are completely reliable.
	 * See Lee, Jinho. Stochastic optimization models for rapid detection of viruses in cellphone networks. Diss. 2012.
	 *
	 * @param g network graph
	 * @param t0_runs array of a pair of (t0, runs), where t0 is simulation time, and runs is number of repetitions of simulation
	 * @param pickleFilename filename (without extension) of serialized file (output)
	 * @param seed an array of length 2; the first seed is for the initial random location of the virus,
	 *             and the second is for random choice of neighbor while spreading
	 */
	public void simulateTN11CRuns(graph g, Pair<Integer, Integer>[] t0_runs, String pickleFilename, int[] seed) throws Exception
	{
		// Remove self-loops if any from the graph
		g.removeSelfLoops();
		System.out.println("Removed self-loops (if any) from the graph:");
		final int n = g.getG().vertexSet().size();
		final int minNode = g.getG().vertexSet().stream().mapToInt(v->v).min().orElseThrow(NoSuchElementException::new);
		final int maxNode = g.getG().vertexSet().stream().mapToInt(v->v).max().orElseThrow(NoSuchElementException::new);
		System.out.println("(new) network has "+n+" nodes and "+g.getG().edgeSet().size()+" edges.");
		
		if (seed.length!=2)
			throw new Exception("Seed array should be of length 2!");
		SplittableRandom initialLocationGenChoice = new SplittableRandom(seed[0]);
		SplittableRandom neighborGenChoice = new SplittableRandom(seed[1]);
		
		for (Pair<Integer, Integer> v: t0_runs)
		{
			int[] initialLocationRuns = initialLocationGenChoice.ints(v.getValue1(), minNode, maxNode+1).toArray();
			
			System.out.println("Starting "+v.getValue1()+" runs of simulating TN11C spread upto "+v.getValue0()
			+" time step for each run on the \""+g.getNetworkName()+"\" network...");
			for (int x=0; x<v.getValue1(); x++)
			{
				System.out.println("\t Simulation run "+(x+1));
				int initialLocation = initialLocationRuns[x];
				System.out.println("\t Initial location of virus: "+initialLocation);
				List<Integer> infected = new ArrayList<>(v.getValue0());
				
				// time step 0
				int currentInfected = initialLocation;
				infected.add(currentInfected);
				
				for (int t=1; t<=v.getValue0(); t++)
				{
					System.out.println("\t\t Time: "+t);
					currentInfected = getRandomInfectedNeighbor(g, neighborGenChoice, infected, currentInfected);
					infected.add(currentInfected);
					
				}
			}
		}
		
	}
	
	private int getRandomInfectedNeighbor(graph g, SplittableRandom neighborGenChoice, List<Integer> infected, int currentInfected)
	{
		List<Integer> currentNeighbors = Graphs.neighborListOf(g.getG(), currentInfected);
		int rnd = neighborGenChoice.nextInt(currentNeighbors.size());
		currentInfected = infected.get(rnd);
		return currentInfected;
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
	
	}
	
}
