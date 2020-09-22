package dataTypes;

import java.util.List;
import java.util.Objects;

/**
 * Represents output of an algorithm.
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: September 22, 2020.
 */
public class algorithmOutput
{
	/**
	 * Value of the objective function.
	 */
	double objectiveValue;
	/**
	 * Nodes where detectors are placed.
	 */
	List<Integer> honeypots;
	/**
	 * Algorithm execution time.
	 */
	double wallTime;
	/**
	 * A priori upper bound.
	 */
	double aPrioriUB;
	/**
	 * Posterior upper bound.
	 */
	double posteriorUB;
	
	/**
	 * Constructor.
	 *
	 * @param objectiveValue value of the objective function
	 * @param honeypots nodes in the honeypots
	 * @param wallTime algorithm execution time
	 * @param aPrioriUB a priori upper bound
	 * @param posteriorUB posterior upper bound.
	 */
	public algorithmOutput(double objectiveValue, List<Integer> honeypots, double wallTime,
	                       double aPrioriUB, double posteriorUB)
	{
		this.objectiveValue = objectiveValue;
		this.honeypots = honeypots;
		this.wallTime = wallTime;
		this.aPrioriUB = aPrioriUB;
		this.posteriorUB = posteriorUB;
	}
	
	/**
	 * Copy constructor.
	 *
	 * @param output An instance of {@code algorithmOutput}.
	 */
	public algorithmOutput(algorithmOutput output)
	{
		this.objectiveValue = output.objectiveValue;
		this.honeypots = output.honeypots;
		this.wallTime = output.wallTime;
		this.aPrioriUB = output.aPrioriUB;
		this.posteriorUB = output.posteriorUB;
	}
	
	/**
	 * Getter.
	 *
	 * @return {@code objectiveValue}.
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
	 * @return {@code honeypots}.
	 */
	public List<Integer> getHoneypots()
	{
		return honeypots;
	}
	
	/**
	 * Setter.
	 *
	 * @param honeypots nodes in the honeypots.
	 */
	public void setHoneypots(List<Integer> honeypots)
	{
		this.honeypots = honeypots;
	}
	
	/**
	 * Getter.
	 *
	 * @return {@code wallTimeInSeconds}.
	 */
	public double getWallTime()
	{
		return wallTime;
	}
	
	/**
	 * Setter.
	 *
	 * @param wallTime algorithm execution time.
	 */
	public void setWallTime(double wallTime)
	{
		this.wallTime = wallTime;
	}
	
	/**
	 * Getter.
	 *
	 * @return {@code aPrioriUB}.
	 */
	public double getAPrioriUB()
	{
		return aPrioriUB;
	}
	
	/**
	 * Setter.
	 *
	 * @param aPrioriUB a priori upper bound.
	 */
	public void setAPrioriUB(double aPrioriUB)
	{
		this.aPrioriUB = aPrioriUB;
	}
	
	/**
	 * Getter.
	 *
	 * @return {@code posteriorUB}.
	 */
	public double getPosteriorUB()
	{
		return posteriorUB;
	}
	
	/**
	 * Setter.
	 *
	 * @param posteriorUB posterior upper bound.
	 */
	public void setPosteriorUB(double posteriorUB)
	{
		this.posteriorUB = posteriorUB;
	}
	
	/**
	 * Returns a string representation of the object.
	 *
	 * @return a string representation of the object.
	 */
	@Override
	public String toString()
	{
		return "Algorithm output: "
				+"objective value = "+this.objectiveValue+"; "
				+"honeypots --- "+this.honeypots +"; "
				+"wall time = "+this.wallTime+"; "
				+"a priori upper bound = "+this.aPrioriUB+"; "
				+"posterior upper bound = "+this.posteriorUB+".";
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
		algorithmOutput that = (algorithmOutput) o;
		return Double.compare(that.objectiveValue, objectiveValue) == 0 &&
				Double.compare(that.wallTime, wallTime) == 0 &&
				Double.compare(that.aPrioriUB, aPrioriUB) == 0 &&
				Double.compare(that.posteriorUB, posteriorUB) == 0 &&
				honeypots.equals(that.honeypots);
	}
	
	/**
	 * Returns a hash code value for the object.
	 *
	 * @return a hash code value for this object.
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(objectiveValue, honeypots, wallTime, aPrioriUB, posteriorUB);
	}
}
