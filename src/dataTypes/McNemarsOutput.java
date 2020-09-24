package dataTypes;

import java.util.Objects;

/**
 * Represents output of McNemar's procedure.
 * The procedure is use to judge the significance of the difference between correlated proportions.
 * @author Sudesh Agrawal (sudesh@utexas.edu).
 * Last Updated: September 24, 2020.
 */
public class McNemarsOutput
{
	/**
	 * Difference between the marginal proportions.
	 */
	double dHat;
	
	/**
	 * Sample standard error.
	 */
	double sampleStandardError;
	
	/**
	 * Alpha value used to generate the confidence interval [{@code lowerCI}, {@code upperCI}].
	 */
	double alpha;
	/**
	 * Lower bound of confidence interval.
	 */
	double lowerCI;
	
	/**
	 * Upper bound of confidence interval.
	 */
	double upperCI;
	
	/**
	 * Sample size used.
	 */
	int sampleSize;
	
	/**
	 * Constructor.
	 *
	 * @param dHat difference between the marginal proportions
	 * @param sampleStandardError sample standard error
	 * @param alpha alpha value used to generate the confidence interval [{@code lowerCI}, {@code upperCI}]
	 * @param lowerCI lower bound of confidence interval
	 * @param upperCI upper bound of confidence interval
	 * @param sampleSize sample size.
	 */
	public McNemarsOutput(double dHat, double sampleStandardError, double alpha,
	                      double lowerCI, double upperCI, int sampleSize)
	{
		this.dHat = dHat;
		this.sampleStandardError = sampleStandardError;
		this.alpha = alpha;
		this.lowerCI = lowerCI;
		this.upperCI = upperCI;
		this.sampleSize = sampleSize;
	}
	
	/**
	 * Getter.
	 *
	 * @return {@code dHat}.
	 */
	public double getdHat()
	{
		return dHat;
	}
	
	/**
	 * Getter.
	 *
	 * @return {@code sampleStandardError}.
	 */
	public double getSampleStandardError()
	{
		return sampleStandardError;
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
	 * Getter.
	 *
	 * @return {@code lowerCI}.
	 */
	public double getLowerCI()
	{
		return lowerCI;
	}
	
	/**
	 * Getter.
	 *
	 * @return {@code upperCI}.
	 */
	public double getUpperCI()
	{
		return upperCI;
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
	 * @param dHat difference between the marginal proportions.
	 */
	public void setdHat(double dHat)
	{
		this.dHat = dHat;
	}
	
	/**
	 * Setter.
	 *
	 * @param sampleStandardError sample standard error.
	 */
	public void setSampleStandardError(double sampleStandardError)
	{
		this.sampleStandardError = sampleStandardError;
	}
	
	/**
	 * Setter.
	 *
	 * @param alpha alpha value used to generate the confidence interval [{@code lowerCI}, {@code upperCI}].
	 */
	public void setAlpha(double alpha)
	{
		this.alpha = alpha;
	}
	
	/**
	 * Setter.
	 *
	 * @param lowerCI lower bound of confidence interval.
	 */
	public void setLowerCI(double lowerCI)
	{
		this.lowerCI = lowerCI;
	}
	
	/**
	 * Setter.
	 *
	 * @param upperCI upper bound of confidence interval.
	 */
	public void setUpperCI(double upperCI)
	{
		this.upperCI = upperCI;
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
	 * Returns a string representation of the object.
	 *
	 * @return a string representation of the object.
	 */
	@Override
	public String toString()
	{return "McNemar's output: "
				+"sample size = "+this.sampleSize+"; "
				+"dhat = "+this.dHat+"; "
				+"sample std. err. = "+this.sampleStandardError +"; "
				+"CI (alpha = "+this.alpha+") = ["+this.lowerCI+", "+this.upperCI+"].";
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
		McNemarsOutput that = (McNemarsOutput) o;
		return Double.compare(that.dHat, dHat) == 0 &&
				Double.compare(that.sampleStandardError, sampleStandardError) == 0 &&
				Double.compare(that.alpha, alpha) == 0 &&
				Double.compare(that.lowerCI, lowerCI) == 0 &&
				Double.compare(that.upperCI, upperCI) == 0 &&
				sampleSize == that.sampleSize;
	}
	
	/**
	 * Returns a hash code value for the object.
	 *
	 * @return a hash code value for this object.
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash(dHat, sampleStandardError, alpha, lowerCI, upperCI, sampleSize);
	}
}
