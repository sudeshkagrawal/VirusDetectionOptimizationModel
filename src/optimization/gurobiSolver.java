package optimization;

import com.opencsv.CSVWriter;
import gurobi.*;
import network.graph;
import org.javatuples.Septet;
import org.javatuples.Sextet;
import org.jgrapht.alg.util.Triple;
import simulation.simulationRuns;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents results of MIP on {@code simulationRuns} using the Gurobi solver.
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: September 16, 2020.
 */
public class gurobiSolver
{
	// Model (TN11C, RAEPC, etc.); Network name; t_0; repetitions; false negative probability; ; transmissability (p);
	// number of honeypots
	/**
	 * A map from a 7-tuple to the objective value for a given solution.
	 * 7-tuple: (model (TN11C, RAEPC, etc.), network name, time step, repetitions,
	 * false negative probability, transmissability (p), number of honeypots).
	 */
	Map<Septet<String, String, Integer, Integer, Double, Double, Integer>, Double> mapToObjectiveValue;
	/**
	 * A map from a 7-tuple to the best upper bound known.
	 * 7-tuple: (model (TN11C, RAEPC, etc.), network name, time step, repetitions,
	 * false negative probability, transmissability (p), number of honeypots).
	 */
	Map<Septet<String, String, Integer, Integer, Double, Double, Integer>, Double> mapToBestUpperBound;
	/**
	 * A map from a 7-tuple to the list of honeypots in a given solution.
	 * 7-tuple: (model (TN11C, RAEPC, etc.), network name, time step, repetitions,
	 * false negative probability, transmissability (p), number of honeypots).
	 */
	Map<Septet<String, String, Integer, Integer, Double, Double, Integer>, List<Integer>> mapToHoneypots;
	/**
	 * A map from a 7-tuple to the CPU time it took to find a given solution.
	 * 7-tuple: (model (TN11C, RAEPC, etc.), network name, time step, repetitions,
	 * false negative probability, transmissability (p), number of honeypots).
	 */
	Map<Septet<String, String, Integer, Integer, Double, Double, Integer>, Double> mapToWallTime;
	/**
	 * This field is there for some compatibility with results from python code.
	 * This value should not be used for any analyses or inferences.
	 */
	Map<Septet<String, String, Integer, Integer, Double, Double, Integer>, Double> mapToTime;
	/**
	 * A map from a 7-tuple to the solver options used for MIP.
	 * 7-tuple: (model (TN11C, RAEPC, etc.), network name, time step, repetitions,
	 * false negative probability, transmissability (p), number of honeypots).
	 */
	Map<Septet<String, String, Integer, Integer, Double, Double, Integer>, String> mapToSolverOptions;
	/**
	 * A map from a 7-tuple to the solver message after MIP.
	 * This message could indicated if the solver solved to optimality or hit time limit, for example.
	 * 7-tuple: (model (TN11C, RAEPC, etc.), network name, time step, repetitions,
	 * false negative probability, transmissability (p), number of honeypots).
	 */
	Map<Septet<String, String, Integer, Integer, Double, Double, Integer>, String> mapToSolverMessage;
	
	/**
	 * Constructor.
	 *
	 * @param mapToObjectiveValue value of objective function for a given solution
	 * @param mapToBestUpperBound best known upper bound
	 * @param mapToHoneypots list of honeypots in a given solution
	 * @param mapToWallTime CPU time taken to find a given solution
	 * @param mapToTime TO BE IGNORED
	 * @param mapToSolverOptions solver options passed for optimization
	 * @param mapToSolverMessage solver message.
	 */
	public gurobiSolver(
			Map<Septet<String, String, Integer, Integer, Double, Double, Integer>, Double> mapToObjectiveValue,
			Map<Septet<String, String, Integer, Integer, Double, Double, Integer>, Double> mapToBestUpperBound,
			Map<Septet<String, String, Integer, Integer, Double, Double, Integer>, List<Integer>> mapToHoneypots,
			Map<Septet<String, String, Integer, Integer, Double, Double, Integer>, Double> mapToWallTime,
			Map<Septet<String, String, Integer, Integer, Double, Double, Integer>, Double> mapToTime,
			Map<Septet<String, String, Integer, Integer, Double, Double, Integer>, String> mapToSolverOptions,
			Map<Septet<String, String, Integer, Integer, Double, Double, Integer>, String> mapToSolverMessage)
	{
		this.mapToObjectiveValue = mapToObjectiveValue;
		this.mapToBestUpperBound = mapToBestUpperBound;
		this.mapToHoneypots = mapToHoneypots;
		this.mapToWallTime = mapToWallTime;
		this.mapToTime = mapToTime;
		this.mapToSolverOptions = mapToSolverOptions;
		this.mapToSolverMessage = mapToSolverMessage;
	}
	
