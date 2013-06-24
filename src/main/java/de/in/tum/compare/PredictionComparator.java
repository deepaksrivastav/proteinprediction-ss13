package de.in.tum.compare;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class PredictionComparator {

	public void getConfusionMatrix(String title, String inputFile,
			String correctDataFile) throws Exception {
		double tp = 0;
		double tn = 0;
		double fp = 0;
		double fn = 0;

		FileInputStream in = new FileInputStream(inputFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		// dump the file in the standard output format
		DataSource testSource = new DataSource(correctDataFile);
		Instances test = testSource.getDataSet();
		test.setClassIndex(test.numAttributes() - 1);

		int index = 0;
		String strLine;

		while ((strLine = br.readLine()) != null) {
			if (strLine.indexOf(">") != -1) {
				continue;
			} else {
				int len = strLine.length();
				for (int i = 0; i < len; i++) {
					char predicted = strLine.charAt(i);
					Instance instance = test.instance(index);
					String actual = instance.classAttribute().value(
							(int) instance.classValue());
					if (actual.equalsIgnoreCase("+") && predicted == '+') {
						tp++;
					}
					if (actual.equalsIgnoreCase("+") && predicted == '-') {
						fn++;
					}
					if (actual.equalsIgnoreCase("-") && predicted == '+') {
						fp++;
					}
					if (actual.equalsIgnoreCase("-") && predicted == '-') {
						tn++;
					}
					index++;
				}
			}
		}
		br.close();

		System.out.println("-----------------------------------");
		System.out.println(title);
		System.out.println("-----------------------------------");
		System.out.println("tp:" + tp);
		System.out.println("fn:" + fn);
		System.out.println("fp:" + fp);
		System.out.println("tn:" + tn);

		double performance = 100 * (tp + tn) / (tp + fp + tn + fn);
		double accuracyPositive = 100 * tp / (tp + fp);
		double coveragePositive = 100 * tp / (tp + fn);
		double accuracyNegative = 100 * tn / (tn + fn);
		double coverageNegative = 100 * tn / (tn + fp);

		System.out.println("performance:" + performance);
		System.out.println("accuracyPositive:" + accuracyPositive);
		System.out.println("coveragePositive:" + coveragePositive);
		System.out.println("accuracyNegative:" + accuracyNegative);
		System.out.println("coverageNegative:" + coverageNegative);
	}

	public static void main(String[] args) throws Exception {
		PredictionComparator p = new PredictionComparator();
		p.getConfusionMatrix("TEAM 21", "1.log", "tmps_independent.arff");
	}
}
