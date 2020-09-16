package algorithm;

import network.graph;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Unit test for {@code nodeInMaxRowsGreedyHeuristic}.
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: September 16, 2020.
 */
class nodeInMaxRowsGreedyHeuristicTest
{
	
	@Test
	void calculateDelta() throws Exception
	{
		// TEST 1
		// build graph
		String networkName = "EUemailcomm_6-core";
		graph network = new graph(networkName);
		String separator = ",";
		network.buildGraphFromFile("./files/networks/"+networkName+".txt", separator);
		
		// create simulation results
		String simulationResultsFile = "./files/dummySimulationRunsForEUemailcomm6core_1.txt";
		List<List<Integer>> simulationResults = new ArrayList<>(15);
		BufferedReader br = new BufferedReader(new FileReader(simulationResultsFile));
		String line = br.readLine();
		while (line!=null)
		{
			List<Integer> samplePath = Arrays.stream(line.split(separator))
										.mapToInt(e -> Integer.parseInt(e.trim())).boxed().collect(Collectors.toList());
			simulationResults.add(samplePath);
			line = br.readLine();
		}
		br.close();
		//System.out.println("Simulation results: "+simulationResults.toString());
		List<Integer> honeypots = new ArrayList<>(2);
		honeypots.add(5);
		honeypots.add(146);
		int honeypotsFrequency = 9;
		nodeInMaxRowsGreedyHeuristic heuristicResults = new nodeInMaxRowsGreedyHeuristic();
		double delta = heuristicResults.calculateDelta(network, simulationResults, honeypots, honeypotsFrequency);
		//System.out.println("delta="+delta);
		assert Math.abs(delta-0.25)<0.00000001;
		
		// TEST 2
		simulationResults.clear();
		simulationResultsFile = "./files/dummySimulationRunsForEUemailcomm6core_2.txt";
		br = new BufferedReader(new FileReader(simulationResultsFile));
		line = br.readLine();
		while (line!=null)
		{
			List<Integer> samplePath = Arrays.stream(line.split(separator))
										.mapToInt(e -> Integer.parseInt(e.trim())).boxed().collect(Collectors.toList());
			simulationResults.add(samplePath);
			line = br.readLine();
		}
		br.close();
		honeypotsFrequency = 10;
		heuristicResults = new nodeInMaxRowsGreedyHeuristic();
		delta = heuristicResults.calculateDelta(network, simulationResults, honeypots, honeypotsFrequency);
		//System.out.println("delta="+delta);
		assert Math.abs(delta-(4.0/15.0))<0.00000001;
		
		// TEST 3
		simulationResults.clear();
		simulationResultsFile = "./files/dummySimulationRunsForEUemailcomm6core_3.txt";
		br = new BufferedReader(new FileReader(simulationResultsFile));
		line = br.readLine();
		while (line!=null)
		{
			List<Integer> samplePath = Arrays.stream(line.split(separator))
					.mapToInt(e -> Integer.parseInt(e.trim())).boxed().collect(Collectors.toList());
			simulationResults.add(samplePath);
			line = br.readLine();
		}
		br.close();
		honeypots.clear();
		honeypots.add(48);
		honeypots.add(5);
		honeypotsFrequency = 11;
		heuristicResults = new nodeInMaxRowsGreedyHeuristic();
		delta = heuristicResults.calculateDelta(network, simulationResults, honeypots, honeypotsFrequency);
		//System.out.println("delta="+delta);
		assert Math.abs(delta-(5.0/17.0))<0.00000001;
	}
}