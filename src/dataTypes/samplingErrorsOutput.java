package dataTypes;

import java.util.Objects;

/**
 * Represents output of sampling errors (see {@link analysis.samplingErrors}).
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: September 23, 2020.
 */
public class samplingErrorsOutput
{
	/**
	 * Point estimate.
	 */
	double pointEstimate;
	/**
	 * Standard error of the point estimator.
	 */
	double standardError;
	/**
	 * Alpha value used.
	 */
	double alpha;
	/**
	 * Half-width.
	 */
	double halfWidth;
	
	/**
	 * Constructor.
	 *
	 * @param pointEstimate point estimate
	 * @param standardError standard error of the point estimator
	 * @param alpha alpha value used
	 * @param halfWidth half-width of confidence interval.
	 */
	public samplingErrorsOutput(double pointEstimate, double standardError, double alpha, double halfWidth)
	{
		this.pointEstimate = pointEstimate;
		this.standardError = standardError;
		this.alpha = alpha;
		this.halfWidth = halfWidth;
	}
	
	/**
	 * Copy constructor.
	 *
	 * @param output an instance of {@code samplingErrorsOutput}.
	 */
	public samplingErrorsOutput(samplingErrorsOutput output)
	{
		this.pointEstimate = output.pointEstimate;
		this.standardError = output.standardError;
		this.alpha = output.alpha;
		this.halfWidth = output.halfWidth;
	}
	
	/**
	 * Getter.
	 *
	 * @return {@code pointEstimate}.
	 */
	public double getPointEstimate()
	{
		return pointEstimate;
	}
	
	/**
	 * Setter.
	 *
	 * @param pointEstimate the point estimate.
	 */
	public void setPointEstimate(double pointEstimate)
	{
		this.pointEstimate = pointEstimate;
	}
	
	/**
	 * Getter.
	 *
	 * @return {@code standardError}.
	 */
	public double getStandardError()
	{
		return standardError;
	}
	
	/**
	 * Setter.
	 *
	 * @param standardError the standard error of the point estimator.
	 */
	public void setStandardError(double standardError)
	{
		this.standardError = standardError;
	}
	
	/**
	 * Getter.
	 *
	 * @return {@code alpha}.
	 */
	public double getAlpha()
	{
		return alpha;
	}
	
	/**
	 * Setter.
	 *
	 * @param alpha alpha value used.
	 */
	public void setAlpha(double alpha)
	{
		this.alpha = alpha;
	}
	
	/**
	 * Getter.
	 *
	 * @return {@code halfWidth}.
	 */
	public double getHalfWidth()
	{
		return halfWidth;
	}
	
	/**
	 * Setter.
	 *
	 * @param halfWidth half-width.
	 */
	public void setHalfWidth(double halfWidth)
	{
		this.halfWidth = halfWidth;
	}
	
	/**
	 * Returns a string representation of the object.
	 *
	 * @return a string representation of the object.
	 */
	@Override
	public String toString()
	{
		return "Point estimate and sampling error: "
				+"point estimate = "+this.pointEstimate+"; "
				+"std. err. = "+this.standardError+"; "
				+"half-width (alpha = "+this.alpha+" ) = "+this.halfWidth+".";
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
		samplingErrorsOutput that = (samplingErrorsOutput) o;
		return Double.compare(that.pointEstimate, pointEstimate) == 0 &&
				Double.compare(that.standardError, standardError) == 0 &&
				Double.compare(that.alpha, alpha) == 0 &&
				Double.compare(that.halfWidth, halfWidth) == 0;
	}
	
	/**
	 * Returns a hash code value for the object.
	 *
	 * @return a hash code value for this object.
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(pointEstimate, standardError, alpha, halfWidth);
	}
}
