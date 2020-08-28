package simulation;

import network.graph;
import org.javatuples.Pair;
import org.jgrapht.Graphs;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

public class simulationRuns
{
	Map<Pair<Integer, Integer>, List<List<Integer>>> dictT0Runs;
	
	public simulationRuns(Map<Pair<Integer, Integer>, List<List<Integer>>> dictT0Runs)
	{
		this.dictT0Runs = dictT0Runs;
	}
	public simulationRuns()
	{
		dictT0Runs = new HashMap<>();
	}
	
	
	
	public Map<Pair<Integer, Integer>, List<List<Integer>>> getDictT0Runs()
	{
		return dictT0Runs;
	}
	
	/**
	 * Function to simulate several runs of TN11C spread models where detectors are completely reliable.
	 * See Lee, Jinho. Stochastic optimization models for rapid detection of viruses in cellphone networks. Diss. 2012.
	 *
	 * @param g network graph
	 * @param t0_runs array of a pair of (t0, runs), where t0 is simulation time, and runs is number of repetitions of simulation
	 * @param seed an array of length 2; the first seed is for the initial random location of the virus,
	 *             and the second is for random choice of neighbor while spreading
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
					currentInfected = getRandomInfectedNeighbor(g, neighborGenChoice, infected, currentInfected);
					infected.add(currentInfected);
					//System.out.println("\t\t Current infected node: "+currentInfected);
				}
				samplePathRuns.add(infected);
			}
			System.out.println("Ending "+rep+" runs of simulating TN11C spread upto "+time0
					+" time step for each run on the \""+g.getNetworkName()+"\" network...");
			dictT0Runs.put(new Pair<>(time0, rep), samplePathRuns);
		}
	}
	
	private int getRandomInfectedNeighbor(graph g, SplittableRandom neighborGenChoice, List<Integer> infected, int currentInfected)
	{
		List<Integer> currentNeighbors = Graphs.neighborListOf(g.getG(), currentInfected);
		int rnd = neighborGenChoice.nextInt(currentNeighbors.size());
		currentInfected = currentNeighbors.get(rnd);
		return currentInfected;
	}
	
	public void serializeSimulationRuns(String serialFilename)
	{
		try
		{
			FileOutputStream fout = new FileOutputStream(serialFilename);
			BufferedOutputStream bout = new BufferedOutputStream(fout);
			ObjectOutputStream objout = new ObjectOutputStream(bout);
			objout.writeObject(dictT0Runs);
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
