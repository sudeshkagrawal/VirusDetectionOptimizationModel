package optimization;

import gurobi.*;
import network.graph;
import org.javatuples.Quintet;
import org.javatuples.Sextet;
import org.jgrapht.alg.util.Triple;
import simulation.simulationRuns;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class gurobiSolver
{
	// Model (TN11C, RAEPC, etc.); Network name; t_0; repetitions; false negative probability; number of honeypots
	Map<Sextet<String, String, Integer, Integer, Double, Integer>, Double> mapToObjectiveValue;
	Map<Sextet<String, String, Integer, Integer, Double, Integer>, Double> mapToBestUpperBound;
	Map<Sextet<String, String, Integer, Integer, Double, Integer>, List<Integer>> mapToHoneypots;
	Map<Sextet<String, String, Integer, Integer, Double, Integer>, Double> mapToWallTime;
	Map<Sextet<String, String, Integer, Integer, Double, Integer>, Double> mapToTime;
	Map<Sextet<String, String, Integer, Integer, Double, Integer>, String> mapToSolverOptions;
	Map<Sextet<String, String, Integer, Integer, Double, Integer>, String> mapToSolverMessage;
	
	public gurobiSolver(Map<Sextet<String, String, Integer, Integer, Double, Integer>, Double> mapToObjectiveValue,
	                    Map<Sextet<String, String, Integer, Integer, Double, Integer>, Double> mapToBestUpperBound,
	                    Map<Sextet<String, String, Integer, Integer, Double, Integer>, List<Integer>> mapToHoneypots,
	                    Map<Sextet<String, String, Integer, Integer, Double, Integer>, Double> mapToWallTime,
	                    Map<Sextet<String, String, Integer, Integer, Double, Integer>, Double> mapToTime,
	                    Map<Sextet<String, String, Integer, Integer, Double, Integer>, String> mapToSolverOptions,
	                    Map<Sextet<String, String, Integer, Integer, Double, Integer>, String> mapToSolverMessage)
	{
		this.mapToObjectiveValue = mapToObjectiveValue;
		this.mapToBestUpperBound = mapToBestUpperBound;
		this.mapToHoneypots = mapToHoneypots;
		this.mapToWallTime = mapToWallTime;
		this.mapToTime = mapToTime;
		this.mapToSolverOptions = mapToSolverOptions;
		this.mapToSolverMessage = mapToSolverMessage;
	}
	public gurobiSolver()
	{
		this(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(),
				new HashMap<>(), new HashMap<>(), new HashMap<>());
	}
	
	/**
	 *
	 * @param modelName
	 * @param g
	 * @param simulationResults
	 * @param k_t0_runs
	 * @param r
	 * @param logFilename
	 * @throws Exception
	 */
	public void solveSAA(String modelName, graph g, simulationRuns simulationResults,
	                     List<Triple<Integer, Integer, Integer>> k_t0_runs, double r, String logFilename) throws Exception
	{
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
		env.set(GRB.IntParam.valueOf("OutputFlag"), 0);
		env.start();
		
		for (Triple<Integer, Integer, Integer> v : k_t0_runs)
		{
			int k = v.getFirst();
			int t_0 = v.getSecond();
			int run = v.getThird();
			Quintet<String, String, Integer, Integer, Double> key = new Quintet<>(modelName, g.getNetworkName(), t_0, run, r);
			Sextet<String, String, Integer, Integer, Double, Integer> fullKey = new Sextet<>(modelName, g.getNetworkName(), t_0, run, r, k);
			
			System.out.println("Solving MIP model (false negative prob = "+r+") for "+k+" honeypots and "+run+" samples...");
			List<List<Integer>> virusSpreadSamples =
					simulationResults.getMapModelNetworkT0RunsFalseNegativeToSimulationRuns().get(key);
			List<List<Integer>> virtualDetectionSamples =
					simulationResults.getMapModelNetworkT0RunsFalseNegativeToVirtualDetections().get(key);
			List<Integer> replicationIndex = IntStream.range(0, run).boxed().collect(Collectors.toList());
			// System.out.println("Virus spread samples:\n"+virusSpreadSamples+"\n"+virtualDetectionSamples);
			List<List<Integer>> successfulDetectMatrix;
			Set<Integer> candidates;
			if (r>0)
			{
				if (zeroNode)
				{
					List<List<Integer>> newVirusSpreadSamples = virusSpreadSamples.stream().map(virusSpreadSample -> virusSpreadSample.stream()
							.map(integer -> integer + 1)
							.collect(Collectors.toCollection(() -> new ArrayList<>(t_0 + 1))))
							.collect(Collectors.toCollection(() -> new ArrayList<>(run)));
					successfulDetectMatrix = elementwiseMultiplyMatrix(Collections.unmodifiableList(newVirusSpreadSamples),
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
			// x.entrySet().forEach(System.out::println);
			// u.entrySet().forEach(System.out::println);
			
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
			System.out.println("Constraints:");
			// Arrays.stream(model.getConstrs()).forEach(System.out::println);
			
			// Solve the model
			double intFeasTol = 1e-9, mipGap = 1e-2, timeLimit=300;
			int threads = 8, presolve=-1;
			model.set(GRB.DoubleParam.IntFeasTol, intFeasTol);
			model.set(GRB.DoubleParam.MIPGap, mipGap);
			model.set(GRB.IntParam.Threads, threads);
			// Presolve: -1 is automatic; 0 is off; 1 is conservative; 2 is aggressive
			model.set(GRB.IntParam.Presolve, presolve);
			model.set(GRB.DoubleParam.TimeLimit, timeLimit);
			
			model.optimize();
			
			// Display/Return the results
			mapToObjectiveValue.put(fullKey, model.get(GRB.DoubleAttr.ObjVal));
			mapToBestUpperBound.put(fullKey, model.get(GRB.DoubleAttr.ObjBound));
			mapToWallTime.put(fullKey, model.get(GRB.DoubleAttr.Runtime));
			List<Integer> honeypots = new ArrayList<>();;
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
			model.dispose();
		}
		env.dispose();
	}
	
	/**
	 * Element-wise multiplication of two list of lists.
	 * @param a the first list of lists
	 * @param b the second list of lists.
	 * @return returns a list of lists.
	 * @throws Exception exception thrown if outer lists {@code a} and {@code b} not of same size.
	 *          Corresponding inner lists should also be of same size, but that exception is not thrown since the check would be expensive.
	 */
	private List<List<Integer>> elementwiseMultiplyMatrix(List<List<Integer>> a, List<List<Integer>> b) throws Exception
	{
		List<List<Integer>> output = new ArrayList<>(a.size());
		if (a.size()!=b.size())
			throw new Exception("Inputs are not of the same size!");
		for (int i=0; i<a.size(); i++)
		{
			List<Integer> colList = new ArrayList<>(a.get(i).size());
			for (int j=0; j<a.get(0).size(); j++)
				colList.add(a.get(i).get(j) * b.get(i).get(j));
			output.add(colList);
		}
		return output;
	}
	
//	Map<Sextet<String, String, Integer, Integer, Double, Integer>, Double> mapToObjectiveValue;
//	Map<Sextet<String, String, Integer, Integer, Double, Integer>, Double> mapToBestUpperBound;
//	Map<Sextet<String, String, Integer, Integer, Double, Integer>, List<Integer>> mapToHoneypots;
//	Map<Sextet<String, String, Integer, Integer, Double, Integer>, Double> mapToWallTime;
//	Map<Sextet<String, String, Integer, Integer, Double, Integer>, Double> mapToTime;
//	Map<Sextet<String, String, Integer, Integer, Double, Integer>, String> mapToSolverOptions;
//	Map<Sextet<String, String, Integer, Integer, Double, Integer>, String> mapToSolverMessage;
	
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
