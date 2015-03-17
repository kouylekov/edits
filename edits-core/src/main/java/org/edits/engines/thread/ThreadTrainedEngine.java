package org.edits.engines.thread;

import org.edits.engines.EntailmentEngine;
import org.edits.etaf.AnnotatedEntailmentPair;

public abstract class ThreadTrainedEngine extends EntailmentEngine {

	private static final long serialVersionUID = 1L;

	public abstract void handle(AnnotatedEntailmentPair p);

}
