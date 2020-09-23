package algorithm;

import com.opencsv.CSVWriter;
import dataTypes.algorithmOutput;
import dataTypes.parameters;
import helper.commonMethods;
import network.graph;
import org.javatuples.Sextet;
import simulation.simulationRuns;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents results of greedy heuristic on {@code simulationRuns}.
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: September 23, 2020.
 */
public class nodeInMaxRowsGreedyHeuristic
{
	/**
	 * A map from {@code parameters} to {@code algorithmOutput}.
	 * Basically, stores the outputs for different input parameters.
	 * <p>
	 *     Parameters: (model (TN11C, RAEPC, etc.), network name, time step, repetitions,
	 *     false negative probability, transmissability (p), number of honeypots).
	 * </p>
	 * <p>
	 *     Algorithm output: objective value, honeypot, wall time, a priori upper bound, and posterior upper bound.
	 * </p>
	 */
	Map<parameters, algorithmOutput> outputMap;
	
	/**
	 * Constructor.
	 */
	public nodeInMaxRowsGreedyHeuristic()
	{
		this(new HashMap<>());
	}
	
	/**
	 * Constructor.
	 *
	 * @param outputMap an instance of {@code outputMap}.
	 */
	public nodeInMaxRowsGreedyHeuristic(Map<parameters, algorithmOutput> outputMap)
	{
		this.outputMap = outputMap;
	}
	
	/**
	 * Getter.
	 *
	 * @return {@code outputMap}.
	 */
	public Map<parameters, algorithmOutput> getOutputMap()
	{
		return outputMap;
	}
	
	/**
	 * Returns a string representation of the object.
	 *
	 * @return a string representation of the object.
	 */
	@Override
	public String toString()
	{
		StringBuilder str = new StringBuilder(1000);
		str.append("Node-in-max-rows greedy heuristic:");
		for(Map.Entry<parameters, algorithmOutput> e: outputMap.entrySet())
		{
			str.append("\n\t<").append(e.getKey()).append(",");
			str.append("\n\t\t Objective value:\n\t\t\t").append(e.getValue().getObjectiveValue());
			str.append("\n\t\t Honeypots:\n\t\t\t").append(e.getValue().getHoneypots());
			str.append("\n\t\t a priori UB:\n\t\t\t").append(e.getValue().getAPrioriUB());
			str.append("\n\t\t posterior UB:\n\t\t\t").append(e.getValue().getPosteriorUB());
			str.append("\n\t\t Wall time (second):\n\t\t\t").append(e.getValue().getWallTime());
			str.append("\n\t>");
		}
		return str.toString();
	}
	
	/**
	 * Writes algorithm results to csv file.
	 *
	 * @param filename path to output file
	 * @param append {@code true}, if you wish to append to existing file; {@code false}, otherwise.
	 * @throws IOException thrown if error in input-output operation.
	 */
	public void writeToCSV(String filename, boolean append) throws IOException
	{
		File fileObj = new File(filename);
		String[] header = {"Model", "Network", "t_0", "Simulation repetitions", "FN probability",
				"transmissability (p)", "no. of honeypots", "objective value", "honeypots",
				"a priori UB", "posterior UB", "Posterior Gap (%)", "Wall time (s)", "UTC"};
		boolean writeHeader = false;
		if (!fileObj.exists())
			writeHeader = true;
		else if (!append)
			writeHeader = true;
		CSVWriter writer = new CSVWriter(new FileWriter(filename, append));
		if (writeHeader)
		{
			writer.writeNext(header);
			writer.flush();
		}
		String now = Instant.now().toString();
		for (Map.Entry<parameters, algorithmOutput> e: outputMap.entrySet())
		{
			String[] line = new String[14];
			line[0] = e.getKey().getSpreadModelName();
			line[1] = e.getKey().getNetworkName();
			line[2] = String.valueOf(e.getKey().getTimeStep());
			line[3] = String.valueOf(e.getKey().getNumberOfSimulationRepetitions());
			line[4] = String.valueOf(e.getKey().getFalseNegativeProbability());
			line[5] = String.valueOf(e.getKey().getTransmissability());
			line[6] = String.valueOf(e.getKey().getNumberOfHoneypots());
			double objectiveValue = e.getValue().getObjectiveValue();
			line[7] = String.valueOf(objectiveValue);
			line[8] = e.getValue().getHoneypots().toString();
			line[9] = String.valueOf(e.getValue().getAPrioriUB());
			double posteriorUB = e.getValue().getPosteriorUB();
			line[10] = String.valueOf(posteriorUB);
			line[11] = String.valueOf(
					100.0*(posteriorUB-objectiveValue)/(objectiveValue));
			line[12] = String.valueOf(e.getValue().getWallTime());
			line[13] = now;
			writer.writeNext(line);
		}
		writer.flush();
		writer.close();
		System.out.println("Heuristic results successfully written to \""+filename+"\".");
	}
	
