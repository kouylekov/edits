package org.edits.engines.weka.features;

import java.util.Map;

import org.edits.etaf.AnnotatedEntailmentPair;

import com.google.common.collect.Maps;

public class EngineFeature extends Feature {

	private static final long serialVersionUID = 1L;
	private final String name;
	private final Map<String, Double> results;

	public EngineFeature(Map<String, Double> results_, String name_) {
		results = results_;
		name = name_;
	}

	@Override
	public Feature clone() {
		return new EngineFeature(Maps.newHashMap(results), name);
	}

	@Override
	public double doubleValue(AnnotatedEntailmentPair pair) {
		return results.get(pair.getId());
	}

	@Override
	public boolean isNumeric() {
		return true;
	}

	@Override
	public String name() {
		return name;
	}

}
