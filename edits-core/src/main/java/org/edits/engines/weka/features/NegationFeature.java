package org.edits.engines.weka.features;

import java.util.Set;

import org.edits.etaf.AnnotatedEntailmentPair;
import org.edits.etaf.AnnotatedText;
import org.edits.etaf.Annotation;

import com.google.common.collect.Sets;

public class NegationFeature extends Feature {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Set<String> negativeWords;

	public NegationFeature() {
		negativeWords = Sets.newHashSet();
		negativeWords.add("not");
		negativeWords.add("never");
		negativeWords.add("no");
	}

	@Override
	public Feature clone() {
		return new NegationFeature();
	}

	@Override
	public double doubleValue(AnnotatedEntailmentPair pair) {

		int hasNagationinH = 0;
		int hasNagationinT = 0;

		for (AnnotatedText a : pair.getH()) {
			for (Annotation h : a.getAnnotation()) {
				if (negativeWords.contains(h.getLemma()))
					hasNagationinH++;
			}
		}
		for (AnnotatedText a : pair.getT()) {
			for (Annotation t : a.getAnnotation()) {
				if (negativeWords.contains(t.getLemma()))
					hasNagationinT++;
			}
		}
		return hasNagationinH - hasNagationinT;
	}

	@Override
	public boolean isNumeric() {
		return true;
	}

	@Override
	public String name() {
		return "negetion";
	}

}
