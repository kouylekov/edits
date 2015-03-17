package org.edits.distance.match;

import java.util.Map;

import lombok.Data;

import org.edits.FileTools;
import org.edits.etaf.AnnotatedText;
import org.edits.etaf.Annotation;

import com.google.common.collect.Maps;

@Data
public class CachedMatcher implements NestedMatcher {

	private static final long serialVersionUID = 1L;

	private Map<String, Double> cache;

	private Matcher matcher;

	public CachedMatcher() {
		matcher = new DefaultMatcher();
	}

	public CachedMatcher(Matcher matcher_) {
		matcher = matcher_;
		cache = Maps.newHashMap();
	}

	public CachedMatcher(String path) throws Exception {
		matcher = new DefaultMatcher();
		cache = FileTools.loadNumberMap(path, "UTF-8");
	}

	@Override
	public Matcher copy() {
		CachedMatcher cm = new CachedMatcher();
		cm.setCache(cache);
		cm.setMatcher(matcher.copy());
		return cm;
	}

	@Override
	public double match(Annotation node1, Annotation node2, AnnotatedText t, AnnotatedText h, String pairID) {

		String key = pairID + "-" + node1.getId() + "-" + node2.getId();
		Double value = cache.get(key);
		if (value != null)
			return value;
		double d = matcher.match(node1, node2, t, h, pairID);
		cache.put(key, d);
		return d;
	}
}
