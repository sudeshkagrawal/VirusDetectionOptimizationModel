package optimization;

import com.opencsv.CSVWriter;
import dataTypes.parameters;
import dataTypes.solverOutput;
import gurobi.*;
import helper.commonMethods;
import network.graph;
import org.javatuples.Sextet;
import simulation.simulationRuns;

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents results of MIP on {@code simulationRuns} using the Gurobi solver.
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: November 16, 2020.
 */
public class gurobiSolver
{
	/**
	 * A map from {@code parameters} to {@code solverOutput}.
	 * Basically, stores the outputs for different input parameters.
	 * <p>
	 *     Parameters: (model (TN11C, RAEPC, etc.), network name, time step, repetitions,
	 *     false negative probability, transmissability (p), number of honeypots).
	 * </p>
	 * <p>
	 *     Solver output: objective value, best upper bound, honeypots, wall time,
	 *     solver options used, and solver message.
	 * </p>
	 */
	Map<parameters, solverOutput> outputMap;
	
	/**
	 * Constructor.
	 *
	 * @param outputMap a map from {@code parameters} to {@code solverOutput}.
	 */
	public gurobiSolver(Map<parameters, solverOutput> outputMap)
	{
		this.outputMap = outputMap;
	}
	
	/**
	 * Constructor.
	 */
	public gurobiSolver()
	{
		this(new HashMap<>());
	}
	
