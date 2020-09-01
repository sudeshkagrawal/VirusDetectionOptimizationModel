import heuristic.nodeInMaxRowsGreedyHeuristic;
import network.graph;
import optimization.gurobiSolver;
import org.javatuples.Pair;
import org.jgrapht.alg.util.Triple;
import simulation.simulationRuns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class run
{
	public static void main(String[] args) throws Exception
	{
		String outputFolder = "./out/production/VirusDetectionOptimizationModel/";
		String networkName = "EUemailcomm_6-core";
		int[] runs = {1000, 5000, 10000, 30000, 50000};
		int[] t_0 = {3, 4};
		double r = 0.0;
		int[] seed = {2507, 2101, 3567};
		boolean doNotUseSerialFile = false;
		boolean append = false;
		
		int[] k = {100, 200, 250};
		String modelName = "TN11C";
		String heuristicOutputFilename = outputFolder + "heuristic_results.csv";
		String logFilename = outputFolder + "mip.log";
		String mipFormulationFilename = outputFolder + modelName +"_mip.lp";
		String mipOutputFilename = outputFolder + "mip_results.csv";
		int threads = 16;
		
		// Read Network
		graph network = new graph(networkName);
		network.buildGraphFromFile("./files/networks/"+networkName+".txt");
		//System.out.println(network.toString());
		
		// Simulations
		List<Pair<Integer, Integer>> t0_runs = getTimeRunPair(runs, t_0);
		String serialFilename = outputFolder + network.getNetworkName()+"_simulationresults_fixedt0.ser";
		simulationRuns simulationResults = new simulationRuns();
		boolean ranNewSimulations = true;
		if (doNotUseSerialFile)
			simulationResults.simulateTN11CRuns(network, t0_runs, r, seed);
		else
		{
			simulationResults.loadTN11CRunsFromFile(serialFilename);
			// Check we have runs for all t0_runs
			ranNewSimulations = simulationResults.simulateOnlyNecessaryTN11CRuns(network, t0_runs, r, seed);
		}
		//System.out.println(simulationResults.getMapModelNetworkT0RunsFalseNegativeToSimulationRuns().toString());
		//System.out.println(simulationResults.getMapModelNetworkT0RunsFalseNegativeToVirtualDetections().toString());
		if (ranNewSimulations)
			simulationResults.serializeTN11CRuns(serialFilename);
		
		
		// Heuristic
		List<Triple<Integer, Integer, Integer>> k_t0_runs = getHoneypotsTimeRunTriplet(runs, t_0, k);
		nodeInMaxRowsGreedyHeuristic heuristicResults = new nodeInMaxRowsGreedyHeuristic();
		heuristicResults.runSAAUsingHeuristic(modelName, network, simulationResults, k_t0_runs, r);
		//System.out.println(heuristicResults.toString());
		heuristicResults.writeToCSV(heuristicOutputFilename, append);
		
		// MIP
		gurobiSolver mipResults = new gurobiSolver();
		mipResults.solveSAA(modelName, network, simulationResults, k_t0_runs, r, threads, logFilename);
		//System.out.println(mipResults.toString());
		mipResults.writeToCSV(mipOutputFilename, append);
	}
	
	private static List<Triple<Integer, Integer, Integer>> getHoneypotsTimeRunTriplet(int[] runs, int[] t_0, int[] k)
	{
		List<Triple<Integer, Integer, Integer>> k_t0_runs = new ArrayList<>();
		for (int j : k)
		{
			for (int value : t_0)
				for (int run : runs)
					k_t0_runs.add(new Triple<>(j, value, run));
		}
		return k_t0_runs;
	}
	
	private static List<Pair<Integer, Integer>> getTimeRunPair(int[] runs, int[] t_0)
	{
		List<Pair<Integer, Integer>> t0_runs = new ArrayList<>(runs.length* t_0.length);
		Arrays.stream(t_0).forEach(time0 -> {
			for (int run : runs)
				t0_runs.add(new Pair<>(time0, run));
		});
		return t0_runs;
	}
}
