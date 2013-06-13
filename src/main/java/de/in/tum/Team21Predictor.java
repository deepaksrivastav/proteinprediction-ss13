package de.in.tum;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Vector;

import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;

public class Team21Predictor {

	private String testFileName = null;

	private String modelFileName = "team21.model";

	private String[] selectedFeatures = { "[A-Z]_pssm", "chemprop_hyd", "b",
			"rel_acc", "ri_acc", "OtL", "md_minus" };

	private int windowSize = 19;

	private int numberOfTrees = 120;
	private int numberOfFeatures = 0;

	private boolean dumpResult = false;

	public Team21Predictor(String testFileName, boolean dumpResult) {
		super();
		this.testFileName = testFileName;
		this.dumpResult = dumpResult;
	}

	/**
	 * Train the machine
	 * 
	 * @return The name of the generated model
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public void test() throws IllegalArgumentException, Exception {

		long startTime = System.nanoTime();
		DataSource testSource = new DataSource(testFileName);
		Instances test = testSource.getDataSet();
		Instances filteredTest = filter(test);
		filteredTest.setClassIndex(filteredTest.numAttributes() - 1);
		numberOfFeatures = filteredTest.numAttributes() / 2;

		// File modelFile = new File(modelFileName);
		// if (!modelFile.exists()) {
		// createModel("tmps.arff");
		// }

		Vector v = (Vector) SerializationHelper.read(this.getClass()
				.getClassLoader().getResourceAsStream(modelFileName));
		Classifier classifier = (Classifier) v.get(0);

		// output predictions
		int numCorrect = 0;
		String curProteinName = null;
		String previousProteinName = "";
		boolean firstLine = true;

		for (int i = 0; i < test.numInstances(); i++) {
			weka.core.Instance fullInstance = test.instance(i);
			weka.core.Instance currentInst = filteredTest.instance(i);

			String value = fullInstance.stringValue(0);
			int index = value.lastIndexOf("_");
			curProteinName = value.substring(0, index);

			if (!previousProteinName.equalsIgnoreCase(curProteinName)) {
				if (firstLine) {
					System.out.println(">" + curProteinName);
					firstLine = false;
				} else {
					System.out.println("\n>" + curProteinName);
				}
				previousProteinName = curProteinName;
			}

			double predictedClass = classifier.classifyInstance(currentInst);
			System.out.print(filteredTest.instance(i).classAttribute()
					.value((int) predictedClass));
			if (dumpResult) {
				if (predictedClass == filteredTest.instance(i).classValue()) {
					numCorrect++;
				}
			}
		}
		// blank line
		System.out.println("");
		long endTime = System.nanoTime();
		long duration = endTime - startTime;

		if (dumpResult) {
			File output = new File(System.nanoTime() + ".run");
			BufferedWriter writer = new BufferedWriter(new FileWriter(output));
			writer.write(numCorrect
					+ " out of "
					+ test.numInstances()
					+ " correct ("
					+ (double) ((double) numCorrect
							/ (double) test.numInstances() * 100.0) + "%)");
			writer.newLine();
			writer.write("Duration:" + Double.toString(duration));
			writer.close();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	private void createModel(String trainDataSet) throws Exception {
		// create the training set
		DataSource ds = new DataSource("tmps.arff");
		Instances train = ds.getDataSet();
		Instances filteredTrain = filter(train);
		filteredTrain.setClassIndex(filteredTrain.numAttributes() - 1);

		RandomForest classifier = new RandomForest();
		classifier.setNumTrees(numberOfTrees);
		classifier.setNumFeatures(numberOfFeatures);
		classifier.buildClassifier(filteredTrain);

		// save model + header
		Vector v = new Vector();
		v.add(classifier);
		v.add(new Instances(filteredTrain, 0));
		SerializationHelper.write(modelFileName, v);
	}

	/**
	 * Filter the data based on the features selected *
	 * 
	 * @param dataSet
	 * @return Filtered Instances
	 * @throws Exception
	 */
	private Instances filter(Instances dataSet) throws Exception {
		RemoveByName remove = new RemoveByName();
		String[] options = new String[4];
		options[0] = "-E";
		options[1] = getFeatureName(selectedFeatures, windowSize);
		options[2] = "-V";
		options[3] = "true";
		remove.setInvertSelection(true);
		remove.setOptions(options);
		remove.setInputFormat(dataSet);

		Instances filteredTrain = Filter.useFilter(dataSet, remove);
		return filteredTrain;
	}

	/**
	 * Get the window size regular expression based on the integer window size
	 * entered
	 * 
	 * @param windowSize
	 * @return
	 */
	private String getWindowSizeRegex(int windowSize) {
		switch (windowSize) {
		case 17:
			return "-*[0-8]?";
		case 19:
			return "-*[0-9]?";
		case 21:
			return "-*[0-9][0]*";
		case 23:
			return "-*[0-9][0-1]*";
		case 25:
			return "-*[0-9][0-2]*";
		case 27:
			return "-*[0-9][0-3]*";
		default:
			System.out.println("This window size is not supported");
		}
		// by default return window size of 17
		return "-*[0-8]*";
	}

	/**
	 * Build the regular expression to select features
	 * 
	 * @param featuresRegex
	 * @param windowSize
	 * @return
	 */
	private String getFeatureName(String[] featuresRegex, int windowSize) {
		StringBuilder builder = new StringBuilder();
		int count = 0;
		for (String feature : featuresRegex) {
			if (count > 0) {
				builder.append("|");
			}
			String windowSizeRegex = getWindowSizeRegex(windowSize);
			builder.append(windowSizeRegex);
			builder.append("_").append(feature);
			count++;
		}
		builder.append("|class");
		return builder.toString();
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Kindly pass the input file path");
			printUsage();
			return;
		}

		// check if input file exists
		File file = new File(args[0]);
		if (!file.exists() || file.isDirectory()) {
			System.out
					.println("The input file does not exist or is a directory. Please enter a valid arff file");
			printUsage();
			return;
		}

		boolean dumpResult = false;
		if (args.length == 2) {
			if (args[1].equals("--dumpResult")) {
				dumpResult = true;
			}
		}

		Team21Predictor mlTest = new Team21Predictor(args[0], dumpResult);
		try {
			mlTest.test();
		} catch (IllegalArgumentException e) {
			System.out
					.println("The input file is not a valid protein arff file. Please enter a valid arff file");
			printUsage();
		} catch (Exception e) {
			System.out
					.println("An unknown error occured while running the predictor.");
		}
	}

	private static void printUsage() {
		System.out
				.println("\nUsage: java -jar team21.jar <input_arff_file_path> [options]");
		System.out.println("Options:");
		System.out
				.println("--dumpResult	Dumps the results of evaluation in a .run file");
	}
}
