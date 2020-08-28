import network.graph;
import org.javatuples.Pair;
import simulation.simulationRuns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class run
{
	public static void main(String[] args) throws Exception
	{
		graph network = new graph("testnetwork3");
		network.buildGraphFromFile("./files/networks/testnetwork3.txt");
		System.out.println(network.toString());
		
		int[] runs = {5, 10, 15};
		int[] t_0 = {3, 4};
		List<Pair<Integer, Integer>> t0_runs = getTimeRunPair(runs, t_0);
		int[] seed = {2507, 2101};
		boolean doNotUseSerialFile = false;
		String folder = "./out/production/VirusDetectionOptimizationModel/";
		String serialFilename = folder +network.getNetworkName()+"_simulationresults_fixedt0.ser";
		simulationRuns simulationResults = new simulationRuns();
		if (doNotUseSerialFile)
		{
			simulationResults.simulateTN11CRuns(network, t0_runs, seed);
		}
		else
		{
			simulationResults.loadTN11CRunsFromFile(serialFilename);
			// Check we have runs for all t0_runs
			simulationResults.simulateOnlyNecessaryTN11CRuns(network, t0_runs, seed);
		}
		System.out.println(simulationResults.getDictT0Runs().toString());
		simulationResults.serializeSimulationRuns(serialFilename);
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
