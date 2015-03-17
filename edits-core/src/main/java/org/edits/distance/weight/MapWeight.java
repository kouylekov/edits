package org.edits.distance.weight;

import java.util.Map;

import lombok.extern.log4j.Log4j;

import org.edits.FileTools;
import org.edits.etaf.AnnotatedText;
import org.edits.etaf.Annotation;

@Log4j
public class MapWeight implements NamedWeightCalculator {

	public static final String IDF = "idf";
	private static final long serialVersionUID = 1L;
	private Map<String, Double> map;
	private final String name;

	private double notFound = 7;

	public MapWeight() {
		notFound = 7;
		name = IDF;
		try {
			map = FileTools.loadNumberMap(this.getClass().getClassLoader().getResourceAsStream("idf/idf-bnc.txt"));
		} catch (Exception e) {
			log.debug(e);
			throw new RuntimeException("Could not load list " + name);
		}
	}

	public MapWeight(String name, Map<String, Double> map, double notFound) {
		super();
		this.name = name;
		this.map = map;
		this.notFound = notFound;
	}

	private double calculate(Annotation w) {
		Double d = map.get(w.getForm());
		if (d == null && w.getLemma() != null)
			d = map.get(w.getForm());
		return d == null ? notFound : d;
	}

	@Override
	public WeightCalculator clone() {
		return new MapWeight(name, map, notFound);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public double weightH(Annotation node, AnnotatedText t, AnnotatedText h) {
		return calculate(node);
	}

	@Override
	public double weightT(Annotation node, AnnotatedText t, AnnotatedText h) {
		return calculate(node);
	}

}
