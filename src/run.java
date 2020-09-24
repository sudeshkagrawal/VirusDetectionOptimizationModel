import algorithm.degreeCentrality;
import algorithm.degreeDiscount;
import algorithm.nodeInMaxRowsGreedyHeuristic;
import analysis.McNemarsProcedure;
import analysis.samplingErrors;
import dataTypes.parameters;
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
		String separator = ",";
		String modelName = "TN11C";
		int[] runs = {1000, 5000, 10000, 30000, 50000};
		int[] t_0 = {4};
		List<Pair<Integer, Integer>> t0_runs = getTimeRunPair(runs, t_0);
		double r = 0;
		double p = 1;
		boolean doNotUseSerialFile = false;
		boolean append = true;
		int sampleSize = 2000000;
		
		String networkInfoFilename = outputFolder + "network_info.csv";
		String simulationsSerialFilename;
		String mipLogFilename = outputFolder + modelName + "_mip.log";
		String mipFormulationFilename = outputFolder + modelName + "_mip.lp";
		String mipOutputFilename = outputFolder + "mip_results.csv";
		String mipSerialFilename = outputFolder + "mip_results.ser";
		String heuristicOutputFilename = outputFolder + "heuristic_results.csv";
		String degreeCentralityOutputFilename = outputFolder + "degreeCentrality_results.csv";
		String degreeDiscountOutputFilename = outputFolder + "degreeDiscount_results.csv";
		String MRPOutputFilename = outputFolder + "heuristic_quality_gap_estimate.csv";
		String comparisonOutputFilename = outputFolder + "compare_mip_and_heuristic.csv";
		String samplingErrorsAlgoFilename = outputFolder + "heuristic_point_estimates.csv";
		String samplingErrorsMIPFilename = outputFolder + "mip_point_estimates.csv";
		
		int[] k = {200};
		List<Triple<Integer, Integer, Integer>> k_t0_runs = getHoneypotsTimeRunTriplet(runs, t_0, k);
		
		List<parameters> listOfParams = new ArrayList<>();
		for (int timeStep: t_0)
		{
			for (int numberOfSimulationRepetitions: runs)
			{
				for (int numberOfHoneypots: k)
				{
					listOfParams.add(new parameters(modelName, networkName, timeStep, numberOfSimulationRepetitions, r,
							p, numberOfHoneypots, 0));
				}
			}
		}
		
		// Read Network
		graph network = new graph(networkName);
		network.buildGraphFromFile("./files/networks/"+networkName+".txt", separator);
		network.removeSelfLoops();
		network.changeGraphToLargestConnectedComponent();
		//network.writeNetworkInfoToCSV(networkInfoFilename, append);
		
		// Simulations
		simulationsSerialFilename = outputFolder + network.getNetworkName()+"_"+modelName+"_simulationresults_fixedt0.ser";
		simulationRuns simulationResults = new simulationRuns();
		boolean ranNewSimulations = true;
		if (modelName.equals("TN11C"))
		{
			int[]  seed = {2507, 2101, 3567};
			if (doNotUseSerialFile)
				simulationResults.simulateTN11CRuns(network, t0_runs, r, seed);
			else
			{
				simulationResults.loadRunsFromFile(simulationsSerialFilename);
				// Check we have runs for all t0_runs
				ranNewSimulations = simulationResults.simulateOnlyNecessaryTN11CRuns(network, t0_runs, r, seed);
			}
		}
		else
		{
			if (modelName.equals("RA1PC"))
			{
				int[] seed = {2507, 2101, 2101, 3567};
				if (doNotUseSerialFile)
					simulationResults.simulateRA1PCRuns(network, t0_runs, r, p, seed);
				else
				{
					simulationResults.loadRunsFromFile(simulationsSerialFilename);
					// Check we have runs for all t0_runs
					ranNewSimulations = simulationResults.simulateOnlyNecessaryRA1PCRuns(network, t0_runs, r, p, seed);
				}
			}
			else if (modelName.equals("RAEPC"))
			{
				int[]  seed = {2507, 2101, 3567};
				if (doNotUseSerialFile)
					simulationResults.simulateRAEPCRuns(network, t0_runs, r, p, seed);
				else
				{
					simulationResults.loadRunsFromFile(simulationsSerialFilename);
					// Check we have runs for all t0_runs
					ranNewSimulations = simulationResults.simulateOnlyNecessaryRAEPCRuns(network, t0_runs, r, p, seed);
				}
			}
		}
		if (ranNewSimulations)
			simulationResults.serializeRuns(simulationsSerialFilename);

		// MIP
		int threads = 16;
		int timeLimit = 300;
		gurobiSolver mipResults = new gurobiSolver();
		mipResults.solveSAA(network, simulationResults, listOfParams, threads, timeLimit, mipLogFilename);
		//System.out.println(mipResults.toString());
		mipResults.writeToCSV(mipOutputFilename, append);
		
		// Heuristic
		nodeInMaxRowsGreedyHeuristic heuristicResults = new nodeInMaxRowsGreedyHeuristic();
		heuristicResults.runSAAUsingHeuristic(network, simulationResults, listOfParams);
		//System.out.println(heuristicResults.toString());
		heuristicResults.writeToCSV(heuristicOutputFilename, append);

		// Degree centrality
		degreeCentrality degreeCentralityResults = new degreeCentrality();
		degreeCentralityResults.runSAAUsingKHighestDegreeNodes(network, simulationResults, listOfParams);
		//System.out.println(degreeCentralityResults.toString());
		degreeCentralityResults.writeToCSV(degreeCentralityOutputFilename, append);

		// Degree discount
		degreeDiscount degreeDiscountResults = new degreeDiscount();
		degreeDiscountResults.runSAAUsingKHighestDegreeSingleDiscountNodes(network, simulationResults,
																			listOfParams);
		//System.out.println(degreeDiscountResults.toString());
		degreeDiscountResults.writeToCSV(degreeDiscountOutputFilename, append);

//		// Multiple Replications Procedure
//		multipleReplicationsProcedure MRPResults = new multipleReplicationsProcedure();
//		MRPResults.estimateGap(network, heuristicResults.getOutputMap(), 0.05, 100000, 20,
//								"greedy");
//		MRPResults.writeToCSV(MRPOutputFilename, append);
		
		// McNemar's procedure to compare MIP and greedy heuristic
		McNemarsProcedure comparisonResults = new McNemarsProcedure();
		comparisonResults.compareMIPAndHeuristic(network, heuristicResults.getOutputMap(),
				mipResults.getOutputMap(), 0.05, 2000000);
		comparisonResults.writeToCSV(comparisonOutputFilename, append);
		
		// Point estimates and sampling errors
		samplingErrors statisticalEstimates = new samplingErrors();
		statisticalEstimates.getPointEstimatesAndErrorsForAlgos(network, heuristicResults.getOutputMap(), 0.05,
				100000);
		statisticalEstimates.writeToCSV(samplingErrorsAlgoFilename, append);
		statisticalEstimates.getPointEstimatesAndErrorsForMIP(network, mipResults.getOutputMap(), 0.05,
				100000);
		statisticalEstimates.writeToCSV(samplingErrorsMIPFilename, append);
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
