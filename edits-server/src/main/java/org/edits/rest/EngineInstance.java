package org.edits.rest;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.edits.EditsTextAnnotator;
import org.edits.EntailmentEngineModel;

@Data
@EqualsAndHashCode(callSuper = false)
public class EngineInstance {
	public enum Status {
		FAIL, NOT_FOUND, ONLINE, TRAINING
	}

	private EditsTextAnnotator annotator;
	private final String id;
	private EntailmentEngineModel model;
	private Status status;

	public EngineInstance(String id, EntailmentEngineModel model_) {
		model = model_;
		this.id = id;
	}

	public EditsTextAnnotator annotator() throws Exception {
		if (annotator != null)
			return annotator;
		annotator = (EditsTextAnnotator) ClassLoader.getSystemClassLoader().loadClass(model.getAnotator())
				.newInstance();
		return annotator;
	}
}
