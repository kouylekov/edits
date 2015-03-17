package org.edits.distance.match;

import lombok.Data;

import org.edits.etaf.AnnotatedText;
import org.edits.etaf.Annotation;
import org.edits.rules.RulesSource;

@Data
public class SimpleRulesMatcher implements NestedMatcher {

	private static final long serialVersionUID = 1L;
	private final Matcher matcher;
	private final RulesSource source;

	public SimpleRulesMatcher(Matcher matcher_, RulesSource source_) {
		source = source_;
		matcher = matcher_;
	}

	public SimpleRulesMatcher(RulesSource source_) {
		source = source_;
		matcher = new DefaultMatcher();
	}

	@Override
	public Matcher copy() {
		return new SimpleRulesMatcher(matcher.copy(), source.clone());
	}

	@Override
	public double match(Annotation node1, Annotation node2, AnnotatedText t, AnnotatedText h, String pairID) {
		double d = matcher.match(node1, node2, t, h, pairID);
		if (d == 0)
			return d;
		double dc = source.entails(node1, node2);
		if (dc == 0.0)
			return d;
		return d * (1 - dc);
	}
}
