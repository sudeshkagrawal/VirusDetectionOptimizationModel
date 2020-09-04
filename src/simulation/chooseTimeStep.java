package simulation;

import dataTypes.parameters;
import network.graph;
import org.jgrapht.Graphs;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Simulation runs to choose appropriate time step for a given spread model and parameters.
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: September 4, 2020.
 */
public class chooseTimeStep
{
	/**
	 * A map from a 7-tuple to the simulation runs.
	 * 7-tuple: (model (TN11C, RAEPC, etc.), network name, time step, repetitions,
	 * false negative probability, transmissability (p), number of honeypots).
	 * 7-tuple is represented through {@code parameters} class.
	 */
	Map<parameters, List<List<Integer>>> mapParametersToSimulationRuns;
	/**
	 * A map from a 7-tuple to the time steps required to get a given percentage infection.
	 * 7-tuple: (model (TN11C, RAEPC, etc.), network name, time step, repetitions,
	 * false negative probability, transmissability (p), number of honeypots).
	 * 7-tuple is represented through {@code parameters} class.
	 * Each element in the list is the time steps for a simulation repetition.
	 */
	Map<parameters, List<Integer>> mapParametersToTimeForInfection;
	/**
	 * A map from a 7-tuple to the average time steps required to get a given percentage infection.
	 * 7-tuple: (model (TN11C, RAEPC, etc.), network name, time step, repetitions,
	 * false negative probability, transmissability (p), number of honeypots).
	 * 7-tuple is represented through {@code parameters} class.
	 */
	Map<parameters, Double> mapParametersToMeanInfectionTime;
	
	/**
	 * Constructor.
	 *
	 * @param mapParametersToSimulationRuns a map from {@code parameters} to the simulation runs
	 * @param mapParametersToTimeForInfection A map from {@code parameters} to
	 *                                        the time steps required to get a given percentage infection
	 * @param mapParametersToMeanInfectionTime A map from {@code parameters} to
	 *                                         the average time steps required to get a given percentage infection.
	 */
	public chooseTimeStep(Map<parameters, List<List<Integer>>> mapParametersToSimulationRuns,
	                      Map<parameters, List<Integer>> mapParametersToTimeForInfection,
	                      Map<parameters, Double> mapParametersToMeanInfectionTime)
	{
		this.mapParametersToSimulationRuns = mapParametersToSimulationRuns;
		this.mapParametersToTimeForInfection = mapParametersToTimeForInfection;
		this.mapParametersToMeanInfectionTime = mapParametersToMeanInfectionTime;
	}
	
	/**
	 * Constructor.
	 */
	public chooseTimeStep()
	{
		this(new HashMap<>(), new HashMap<>(), new HashMap<>());
	}
	
	/**
	 * Getter.
	 *
	 * @return returns {@code mapParametersToSimulationRuns}.
	 */
	public Map<parameters, List<List<Integer>>> getMapParametersToSimulationRuns()
	{
		return mapParametersToSimulationRuns;
	}
	
	/**
	 * Getter.
	 *
	 * @return returns {@code mapParametersToTimeForInfection}.
	 */
	public Map<parameters, List<Integer>> getMapParametersToTimeForInfection()
	{
		return mapParametersToTimeForInfection;
	}
	
	/**
	 * Getter.
	 *
	 * @return returns {@code mapParametersToMeanInfectionTime}.
	 */
	public Map<parameters, Double> getMapParametersToMeanInfectionTime()
	{
		return mapParametersToMeanInfectionTime;
	}
	
