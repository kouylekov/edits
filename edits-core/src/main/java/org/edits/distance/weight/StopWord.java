package org.edits.distance.weight;

import java.util.Set;

import lombok.Data;
import lombok.extern.log4j.Log4j;

import org.edits.FileTools;
import org.edits.etaf.AnnotatedText;
import org.edits.etaf.Annotation;

@Data
@Log4j
public class StopWord implements NamedWeightCalculator {

	private static final long serialVersionUID = 1L;

	public static final String STOPWORDS = "stopwords";
	private final Set<String> stopWords;

	public StopWord() {
		try {
			stopWords = FileTools.loadSet(getClass().getClassLoader().getResourceAsStream("stopwords/en.txt"));
		} catch (Exception e) {
			log.debug(e);
			throw new RuntimeException("Could not load stopwords file.");
		}

	}

	public StopWord(Set<String> stopWords) {
		super();
		this.stopWords = stopWords;
	}

	@Override
	public WeightCalculator clone() {
		return new StopWord(stopWords);
	}

	@Override
	public String name() {
		return STOPWORDS;
	}

	public boolean stopWord(Annotation a) {
		return (stopWords.contains(a.getForm()) || stopWords.contains(a.getLemma()));
	}

	@Override
	public double weightH(Annotation node, AnnotatedText t, AnnotatedText h) {
		return stopWord(node) ? 0 : 1;
	}

	@Override
	public double weightT(Annotation node, AnnotatedText t, AnnotatedText h) {
		return stopWord(node) ? 0 : 1;
	}

}