	/**
	 * Solves the sample-average approximation model using a greedy algorithm.
	 * See model 4.6 in
	 * Lee, Jinho. Stochastic optimization models for rapid detection of viruses in cellphone networks. Diss. 2012.
	 *
	 * @param g network graph
	 * @param simulationResults results of simulation as an instance of {@code simulationRuns}
	 * @param listOfParams list of the set of parameters used to get {@code simulationResults}.
	 * @throws Exception thrown if graph {@code g} has self-loops,
	 *  or if node labels are negative integers,
	 *  or if the network name in one of the parameters and the network name stored in the graph {@code g}
	 *      do not match,
	 *  or if the number of nodes is less than the number of honeypots in any of the parameters in {@code listOfParams}.
	 */
	public void runSAAUsingHeuristic(graph g, simulationRuns simulationResults,
	                                 List<parameters> listOfParams) throws Exception
	{
		if (g.hasSelfLoops())
			throw new Exception("Graphs has self-loops!");
		
		// minimum label of vertex
		boolean zeroNode = false;
		int minNode = g.findMinimumNodeLabel();
		if (minNode==0)
			zeroNode = true;
		else
		{
			if (minNode<0)
				throw new Exception("Node labels are negative integers!");
		}
		
		//for (Triple<Integer, Integer, Integer> v : k_t0_runs)
		for (parameters param: listOfParams)
		{
			String modelName = param.getSpreadModelName();
			String networkName = param.getNetworkName();
			if (!networkName.equals(g.getNetworkName()))
				throw new Exception("Parameters are for a different network than that has been provided as input!");
			int k = param.getNumberOfHoneypots();
			if (k>g.getVertexSet().size())
				throw new Exception("Number of honeypots cannot be greater than the number of nodes!");
			int t_0 = param.getTimeStep();
			int run = param.getNumberOfSimulationRepetitions();
			double r = param.getFalseNegativeProbability();
			double p = param.getTransmissability();
			
			Sextet<String, String, Integer, Integer, Double, Double> keyForSimulation =
														new Sextet<>(modelName, networkName, t_0, run, r, p);
			
			System.out.println("Using greedy algorithm: "+modelName+" spread model on "+networkName
								+"network; "+k+" honeypots; "+t_0+" time step(s); "
								+run+" samples; false negative probability="+r+"; transmissability (p)="+p);
			List<List<Integer>> virusSpreadSamples =
					simulationResults.getMapModelNetworkT0RunsFalseNegativeToSimulationRuns().get(keyForSimulation);
			List<List<Integer>> virtualDetectionSamples =
					simulationResults.getMapModelNetworkT0RunsFalseNegativeToVirtualDetections().get(keyForSimulation);
			// System.out.println("Virus spread samples:\n"+virusSpreadSamples+"\n"+virtualDetectionSamples);
			List<List<Integer>> successfulDetectMatrix;
			Set<Integer> candidates;
			if (r>0)
			{
				if (zeroNode)
				{
					List<List<Integer>> newVirusSpreadSamples = virusSpreadSamples.stream()
								.map(virusSpreadSample -> virusSpreadSample.stream()
								.map(integer -> integer + 1)
								.collect(Collectors.toCollection(ArrayList::new)))
								.collect(Collectors.toCollection(() -> new ArrayList<>(run)));
					successfulDetectMatrix = commonMethods.elementwiseMultiplyMatrix(
							Collections.unmodifiableList(newVirusSpreadSamples),
							Collections.unmodifiableList(virtualDetectionSamples));
					candidates = g.getVertexSet().stream().map(e -> e+1).collect(Collectors.toSet());
				}
				else
				{
					successfulDetectMatrix = commonMethods.elementwiseMultiplyMatrix(
												Collections.unmodifiableList(virusSpreadSamples),
												Collections.unmodifiableList(virtualDetectionSamples));
					candidates = new HashSet<>(g.getVertexSet());
				}
			}
			else
			{
				successfulDetectMatrix = new ArrayList<>(virusSpreadSamples);
				candidates = new HashSet<>(g.getVertexSet());
			}
			//System.out.println("Successful detection matrix: \n"
			//		 +successfulDetectMatrix.toString()+"\n---------------------------");
			//System.out.println("Candidate nodes: \n"+candidates.toString()+"\n---------------------------");
			
			List<Integer> honeypots = new ArrayList<>();
			int numberOfHoneypotsFound = 0;
			Set<Integer> indicesOfSamplesToBeConsidered = IntStream.range(0, run).boxed().collect(Collectors.toSet());
			Set<Integer> indicesOfSamplesToBeRemoved;
			Set<Integer> indicesOfSamplesCovered = new HashSet<>(indicesOfSamplesToBeConsidered);
			
			Instant tic = Instant.now();
			while (numberOfHoneypotsFound<k)
			{
				List<List<Integer>> samplesToBeConsidered = IntStream.range(0, successfulDetectMatrix.size())
													.filter(indicesOfSamplesToBeConsidered::contains)
													.mapToObj(successfulDetectMatrix::get).collect(Collectors.toList());
				int currentCandidate = commonMethods.findMaxRowFrequencyNode(
										Collections.unmodifiableList(samplesToBeConsidered),
										List.copyOf(candidates));
				// System.out.println("Current candidate: "+currentCandidate);
				honeypots.add(currentCandidate);
				numberOfHoneypotsFound++;
				candidates.remove(currentCandidate);
				indicesOfSamplesToBeRemoved = new HashSet<>(
						commonMethods.findRowOccurrenceIndices(Collections.unmodifiableList(successfulDetectMatrix),
								currentCandidate));
				indicesOfSamplesToBeConsidered.removeAll(indicesOfSamplesToBeRemoved);
				// TODO: What if current set of honeypots cover all sample paths?
			}
			Instant toc = Instant.now();
			if ((r>0) && (zeroNode))
				honeypots = honeypots.stream().map(e -> e - 1).collect(Collectors.toList());
			// System.out.println("Honeypots: \n"+honeypots.toString()+"\n---------------------------");
			indicesOfSamplesCovered.removeAll(indicesOfSamplesToBeConsidered);
			double objectiveValue = indicesOfSamplesCovered.size()*1.0/run;
			System.out.println("Objective value = "+objectiveValue);
			double wallTimeInSeconds = 1.0*Duration.between(tic, toc).toMillis()/1000;
			System.out.println("Wall time (second) = "+ wallTimeInSeconds);
			
			double factor = Math.exp(1)/(Math.exp(1)-1);
			double delta = commonMethods.calculateDelta(g,
					successfulDetectMatrix, honeypots, indicesOfSamplesCovered.size());
			outputMap.put(param, new algorithmOutput(objectiveValue, honeypots,
					wallTimeInSeconds, Math.min(factor*objectiveValue, 1),
					Math.min(objectiveValue+delta, 1)));
		}
	}
	