	/**
	 * Function to determine what values of t_0 are appropriate for TN1PC spread dynamics.
	 * See Lee, Jinho. Stochastic optimization models for rapid detection of viruses in cellphone networks. Diss. 2012.
	 *
	 * @param g network graph
	 * @param params parameters needed for the simulation run; for example, no. of runs; %age infection, etc.
	 * @param seed an array of length 3;
	 *             the first seed is for the initial random location of the virus,
	 *             the second is for random choice of neighbor while spreading,
	 *             and the third is for transmissability.
	 * @throws Exception exception thrown if length of {@code seed} is not 3.
	 */
	public void TN1PCSimulationRuns(graph g, List<parameters> params, int[] seed) throws Exception
	{
		// Remove self-loops if any from the graph
		System.out.println("Network has "+g.getG().vertexSet().size()
				+" nodes and "+g.getG().edgeSet().size()+" edges.");
		g.removeSelfLoops();
		System.out.print("Removed self-loops (if any) from the graph: ");
		final int n = g.getG().vertexSet().size();
		System.out.println("(new) network has "+n+" nodes and "+g.getG().edgeSet().size()+" edges.");
		
		if ((seed.length!=2) && (seed.length!=3))
			throw new Exception("Seed array should be of length 3!");
		
		System.out.println("Starting simulation runs...");
		for(parameters param : params)
		{
			System.out.println("\t "+param.toString());
			double pi = param.getPercentInfection();
			int rep = param.getNumberOfSimulationRepetitions();
			double p = param.getTransmissability();
			int numberOfNodesInfected = (int) Math.floor(pi*n*0.01);
			System.out.println("\t Number of nodes to be infected = "+numberOfNodesInfected);
			
			SplittableRandom initialLocationGenChoice =
					new SplittableRandom(seed[0]+Double.doubleToLongBits(pi)+rep+Double.doubleToLongBits(p));
			SplittableRandom neighborGenChoice =
					new SplittableRandom(seed[1]+Double.doubleToLongBits(pi)+rep+Double.doubleToLongBits(p));
			SplittableRandom transmissableGen =
					new SplittableRandom(seed[2]+Double.doubleToLongBits(pi)+rep+Double.doubleToLongBits(p));
			//int[] initialLocationRuns = getInitialLocationRuns(g, initialLocationGenChoice, rep);
			List<Integer> nodes = new ArrayList<>(g.getG().vertexSet());
			int[] initialLocationRuns = IntStream.range(0, rep)
										.map(i -> nodes.get(initialLocationGenChoice.nextInt(0, n))).toArray();
			
			List<Integer> t0Runs = new ArrayList<>(rep);
			List<List<Integer>> samplePathRuns = new ArrayList<>(rep);
			for (int x=0; x<rep; x++)
			{
				//System.out.println("\n\t\t Simulation run "+(x+1));
				int initialLocation = initialLocationRuns[x];
				//System.out.println("\t\t Initial location of virus: "+initialLocation);
				Set<Integer> infected = new HashSet<>(numberOfNodesInfected);
				
				// time step 0
				int currentInfected = initialLocation;
				infected.add(currentInfected);
				
				
				int t=1;
				while (infected.size()<numberOfNodesInfected)
				{
					//System.out.println("\t\t\t Time: "+t);
					double transmissable = transmissableGen.nextDouble(0, 1);
					if (transmissable<=p)
					{
						List<Integer> currentNeighbors = Graphs.neighborListOf(g.getG(), currentInfected);
						currentInfected = currentNeighbors.get(neighborGenChoice.nextInt(currentNeighbors.size()));
						infected.add(currentInfected);
					}
					//System.out.println("\t\t\t\t Current infected node: "+currentInfected);
					t++;
				}
				t0Runs.add(t-1);
				samplePathRuns.add(new ArrayList<>(infected));
				//System.out.println("\t\t Infected nodes: "+infected.toString());
			}
			double meanInfectionTime = t0Runs.stream().mapToInt(Integer::intValue).average().orElseThrow();
			mapParametersToSimulationRuns.put(param, samplePathRuns);
			mapParametersToTimeForInfection.put(param, t0Runs);
			mapParametersToMeanInfectionTime.put(param, meanInfectionTime);
			//System.out.println("\t Infection times: "+t0Runs);
			System.out.println("\t Mean infection time: "+meanInfectionTime);
		}
		System.out.println("Ending simulation runs...");
	}
	
