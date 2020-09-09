package dataTypes;


import lombok.EqualsAndHashCode;

import java.util.Objects;

/**
 * Represents parameters for model simulation, optimization, etc.
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: September 9, 2020.
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
	 * @return returns {@code spreadModelName}.
	 */
	public String getSpreadModelName()
	{
		return spreadModelName;
	}
	
	/**
	 * Getter.
	 *
	 * @return returns {@code networkName}.
	 */
	public String getNetworkName()
	{
		return networkName;
	}
	
	/**
	 * Getter.
	 *
	 * @return returns {@code timeStep}.
	 */
	public int getTimeStep()
	{
		return timeStep;
	}
	
	/**
	 * Getter.
	 *
	 * @return returns {@code numberOfSimulationRepetitions}.
	 */
	public int getNumberOfSimulationRepetitions()
	{
		return numberOfSimulationRepetitions;
	}
	
	/**
	 * Getter.
	 *
	 * @return returns {@code falseNegativeProbability}.
	 */
	public double getFalseNegativeProbability()
	{
		return falseNegativeProbability;
	}
	
	/**
	 * Getter.
	 *
	 * @return returns {@code transmissability}.
	 */
	public double getTransmissability()
	{
		return transmissability;
	}
	
	/**
	 * Getter.
	 *
	 * @return returns {@code numberOfHoneypots}.
	 */
	public int getNumberOfHoneypots()
	{
		return numberOfHoneypots;
	}
	
	/**
	 * Getter.
	 *
	 * @return returns {@code percentInfection}.
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
	 * Overrides {@code toString}.
	 *
	 * @return returns a string representation of all the fields in the class.
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
	 * Overrides {@code equals}.
	 * Used guidelines at <a href="http://www.technofundo.com/tech/java/equalhash.html" target="_blank">
	 *     "Equals and Hash Code"</a>.
	 * @param obj an object.
	 * @return returns true if the values of all individual fields match; false, otherwise.
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
	
	// TODO: Which implemetatoin of hashCode is more efficient?
	///**
	// * Overrides {@code hashCode}.
	// * Used guidelines at <a href="http://www.technofundo.com/tech/java/equalhash.html" target="_blank">
	// *     "Equals and Hash Code"</a>.
	// *
	// * @return returns a integer value representing the hash code for an object.
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
	 * Overrides {@code hashCode}.
	 * Uses lombok.
	 *
	 * @return returns a integer value representing the hash code for an object.
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(spreadModelName, networkName, timeStep, numberOfSimulationRepetitions,
							falseNegativeProbability, transmissability, numberOfHoneypots, percentInfection);
	}
}
