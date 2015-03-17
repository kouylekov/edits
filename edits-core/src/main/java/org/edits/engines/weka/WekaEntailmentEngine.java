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
package org.edits.engines.weka;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.edits.MapInteger;
import org.edits.distance.algorithm.CosineSimilarity;
import org.edits.distance.algorithm.OverlapDistance;
import org.edits.distance.algorithm.TokenEditDistance;
import org.edits.engines.EvaluationResult;
import org.edits.engines.EvaluationStatistics;
import org.edits.engines.thread.ThreadExecutor;
import org.edits.engines.thread.ThreadTrainedEngine;
import org.edits.engines.weka.features.DistanceFeature;
import org.edits.engines.weka.features.Feature;
import org.edits.engines.weka.features.NegationFeature;
import org.edits.etaf.AnnotatedEntailmentPair;
import org.edits.etaf.EntailmentPair;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.meta.ThresholdSelector;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import com.google.common.collect.Lists;

@Data
@EqualsAndHashCode(callSuper = false)
public class WekaEntailmentEngine extends ThreadTrainedEngine {

	private static final long serialVersionUID = 1L;

	private Instance[] calculatedInstances;

	private List<String> categories;
	private AbstractClassifier classifier;
	private List<Feature> features;
	private Instances instances;
	public Iterator<EntailmentPair> processor;

	public WekaEntailmentEngine() {
		features = Lists.newArrayList();
		features.add(new NegationFeature());
		features.add(new DistanceFeature(new TokenEditDistance()));
		features.add(new DistanceFeature(new OverlapDistance()));
		features.add(new DistanceFeature(new CosineSimilarity()));
	}

	public WekaEntailmentEngine(Feature f) {
		features = Lists.newArrayList();
		features.add(f);
	}

	public WekaEntailmentEngine(List<Feature> features_) {
		features = features_;
	}

	private ArrayList<Attribute> buildAttributes() {
		ArrayList<Attribute> as = Lists.newArrayList();
		int i = 0;
		for (Feature f : features) {
			if (f.isNumeric())
				as.add(new Attribute("a" + i));
			else
				as.add(new Attribute("a" + i, f.values()));
			i++;
		}
		Attribute cls = new Attribute("class", categories);
		as.add(cls);
		return as;
	}

	private Instance buildInstance(AnnotatedEntailmentPair pair) {
		Instance i = new DenseInstance(features.size() + 1);
		i.setDataset(instances);
		int pos = 0;
		for (Feature f : features) {
			if (f.isNumeric())
				i.setValue(pos, f.doubleValue(pair));
			else
				i.setValue(pos, f.stringValue(pair));
			pos++;
		}
		i.setValue(
				pos,
				pair.getEntailment() == null || pair.getEntailment().length() == 0
						|| !categories.contains(pair.getEntailment()) ? categories.get(0) : pair.getEntailment());
		return i;
	}

	@Override
	public EvaluationResult evaluate(AnnotatedEntailmentPair p) {
		Instance i = buildInstance(p);
		return evaluate(i, p.getId(), p.getEntailment());
	}

	public EvaluationResult evaluate(Instance inst, String id, String benchmark) {
		try {
			double[] s = classifier.distributionForInstance(inst);
			String max = null;
			double maxValue = 0;
			for (int i = 0; i < s.length; i++) {
				String className = inst.classAttribute().value(i);
				if (s[i] > maxValue) {
					max = className;
					maxValue = s[i];
				}
			}
			return new EvaluationResult(id, max, benchmark, maxValue, maxValue);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return null;

	}

	@Override
	public void handle(AnnotatedEntailmentPair o) {
		Instance i = buildInstance(o);
		calculatedInstances[o.getOrder()] = i;
	}

	private void initClassifier(MapInteger values) throws Exception {
		if (classifier == null) {
			classifier = new NaiveBayes();
		}
		if (values.size() == 2) {
			Iterator<String> s = values.keySet().iterator();
			int count1 = values.get(s.next());
			int count2 = values.get(s.next());
			if (count2 < count1 / 2 || count1 < count2 / 2) {
				ThresholdSelector sel = new ThresholdSelector();
				sel.setClassifier(new SMO());
				sel.setOptions(new String[] { "-C", count1 > count2 ? "1" : "2", "-M", "FMEASURE" });
				classifier = sel;
			}
		}
	}

	@Override
	public EvaluationStatistics train(List<AnnotatedEntailmentPair> training) throws Exception {

		if (getGoal() == null)
			setDefaultGoal();

		for (Feature f : features)
			f.init(training, getGoal());

		Set<String> tasks = new HashSet<String>();
		MapInteger values = new MapInteger();

		for (AnnotatedEntailmentPair p : training) {
			tasks.add(p.getTask());
			values.increment(p.getEntailment());
		}
		categories = Lists.newArrayList(values.keySet());
		calculatedInstances = new Instance[training.size()];

		instances = new Instances("dataset", buildAttributes(), training.size());
		instances.setClassIndex(features.size());

		ThreadExecutor engine = new ThreadExecutor(this, true);
		engine.run(training);

		for (Instance i : calculatedInstances)
			instances.add(i);

		initClassifier(values);
		classifier.buildClassifier(instances);
		EvaluationStatistics out = new EvaluationStatistics();
		for (int i = 0; i < instances.numInstances(); i++) {
			Instance x = instances.instance(i);
			String benchmark = categories.get((int) x.classValue());
			EvaluationResult dd = evaluate(x, null, benchmark);
			out.add(benchmark, dd.getAssigned());
		}
		out.calculate();
		calculatedInstances = null;
		instances.clear();
		return out;
	}
}