	/**
	 * Constructor.
	 */
	public gurobiSolver()
	{
		this(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(),
				new HashMap<>(), new HashMap<>(), new HashMap<>());
	}
	
	/**
	 * Solves the sample-average approximation model.
	 * Optimization formulation written using Gurobi API.
	 * See model 4.6 in Lee, Jinho. Stochastic optimization models for rapid detection of viruses in cellphone networks. Diss. 2012.
	 *
	 * @param modelName name of virus spread model (TN11C, RAEPC, etc.)
	 * @param g network graph
	 * @param simulationResults results of simulation as an instance of {@code simulationRuns}
	 * @param k_t0_runs list of a 3-tuple of (k, t0, runs),
	 *                  where k is number of honeypots, t0 is simulation time,
	 *                  and runs is number of repetitions of simulation
	 * @param r false negative probability
	 * @param p transmissability probability
	 * @param threads number of threads solver should use
	 * @param logFilename file path to log file; logs from solver written here.
	 * @throws Exception exception thrown in node labels are negative integers.
	 */
	public void solveSAA(String modelName, graph g, simulationRuns simulationResults,
	                     List<Triple<Integer, Integer, Integer>> k_t0_runs, double r, double p,
	                     int threads, String logFilename) throws Exception
	{
		System.out.println("Network has "+g.getG().vertexSet().size()
				+" nodes and "+g.getG().edgeSet().size()+" edges.");
		// Remove self-loops if any from the graph
		g.removeSelfLoops();
		System.out.print("Removed self-loops (if any) from the graph: ");
		final int n = g.getG().vertexSet().size();
		System.out.println("(new) network has "+n+" nodes and "+g.getG().edgeSet().size()+" edges.");
		
		// minimum label of vertex
		boolean zeroNode = false;
		int minNode = g.getG().vertexSet().stream().mapToInt(v -> v).min().orElseThrow(NoSuchElementException::new);
		if (minNode==0)
			zeroNode = true;
		else
		if (minNode<0)
			throw new Exception("Node labels are negative integers! Terminating...");
		
		// Create empty environment, set options, and start
		GRBEnv env = new GRBEnv(true);
		env.set("logFile", logFilename);
		env.set(GRB.IntParam.LogToConsole, 0);
		// OutputFlag turns on/off both console and log file output
		// env.set(GRB.IntParam.valueOf("OutputFlag"), 0);
		env.start();
		
		for (Triple<Integer, Integer, Integer> v : k_t0_runs)
		{
			int k = v.getFirst();
			int t_0 = v.getSecond();
			int run = v.getThird();
			Sextet<String, String, Integer, Integer, Double, Double> key =
															new Sextet<>(modelName, g.getNetworkName(), t_0, run, r, p);
			Septet<String, String, Integer, Integer, Double, Double, Integer> fullKey =
														new Septet<>(modelName, g.getNetworkName(), t_0, run, r, p, k);
			
			System.out.println("Solving MIP: "+modelName+" spread model on "+g.getNetworkName()
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
					successfulDetectMatrix = elementwiseMultiplyMatrix(Collections
											.unmodifiableList(newVirusSpreadSamples),
											Collections.unmodifiableList(virtualDetectionSamples));
					candidates = g.getG().vertexSet().stream().map(e -> e+1).collect(Collectors.toSet());
				}
				else
				{
					successfulDetectMatrix = elementwiseMultiplyMatrix(Collections.unmodifiableList(virusSpreadSamples),
							Collections.unmodifiableList(virtualDetectionSamples));
					candidates = new HashSet<>(g.getG().vertexSet());
				}
				
			}
			else
			{
				successfulDetectMatrix = new ArrayList<>(virusSpreadSamples);
				candidates = new HashSet<>(g.getG().vertexSet());
			}
			
			// Create empty model
			GRBModel model = new GRBModel(env);
			
			// Create variables
			Map<Integer, GRBVar> x = new HashMap<>(n);
			for (int node: candidates)
				x.put(node, model.addVar(0, 1, 0, GRB.BINARY, "x_" + node));
			Map<Integer, GRBVar> u = new HashMap<>(run);
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
			double intFeasTol = 1e-9, mipGap = 1e-2, timeLimit=300;
			int presolve=-1;
			model.set(GRB.DoubleParam.IntFeasTol, intFeasTol);
			model.set(GRB.DoubleParam.MIPGap, mipGap);
			model.set(GRB.IntParam.Threads, threads);
			// Presolve: -1 is automatic; 0 is off; 1 is conservative; 2 is aggressive
			model.set(GRB.IntParam.Presolve, presolve);
			model.set(GRB.DoubleParam.TimeLimit, timeLimit);
			
			// model.write("mip.lp");
			Instant tic = Instant.now();
			model.optimize();
			Instant toc = Instant.now();
			double timeInSeconds = 1.0* Duration.between(tic, toc).toMillis()/1000;
			
			// Display/Return the results
			mapToObjectiveValue.put(fullKey, model.get(GRB.DoubleAttr.ObjVal));
			mapToBestUpperBound.put(fullKey, model.get(GRB.DoubleAttr.ObjBound));
			mapToWallTime.put(fullKey, model.get(GRB.DoubleAttr.Runtime));
			mapToTime.put(fullKey, timeInSeconds);
			List<Integer> honeypots = new ArrayList<>();
			for (int node : candidates)
			{
				if ((int)x.get(node).get(GRB.DoubleAttr.X)==1)
					honeypots.add(node);
			}
			if ((r>0) && (zeroNode))
				honeypots = honeypots.stream().map(e -> e - 1).collect(Collectors.toList());
			mapToHoneypots.put(fullKey, honeypots);
			mapToSolverOptions.put(fullKey, "IntFeasTol="+intFeasTol+" MIPGap="+mipGap+" threads="+threads+
											" Presolve="+presolve+" TimeLimit="+timeLimit);
			// https://www.gurobi.com/documentation/9.0/refman/optimization_status_codes.html
			switch (model.get(GRB.IntAttr.Status))
			{
				case 1 -> mapToSolverMessage.put(fullKey,
						"Loaded: Model is loaded, but no solution info is available.");
				case 2 -> mapToSolverMessage.put(fullKey,
						"Optimal: Model was solved to optimality (subject to tolerances).");
				case 3 -> mapToSolverMessage.put(fullKey, "Infeasible: Model was proven to be infeasible.");
				case 4 -> mapToSolverMessage.put(fullKey,
						"Infeasible or Unbounded: Model was proven to be either infeasible or unbounded.");
				case 5 -> mapToSolverMessage.put(fullKey, "Unbounded: Model was proven to be unbounded.");
				case 7 -> mapToSolverMessage.put(fullKey, "Simplex Iteration Limit or Barrier Iteration Limit");
				case 9 -> mapToSolverMessage.put(fullKey, "Time Limit");
				case 13 -> mapToSolverMessage.put(fullKey, "Suboptimal");
				case 14 -> mapToSolverMessage.put(fullKey, "In progress");
				case 6, 8, 10, 11, 12, 15 -> mapToSolverMessage.put(fullKey, "Others");
				default -> throw new IllegalStateException("Unexpected value: " + model.get(GRB.IntAttr.Status));
			}
			
			// find average run time over several optimization calls
			List<Double> wallTimes = new ArrayList<>(5);
			List<Double> times = new ArrayList<>(5);
			double currentWallTime = model.get(GRB.DoubleAttr.Runtime);
			wallTimes.add(currentWallTime);
			times.add(timeInSeconds);
			int timerRepeat = 0;
			if (currentWallTime <= 1)
				timerRepeat = 5;
			else if (currentWallTime <= 10)
				timerRepeat = 3;
			for (int i=1; i<=timerRepeat; i++)
			{
				// reset the value of variables
				model.reset(0);
				tic = Instant.now();
				model.optimize();
				// System.out.println("Last Objective Value = "+model.get(GRB.DoubleAttr.ObjVal));
				toc = Instant.now();
				wallTimes.add(model.get(GRB.DoubleAttr.Runtime));
				timeInSeconds = 1.0* Duration.between(tic, toc).toMillis()/1000;
				times.add(timeInSeconds);
			}
			//System.out.println("Wall Times (s): "+wallTimes.toString());
			//System.out.println("Times (s): "+times.toString());
			mapToWallTime.put(fullKey, wallTimes.stream().mapToDouble(e -> e).average().getAsDouble());
			mapToTime.put(fullKey, times.stream().mapToDouble(e -> e).average().getAsDouble());
			System.out.println("Objective value = "+mapToObjectiveValue.get(fullKey));
			System.out.println("Wall time (second) = "+ mapToWallTime.get(fullKey));
			
			model.dispose();
		}
		env.dispose();
	}
	
	/**
	 * Element-wise multiplication of two list of lists.
	 *
	 * @param a the first list of lists
	 * @param b the second list of lists.
	 * @return returns a list of lists.
	 * @throws Exception exception thrown if outer lists {@code a} and {@code b} not of same size.
	 *          Corresponding inner lists should also be of same size,
	 *          but that exception is not thrown since the check would be expensive.
	 */
	private List<List<Integer>> elementwiseMultiplyMatrix(List<List<Integer>> a, List<List<Integer>> b) throws Exception
	{
		List<List<Integer>> output = new ArrayList<>(a.size());
		if (a.size()!=b.size())
			throw new Exception("Inputs are not of the same size!");
		for (int i=0; i<a.size(); i++)
		{
			List<Integer> colList = new ArrayList<>(a.get(i).size());
			for (int j=0; j<a.get(i).size(); j++)
				colList.add(a.get(i).get(j) * b.get(i).get(j));
			output.add(colList);
		}
		return output;
	}
	
	/**
	 * Writes MIP results to csv file.
	 *
	 * @param filename path to output file
	 * @param append true, if you wish to append to existing file; false, otherwise.
	 * @throws IOException exception thrown if error in input-output operation.
	 */
	public void writeToCSV(String filename, boolean append) throws IOException
	{
		File fileObj = new File(filename);
		String[] header = {"Model", "Network", "t_0", "Simulation repetitions", "FN probability",
							"transmissability (p)", "no. of honeypots", "solver", "solver options",
							"objective value", "best UB", "solver message", "honeypots", "Wall time (s)",
							"Time (s)", "UTC"};
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
		for (Septet<String, String, Integer, Integer, Double, Double, Integer> key : mapToObjectiveValue.keySet())
		{
			String[] line = new String[16];
			line[0] = key.getValue0();                      // Model (TN11C, RAEPC, etc.)
			line[1] = key.getValue1();                      // network name
			line[2] = key.getValue2().toString();           // t_0
			line[3] = key.getValue3().toString();           // reps
			line[4] = key.getValue4().toString();           // false negative probs.
			line[5] = key.getValue5().toString();           // transmissability
			line[6] = key.getValue6().toString();           // no. of honeypots
			line[7] = "gurobi";
			line[8] = mapToSolverOptions.get(key);
			line[9] = mapToObjectiveValue.get(key).toString();
			line[10] = mapToBestUpperBound.get(key).toString();
			line[11] = mapToSolverMessage.get(key);
			line[12] = mapToHoneypots.get(key).toString();
			line[13] = mapToWallTime.get(key).toString();
			line[14] = mapToTime.get(key).toString();
			line[15] = now;
			writer.writeNext(line);
		}
		writer.flush();
		writer.close();
		System.out.println("MIP results successfully written to \""+filename+"\".");
	}
	
	/**
	 * Overrides {@code toString()}.
	 *
	 * @return returns a string representation of values in class.
	 */
	@Override
	public String toString()
	{
		return "Gurobi Solver Results:"
				+"\n\t Objective value:\n\t\t"+mapToObjectiveValue.toString()
				+"\n\t Best UB:\n\t\t"+mapToBestUpperBound.toString()
				+"\n\t Honeypots:\n\t\t"+mapToHoneypots.toString()
				+"\n\t Wall time (second):\n\t\t"+mapToWallTime.toString()
				+"\n\t Time (second):\n\t\t"+mapToTime.toString()
				+"\n\t Solver options:\n\t\t"+mapToSolverOptions.toString()
				+"\n\t Solver message:\n\t\t"+mapToSolverMessage.toString();
	}
}
