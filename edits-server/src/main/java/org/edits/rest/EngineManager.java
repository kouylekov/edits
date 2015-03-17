package org.edits.rest;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.edits.EntailmentEngineModel;
import org.edits.FileTools;
import org.edits.engines.EntailmentEngine;
import org.edits.engines.EvaluationStatistics;
import org.edits.engines.distance.DistanceEntailmentEngine;
import org.edits.engines.weka.WekaEntailmentEngine;
import org.edits.etaf.AnnotatedEntailmentPair;

import com.google.common.collect.Maps;

public class EngineManager {

	public static final String DEFAULT_ENTAILMENT_ENGINE = "DEFAULT_ENTAILMENT_ENGINE";

	private final Map<String, EngineInstance> engines;

	private String pathname;

	public EngineManager() {
		engines = Maps.newHashMap();
		EngineInstance inst = new EngineInstance(DEFAULT_ENTAILMENT_ENGINE, new EntailmentEngineModel(
				"org.edits.LuceneTokenizer", new DistanceEntailmentEngine(), new EvaluationStatistics()));
		inst.setStatus(EngineInstance.Status.ONLINE);
		engines.put(DEFAULT_ENTAILMENT_ENGINE, inst);
	}

	public EngineManager(String pathname_) throws Exception {
		this();
		pathname = pathname_;
		for (String filename : new File(pathname).list()) {
			if (!filename.endsWith(".model"))
				continue;
			String id = filename.substring(0, filename.lastIndexOf("."));
			EngineInstance inst = new EngineInstance(id, (EntailmentEngineModel) FileTools.read(pathname + filename));
			inst.setStatus(EngineInstance.Status.ONLINE);
			engines.put(id, inst);
		}
	}

	public void addEngine(final String engineName, final List<AnnotatedEntailmentPair> ps, boolean simple,
			String annString) throws Exception {
		if (engineName == null || engineName.length() == 0)
			throw new RuntimeException("Invalid engine name!");
		if (engines.keySet().contains(engineName))
			throw new RuntimeException("Engine with the name \"" + engineName + "\" already exists!");
		EntailmentEngine e = simple ? new DistanceEntailmentEngine() : new WekaEntailmentEngine();
		final EntailmentEngineModel model = new EntailmentEngineModel();
		model.setAnotator(annString);
		model.setEngine(e);
		final EngineInstance instance = new EngineInstance(engineName, model);
		engines.put(engineName, instance);
		instance.setStatus(EngineInstance.Status.TRAINING);
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					EvaluationStatistics stats = instance.getModel().getEngine().train(ps);
					instance.setStatus(EngineInstance.Status.ONLINE);
					instance.getModel().setStatistics(stats);
					if (pathname != null)
						FileTools.write(model, pathname + engineName, true);
				} catch (Exception e) {
					instance.setStatus(EngineInstance.Status.FAIL);
				}
			}
		};
		t.start();
	}

	public void delete(String engineName) {
		if (engineName == null || engineName.length() == 0)
			throw new RuntimeException("Invalid engine name!");
		if (!engines.keySet().contains(engineName))
			throw new RuntimeException("Engine with the name \"" + engineName + "\" does not exists!");
		engines.remove(engineName);
	}

	public EngineInstance get(String engineName) {
		if (engineName == null || engineName.length() == 0)
			engineName = DEFAULT_ENTAILMENT_ENGINE;
		EngineInstance instance = engines.get(engineName);
		if (instance == null)
			throw new RuntimeException("Entailment engine with the name \"" + engineName + "\" not found!");
		if (instance.getStatus().equals(EngineInstance.Status.FAIL))
			throw new RuntimeException("Entailment engine training\"" + engineName + "\" training failed!");
		if (instance.getStatus().equals(EngineInstance.Status.TRAINING))
			throw new RuntimeException("Entailment engine \"" + engineName + "\" training did not finnish!");
		return instance;
	}

	public EvaluationStatistics statistics(String engineName) {
		if (engineName == null || engineName.length() == 0)
			engineName = DEFAULT_ENTAILMENT_ENGINE;
		EngineInstance instance = engines.get(engineName);
		if (instance == null)
			throw new RuntimeException("Engine with the name \"" + engineName + "\" does not exists!");
		if (instance.getModel().getStatistics() == null)
			throw new RuntimeException("Engine  \"" + engineName + "\" does not have calculated statistics!");
		return instance.getModel().getStatistics();
	}

	public EngineInstance.Status status(String engineName) {
		if (engineName == null || engineName.length() == 0)
			engineName = DEFAULT_ENTAILMENT_ENGINE;
		EngineInstance instance = engines.get(engineName);
		if (instance == null)
			return EngineInstance.Status.NOT_FOUND;
		return instance.getStatus();
	}
}
