import dataTypes.parameters;
import network.graph;
import org.javatuples.Pair;
import org.jgrapht.alg.util.Triple;
import simulation.chooseTimeStep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class run
{
	public static void main(String[] args) throws Exception
	{
		String outputFolder = "./out/production/VirusDetectionOptimizationModel/";
		String networkName = "EUemailcomm_6-core";
		String modelName = "RAEPC";
		String separator = ",";
		int[] runs = {50000, 100000, 500000};
		int[] t_0 = {2, 3};
		List<Pair<Integer, Integer>> t0_runs = getTimeRunPair(runs, t_0);
		double r = 0.05;
		double p = 0.10;
		boolean doNotUseSerialFile = false;
		boolean append = true;

		String simulationsSerialFilename;
		String heuristicOutputFilename = outputFolder + "heuristic_results.csv";
		String mipLogFilename = outputFolder + modelName + "_mip.log";
		String mipFormulationFilename = outputFolder + modelName + "_mip.lp";
		String mipOutputFilename = outputFolder + "mip_results.csv";

		int[] k = {100, 200, 250};
		List<Triple<Integer, Integer, Integer>> k_t0_runs = getHoneypotsTimeRunTriplet(runs, t_0, k);

		int threads = 16;

		// Read Network
		graph network = new graph(networkName);
		network.buildGraphFromFile("./files/networks/"+networkName+".txt", separator);
		//System.out.println(network.toString());
//
//		// Simulations
//		simulationsSerialFilename = outputFolder + network.getNetworkName()+"_"+modelName+"_simulationresults_fixedt0.ser";
//		simulationRuns simulationResults = new simulationRuns();
//		boolean ranNewSimulations = true;
//		if (modelName=="TN11C")
//		{
//			int[]  seed = {2507, 2101, 3567};
//			if (doNotUseSerialFile)
//				simulationResults.simulateTN11CRuns(network, t0_runs, r, seed);
//			else
//			{
//				simulationResults.loadRunsFromFile(simulationsSerialFilename);
//				// Check we have runs for all t0_runs
//				ranNewSimulations = simulationResults.simulateOnlyNecessaryTN11CRuns(network, t0_runs, r, seed);
//			}
//		}
//		else
//		{
//			if (modelName=="RA1PC")
//			{
//				int[] seed = {2507, 2101, 2101, 3567};
//				if (doNotUseSerialFile)
//					simulationResults.simulateRA1PCRuns(network, t0_runs, r, p, seed);
//				else
//				{
//					simulationResults.loadRunsFromFile(simulationsSerialFilename);
//					// Check we have runs for all t0_runs
//					ranNewSimulations = simulationResults.simulateOnlyNecessaryRA1PCRuns(network, t0_runs, r, p, seed);
//				}
//			}
//			else if (modelName=="RAEPC")
//			{
//				int[]  seed = {2507, 2101, 3567};
//				if (doNotUseSerialFile)
//					simulationResults.simulateRAEPCRuns(network, t0_runs, r, p, seed);
//				else
//				{
//					simulationResults.loadRunsFromFile(simulationsSerialFilename);
//					// Check we have runs for all t0_runs
//					ranNewSimulations = simulationResults.simulateOnlyNecessaryRAEPCRuns(network, t0_runs, r, p, seed);
//				}
//			}
//		}
//		//System.out.println(simulationResults.getMapModelNetworkT0RunsFalseNegativeToSimulationRuns().toString());
//		//System.out.println(simulationResults.getMapModelNetworkT0RunsFalseNegativeToVirtualDetections().toString());
//		if (ranNewSimulations)
//			simulationResults.serializeRuns(simulationsSerialFilename);
//
//		// Heuristic
//		nodeInMaxRowsGreedyHeuristic heuristicResults = new nodeInMaxRowsGreedyHeuristic();
//		heuristicResults.runSAAUsingHeuristic(modelName, network, simulationResults, k_t0_runs, r, p);
//		//System.out.println(heuristicResults.toString());
//		heuristicResults.writeToCSV(heuristicOutputFilename, append);
//
//		// MIP
//		gurobiSolver mipResults = new gurobiSolver();
//		mipResults.solveSAA(modelName, network, simulationResults, k_t0_runs, r, p, threads, mipLogFilename);
//		//System.out.println(mipResults.toString());
//		mipResults.writeToCSV(mipOutputFilename, append);
		
		chooseTimeStep timeStepSimulationResults = new chooseTimeStep();
		parameters param = new parameters(modelName, networkName, 3,
				100000, 0.5, 0.75, 10, 0.1);
		List<parameters> params = new ArrayList<>();
		params.add(param);
		int[]  seed = {2507, 2101, 3567};
		timeStepSimulationResults.TN1PCSimulationRuns(network, new ArrayList<parameters>(params), seed);
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
