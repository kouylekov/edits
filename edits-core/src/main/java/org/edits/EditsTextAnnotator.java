package org.edits;

import java.util.List;

import org.edits.etaf.Annotation;

public interface EditsTextAnnotator {

	public List<Annotation> annotate(String text) throws Exception;

}
