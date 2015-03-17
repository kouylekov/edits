package org.edits.engines.weka;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j;

import org.edits.engines.EntailmentEngine;
import org.edits.engines.EvaluationResult;
import org.edits.engines.EvaluationStatistics;
import org.edits.engines.thread.ThreadExecutor;
import org.edits.engines.weka.features.EngineFeature;
import org.edits.engines.weka.features.Feature;
import org.edits.etaf.AnnotatedEntailmentPair;

import weka.core.DenseInstance;
import weka.core.Instance;

import com.google.common.collect.Lists;

@Log4j
@Data
@EqualsAndHashCode(callSuper = false)
public class BinnaryWekaEngine extends EntailmentEngine {

	public static final String NOT_VALUE = "XXX";

	private static final long serialVersionUID = 1L;

	private boolean addFeatures = true;
	private final List<WekaEntailmentEngine> engines;
	private final List<Feature> features;
	private final WekaEntailmentEngine main;

	public BinnaryWekaEngine() {
		this(new WekaEntailmentEngine());
	}

	public BinnaryWekaEngine(WekaEntailmentEngine engine) {
		features = Lists.newArrayList(engine.getFeatures());
		main = engine;
		engines = Lists.newArrayList();
	}

	private Instance buildInstance(AnnotatedEntailmentPair pair) {
		Instance i = new DenseInstance((addFeatures ? features.size() : 0) + engines.size() * 2 + 1);
		i.setDataset(main.getInstances());
		int pos = 0;
		if (addFeatures) {
			for (Feature f : features) {
				if (f.isNumeric())
					i.setValue(pos, f.doubleValue(pair));
				else
					i.setValue(pos, f.stringValue(pair));
				pos++;
			}
		}

		String orig = pair.getEntailment();
		pair.setEntailment(NOT_VALUE);
		for (WekaEntailmentEngine f : engines) {
			EvaluationResult r = f.evaluate(pair);
			i.setValue(pos, r.getAssigned().equals(NOT_VALUE) ? 0.0 : 1.0);
			pos++;
			i.setValue(pos, r.getConfidence());
			pos++;
		}
		pair.setEntailment(orig);
		if (main.getCategories().contains(pair.getEntailment()))
			i.setValue(pos, pair.getEntailment() == null || pair.getEntailment().length() == 0 ? main.getCategories()
					.get(0) : pair.getEntailment());
		return i;
	}

	public List<Feature> clone(List<Feature> features) {
		List<Feature> out = Lists.newArrayList();
		for (Feature f : features)
			out.add(f.clone());
		return out;
	}

	@Override
	public EvaluationResult evaluate(AnnotatedEntailmentPair p) {
		Instance i = buildInstance(p);
		return main.evaluate(i, p.getId(), p.getEntailment());
	}

	@Override
	public EvaluationStatistics train(List<AnnotatedEntailmentPair> training) throws Exception {
		Map<String, String> orig = new HashMap<String, String>();
		Set<String> cats = new HashSet<String>();
		for (AnnotatedEntailmentPair p : training) {
			cats.add(p.getEntailment());
			orig.put(p.getId(), p.getEntailment());
		}
		List<Map<String, Double>> calculatedTraining = Lists.newArrayList();
		for (String cat : cats) {
			for (AnnotatedEntailmentPair p : training) {
				p.setEntailment(orig.get(p.getId()));
				if (!p.getEntailment().equals(cat))
					p.setEntailment(NOT_VALUE);
			}
			WekaEntailmentEngine engine = new WekaEntailmentEngine(clone(features));
			EvaluationStatistics stats = engine.train(training);
			engine.setGoal(getGoal());
			log.info("Category:" + cat);
			log.info(stats);
			MapTarget mt = new MapTarget();
			ThreadExecutor mte = new ThreadExecutor(engine, mt);
			mte.run(training);
			calculatedTraining.add(mt.getAssigned());
			calculatedTraining.add(mt.getConfidence());
			engines.add(engine);
		}
		if (!addFeatures)
			main.getFeatures().clear();
		for (AnnotatedEntailmentPair p : training)
			p.setEntailment(orig.get(p.getId()));
		for (int i = 0; i < calculatedTraining.size(); i++) {
			Map<String, Double> px = calculatedTraining.get(i);
			EngineFeature f = new EngineFeature(px, "" + i);
			main.getFeatures().add(f);
		}
		return main.train(training);
	}
}
