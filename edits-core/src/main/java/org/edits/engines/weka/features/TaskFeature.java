package org.edits.engines.weka.features;

import java.util.List;

import org.edits.etaf.AnnotatedEntailmentPair;

public class TaskFeature extends Feature {

	private static final long serialVersionUID = 1L;
	private List<String> tasknames;

	@Override
	public Feature clone() {
		return new TaskFeature();
	}

	@Override
	public boolean isNumeric() {
		return false;
	}

	@Override
	public String name() {
		return "task";
	}

	@Override
	public String stringValue(AnnotatedEntailmentPair pair) {
		return tasknames.contains(pair.getTask()) ? pair.getTask() : tasknames.get(0);
	}

	@Override
	public List<String> values() {
		return tasknames;
	}
}
