package dataTypes;

import java.util.List;
import java.util.Objects;

/**
 * Represents output of an algorithm.
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: September 11, 2020.
 */
public class algorithmOutput
{
	/**
	 * Value of the objective function.
	 */
	double objectiveValue;
	/**
	 * Nodes in the honeypot.
	 */
	List<Integer> honeypot;
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
	 * @param honeypot nodes in the honeypot
	 * @param wallTime algorithm execution time
	 * @param aPrioriUB a priori upper bound
	 * @param posteriorUB posterior upper bound.
	 */
	public algorithmOutput(double objectiveValue, List<Integer> honeypot, double wallTime,
	                       double aPrioriUB, double posteriorUB)
	{
		this.objectiveValue = objectiveValue;
		this.honeypot = honeypot;
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
		this.honeypot = output.honeypot;
		this.wallTime = output.wallTime;
		this.aPrioriUB = output.aPrioriUB;
		this.posteriorUB = output.posteriorUB;
	}
	
	/**
	 * Getter.
	 *
	 * @return returns {@code objectiveValue}.
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
	 * @return returns {@code honeypot}.
	 */
	public List<Integer> getHoneypot()
	{
		return honeypot;
	}
	
	/**
	 * Setter.
	 *
	 * @param honeypot nodes in the honeypot.
	 */
	public void setHoneypot(List<Integer> honeypot)
	{
		this.honeypot = honeypot;
	}
	
	/**
	 * Getter.
	 *
	 * @return returns {@code wallTime}.
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
	 * @return returns {@code aPrioriUB}.
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
	 * @return returns {@code posteriorUB}.
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
	 * Returns a string representation of all the fields in the object.
	 * Overrides {@code toString()}.
	 *
	 * @return a string representation of the object.
	 */
	@Override
	public String toString()
	{
		return "Algorithm output: "
				+"objective value = "+this.objectiveValue+"; "
				+"honeypot --- "+this.honeypot+"; "
				+"wall time = "+this.wallTime+"; "
				+"a priori upper bound = "+this.aPrioriUB+"; "
				+"posterior upper bound = "+this.posteriorUB+".";
	}
	
	/**
	 * Overrides {@code equals}.
	 * Used guidelines at <a href="http://www.technofundo.com/tech/java/equalhash.html" target="_blank">
	 *     "Equals and Hash Code"</a>.
	 *
	 * @param o an object.
	 * @return returns true if the values of all individual fields match; false, otherwise.
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
				honeypot.equals(that.honeypot);
	}
	
	/**
	 * Overrides {@code hashCode()}.
	 *
	 * @return returns a integer value representing the hash code for an object of this class.
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(objectiveValue, honeypot, wallTime, aPrioriUB, posteriorUB);
	}
}
