package org.edits.engines.thread;

import java.util.List;

import org.edits.engines.EntailmentEngine;
import org.edits.engines.EvaluationResult;
import org.edits.engines.EvaluationStatistics;
import org.edits.etaf.AnnotatedEntailmentPair;
import org.edits.target.Target;

public class ThreadExecutor extends EditsThread<AnnotatedEntailmentPair> {

	private final EntailmentEngine engine;
	private EvaluationStatistics stats;
	private Target<EvaluationResult> target;

	private final boolean training;

	public ThreadExecutor(EntailmentEngine engine_, boolean training_) {
		engine = engine_;
		training = training_;
		if (!training)
			stats = new EvaluationStatistics();
	}

	public ThreadExecutor(EntailmentEngine engine_, Target<EvaluationResult> target_) {
		engine = engine_;
		training = false;
		target = target_;
		stats = new EvaluationStatistics();
	}

	@Override
	public void process(AnnotatedEntailmentPair object) throws Exception {
		if (training)
			((ThreadTrainedEngine) engine).handle(object);
		else {
			EvaluationResult res = engine.evaluate(object);
			if (res.getBenchmark() == null)
				res.setBenchmark("UNKNOWN");
			stats.add(res.getBenchmark(), res.getAssigned());
			if (target != null)
				target.handle(engine.evaluate(object));
		}
	}

	public void run(List<AnnotatedEntailmentPair> pairs) {
		for (int i = 0; i < pairs.size(); i++)
			pairs.get(i).setOrder(i);
		start(pairs.iterator());
	}

	public EvaluationStatistics statistics() {
		stats.calculate();
		return stats;
	}
}
