package de.in.tum.compare;

public class ResultModel {

	private double tp = 0;
	private double tn = 0;
	private double fp = 0;
	private double fn = 0;

	public ResultModel(double tp, double tn, double fp, double fn) {
		super();
		this.tp = tp;
		this.tn = tn;
		this.fp = fp;
		this.fn = fn;
	}

	/**
	 * @return the tp
	 */
	public double getTp() {
		return tp;
	}

	/**
	 * @param tp
	 *            the tp to set
	 */
	public void setTp(double tp) {
		this.tp = tp;
	}

	/**
	 * @return the tn
	 */
	public double getTn() {
		return tn;
	}

	/**
	 * @param tn
	 *            the tn to set
	 */
	public void setTn(double tn) {
		this.tn = tn;
	}

	/**
	 * @return the fp
	 */
	public double getFp() {
		return fp;
	}

	/**
	 * @param fp
	 *            the fp to set
	 */
	public void setFp(double fp) {
		this.fp = fp;
	}

	/**
	 * @return the fn
	 */
	public double getFn() {
		return fn;
	}

	/**
	 * @param fn
	 *            the fn to set
	 */
	public void setFn(double fn) {
		this.fn = fn;
	}

	/**
	 * @return the performance
	 */
	public double getPerformance() {
		return 100 * (tp + tn) / (tp + fp + tn + fn);
	}

	/**
	 * @return the accuracyPositive
	 */
	public double getAccuracyPositive() {
		return 100 * tp / (tp + fp);
	}

	/**
	 * @return the coveragePositive
	 */
	public double getCoveragePositive() {
		return 100 * tp / (tp + fn);
	}

	/**
	 * @return the accuracyNegative
	 */
	public double getAccuracyNegative() {
		return 100 * tn / (tn + fn);
	}

	/**
	 * @return the coverageNegative
	 */
	public double getCoverageNegative() {
		return 100 * tn / (tn + fp);
	}

}
