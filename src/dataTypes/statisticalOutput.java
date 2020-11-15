package dataTypes;

import java.util.Objects;

/**
 * Represents output (mean, CI width, etc.) of a statistical analysis.
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: November 14, 2020.
 */
public class statisticalOutput
{
	/**
	 * Mean (average).
	 */
	double mean;
	/**
	 * Standard deviation;
	 */
	double stDev;
	/**
	 * Alpha value used to generate {@code CIWidth}.
	 */
	double alpha;
	/**
	 * Width of confidence interval.
	 */
	double CIWidth;
	
	/**
	 * Sample size.
	 */
	int sampleSize;
	
	/**
	 * Replication size.
	 */
	int replicationSize;
	
	/**
	 * Constructor.
	 *
	 * @param mean mean (average)
	 * @param stDev standard deviation
	 * @param alpha alpha value used to generate {@code CIWidth}
	 * @param CIWidth width of confidence interval
	 * @param sampleSize sample size
	 * @param replicationSize replication size.
	 */
	public statisticalOutput(double mean, double stDev, double alpha, double CIWidth,
	                         int sampleSize, int replicationSize)
	{
		this.mean = mean;
		this.stDev = stDev;
		this.alpha = alpha;
		this.CIWidth = CIWidth;
		this.sampleSize = sampleSize;
		this.replicationSize = replicationSize;
	}
	
	/**
	 * Copy constructor.
	 *
	 * @param output an instance of {@code statisticalOutput}.
	 */
	public statisticalOutput(statisticalOutput output)
	{
		this.mean = output.mean;
		this.stDev = output.stDev;
		this.alpha = output.alpha;
		this.CIWidth = output.CIWidth;
		this.sampleSize = output.sampleSize;
		this.replicationSize = output.replicationSize;
	}
	
	/**
	 * Getter.
	 *
	 * @return the mean {@code mean}.
	 */
	public double getMean()
	{
		return mean;
	}
	
	/**
	 * Setter.
	 *
	 * @param mean the mean value.
	 */
	public void setMean(double mean)
	{
		this.mean = mean;
	}
	
	/**
	 * Getter.
	 *
	 * @return the standard deviation {@code stDev}.
	 */
	public double getStDev()
	{
		return stDev;
	}
	
	/**
	 * Setter.
	 *
	 * @param stDev the standard deviation.
	 */
	public void setStDev(double stDev)
	{
		this.stDev = stDev;
	}
	
	/**
	 * Getter.
	 *
	 * @return alpha value used to generate {@code CIWidth}.
	 */
	public double getAlpha()
	{
		return alpha;
	}
	
	/**
	 * Setter.
	 *
	 * @param alpha alpha value used to generate {@code CIWidth}.
	 */
	public void setAlpha(double alpha)
	{
		this.alpha = alpha;
	}
	
	/**
	 * Getter.
	 *
	 * @return the width of the confidence interval {@code CIWidth}.
	 */
	public double getCIWidth()
	{
		return CIWidth;
	}
	
	/**
	 * Setter.
	 *
	 * @param CIWidth the width of the confidence interval.
	 */
	public void setCIWidth(double CIWidth)
	{
		this.CIWidth = CIWidth;
	}
	
	/**
	 * Getter.
	 *
	 * @return {@code sampleSize}.
	 */
	public int getSampleSize()
	{
		return sampleSize;
	}
	
	/**
	 * Setter.
	 *
	 * @param sampleSize sample size.
	 */
	public void setSampleSize(int sampleSize)
	{
		this.sampleSize = sampleSize;
	}
	
	/**
	 * Getter.
	 *
	 * @return {@code replicationSize}.
	 */
	public int getReplicationSize()
	{
		return replicationSize;
	}
	
	/**
	 * Setter.
	 *
	 * @param replicationSize replication size.
	 */
	public void setReplicationSize(int replicationSize)
	{
		this.replicationSize = replicationSize;
	}
	
	/**
	 * Returns a string representation of the object.
	 *
	 * @return a string representation of the object.
	 */
	@Override
	public String toString()
	{
		return "Statistical output: "
				+"replication size = "+this.replicationSize+"; "
				+"sample size = "+this.sampleSize+"; "
				+"mean = "+this.mean+"; "
				+"std. dev. = "+this.stDev+"; "
				+"CI width (alpha = "+this.alpha+" ) = "+this.CIWidth+".";
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
		statisticalOutput output = (statisticalOutput) o;
		return Double.compare(output.mean, mean) == 0 &&
				Double.compare(output.stDev, stDev) == 0 &&
				Double.compare(output.alpha, alpha) == 0 &&
				Double.compare(output.CIWidth, CIWidth) == 0 &&
				sampleSize == output.sampleSize &&
				replicationSize == output.replicationSize;
	}
	
	/**
	 * Returns a hash code value for the object.
	 *
	 * @return a hash code value for this object.
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(mean, stDev, alpha, CIWidth, sampleSize, replicationSize);
	}
}
