package org.edits.engines;

import java.io.Serializable;

import lombok.Getter;
import lombok.ToString;

@ToString(includeFieldNames = true)
public class OptimizationGoal implements Serializable {

	public enum Target {
		ACCURACY, FMEASURE, PRECISION, RECALL
	}

	public static final OptimizationGoal ACCURACY = new OptimizationGoal(Target.ACCURACY, null);

	public static final OptimizationGoal FMEASURE_NO = new OptimizationGoal(Target.FMEASURE, "NO");
	public static final OptimizationGoal FMEASURE_YES = new OptimizationGoal(Target.FMEASURE, "YES");
	public static final OptimizationGoal PRECISION_NO = new OptimizationGoal(Target.PRECISION, "NO");
	public static final OptimizationGoal PRECISION_YES = new OptimizationGoal(Target.PRECISION, "YES");
	public static final OptimizationGoal RECALL_NO = new OptimizationGoal(Target.RECALL, "NO");
	public static final OptimizationGoal RECALL_YES = new OptimizationGoal(Target.RECALL, "YES");

	private static final long serialVersionUID = 1L;

	@Getter
	private final String relation;
	@Getter
	private final Target target;

	public OptimizationGoal() {
		this(Target.ACCURACY, null);
	}

	public OptimizationGoal(Target target, String relation) {
		super();
		this.relation = relation;
		this.target = target;
	}

}
