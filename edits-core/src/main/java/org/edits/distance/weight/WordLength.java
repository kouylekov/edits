package org.edits.distance.weight;

import org.edits.etaf.AnnotatedText;
import org.edits.etaf.Annotation;

public class WordLength implements NamedWeightCalculator {

	private static final long serialVersionUID = 1L;
	public static final String WORD_LENGTH = "wordlength";

	@Override
	public WeightCalculator clone() {
		return new WordLength();
	}

	@Override
	public String name() {
		return WORD_LENGTH;
	}

	@Override
	public double weightH(Annotation node, AnnotatedText t, AnnotatedText h) {
		return node.getForm().length();
	}

	@Override
	public double weightT(Annotation node, AnnotatedText t, AnnotatedText h) {
		return node.getForm().length();
	}

}
