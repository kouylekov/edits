package org.edits.rules;

import java.io.Closeable;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.edits.etaf.AnnotatedText;
import org.edits.etaf.Annotation;

public abstract class RulesSource implements Serializable, Cloneable, Closeable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public abstract void close();

	public double contradiction(Annotation a1, Annotation a2) {
		double d = probability(a1, a2);
		if (d == 0)
			return 0;
		if (d < 0)
			return Math.abs(d);
		return 0.0;
	}

	@Override
	public abstract RulesSource clone();

	public double entails(Annotation a1, Annotation a2) {
		double d = probability(a1, a2);
		if (d == 0)
			return 0;
		if (d > 0)
			return d;
		return 0.0;
	}

	public abstract List<Rule> extractRules(AnnotatedText t, AnnotatedText h);

	public abstract double probability(Annotation a1, Annotation a2);

	public abstract Set<String> uses();

}