	/**
	 * Function to determine what values of t_0 are appropriate for RA1PC spread dynamics.
	 * See Lee, Jinho. Stochastic optimization models for rapid detection of viruses in cellphone networks. Diss. 2012.
	 *
	 * @param g network graph
	 * @param params parameters needed for the simulation run; for example, no. of runs; %age infection, etc.
	 * @param seed an array of length 3;
	 *             the first seed is for the initial random location of the virus,
	 *             the second is for random choice of neighbor while spreading,
	 *             and the third is for transmissability.
	 * @throws Exception exception thrown if length of {@code seed} is not 3.
	 */
	public void RA1PCSimulationRuns(graph g, List<parameters> params, int[] seed) throws Exception
	{
		// Remove self-loops if any from the graph
		System.out.println("Network has "+g.getG().vertexSet().size()
				+" nodes and "+g.getG().edgeSet().size()+" edges.");
		g.removeSelfLoops();
		System.out.print("Removed self-loops (if any) from the graph: ");
		final int n = g.getG().vertexSet().size();
		System.out.println("(new) network has "+n+" nodes and "+g.getG().edgeSet().size()+" edges.");
		
		if ((seed.length!=2) && (seed.length!=3))
			throw new Exception("Seed array should be of length 3!");
		
		System.out.println("Starting simulation runs...");
		for(parameters param : params)
		{
			System.out.println("\t "+param.toString());
			double pi = param.getPercentInfection();
			int rep = param.getNumberOfSimulationRepetitions();
			double p = param.getTransmissability();
			int numberOfNodesInfected = (int) Math.floor(pi*n*0.01);
			System.out.println("\t Number of nodes to be infected = "+numberOfNodesInfected);
			
			SplittableRandom initialLocationGenChoice =
					new SplittableRandom(seed[0]+Double.doubleToLongBits(pi)+rep+Double.doubleToLongBits(p));
			SplittableRandom neighborGenChoice =
					new SplittableRandom(seed[1]+Double.doubleToLongBits(pi)+rep+Double.doubleToLongBits(p));
			SplittableRandom transmissableGen =
					new SplittableRandom(seed[2]+Double.doubleToLongBits(pi)+rep+Double.doubleToLongBits(p));
			
			List<Integer> nodes = new ArrayList<>(g.getG().vertexSet());
			int[] initialLocationRuns = IntStream.range(0, rep)
					.map(i -> nodes.get(initialLocationGenChoice.nextInt(0, n))).toArray();
			
			List<Integer> t0Runs = new ArrayList<>(rep);
			List<List<Integer>> samplePathRuns = new ArrayList<>(rep);
			for (int x=0; x<rep; x++)
			{
				System.out.println("\n\t\t Simulation run "+(x+1));
				int initialLocation = initialLocationRuns[x];
				System.out.println("\t\t Initial location of virus: "+initialLocation);
				Set<Integer> infected = new HashSet<>(numberOfNodesInfected);
				
				// time step 0
				infected.add(initialLocation);
				
				
				int t=1;
				while (infected.size()<numberOfNodesInfected)
				{
					System.out.println("\t\t\t Time: "+t);
					List<Integer> tmpInfected = infected.stream()
							.mapToInt(node -> {
								List<Integer> currentNeighbors = Graphs.neighborListOf(g.getG(), node);
								return currentNeighbors.get(neighborGenChoice.nextInt(currentNeighbors.size()));
							})
							.filter(currentTarget -> transmissableGen.nextDouble() <= p)
							.boxed().collect(Collectors.toList());
					infected.addAll(tmpInfected);
					System.out.println("\t\t\t\t Nodes infected: "+tmpInfected);
					t++;
				}
				t0Runs.add(t-1);
				samplePathRuns.add(new ArrayList<>(infected));
				System.out.println("\t\t Infected nodes: "+infected.toString());
			}
			double meanInfectionTime = t0Runs.stream().mapToInt(Integer::intValue).average().orElseThrow();
			mapParametersToSimulationRuns.put(param, samplePathRuns);
			mapParametersToTimeForInfection.put(param, t0Runs);
			mapParametersToMeanInfectionTime.put(param, meanInfectionTime);
			System.out.println("\t Infection times: "+t0Runs);
			System.out.println("\t Mean infection time: "+meanInfectionTime);
		}
		System.out.println("Ending simulation runs...");
	}
	
	/**
	 * Overrides {@code toString}.
	 *
	 * @return returns a string representation of all the fields in the class.
	 */
	@Override
	public String toString()
	{
		return "chooseTimeStep{" +
				"\nmapParametersToSimulationRuns=" + mapParametersToSimulationRuns +
				", \nmapParametersToTimeForInfection=" + mapParametersToTimeForInfection +
				", \nmapParametersToMeanInfectionTime=" + mapParametersToMeanInfectionTime +
				"\n}";
	}
}
