package algorithm;

import com.opencsv.CSVWriter;
import dataTypes.algorithmOutput;
import dataTypes.parameters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents results of degree centrality on {@code simulationRuns}.
 * In degree centrality we choose the k vertices with highest degree.
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: September 16, 2020.
 */
public class degreeCentrality
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
	public degreeCentrality()
	{
		this(new HashMap<>());
	}
	
	/**
	 * Constructor.
	 *
	 * @param outputMap a map from {@code parameters} to {@code algorithmOutput}.
	 */
	public degreeCentrality(Map<parameters, algorithmOutput> outputMap)
	{
		this.outputMap = outputMap;
	}
	
	/**
	 * Getter.
	 *
	 * @return returns {@code outputMap}.
	 */
	public Map<parameters, algorithmOutput> getOutputMap()
	{
		return outputMap;
	}
	
	public void getKHighestDegreeNodes()
	{
	
	}
	
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
			line[8] = e.getValue().getHoneypot().toString();
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
	 * Overrides {@code toString()}.
	 *
	 * @return returns string representation of values of field(s) in the class.
	 */
	@Override
	public String toString()
	{
		StringBuilder str = new StringBuilder(1000);
		str.append("degreeCentrality:");
		for(Map.Entry<parameters, algorithmOutput> e: outputMap.entrySet())
		{
			str.append("\n\t<").append(e.getKey()).append(", ").append(e.getValue()).append(">");
			str.append("\n\t\t Objective value:\n\t\t\t").append(e.getValue().getObjectiveValue());
			str.append("\n\t\t Honeypots:\n\t\t\t").append(e.getValue().getHoneypot());
			str.append("\n\t\t a priori UB:\n\t\t\t").append(e.getValue().getAPrioriUB());
			str.append("\n\t\t posterior UB:\n\t\t\t").append(e.getValue().getPosteriorUB());
			str.append("\n\t\t Wall time (second):\n\t\t\t").append(e.getValue().getWallTime());
		}
		return str.toString();
	}
}
