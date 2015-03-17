package org.edits.distance.weight;

import org.edits.etaf.AnnotatedText;
import org.edits.etaf.Annotation;

public class SymbolWeight implements NamedWeightCalculator {

	private static final long serialVersionUID = 1L;

	public static final String SYMBOL = "symbol";

	private static boolean isSymbol(String s) {
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if (Character.isLetter(ch) || Character.isDigit(ch))
				return false;
		}
		return true;
	}

	@Override
	public WeightCalculator clone() {
		return new SymbolWeight();
	}

	@Override
	public String name() {
		return SYMBOL;
	}

	@Override
	public double weightH(Annotation node, AnnotatedText t, AnnotatedText h) {
		return isSymbol(node.getForm()) ? 1 : 0;
	}

	@Override
	public double weightT(Annotation node, AnnotatedText t, AnnotatedText h) {
		return isSymbol(node.getForm()) ? 1 : 0;
	}

}
