package analysis;

import com.opencsv.CSVWriter;
import dataTypes.algorithmOutput;
import dataTypes.parameters;
import dataTypes.statisticalOutput;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents results of the multiple replications procedure (<b>MRP</b>).
 * <p>
 *     Reference:
 * </p>
 * <p>
 *     [1] Bayraksan, G&uuml;zin, and David P. Morton.
 *     "Assessing solution quality in stochastic programs." <i>Mathematical Programming</i>
 *     108.2&ndash;3 (2006): 495&ndash;514.
 * </p>
 *
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: September 21, 2020.
 */
public class multipleReplicationsProcedure
{
	/**
	 * A map from {@code parameters} to {@code statisticalOutput}.
	 * Basically, stores the outputs for different input parameters.
	 * <p>
	 *     Parameters: (model (TN11C, RAEPC, etc.), network name, time step, repetitions,
	 *     false negative probability, transmissability (p), number of honeypots).
	 * </p>
	 * <p>
	 *     Statistical output: mean, standard deviation, CI width, etc.
	 * </p>
	 */
	Map<parameters, statisticalOutput> outputMap;
	
	/**
	 * Constructor.
	 */
	public multipleReplicationsProcedure()
	{
		this(new HashMap<>());
	}
	
	/**
	 * Constructor.
	 *
	 * @param outputMap an instance of {@code outputMap}.
	 */
	public multipleReplicationsProcedure(Map<parameters, statisticalOutput> outputMap)
	{
		this.outputMap = outputMap;
	}
	
	/**
	 * Getter.
	 *
	 * @return the instance of {@code outputMap}.
	 */
	public Map<parameters, statisticalOutput> getOutputMap()
	{
		return outputMap;
	}
	
	/**
	 * Overrides {@code toString}.
	 *
	 * @return string representation of values of field(s) in the class.
	 */
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder("Multiple Replications Procedure:");
		for(Map.Entry<parameters, statisticalOutput> e: outputMap.entrySet())
		{
			sb.append("\n\t<").append(e.getKey().toString()).append(", ");
			sb.append("\n\t\t mean =\n\t\t\t").append(e.getValue().getMean());
			sb.append("\n\t\t std. dev = \n\t\t\t").append(e.getValue().getStDev());
			sb.append("\n\t\t CI width (alpha = ").append(e.getValue().getAlpha());
			sb.append(") = ").append(e.getValue().getCIWidth());
			sb.append("\n\t>");
		}
		return sb.toString();
	}
	
	/**
	 * Writes MRP results to csv file.
	 *
	 * @param filename path to output file
	 * @param append true, if you wish to append to existing file; false, otherwise.
	 * @throws IOException thrown if error in input-output operation.
	 */
	public void writeToCSV(String filename, boolean append) throws IOException
	{
		File fileObj = new File(filename);
		String[] header = {"Model", "Network", "t_0", "Simulation repetitions", "FN probability",
				"transmissability (p)", "no. of honeypots", "mean", "std. dev.", "alpha", "CI width", "UTC"};
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
		for (Map.Entry<parameters, statisticalOutput> e: outputMap.entrySet())
		{
			String[] line = new String[12];
			line[0] = e.getKey().getSpreadModelName();
			line[1] = e.getKey().getNetworkName();
			line[2] = String.valueOf(e.getKey().getTimeStep());
			line[3] = String.valueOf(e.getKey().getNumberOfSimulationRepetitions());
			line[4] = String.valueOf(e.getKey().getFalseNegativeProbability());
			line[5] = String.valueOf(e.getKey().getTransmissability());
			line[6] = String.valueOf(e.getKey().getNumberOfHoneypots());
			line[7] = String.valueOf(e.getValue().getMean());
			line[8] = String.valueOf(e.getValue().getStDev());
			line[9] = String.valueOf(e.getValue().getAlpha());
			line[10] = String.valueOf(e.getValue().getCIWidth());
			line[11] = now;
			writer.writeNext(line);
		}
		writer.flush();
		writer.close();
		System.out.println("MRP results successfully written to \""+filename+"\".");
	}
	
	/**
	 *
	 * @param algorithmOutputs list of honeypots as a candidate solution
	 * @param alpha alpha value for confidence interval
	 * @param sampleSize sample size
	 * @param replicationSize replication size
	 * @throws Exception
	 */
	public void estimateGap(Map<parameters, algorithmOutput> algorithmOutputs, double alpha, int sampleSize, int replicationSize) throws Exception
	{
		if ((alpha<=0) || (alpha>=1))
			throw new Exception("Invalid value of alpha!");
	}
}
