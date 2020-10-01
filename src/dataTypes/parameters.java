package dataTypes;


import lombok.EqualsAndHashCode;

import java.util.Objects;

/**
 * Represents parameters for model simulation, optimization, etc.
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: October 1, 2020.
 */
@EqualsAndHashCode
public class parameters
{
	/**
	 * Name of the spread model (TN11C, RAEPC, etc.).
	 */
	String spreadModelName;
	/**
	 * Name of the network.
	 */
	String networkName;
	/**
	 * Time step of each simulation run.
	 */
	int timeStep;
	/**
	 * Number of times simulation is repeated.
	 * Each repetition represents a sample path.
	 */
	int numberOfSimulationRepetitions;
	/**
	 * False negative probability of detectors.
	 * Assuming it is the same for all detectors.
	 */
	double falseNegativeProbability;
	/**
	 * Transmissability probability.
	 */
	double transmissability;
	/**
	 * Number of honeypots to be installed.
	 */
	int numberOfHoneypots;
	/**
	 * Percentage of nodes to infect.
	 */
	double percentInfection;
	
	/**
	 * Constructor.
	 *
	 * @param spreadModelName name of the spread model
	 * @param networkName name of the network
	 * @param timeStep time step of each simulation run
	 * @param numberOfSimulationRepetitions number of times simulation is repeated
	 * @param falseNegativeProbability false negative probability of detectors
	 * @param transmissability transmissability probability
	 * @param numberOfHoneypots number of honeypots to be installed
	 * @param percentInfection percentage of nodes to infect.
	 */
	public parameters(String spreadModelName, String networkName, int timeStep, int numberOfSimulationRepetitions,
	                  double falseNegativeProbability, double transmissability, int numberOfHoneypots,
	                  double percentInfection)
	{
		this.spreadModelName = spreadModelName;
		this.networkName = networkName;
		this.timeStep = timeStep;
		this.numberOfSimulationRepetitions = numberOfSimulationRepetitions;
		this.falseNegativeProbability = falseNegativeProbability;
		this.transmissability = transmissability;
		this.numberOfHoneypots = numberOfHoneypots;
		this.percentInfection = percentInfection;
	}
	
	/**
	 * Copy constructor.
	 *
	 * @param param {@code parameters}.
	 */
	public parameters(parameters param)
	{
		this.spreadModelName = param.spreadModelName;
		this.networkName = param.networkName;
		this.timeStep = param.timeStep;
		this.numberOfSimulationRepetitions = param.numberOfSimulationRepetitions;
		this.falseNegativeProbability = param.falseNegativeProbability;
		this.transmissability = param.transmissability;
		this.numberOfHoneypots = param.numberOfHoneypots;
		this.percentInfection = param.percentInfection;
	}
	
	/**
	 * Getter.
	 *
	 * @return {@code spreadModelName}.
	 */
	public String getSpreadModelName()
	{
		return spreadModelName;
	}
	
	/**
	 * Getter.
	 *
	 * @return {@code networkName}.
	 */
	public String getNetworkName()
	{
		return networkName;
	}
	
	/**
	 * Getter.
	 *
	 * @return {@code timeStep}.
	 */
	public int getTimeStep()
	{
		return timeStep;
	}
	
	/**
	 * Getter.
	 *
	 * @return {@code numberOfSimulationRepetitions}.
	 */
	public int getNumberOfSimulationRepetitions()
	{
		return numberOfSimulationRepetitions;
	}
	
	/**
	 * Getter.
	 *
	 * @return {@code falseNegativeProbability}.
	 */
	public double getFalseNegativeProbability()
	{
		return falseNegativeProbability;
	}
	
	/**
	 * Getter.
	 *
	 * @return {@code transmissability}.
	 */
	public double getTransmissability()
	{
		return transmissability;
	}
	
	/**
	 * Getter.
	 *
	 * @return {@code numberOfHoneypots}.
	 */
	public int getNumberOfHoneypots()
	{
		return numberOfHoneypots;
	}
	
	/**
	 * Getter.
	 *
	 * @return {@code percentInfection}.
	 */
	public double getPercentInfection()
	{
		return percentInfection;
	}
	
	/**
	 * Setter.
	 *
	 * @param spreadModelName name of the spread model.
	 */
	public void setSpreadModelName(String spreadModelName)
	{
		this.spreadModelName = spreadModelName;
	}
	
	/**
	 * Setter.
	 *
	 * @param networkName name of the network.
	 */
	public void setNetworkName(String networkName)
	{
		this.networkName = networkName;
	}
	
	/**
	 * Setter.
	 *
	 * @param timeStep time step of each simulation run.
	 */
	public void setTimeStep(int timeStep)
	{
		this.timeStep = timeStep;
	}
	
