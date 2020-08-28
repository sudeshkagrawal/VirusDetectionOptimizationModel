import network.graph;
import org.javatuples.Pair;
import simulation.simulationRuns;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class run
{
	public static void main(String[] args) throws Exception
	{
		graph network = new graph("testnetwork3");
		network.buildGraphFromFile("./files/networks/testnetwork3.txt");
		System.out.println(network.toString());
		
		int[] runs = {5, 10, 15, 20};
		int[] t_0 = {3, 4, 5, 6};
		List<Pair<Integer, Integer>> t0_runs = getTimeRunPair(runs, t_0);
		int[] seed = {2507, 2101};
		
		simulationRuns simulationResults = null;
		
		String folder = "./out/production/VirusDetectionOptimizationModel/";
		String serialFilename = folder +network.getNetworkName()+"_simulationresults_fixedt0.ser";
		boolean doNotUseSerialFile = false;
		// if serialized file not there
		try
		{
			if (doNotUseSerialFile)
				throw new FileNotFoundException("User does not wish to use serialized object on file!");
			FileInputStream fin = new FileInputStream(serialFilename);
			BufferedInputStream bin = new BufferedInputStream(fin);
			ObjectInputStream objin = new ObjectInputStream(bin);
			Map<Pair<Integer, Integer>, List<List<Integer>>> serobjin = (Map) objin.readObject();
			simulationResults = new simulationRuns(serobjin);
			objin.close();
			bin.close();
			fin.close();
			System.out.println("Using simulation results in \"" + serialFilename + "\".");
			
			// Check we have runs for all t0_runs
			List<Pair<Integer, Integer>> new_t0_runs = new ArrayList<>();
			runMoreSimulationsIfNeeded(network, t0_runs, seed, simulationResults, new_t0_runs);
			
		} catch (FileNotFoundException e1)
		{
			System.out.println(e1.getMessage());
			System.out.println("Running simulation...");
			simulationResults = new simulationRuns();
			simulationResults.simulateTN11CRuns(network, t0_runs, seed);
		} catch (IOException e2)
		{
			System.out.println("Input-Output Exception:");
			e2.printStackTrace();
			System.out.println("Exiting the program...");
			System.exit(0);
		}
		catch (Exception e3)
		{
			System.out.println("An exception occurred:");
			e3.printStackTrace();
			System.out.println("Exiting the program...");
			System.exit(0);
		}
		
		
		System.out.println(simulationResults.getDictT0Runs().toString());
		simulationResults.serializeSimulationRuns(serialFilename);
	}
	
	private static void runMoreSimulationsIfNeeded(graph network, List<Pair<Integer, Integer>> t0_runs, int[] seed, simulationRuns simulationResults, List<Pair<Integer, Integer>> new_t0_runs) throws Exception
	{
		for (Pair<Integer, Integer> time0_run : t0_runs)
		{
			if (!simulationResults.getDictT0Runs().containsKey(time0_run))
				new_t0_runs.add(time0_run);
		}
		if (new_t0_runs.size() > 0)
		{
			System.out.println("Running more simulations for: " + new_t0_runs.toString());
			simulationResults.simulateTN11CRuns(network, new_t0_runs, seed);
		}
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