	/**
	 * Loads any results of algorithm from serialized object in file.
	 *
	 * @param serialFilename path of the file where the serialized object is stored.
	 */
	public void loadHeuristicResultsFromFile(String serialFilename)
	{
		try
		{
			FileInputStream fin = new FileInputStream(serialFilename);
			BufferedInputStream bin = new BufferedInputStream(fin);
			ObjectInputStream objin = new ObjectInputStream(bin);
			List<Object> serObject = (List<Object>) objin.readObject();
			outputMap = (Map<parameters, algorithmOutput>) serObject.get(0);
			objin.close();
			bin.close();
			fin.close();
			System.out.println("Using results of algorithm in \""+serialFilename+"\".");
		}
		catch (FileNotFoundException e1)
		{
			System.out.println("Error, file not found!");
			System.out.println(e1.getMessage());
		}
		catch (Exception e2)
		{
			System.out.println("An exception occurred:");
			e2.printStackTrace();
			System.out.println("Exiting the program...");
			System.exit(0);
		}
	}
	/**
	 * Serializes {@code outputMap}.
	 *
	 * @param serialFilename path of the file where the serialized object is to be stored.
	 */
	public void serializeResults(String serialFilename)
	{
		try
		{
			FileOutputStream fout = new FileOutputStream(serialFilename);
			BufferedOutputStream bout = new BufferedOutputStream(fout);
			ObjectOutputStream objout = new ObjectOutputStream(bout);
			List<Object> serObject = new ArrayList<>(3);
			serObject.add(outputMap);
			objout.writeObject(serObject);
			objout.close();
			bout.close();
			fout.close();
			System.out.println("Heuristic results serialized at \""+ serialFilename +"\".");
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
}