	/**
	 * Setter.
	 *
	 * @param numberOfSimulationRepetitions number of times simulation is repeated.
	 */
	public void setNumberOfSimulationRepetitions(int numberOfSimulationRepetitions)
	{
		this.numberOfSimulationRepetitions = numberOfSimulationRepetitions;
	}
	
	/**
	 * Setter.
	 *
	 * @param falseNegativeProbability false negative probability of detectors.
	 */
	public void setFalseNegativeProbability(double falseNegativeProbability)
	{
		this.falseNegativeProbability = falseNegativeProbability;
	}
	
	/**
	 * Setter.
	 *
	 * @param transmissability transmissability probability.
	 */
	public void setTransmissability(double transmissability)
	{
		this.transmissability = transmissability;
	}
	
	/**
	 * Setter.
	 *
	 * @param numberOfHoneypots number of honeypots to be installed.
	 */
	public void setNumberOfHoneypots(int numberOfHoneypots)
	{
		this.numberOfHoneypots = numberOfHoneypots;
	}
	
	/**
	 * Setter.
	 *
	 * @param percentInfection percentage of node to infect.
	 */
	public void setPercentInfection(double percentInfection)
	{
		this.percentInfection = percentInfection;
	}
	
	/**
	 * Returns a string representation of the object.
	 *
	 * @return a string representation of the object.
	 */
	@Override
	public String toString()
	{
		return "Parameters: "
				+"spread model --- "+spreadModelName+"; "
				+"network --- "+networkName+"; "
				+"time step = "+timeStep+"; "
				+"simulation repetitions = "+numberOfSimulationRepetitions+"; "
				+"false negative probability = "+falseNegativeProbability+"; "
				+"transmissability = "+transmissability+"; "
				+"number of honeypots = "+numberOfHoneypots+"; "
				+"percentage infection = "+percentInfection+".";
	}
	
	/**
	 * Indicates whether some other object is "equal to" this one.
	 * Used guidelines at <a href="http://www.technofundo.com/tech/java/equalhash.html" target="_blank">
	 *     "Equals and Hash Code"</a>.
	 *
	 * @param obj the reference object with which to compare.
	 * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
	 */
	@Override
	public boolean equals(Object obj)
	{
		// this instance check
		if (this == obj)
			return true;
		// null check
		if (obj == null || (obj.getClass() != this.getClass()))
			return  false;
		parameters newObj = (parameters) obj;
		
		if ((newObj.timeStep != timeStep) || (newObj.numberOfSimulationRepetitions != numberOfSimulationRepetitions)
			|| (newObj.numberOfHoneypots != numberOfHoneypots))
			return false;
		else
		{
			if ((Double.compare(newObj.falseNegativeProbability, falseNegativeProbability) != 0)
					|| (Double.compare(newObj.transmissability, transmissability) !=0)
					|| (Double.compare(newObj.percentInfection, percentInfection) !=0))
				return false;
			else
				return (spreadModelName == newObj.spreadModelName
						|| (spreadModelName != null && spreadModelName.equals(newObj.spreadModelName)))
						&& (networkName == newObj.networkName
							|| (networkName != null && networkName.equals(newObj.networkName)));
		}
		
	}
	
	// TODO: Which implementation of hashCode is more efficient?
	///**
	// * Returns a hash code value for the object.
	// * Used guidelines at <a href="http://www.technofundo.com/tech/java/equalhash.html" target="_blank">
	// *     "Equals and Hash Code"</a>.
	// *
	// * @return a hash code value for this object.
	// */
	//@Override
	//public int hashCode()
	//{
	//	int hash = 7;
	//	// integer fields
	//	hash = 31*hash + timeStep;
	//	hash = 31*hash + numberOfSimulationRepetitions;
	//	hash = 31*hash + numberOfHoneypots;
	//	// double fields
	//	long bits;
	//	bits = Double.doubleToLongBits(falseNegativeProbability);
	//	hash = 31*hash + ((int) (bits ^ (bits >>> 32)));
	//	bits = Double.doubleToLongBits(transmissability);
	//	hash = 31*hash + ((int) (bits ^ (bits >>> 32)));
	//	bits = Double.doubleToLongBits(percentInfection);
	//	hash = 31 * hash + (int) (bits ^ (bits >>> 32));
	//	// String fields
	//	hash = 31*hash + (null == spreadModelName ? 0 : spreadModelName.hashCode());
	//	hash = 31*hash + (null == networkName ? 0 : networkName.hashCode());
	//	return hash;
	//}
	
	/**
	 * Returns a hash code value for the object.
	 *
	 * @return a hash code value for this object.
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(spreadModelName, networkName, timeStep, numberOfSimulationRepetitions,
							falseNegativeProbability, transmissability, numberOfHoneypots, percentInfection);
	}
}
