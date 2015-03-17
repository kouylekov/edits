package org.edits.engines.weka.features;

import java.io.Serializable;
import java.util.List;

import org.edits.engines.OptimizationGoal;
import org.edits.etaf.AnnotatedEntailmentPair;

public abstract class Feature implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	@Override
	public abstract Feature clone();

	public double doubleValue(AnnotatedEntailmentPair pair) {
		throw new UnsupportedOperationException("@Override this method for  numeric features.");
	}

	public void init(List<AnnotatedEntailmentPair> training, OptimizationGoal goal) throws Exception {

	}

	public abstract boolean isNumeric();

	public abstract String name();

	public String stringValue(AnnotatedEntailmentPair pair) {
		throw new UnsupportedOperationException("@Override this method for non numeric features.");
	}

	public List<String> values() {
		throw new UnsupportedOperationException("@Override this method for non numeric features.");
	}

}
