package org.edits.rules;

import lombok.Data;

import org.edits.etaf.Annotation;

@Data
public class AnnotationRule implements Rule {

	private Annotation h;
	private double probability;
	private Annotation t;

}
