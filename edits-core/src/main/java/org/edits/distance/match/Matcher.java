package org.edits.distance.match;

import java.io.Serializable;

import org.edits.etaf.AnnotatedText;
import org.edits.etaf.Annotation;

public interface Matcher extends Serializable {

	public double match(Annotation node1, Annotation node2, AnnotatedText t, AnnotatedText h, String pairID);

	public Matcher copy();
}
