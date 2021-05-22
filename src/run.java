import analysis.compareHoneypots;
import dataTypes.parameters;
import network.graph;
import org.javatuples.Pair;
import org.jgrapht.alg.util.Triple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class run
{
	public static void main(String[] args) throws Exception
	{
		String outputFolder = "./out/production/VirusDetectionOptimizationModel/";
		String networkName = "EUemailcomm_35-core";
		String separator = ",";
		
		// Read Network
		graph network = new graph(networkName);
		network.buildGraphFromFile("./files/networks/"+networkName+".txt", separator);
		network.removeSelfLoops();
		network.changeGraphToLargestConnectedComponent();
		//network.writeNetworkInfoToCSV(networkInfoFilename, append);
		
//		// ----------------------------------------------------------------------------------------------------
//		// FIND TIME STEPS
//		String modelNameForTimeSteps = "RA1PC";
//		String timeStepsResultsFilename = outputFolder + "simulation_results_estimating_time_threshold.csv";
//		chooseTimeStep timeStepSimulationResults = new chooseTimeStep();
//		List<parameters> listOfParamsForTimeSteps = new ArrayList<>();
//		double[] percentInfections = {0.1, 0.5, 1, 5};
//		int[] runsForTimeSteps = {50000, 100000};
//		double[] pForTimeSteps = {0.25, 0.5, 0.75, 1};
//		for (double pi: percentInfections)
//		{
//			for (int reps: runsForTimeSteps)
//			{
//				for (double trans: pForTimeSteps)
//				{
//					listOfParamsForTimeSteps.add(new parameters(modelNameForTimeSteps, networkName, 3,
//							reps, 0, trans, 0, pi));
//				}
//			}
//		}
//		if (modelNameForTimeSteps.equals("TN11C") || modelNameForTimeSteps.equals("TN1PC")
//				|| modelNameForTimeSteps.equals("RAEPC"))
//		{
//			int[]  seedForTimeSteps = {5072, 1012};
//			timeStepSimulationResults.doSimulationRuns(network, listOfParamsForTimeSteps, seedForTimeSteps);
//		}
//		else //if (modelName.equals("RA1PC"))
//		{
//			int[] seedForTimeSteps = {5072, 1012, 5673};
//			timeStepSimulationResults.doSimulationRuns(network, listOfParamsForTimeSteps, seedForTimeSteps);
//		}
//		timeStepSimulationResults.writeToCSV(timeStepsResultsFilename, true);
//		// ----------------------------------------------------------------------------------------------------
		
		
		String modelName = "RA1PC";
		int[] runs = {1000, 5000, 10000, 30000, 50000};
		int[] t_0 = {2};
		//int[] k = {5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 60, 70, 80, 90, 100, 125, 150, 175, 200};
		int[] k = {25, 50, 100};
		double r = 0.0;
		double p = 0.75;
		List<Pair<Integer, Integer>> t0_runs = getTimeRunPair(runs, t_0);
		//List<Triple<Integer, Integer, Integer>> k_t0_runs = getHoneypotsTimeRunTriplet(runs, t_0, k);

		boolean doNotUseSerialFile = false;
		boolean doNotUseMIPResultsInCSVFile = false;
		boolean append = true;

//		//String networkInfoFilename = outputFolder + "network_info.csv";
//		String simulationsSerialFilename;
//		String mipLogFilename = outputFolder + modelName + "_mip.log";
//		//String mipFormulationFilename = outputFolder + modelName + "_mip.lp";
//		String mipOutputFilename = outputFolder + "mip_results_"+modelName+".csv";
//		//String mipSerialFilename = outputFolder + "mip_results_"+modelName+".ser";
//		String heuristicOutputFilename = outputFolder + "heuristic_results_"+modelName+".csv";
//		//String heuristicOutputFilename = outputFolder + "heuristic_results_forgraph_"+modelName+"_runs_"+runs[0]
//		//									+"_t"+t_0[0]+"_p"+(int) (100*p)+"_r"+(int) (100*r)+".csv";
//		//String heuristicOutputFilename = outputFolder + "heuristic_results_"+modelName+"_t"+t_0[0]+"_p"+(int) (100*p)
//		//												+"_k"+k[0]+"_r"+(int) (100*r)+".csv";
//		//String degreeCentralityOutputFilename = outputFolder + "degreeCentrality_results_"+modelName+".csv";
//		//String degreeDiscountOutputFilename = outputFolder + "degreeDiscount_results_"+modelName+".csv";
//		//String MRPOutputFilename = outputFolder + "heuristic_quality_gap_estimate_"+modelName+".csv";
//		//String comparisonOutputFilename = outputFolder + "compare_mip_and_heuristic_"+modelName+".csv";
//		String samplingErrorsAlgoFilename = outputFolder + "heuristic_point_estimates_"+modelName+".csv";
//		//String samplingErrorsMIPFilename = outputFolder + "mip_point_estimates_"+modelName+".csv";
//
//		List<parameters> listOfParams = new ArrayList<>();
//		for (int timeStep: t_0)
//		{
//			for (int numberOfSimulationRepetitions: runs)
//			{
//				for (int numberOfHoneypots: k)
//				{
//					listOfParams.add(new parameters(modelName, networkName, timeStep, numberOfSimulationRepetitions, r,
//							p, numberOfHoneypots, 0));
//				}
//			}
//		}
//
//
//		// Simulations
//		simulationsSerialFilename = outputFolder
//									+ network.getNetworkName()+"_"+modelName
//									+"_r"+(int) (100*r)+"_p"+(int) (100*p)+"_simulationresults_fixedt0.ser";
//		simulationRuns simulationResults = new simulationRuns();
//		boolean ranNewSimulations = true;
//		if (modelName.equals("TN11C"))
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
//			if (modelName.equals("RA1PC"))
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
//			else if (modelName.equals("RAEPC"))
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
//		if (ranNewSimulations)
//			simulationResults.serializeRuns(simulationsSerialFilename);
//
//		// MIP
//		int threads = 1;
//		int timeLimit = 3600;
//		boolean ranNewOptimization = true;
//		gurobiSolver mipResults = new gurobiSolver();
//		if (doNotUseMIPResultsInCSVFile)
//		{
//			mipResults.solveSAA(network, simulationResults, listOfParams, threads, timeLimit, mipLogFilename);
//		}
//		else
//		{
//			mipResults.loadResultsFromCSVFile(mipOutputFilename);
//			ranNewOptimization = mipResults.solveSAAOnlyNecessaryOnes(network, simulationResults, listOfParams,
//																		threads, timeLimit, mipLogFilename);
//		}
//		//System.out.println(mipResults.toString());
//		if (ranNewOptimization)
//			mipResults.writeToCSV(mipOutputFilename, append);

//		// Heuristic
//		nodeInMaxRowsGreedyHeuristic heuristicResults = new nodeInMaxRowsGreedyHeuristic();
//		heuristicResults.runSAAUsingHeuristic(network, simulationResults, listOfParams);
//		//System.out.println(heuristicResults.toString());
//		heuristicResults.writeToCSV(heuristicOutputFilename, append);

//		// Degree centrality
//		degreeCentrality degreeCentralityResults = new degreeCentrality();
//		degreeCentralityResults.runSAAUsingKHighestDegreeNodes(network, simulationResults, listOfParams);
//		//System.out.println(degreeCentralityResults.toString());
//		degreeCentralityResults.writeToCSV(degreeCentralityOutputFilename, append);
//
//		// Degree discount
//		degreeDiscount degreeDiscountResults = new degreeDiscount();
//		degreeDiscountResults.runSAAUsingKHighestDegreeSingleDiscountNodes(network, simulationResults,
//																			listOfParams);
//		//System.out.println(degreeDiscountResults.toString());
//		degreeDiscountResults.writeToCSV(degreeDiscountOutputFilename, append);
//
//		// Multiple Replications Procedure
//		multipleReplicationsProcedure MRPResults = new multipleReplicationsProcedure();
//		MRPResults.estimateGap(network, heuristicResults.getOutputMap(), 0.05, 500000, 20,
//								"greedy");
//		MRPResults.writeToCSV(MRPOutputFilename, append);
//
//		// McNemar's procedure to compare MIP and greedy heuristic
//		McNemarsProcedure comparisonResults = new McNemarsProcedure();
//		comparisonResults.compareMIPAndHeuristic(network, heuristicResults.getOutputMap(),
//				mipResults.getOutputMap(), 0.05, 2000000);
//		comparisonResults.writeToCSV(comparisonOutputFilename, append);
//
//		// Point estimates and sampling errors
//		samplingErrors statisticalEstimates = new samplingErrors();
//		statisticalEstimates.getPointEstimatesAndErrorsForAlgos(network, heuristicResults.getOutputMap(), 0.05,
//				2000000);
//		statisticalEstimates.writeToCSV(samplingErrorsAlgoFilename, append);
//		statisticalEstimates.getPointEstimatesAndErrorsForMIP(network, mipResults.getOutputMap(), 0.05,
//				2000000);
//		statisticalEstimates.writeToCSV(samplingErrorsMIPFilename, append);
	
		// Cost of modeling false negative
		int outSampleSize = 5000000;
		String compareModelName = "RA1PC";
		boolean compareAppend = true;
		int[] compareRuns = {50000};
		int[] compareT0s= {3};
		//int[] compareKs = {100, 200, 250};
		int[] compareKs = {25, 50, 100};
		double[] compareRs = {0.05, 0.1, 0.25, 0.3, 0.4, 0.5};
		//double[] compareRs = {0.05};
		double compareP = 0.5;
		List<Pair<parameters, parameters>> compareParams = new ArrayList<>();
		
		
		
		for (int compareRun: compareRuns)
		{
			for (int compareT0: compareT0s)
			{
				for (int compareK: compareKs)
				{
					for (double compareR: compareRs)
					{
						parameters param1 = new parameters(compareModelName, networkName, compareT0,
												compareRun, 0, compareP, compareK, 0.0);
						parameters param2 = new parameters(compareModelName, networkName, compareT0,
												compareRun, compareR, compareP, compareK, 0.0);
						compareParams.add(new Pair<>(param1, param2));
					}
				}
			}
		}
		
		String costOfFNModelFilename = outputFolder + "cost_of_FNmodel_"+networkName+"_"+compareModelName+".csv";
		//String costOfFNModelFilename = outputFolder + "cost_of_FNmodel_"+networkName+"_"
		//								+compareModelName+"_varyHoneypots_R5_P50.csv";
		compareHoneypots costOfFNModel = new compareHoneypots();
		costOfFNModel.evaluateHoneypotsOnFalseNegativeModel(network, compareParams, outSampleSize);
		costOfFNModel.writeToCSV(costOfFNModelFilename, compareAppend);
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
