package dataTypes;

import java.util.Objects;

/**
 * Represents output (mean, CI width, etc.) of a statistical analysis.
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: September 22, 2020.
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
	 * Returns a string representation of the object.
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
		statisticalOutput that = (statisticalOutput) o;
		return Double.compare(that.mean, mean) == 0 &&
				Double.compare(that.stDev, stDev) == 0 &&
				Double.compare(that.alpha, alpha) == 0 &&
				Double.compare(that.CIWidth, CIWidth) == 0;
	}
	
	/**
	 * Returns a hash code value for the object.
	 *
	 * @return a hash code value for this object.
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(mean, stDev, alpha, CIWidth);
	}
}