	/**
	 * Getter.
	 *
	 * @return {@code outputMap}.
	 */
	public Map<parameters, solverOutput> getOutputMap()
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
		str.append("Gurobi solver results:");
		for(Map.Entry<parameters, solverOutput> e: outputMap.entrySet())
		{
			str.append("\n\t<").append(e.getKey()).append(",");
			str.append("\n\t\t objective value = ").append(e.getValue().getObjectiveValue());
			str.append("\n\t\t best upper bound = ").append(e.getValue().getBestUB());
			str.append("\n\t\t honeypots:\n\t\t\t").append(e.getValue().getHoneypots());
			str.append("\n\t\t wall time (second) = ").append(e.getValue().getWallTimeInSeconds());
			str.append("\n\t\t solver options used:\n\t\t\t").append(e.getValue().getSolverOptionsUsed());
			str.append("\n\t\t solver message:\n\t\t\t").append(e.getValue().getSolverMessage());
			str.append("\n\t>");
		}
		return str.toString();
	}
	
	/**
	 * Writes MIP results to csv file.
	 *
	 * @param filename path to output file
	 * @param append true, if you wish to append to existing file; false, otherwise.
	 * @throws IOException thrown if error in input-output operation.
	 */
	public void writeToCSV(String filename, boolean append) throws IOException
	{
		File fileObj = new File(filename);
		String[] header = {"Model", "Network", "t_0", "Simulation repetitions", "FN probability",
							"transmissability (p)", "no. of honeypots", "solver", "solver options",
							"objective value", "best UB", "solver message", "honeypots", "Wall time (s)", "UTC"};
		String solverName = "gurobi";
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
		for (Map.Entry<parameters, solverOutput> e: outputMap.entrySet())
		{
			String[] line = new String[15];
			line[0] = e.getKey().getSpreadModelName();
			line[1] = e.getKey().getNetworkName();
			line[2] = String.valueOf(e.getKey().getTimeStep());
			line[3] = String.valueOf(e.getKey().getNumberOfSimulationRepetitions());
			line[4] = String.valueOf(e.getKey().getFalseNegativeProbability());
			line[5] = String.valueOf(e.getKey().getTransmissability());
			line[6] = String.valueOf(e.getKey().getNumberOfHoneypots());
			line[7] = solverName;
			line[8] = e.getValue().getSolverOptionsUsed().toString();
			line[9] = String.valueOf(e.getValue().getObjectiveValue());
			line[10] = String.valueOf(e.getValue().getBestUB());
			line[11] = e.getValue().getSolverMessage();
			line[12] = e.getValue().getHoneypots().toString();
			line[13] = String.valueOf(e.getValue().getWallTimeInSeconds());
			line[14] = now;
			writer.writeNext(line);
		}
		writer.flush();
		writer.close();
		System.out.println("MIP results successfully written to \""+filename+"\".");
	}
	
	/**
	 * Solves the sample-average approximation (SAA) model using the Gurobi solver.
	 * Optimization formulation written using Gurobi API.
	 * See model 4.6 in
	 * Lee, Jinho. Stochastic optimization models for rapid detection of viruses in cellphone networks. Diss. 2012.
	 * @param g network graph
	 * @param simulationResults results of simulation as an instance of {@code simulationRuns}
	 * @param listOfParams list of the set of parameters used to get {@code simulationResults}
	 * @param threads number of threads solver should use
	 * @param timeLimit total time to spend in optimization, solver terminates the optimization process after this much
	 *                      time is expended
	 * @param logFilename file path to log file; logs from solver written here.
	 * @throws Exception thrown if the graph {@code g} has self loops,
	 *  or if the label of a node in {@code g} is a negative integer,
	 *  or if the network name in one of the parameters and the network name stored in the graph {@code g}
	 *      do not match,
	 *  or if the number of nodes is less than the number of honeypots in any of the parameters in {@code listOfParams}.
	 */
	public void solveSAA(graph g, simulationRuns simulationResults, List<parameters> listOfParams,
	                     int threads, int timeLimit, String logFilename) throws Exception
	{
		if (g.hasSelfLoops())
			throw new Exception("Graphs has self-loops!");
		final int n = g.getVertexSet().size();
		
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
		
		// Create empty environment, set options, and start
		GRBEnv env = new GRBEnv(true);
		env.set("logFile", logFilename);
		env.set(GRB.IntParam.LogToConsole, 0);
		// OutputFlag turns on/off both console and log file output
		// env.set(GRB.IntParam.valueOf("OutputFlag"), 0);
		env.start();
		
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
			
			Sextet<String, String, Integer, Integer, Double, Double> key =
															new Sextet<>(modelName, networkName, t_0, run, r, p);
			System.out.println("Solving MIP: "+modelName+" spread model on "+networkName
					+"network; "+k+" honeypots; "+t_0+" time step(s); "
					+run+" samples; false negative probability="+r+"; transmissability (p)="+p);
			List<List<Integer>> virusSpreadSamples =
					simulationResults.getMapModelNetworkT0RunsFalseNegativeToSimulationRuns().get(key);
			List<List<Integer>> virtualDetectionSamples =
					simulationResults.getMapModelNetworkT0RunsFalseNegativeToVirtualDetections().get(key);
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
							.collect(Collectors.toCollection(() -> new ArrayList<>(t_0 + 1))))
							.collect(Collectors.toCollection(() -> new ArrayList<>(run)));
					successfulDetectMatrix = commonMethods.elementwiseMultiplyMatrix(Collections
											.unmodifiableList(newVirusSpreadSamples),
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
			
			// Create empty model
			GRBModel model = new GRBModel(env);
			
			// Create variables
			Map<Integer, GRBVar> x = new HashMap<>(n);
			for (int node: candidates)
				x.put(node, model.addVar(0, 1, 0, GRB.BINARY, "x_" + node));
			Map<Integer, GRBVar> u = new HashMap<>(run);
			// u can be relaxed to GRB.CONTINUOUS
			for (int i=0; i<run; i++)
				u.put(i+1, model.addVar(0, 1, 0, GRB.BINARY, "u_" + (i+1)));
			
			// Create Objective
			GRBLinExpr obj = new GRBLinExpr();
			final double objCoefficient = 1.0/run;
			for (int i=0; i<run; i++)
				obj.addTerm(objCoefficient, u.get(i + 1));
			model.setObjective(obj, GRB.MAXIMIZE);
			
			// Create Constraints
			GRBLinExpr expr;
			// honeypot in sample constraint
			for (int i=0; i<run; i++)
			{
				expr  = new GRBLinExpr();
				expr.addTerm(-1, u.get(i+1));
				for (int node : candidates)
					if (successfulDetectMatrix.get(i).contains(node))
						expr.addTerm(1, x.get(node));
				model.addConstr(expr, GRB.GREATER_EQUAL, 0, "Honeypot in sample "+(i+1)+" constraint");
			}
			
			// honeypot budget constraint
			expr  = new GRBLinExpr();
			for (int node : candidates)
				expr.addTerm(1.0, x.get(node));
			model.addConstr(expr, GRB.EQUAL, k, "Honeypot budget constraint");
			
			// Solve the model
			double intFeasTol = 1e-9, mipGap = 1e-2;
			int presolve=-1;
			model.set(GRB.DoubleParam.IntFeasTol, intFeasTol);
			model.set(GRB.DoubleParam.MIPGap, mipGap);
			model.set(GRB.IntParam.Threads, threads);
			// Presolve: -1 is automatic; 0 is off; 1 is conservative; 2 is aggressive
			model.set(GRB.IntParam.Presolve, presolve);
			model.set(GRB.DoubleParam.TimeLimit, timeLimit);
			
			// model.write("mip.lp");
			model.optimize();
			
			// Display/Return the results
			solverOutput currOutput = new solverOutput();
			currOutput.setObjectiveValue(model.get(GRB.DoubleAttr.ObjVal));
			currOutput.setBestUB(model.get(GRB.DoubleAttr.ObjBound));
			currOutput.setWallTimeInSeconds(model.get(GRB.DoubleAttr.Runtime));
			
			List<Integer> honeypots = new ArrayList<>();
			for (int node : candidates)
			{
				if ((int)x.get(node).get(GRB.DoubleAttr.X)==1)
					honeypots.add(node);
			}
			if ((r>0) && (zeroNode))
				honeypots = honeypots.stream().map(e -> e - 1).collect(Collectors.toList());
			currOutput.setHoneypots(honeypots);
			currOutput.getSolverOptionsUsed().put("IntFeasTol", String.valueOf(intFeasTol));
			currOutput.getSolverOptionsUsed().put("MIPGap", String.valueOf(mipGap));
			currOutput.getSolverOptionsUsed().put("threads", String.valueOf(threads));
			currOutput.getSolverOptionsUsed().put("Presolve", String.valueOf(presolve));
			currOutput.getSolverOptionsUsed().put("TimeLimit", String.valueOf(timeLimit));
			
			// https://www.gurobi.com/documentation/9.0/refman/optimization_status_codes.html
			switch (model.get(GRB.IntAttr.Status))
			{
				case 1 -> currOutput.setSolverMessage(
						"Loaded: Model is loaded, but no solution info is available.");
				case 2 -> currOutput.setSolverMessage(
						"Optimal: Model was solved to optimality (subject to tolerances).");
				case 3 -> currOutput.setSolverMessage("Infeasible: Model was proven to be infeasible.");
				case 4 -> currOutput.setSolverMessage(
						"Infeasible or Unbounded: Model was proven to be either infeasible or unbounded.");
				case 5 -> currOutput.setSolverMessage("Unbounded: Model was proven to be unbounded.");
				case 7 -> currOutput.setSolverMessage("Simplex Iteration Limit or Barrier Iteration Limit");
				case 9 -> currOutput.setSolverMessage("Time Limit");
				case 13 -> currOutput.setSolverMessage("Suboptimal");
				case 14 -> currOutput.setSolverMessage("In progress");
				case 6, 8, 10, 11, 12, 15 -> currOutput.setSolverMessage("Others");
				default -> throw new IllegalStateException("Unexpected value: " + model.get(GRB.IntAttr.Status));
			}
			
			// find average run time over several optimization calls
			List<Double> wallTimes = new ArrayList<>(5);
			double currentWallTime = model.get(GRB.DoubleAttr.Runtime);
			wallTimes.add(currentWallTime);
			int timerRepeat = 0;
			if (currentWallTime <= 1)
				timerRepeat = 5;
			else if (currentWallTime <= 10)
				timerRepeat = 3;
			for (int i=1; i<=timerRepeat; i++)
			{
				// reset the value of variables
				model.reset(0);
				model.optimize();
				// System.out.println("Last Objective Value = "+model.get(GRB.DoubleAttr.ObjVal));
				wallTimes.add(model.get(GRB.DoubleAttr.Runtime));
				
			}
			//System.out.println("Wall Times (s): "+wallTimes.toString());
			currOutput.setWallTimeInSeconds(wallTimes.stream().mapToDouble(e -> e).average().getAsDouble());
			
			System.out.println("Objective value = "+currOutput.getObjectiveValue());
			System.out.println("Wall time (second) = "+currOutput.getWallTimeInSeconds());
			outputMap.put(param, currOutput);
			
			model.dispose();
		}
		env.dispose();
	}
	
	/**
	 * Solves SAA by calling {@code solveSAA} only for those parameters
	 * whose results are not already there in {@code outputMap}.
	 *
	 * @param g network graph
	 * @param simulationResults results of simulation as an instance of {@code simulationRuns}
	 * @param listOfParams list of the set of parameters used to get {@code simulationResults}
	 * @param threads number of threads solver should use
	 * @param timeLimit total time to spend in optimization, solver terminates the optimization process after this much
	 *                      time is expended
	 * @param logFilename file path to log file; logs from solver written here.
	 * @throws Exception thrown if the graph {@code g} has self loops,
	 *  or if the label of a node in {@code g} is a negative integer,
	 *  or if the network name in one of the parameters and the network name stored in the graph {@code g}
	 *      do not match,
	 *  or if the number of nodes is less than the number of honeypots in any of the parameters in {@code listOfParams}.
	 */
	public boolean solveSAAOnlyNecessaryOnes(graph g, simulationRuns simulationResults, List<parameters> listOfParams,
	                     int threads, int timeLimit, String logFilename) throws Exception
	{
		boolean ranNewSimulations = false;
		List<parameters> newListOfParams = new ArrayList<>();
		for (parameters param: listOfParams)
		{
			if (!outputMap.containsKey(param))
				newListOfParams.add(param);
		}
		if (newListOfParams.size()>0)
		{
			System.out.println("Running MIP for: \n\t"+newListOfParams.toString());
			solveSAA(g, simulationResults, newListOfParams, threads, timeLimit, logFilename);
			ranNewSimulations = true;
		}
		return ranNewSimulations;
	}
	
	/**
	 * Loads results created using {@link gurobiSolver#writeToCSV(String, boolean)} from previous run(s).
	 *
	 * @param filename path of the file where results from previous run(s) are stored.
	 */
	public void loadResultsFromCSVFile(String filename)
	{
		try
		{
			FileReader fileReader = new FileReader(filename);
			BufferedReader csvReader = new BufferedReader(fileReader);
			String row;
			boolean header = true;
			Map<parameters, Instant> timeRecordOfRows = new HashMap<>();
			while ((row = csvReader.readLine()) != null)
			{
				if (!header)
				{
					String[] data = row.split("\",");
					String modelName = data[0].substring(1);
					String networkName = data[1].substring(1);
					int timeSteps = Integer.parseInt(data[2].substring(1));
					int runs = Integer.parseInt(data[3].substring(1));
					double falseNegProb = Double.parseDouble(data[4].substring(1));
					double transmissability = Double.parseDouble(data[5].substring(1));
					int numberOfHoneypots = Integer.parseInt(data[6].substring(1));
					//String solverName = data[7].substring(1);
					double objective = Double.parseDouble(data[9].substring(1));
					double bestUB = Double.parseDouble(data[10].substring(1));
					String solverMessage = data[11].substring(1);
					double wallTime = Double.parseDouble(data[13].substring(1));
					Map<String, String> solverOptions;
					String solverOptionsAsString = data[8].substring(1);
					String[] tokens = solverOptionsAsString
										.replaceAll("\\s","")
										.replaceAll("}", "")
										.substring(1).split(",");
					
					solverOptions = Arrays.stream(tokens).map(token -> token.split("="))
									.collect(Collectors
									.toMap(innerTokens -> innerTokens[0], innerTokens -> innerTokens[1], (a, b) -> b));
					String[] tokens2AsString = data[12].substring(2, data[12].length()-1).split(",");
					List<Integer> honeypots = Arrays.stream(tokens2AsString)
												.map(s -> Integer.parseInt(s.trim()))
												.collect(Collectors.toCollection(() ->
														new ArrayList<>(numberOfHoneypots)));
					parameters param = new parameters(modelName, networkName, timeSteps, runs, falseNegProb,
														transmissability, numberOfHoneypots, 0);
					solverOutput output = new solverOutput(objective, bestUB, honeypots, wallTime,
															solverOptions, solverMessage);
					Instant timeStamp = Instant.parse(data[14].substring(1, data[14].length()-1));
					if (timeRecordOfRows.containsKey(param))
					{
						if (timeStamp.compareTo(timeRecordOfRows.get(param)) > 0)
						{
							outputMap.put(param, output);
							timeRecordOfRows.put(param, timeStamp);
						}
					}
					else
					{
						outputMap.put(param, output);
						timeRecordOfRows.put(param, timeStamp);
						System.out.println("Using MIP results in \""+filename+"\" for "+param.toString());
					}
					
				}
				header = false;
			}
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
}
