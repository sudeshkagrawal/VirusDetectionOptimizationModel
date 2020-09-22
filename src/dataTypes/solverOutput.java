package dataTypes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents output of an optimization solver.
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: September 22, 2020.
 */
public class solverOutput
{
	/**
	 * Value of the objective function.
	 */
	double objectiveValue;
	
	/**
	 * Best known upper bound.
	 */
	double bestUB;
	
	/**
	 * Nodes where detectors are placed.
	 */
	List<Integer> honeypots;
	
	/**
	 * Solver execution time.
	 */
	double wallTimeInSeconds;
	
	/**
	 * Solver options (tolerance, iteration limit, etc.) used in optimization.
	 *
	 * Allowed solver options:
	 * <dl>
	 *     <dt>IntFeasTol</dt> <dd>integer feasibility tolerance</dd>
	 *     <dt>MIPGap</dt> <dd>current relative MIP optimality gap</dd>
	 *     <dt>threads</dt> <dd>number of threads to apply to parallel algorithms</dd>
	 *     <dt>Presolve</dt> <dd>controls the presolve level</dd>
	 *     <dt>TimeLimit</dt> <dd>limits the total time expended (in seconds).</dd>
	 * </dl>
	 */
	Map<String, String> solverOptionsUsed;
	
	/**
	 * Solver message (generally indicating the solver status).
	 */
	String solverMessage;
	
	/**
	 * Constructor.
	 *
	 * @param objectiveValue value of the objective function
	 * @param bestUB best known upper bound
	 * @param honeypots nodes where detectors are placed
	 * @param wallTimeInSeconds solver execution time
	 * @param solverOptionsUsed solver options used in optimization
	 * @param solverMessage solver message.
	 */
	public solverOutput(double objectiveValue, double bestUB, List<Integer> honeypots, double wallTimeInSeconds,
	                    Map<String, String> solverOptionsUsed, String solverMessage)
	{
		this.objectiveValue = objectiveValue;
		this.bestUB = bestUB;
		this.honeypots = honeypots;
		this.wallTimeInSeconds = wallTimeInSeconds;
		this.solverOptionsUsed = solverOptionsUsed;
		this.solverMessage = solverMessage;
	}
	
	/**
	 * Copy constructor.
	 *
	 * @param output an instance of {@code solverOutput}.
	 */
	public solverOutput(solverOutput output)
	{
		this.objectiveValue = output.objectiveValue;
		this.bestUB = output.bestUB;
		this.honeypots = output.honeypots;
		this.wallTimeInSeconds = output.wallTimeInSeconds;
		this.solverOptionsUsed = output.solverOptionsUsed;
		this.solverMessage = output.solverMessage;
	}
	
	/**
	 * Constructor.
	 */
	public solverOutput()
	{
		this.solverOptionsUsed = new HashMap<>();
		this.solverMessage = "";
	}
	
	/**
	 * Getter.
	 *
	 * @return value of the objective function {@code objectiveValue}.
	 */
	public double getObjectiveValue()
	{
		return objectiveValue;
	}
	
	/**
	 * Setter.
	 *
	 * @param objectiveValue value of the objective function.
	 */
	public void setObjectiveValue(double objectiveValue)
	{
		this.objectiveValue = objectiveValue;
	}
	
	/**
	 * Getter.
	 *
	 * @return best known upper bound {@code bestUB}.
	 */
	public double getBestUB()
	{
		return bestUB;
	}
	
	/**
	 * Setter.
	 *
	 * @param bestUB best known upper bound.
	 */
	public void setBestUB(double bestUB)
	{
		this.bestUB = bestUB;
	}
	
	/**
	 * Getter.
	 *
	 * @return nodes where detectors are installed {@code honeypots}
	 */
	public List<Integer> getHoneypots()
	{
		return honeypots;
	}
	
	/**
	 * Setter.
	 *
	 * @param honeypots nodes where detectors are installed.
	 */
	public void setHoneypots(List<Integer> honeypots)
	{
		this.honeypots = honeypots;
	}
	
	/**
	 * Getter.
	 *
	 * @return solver execution time {@code wallTimeInSeconds}.
	 */
	public double getWallTimeInSeconds()
	{
		return wallTimeInSeconds;
	}
	
	/**
	 * Setter.
	 *
	 * @param wallTimeInSeconds solver execution time.
	 */
	public void setWallTimeInSeconds(double wallTimeInSeconds)
	{
		this.wallTimeInSeconds = wallTimeInSeconds;
	}
	
	/**
	 * Getter.
	 *
	 * @return solver options used in optimization {@code solverOptionsUsed}.
	 */
	public Map<String, String> getSolverOptionsUsed()
	{
		return solverOptionsUsed;
	}
	
	/**
	 * Setter.
	 *
	 * @param solverOptionsUsed solver options used in optimization.
	 */
	public void setSolverOptionsUsed(Map<String, String> solverOptionsUsed)
	{
		this.solverOptionsUsed = solverOptionsUsed;
	}
	
	/**
	 * Getter.
	 *
	 * @return the solver message {@code solverMessage}.
	 */
	public String getSolverMessage()
	{
		return solverMessage;
	}
	
	/**
	 * Setter.
	 *
	 * @param solverMessage solver message.
	 */
	public void setSolverMessage(String solverMessage)
	{
		this.solverMessage = solverMessage;
	}
	
	/**
	 * Returns a string representation of the object.
	 *
	 * @return a string representation of the object.
	 */
	@Override
	public String toString()
	{
		String s = "Solver Output: " + "objective value = " + this.objectiveValue +
				"; bestUB = " + this.bestUB +
				"; honeypots: " + this.honeypots +
				"; wall time (second) = " + this.wallTimeInSeconds +
				"; solver options used: " + this.solverOptionsUsed.toString() +
				"; solverMessage: '" + this.solverMessage + '\'' +
				'.';
		return s;
	}
	
	/**
	 * Indicates whether some other object is "equal to" this one.
	 * Used guidelines at <a href="http://www.technofundo.com/tech/java/equalhash.html" target="_blank">
	 *     "Equals and Hash Code"</a>.
	 *
	 * @param o the reference object with which to compare.
	 * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
	 */
	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		solverOutput that = (solverOutput) o;
		return Double.compare(that.objectiveValue, objectiveValue) == 0 &&
				Double.compare(that.bestUB, bestUB) == 0 &&
				Double.compare(that.wallTimeInSeconds, wallTimeInSeconds) == 0 &&
				honeypots.equals(that.honeypots) &&
				solverOptionsUsed.equals(that.solverOptionsUsed) &&
				solverMessage.equals(that.solverMessage);
	}
	
	/**
	 * Returns a hash code value for the object.
	 *
	 * @return a hash code value for this object.
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(objectiveValue, bestUB, honeypots, wallTimeInSeconds, solverOptionsUsed, solverMessage);
	}
}
