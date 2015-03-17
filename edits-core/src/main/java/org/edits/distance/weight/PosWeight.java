package org.edits.distance.weight;

import org.edits.etaf.AnnotatedText;
import org.edits.etaf.Annotation;

public class PosWeight implements NamedWeightCalculator {

	public static final String ADJECTIVE = "adj";
	public static final String NOUN = "noun";
	private static final long serialVersionUID = 1L;
	public static final String VERB = "verb";

	public static PosWeight posWeightAdjective() {
		return new PosWeight(ADJECTIVE);
	}

	public static PosWeight posWeightNoun() {
		return new PosWeight(NOUN);
	}

	public static PosWeight posWeightVerb() {
		return new PosWeight(VERB);
	}

	private final String pos;

	private PosWeight(String pos) {
		super();
		this.pos = pos;
	}

	@Override
	public WeightCalculator clone() {
		return new PosWeight(pos);
	}

	@Override
	public String name() {
		return pos;
	}

	@Override
	public double weightH(Annotation node, AnnotatedText t, AnnotatedText h) {
		return pos.equals(node.getCpostag()) ? 1 : 0;
	}

	@Override
	public double weightT(Annotation node, AnnotatedText t, AnnotatedText h) {
		return pos.equals(node.getCpostag()) ? 1 : 0;
	}

}
