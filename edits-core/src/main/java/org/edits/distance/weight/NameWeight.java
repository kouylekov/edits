package org.edits.distance.weight;

import org.edits.etaf.AnnotatedText;
import org.edits.etaf.Annotation;

public class NameWeight implements NamedWeightCalculator {

	public static final String NAME = "name";
	private static final long serialVersionUID = 1L;

	private static boolean isName(String s) {
		if (!Character.isUpperCase(s.charAt(0)))
			return false;
		for (int i = 1; i < s.length(); i++) {
			char ch = s.charAt(i);
			if (Character.isLowerCase(ch))
				continue;
			return false;
		}
		return true;
	}

	@Override
	public WeightCalculator clone() {
		return new NameWeight();
	}

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public double weightH(Annotation node, AnnotatedText t, AnnotatedText h) {
		return isName(node.getForm()) ? 1 : 0;
	}

	@Override
	public double weightT(Annotation node, AnnotatedText t, AnnotatedText h) {
		return isName(node.getForm()) ? 1 : 0;
	}

}
