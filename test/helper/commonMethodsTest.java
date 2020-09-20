package helper;

import network.graph;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Unit test for {@code commonMethods}.
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: September 19, 2020.
 */
class commonMethodsTest
{
	@Test
	void elementwiseMultiplyMatrix() throws Exception
	{
		// load two list of lists
		String listOfListsFile = "./files/dummyListOfLists1.txt";
		List<List<Integer>> listOfLists1 = new ArrayList<>(5);
		BufferedReader br = new BufferedReader(new FileReader(listOfListsFile));
		String line = br.readLine();
		while (line!=null)
		{
			List<Integer> innerList = Arrays.stream(line.split(","))
					.mapToInt(e -> Integer.parseInt(e.trim())).boxed().collect(Collectors.toList());
			listOfLists1.add(innerList);
			line = br.readLine();
		}
		br.close();
		
		listOfListsFile = "./files/dummyListOfLists2.txt";
		List<List<Integer>> listOfLists2 = new ArrayList<>(5);
		br = new BufferedReader(new FileReader(listOfListsFile));
		line = br.readLine();
		while (line!=null)
		{
			List<Integer> innerList = Arrays.stream(line.split(","))
					.mapToInt(e -> Integer.parseInt(e.trim())).boxed().collect(Collectors.toList());
			listOfLists2.add(innerList);
			line = br.readLine();
		}
		br.close();
		
		List<List<Integer>> productMatrix = commonMethods.elementwiseMultiplyMatrix(listOfLists1, listOfLists2);
		
		assert productMatrix.get(0).get(0)==2;
		assert productMatrix.get(0).get(1)==63;
		assert productMatrix.get(1).get(0)==8;
		assert productMatrix.get(1).get(1)==16;
		assert productMatrix.get(1).get(2)==21;
		assert productMatrix.get(2).get(0)==15;
		assert productMatrix.get(2).get(1)==54;
		assert productMatrix.get(2).get(2)==31;
		assert productMatrix.get(2).get(3)==8;
		assert productMatrix.get(2).get(4)==90;
		assert productMatrix.get(3).get(0)==25;
	}
	
	@Test
	void findMaxRowFrequencyNode() throws Exception
	{
		// graph
		String networkName = "EUemailcomm_6-core";
		graph network = new graph(networkName);
		String separator = ",";
		network.buildGraphFromFile("./files/networks/"+networkName+".txt", separator);
		List<Integer> nodes = new ArrayList<>(network.getVertexSet());
		
		// TEST 1
		// simulation results --> List<List<Integer>> arr
		String simulationResultsFile = "./files/dummySimulationRunsForEUemailcomm6core_1.txt";
		BufferedReader br = new BufferedReader(new FileReader(simulationResultsFile));
		String line = br.readLine();
		List<List<Integer>> simulationResults = new ArrayList<>(15);
		while (line!=null)
		{
			List<Integer> samplePath = Arrays.stream(line.split(separator))
					.mapToInt(e -> Integer.parseInt(e.trim())).boxed().collect(Collectors.toList());
			simulationResults.add(samplePath);
			line = br.readLine();
		}
		br.close();
		
		int selectedNode = commonMethods.findMaxRowFrequencyNode(simulationResults, nodes);
		assert selectedNode==5;
		
		// TEST 2
		// simulation results --> List<List<Integer>> arr
		simulationResultsFile = "./files/dummySimulationRunsForEUemailcomm6core_3.txt";
		br = new BufferedReader(new FileReader(simulationResultsFile));
		line = br.readLine();
		simulationResults = new ArrayList<>(15);
		while (line!=null)
		{
			List<Integer> samplePath = Arrays.stream(line.split(separator))
					.mapToInt(e -> Integer.parseInt(e.trim())).boxed().collect(Collectors.toList());
			simulationResults.add(samplePath);
			line = br.readLine();
		}
		br.close();
		
		selectedNode = commonMethods.findMaxRowFrequencyNode(simulationResults, nodes);
		assert ((selectedNode==11) || (selectedNode==48));
	}
	
	@Test
	void findRowOccurrenceIndices() throws Exception
	{
		// simulation results --> List<List<Integer>> arr
		String simulationResultsFile = "./files/dummySimulationRunsForEUemailcomm6core_1.txt";
		String separator = ",";
		BufferedReader br = new BufferedReader(new FileReader(simulationResultsFile));
		String line = br.readLine();
		List<List<Integer>> simulationResults = new ArrayList<>(15);
		while (line!=null)
		{
			List<Integer> samplePath = Arrays.stream(line.split(separator))
					.mapToInt(e -> Integer.parseInt(e.trim())).boxed().collect(Collectors.toList());
			simulationResults.add(samplePath);
			line = br.readLine();
		}
		br.close();
		
		List<Integer> indices = commonMethods.findRowOccurrenceIndices(simulationResults, 5);
		assert indices.contains(0);
		assert indices.contains(1);
		assert indices.contains(3);
		assert indices.contains(4);
		assert indices.contains(7);
		assert indices.contains(8);
		assert indices.size()==6;
		
		indices = commonMethods.findRowOccurrenceIndices(simulationResults, 23);
		assert indices.size()==0;
	}
	
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
		double delta = commonMethods.calculateDelta(network, simulationResults, honeypots, honeypotsFrequency);
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
		delta = commonMethods.calculateDelta(network, simulationResults, honeypots, honeypotsFrequency);
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
		delta = commonMethods.calculateDelta(network, simulationResults, honeypots, honeypotsFrequency);
		assert Math.abs(delta-(5.0/17.0))<0.00000001;
	}
}