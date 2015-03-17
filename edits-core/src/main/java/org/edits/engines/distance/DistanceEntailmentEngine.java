/**
 * Edits - Edit Distance Textual Entailment Suite Copyright (C) 2011 Milen
 * Kouylekov This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of the License,
 * or (at your option) any later version. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package org.edits.engines.distance;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.edits.MapList;
import org.edits.distance.algorithm.EditDistanceAlgorithm;
import org.edits.distance.algorithm.OverlapDistance;
import org.edits.engines.EvaluationResult;
import org.edits.engines.EvaluationStatistics;
import org.edits.engines.thread.ThreadTrainedEngine;
import org.edits.engines.thread.ThreadExecutor;
import org.edits.etaf.AnnotatedEntailmentPair;
import org.edits.etaf.AnnotatedText;

import com.google.common.collect.Lists;

/**
 * @author Milen Kouylekov
 */
public class DistanceEntailmentEngine extends ThreadTrainedEngine {
	public static final String DEFAULT_KEY = "DEFAULT";
	private static final long serialVersionUID = 1L;

	private static double confidence(double threshold, double score) {
		double confidence = score - threshold;
		if (score > 1)
			return 0;
		if (threshold > 1)
			return 0;
		if (confidence == 0)
			return 0;
		if (confidence > 0)
			return confidence / (1 - threshold);
		return Math.abs(confidence) / threshold;
	}

	private static double scoreFolds(double[] xx) {
		double sum = 0.0;
		for (int i = 0; i < xx.length - 1; i++) {
			for (int k = i + 1; k < xx.length; k++)
				sum += Math.abs(xx[i] - xx[k]);
		}
		return sum;
	}

	private final EditDistanceAlgorithm algorithm;
	private final int folds = 10;

	public String relationHigh;

	public String relationLow;

	private Map<String, Double> threshold;

	private MapList<EvaluationResult> trainingTable;

	private final boolean useTask;

	public DistanceEntailmentEngine() {
		super();
		algorithm = new OverlapDistance();
		threshold = new HashMap<String, Double>();
		threshold.put(DEFAULT_KEY, 0.5);
		relationLow = "YES";
		relationHigh = "NO";
		useTask = false;
	}

	private synchronized void addToTable(String key, EvaluationResult result) {
		trainingTable.get(key).add(result);
	}

	private EvaluationStatistics calculate(double threshold, List<double[]> d) {
		EvaluationStatistics stats = new EvaluationStatistics();
		for (double[] x : d) {
			if (x[1] == 1) {
				if (makeDecision(threshold, x[0]).equals(relationLow))
					stats.add(relationHigh, relationLow);
				else
					stats.add(relationHigh, relationHigh);
			} else {
				if (makeDecision(threshold, x[0]).equals(relationLow))
					stats.add(relationLow, relationLow);
				else
					stats.add(relationLow, relationHigh);
			}
		}
		stats.calculate();
		return stats;
	}

	private double calculateEntailmentScore(AnnotatedEntailmentPair pk) {
		Double scoreFinal = null;
		for (AnnotatedText text : pk.getT()) {
			for (AnnotatedText hypothesis : pk.getH()) {
				double score = algorithm.distance(text, hypothesis, pk.getId());
				if (scoreFinal == null || score < scoreFinal) {
					scoreFinal = score;
				}
			}
		}
		return scoreFinal;
	}

	private ModelInfo calculateThreshold(String key, List<EvaluationResult> vals, int folds) {
		List<List<double[]>> dx = valListToArray(vals, folds);

		List<double[]> d = dx.remove(0);

		List<Threshold> ox = Lists.newArrayList();

		for (double[] dv : d) {

			double[] xx = new double[dx.size()];

			int i = 0;
			for (List<double[]> f : dx) {
				xx[i] = calculate(dv[0], f).value(getGoal());
				i++;
			}
			double min = scoreFolds(xx);
			EvaluationStatistics stats = calculate(dv[0], d);
			ox.add(new Threshold(min, stats, dv[0], stats.value(getGoal())));
		}

		Collections.sort(ox, new Threshold(true));

		if (ox.size() > 25)
			ox = ox.subList(ox.size() - 15, ox.size());

		Collections.sort(ox, new Threshold(false));

		ModelInfo info = new ModelInfo();
		info.setStatistics(ox.get(0).getStats());
		info.getThreshold().put(key, ox.get(0).getThreshold());
		return info;
	}

	@Override
	public EvaluationResult evaluate(AnnotatedEntailmentPair p) {
		double result = calculateEntailmentScore(p);
		String benchmark = p.getEntailment();
		if (benchmark == null)
			benchmark = relationHigh;
		String key = thresholdKey(p.getTask());
		double thr = threshold.get(key);
		String decision = makeDecision(thr, result);
		return new EvaluationResult(p.getId(), decision, benchmark, result, confidence(thr, result));
	}

	@Override
	public void handle(AnnotatedEntailmentPair pair) {
		double score = calculateEntailmentScore(pair);
		String benchmark = pair.getEntailment();
		String key = thresholdKey(pair.getTask());
		EvaluationResult result = new EvaluationResult(pair.getId(), null, benchmark, score, 0.0);
		addToTable(key, result);
	}

	private String makeDecision(double thresholdValue, double score) {
		return score < thresholdValue ? relationLow : relationHigh;
	}

	private String thresholdKey(String task) {
		String key = DEFAULT_KEY;
		if (useTask)
			key = task;
		return key;
	}

	@Override
	public EvaluationStatistics train(List<AnnotatedEntailmentPair> corpus) {
		trainingTable = new MapList<EvaluationResult>();

		ThreadExecutor engine = new ThreadExecutor(this, true);
		engine.run(corpus);

		Map<String, ModelInfo> out = new HashMap<String, ModelInfo>();

		if (getGoal() == null)
			setDefaultGoal();

		for (String key : trainingTable.keySet()) {
			List<EvaluationResult> dv = trainingTable.get(key);
			Collections.sort(dv, dv.get(0));
			ModelInfo info = calculateThreshold(key, dv, folds);
			info.getStatistics().setId(key);
			out.put(key, info);
		}
		ModelInfo info = ModelInfo.mergeDistanceModels(out.values());
		threshold = info.getThreshold();
		return info.getStatistics();
	}

	private List<List<double[]>> valListToArray(List<EvaluationResult> vals, int folds) {
		List<double[]> d = Lists.newArrayList();

		List<Double> p = Lists.newArrayList();
		List<Double> n = Lists.newArrayList();

		for (EvaluationResult val : vals) {
			if (val.getBenchmark().equals(relationLow)) {
				d.add(new double[] { val.getScore(), 0 });
				p.add(val.getScore());
			} else {
				d.add(new double[] { val.getScore(), 1 });
				n.add(val.getScore());
			}
		}
		List<List<double[]>> out = Lists.newArrayList();
		out.add(d);

		for (int i = 0; i < folds; i++) {
			List<double[]> list = Lists.newArrayList();
			out.add(list);
		}

		for (int k = 0; k < p.size();) {
			for (int i = 0; i < folds && k < p.size(); i++) {
				out.get(i + 1).add(new double[] { p.get(k), 0 });
				k++;
			}
		}

		for (int k = 0; k < n.size();) {
			for (int i = 0; i < folds && k < n.size(); i++) {
				out.get(i + 1).add(new double[] { n.get(k), 0 });
				k++;
			}
		}
		return out;
	}
}
