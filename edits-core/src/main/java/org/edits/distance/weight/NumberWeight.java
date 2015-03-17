package org.edits.distance.weight;

import org.edits.etaf.AnnotatedText;
import org.edits.etaf.Annotation;

public class NumberWeight implements NamedWeightCalculator {
	public static final String MONTH = "month";
	public static final String MONTHDAY = "monthday";
	public static final String NUMBER = "number";
	private static final long serialVersionUID = 1L;
	public static final Object WEEK = "week";
	public static final String WEEKDAY = "weekday";
	public static final String YEAR = "year";
	public static final String YEARDAY = "yearday";

	public static boolean isNumber(String s) {
		try {
			Double.parseDouble(s);
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	private final String name;

	public NumberWeight(String name_) {
		name = name_;
	}

	@Override
	public WeightCalculator clone() {
		return new NumberWeight(name);
	}

	public boolean isValue(String s) {
		if (!NumberWeight.isNumber(s))
			return false;
		if (name.equals(NUMBER))
			return true;
		if (s.indexOf('.') != -1)
			return false;
		int i = Integer.parseInt(s);
		if (name.equals(YEAR))
			return true;
		if (name.equals(WEEKDAY))
			return i <= 1 && i >= 366;
		if (name.equals(MONTH))
			return i <= 12 && i >= 1;
		if (name.equals(MONTHDAY))
			return i <= 31 && i >= 1;
		if (name.equals(WEEK))
			return i <= 52 && i >= 1;
		if (name.equals(WEEKDAY))
			return i <= 7 && i >= 1;
		return false;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public double weightH(Annotation node, AnnotatedText t, AnnotatedText h) {
		return isValue(node.getForm()) ? 1 : 0;
	}

	@Override
	public double weightT(Annotation node, AnnotatedText t, AnnotatedText h) {
		return isValue(node.getForm()) ? 1 : 0;
	}
}
