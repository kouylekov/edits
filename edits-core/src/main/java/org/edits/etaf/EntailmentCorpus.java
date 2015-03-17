package org.edits.etaf;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;

import org.edits.EditsTextAnnotator;

import com.google.common.collect.Lists;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EntailmentCorpus", propOrder = { "pair", "annotated" })
@XmlRootElement
public class EntailmentCorpus {

	public static List<AnnotatedEntailmentPair> annotate(EditsTextAnnotator annotator, List<EntailmentPair> paies)
			throws Exception {
		List<AnnotatedEntailmentPair> out = Lists.newArrayList();
		for (EntailmentPair p : paies) {
			AnnotatedEntailmentPair px = new AnnotatedEntailmentPair();
			px.setEntailment(p.getEntailment());
			px.setTask(p.getTask());
			px.setId(p.getId());
			for (String text : p.getT()) {
				AnnotatedText at = new AnnotatedText();
				at.getAnnotation().addAll(annotator.annotate(text));
				px.getT().add(at);
			}
			for (String text : p.getH()) {
				AnnotatedText at = new AnnotatedText();
				at.getAnnotation().addAll(annotator.annotate(text));
				px.getH().add(at);
			}
			out.add(px);
		}
		return out;
	}

	@XmlElement
	protected List<AnnotatedEntailmentPair> annotated;

	@XmlElement
	protected List<EntailmentPair> pair;

	public EntailmentCorpus() {
		pair = Lists.newArrayList();
		annotated = Lists.newArrayList();
	}

	public void annotate(EditsTextAnnotator annotator) throws Exception {
		annotated = annotate(annotator, pair);
		pair.clear();
	}
}
