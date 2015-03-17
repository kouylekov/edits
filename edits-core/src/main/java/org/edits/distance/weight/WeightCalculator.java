package org.edits.distance.weight;

import java.io.Serializable;

import org.edits.etaf.AnnotatedText;
import org.edits.etaf.Annotation;

public interface WeightCalculator extends Serializable, Cloneable {

	public double weightH(Annotation node, AnnotatedText t, AnnotatedText h);

	public double weightT(Annotation node, AnnotatedText t, AnnotatedText h);

	public WeightCalculator clone();

}
