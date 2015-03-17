package org.edits.distance.weight;

import lombok.Data;

import org.edits.etaf.AnnotatedText;
import org.edits.etaf.Annotation;

@Data
public class WordsIn implements NamedWeightCalculator {

	private static final long serialVersionUID = 1L;
	public static final String WORDS_IN_H = "wordsinh";

	public static final String WORDS_IN_T = "wordsint";

	public static WordsIn wordsInH() {
		return new WordsIn(false);
	}

	public static WordsIn wordsInT() {
		return new WordsIn(true);
	}

	private boolean inT;

	private WordsIn(boolean b) {
		inT = b;
	}

	@Override
	public WeightCalculator clone() {
		return new WordsIn(inT);
	}

	@Override
	public String name() {
		return inT ? WORDS_IN_T : WORDS_IN_H;
	}

	@Override
	public double weightH(Annotation node, AnnotatedText t, AnnotatedText h) {
		return inT ? t.getAnnotation().size() : 0;
	}

	@Override
	public double weightT(Annotation node, AnnotatedText t, AnnotatedText h) {
		return inT ? 0 : h.getAnnotation().size();
	}

}
