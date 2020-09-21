package dataTypes;

import java.util.Objects;

/**
 * Represents output (mean, CI width, etc.) of a statistical analysis.
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: September 21, 2020.
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
	 * Constructor.
	 *
	 * @param mean mean (average)
	 * @param stDev standard deviation
	 * @param alpha alpha value used to generate {@code CIWidth}
	 * @param CIWidth width of confidence interval.
	 */
	public statisticalOutput(double mean, double stDev, double alpha, double CIWidth)
	{
		this.mean = mean;
		this.stDev = stDev;
		this.alpha = alpha;
		this.CIWidth = CIWidth;
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
	}
	
	/**
	 * Getter.
	 *
	 * @return the mean.
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
	 * @return the standard deviation.
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
	 * @return the width of the confidence interval.
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
	 * Returns a string representation of all the fields in the object.
	 * Overrides {@code toString}.
	 *
	 * @return a string representation of the object.
	 */
	@Override
	public String toString()
	{
		return "Statistical output: "
				+"mean = "+this.mean+"; "
				+"std. dev. = "+this.stDev+"; "
				+"CI width (alpha = "+this.alpha+" ) = "+this.CIWidth+".";
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
		statisticalOutput that = (statisticalOutput) o;
		return Double.compare(that.mean, mean) == 0 &&
				Double.compare(that.stDev, stDev) == 0 &&
				Double.compare(that.alpha, alpha) == 0 &&
				Double.compare(that.CIWidth, CIWidth) == 0;
	}
	
	/**
	 * Overrides {@code hashCode}.
	 *
	 * @return returns a integer value representing the hash code for an object of this class.
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(mean, stDev, alpha, CIWidth);
	}
}
