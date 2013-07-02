package de.in.tum.compare;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class PredictionComparator {

	private Map<String, ResultModel> resultMap = new HashMap<String, ResultModel>();
	private String inputFile = null;
	private List<Query> queries = null;

	public PredictionComparator(String inputFile, List<Query> queries) {
		super();
		this.queries = queries;
		this.inputFile = inputFile;
	}

	public void compareResults() throws Exception {
		for (Query query : queries) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(query.getUrl());

			FileBody bin = new FileBody(new File(inputFile));

			MultipartEntity reqEntity = new MultipartEntity();
			reqEntity.addPart("upload", bin);
			httppost.setEntity(reqEntity);

			HttpResponse response = httpclient.execute(httppost);
			HttpEntity responseEntity = response.getEntity();
			if (responseEntity != null) {
				String respString = EntityUtils.toString(responseEntity);
				updateResults(query.getTeamName(), respString);
			}
		}
	}

	public void updateResults(String teamName, String resultString)
			throws Exception {

		double tp = 0;
		double tn = 0;
		double fp = 0;
		double fn = 0;

		// convert String into InputStream
		InputStream is = new ByteArrayInputStream(resultString.getBytes());
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		// dump the file in the standard output format
		DataSource testSource = new DataSource(inputFile);
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
		ResultModel result = new ResultModel(tp, tn, fp, fn);
		resultMap.put(teamName, result);
	}

	public void displayResults() {
		Set<String> keySet = resultMap.keySet();
		for (String key : keySet) {
			ResultModel result = resultMap.get(key);
			System.out.println("-----------------------------------");
			System.out.println(key);
			System.out.println("-----------------------------------");
			System.out.println("tp:" + result.getTp());
			System.out.println("fn:" + result.getFn());
			System.out.println("fp:" + result.getFp());
			System.out.println("tn:" + result.getTn());

			System.out.println("performance:" + result.getPerformance());
			System.out.println("accuracyPositive:"
					+ result.getAccuracyPositive());
			System.out.println("coveragePositive:"
					+ result.getCoveragePositive());
			System.out.println("accuracyNegative:"
					+ result.getAccuracyNegative());
			System.out.println("coverageNegative:"
					+ result.getCoverageNegative());
		}
	}

	public static void main(String[] args) throws Exception {
		Query query = new Query(
				"Team 21",
				"http://i12k-biolab01.informatik.tu-muenchen.de/~ppgroup21/cgi-bin/ppgroup21.cgi");
		Query query2 = new Query(
				"Team 22",
				"http://i12k-biolab01.informatik.tu-muenchen.de/~ppgroup22/cgi-bin/ppgroup22.cgi");
		List<Query> queryList = new ArrayList<Query>();
		queryList.add(query);
		queryList.add(query2);

		PredictionComparator p = new PredictionComparator(
				"tmps_independent.arff", queryList);
		p.compareResults();
		p.displayResults();

	}
}
