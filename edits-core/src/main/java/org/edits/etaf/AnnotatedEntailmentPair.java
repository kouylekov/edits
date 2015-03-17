package org.edits.etaf;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;

import org.edits.EditsTextAnnotator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AnnotatedEntailmentPair", propOrder = { "t", "h" })
public class AnnotatedEntailmentPair {

	public static AnnotatedEntailmentPair create(String t2, String h2, EditsTextAnnotator annotator) throws Exception {
		AnnotatedEntailmentPair px = new AnnotatedEntailmentPair();
		AnnotatedText at = new AnnotatedText();
		at.getAnnotation().addAll(annotator.annotate(t2));
		px.getT().add(at);
		at = new AnnotatedText();
		at.getAnnotation().addAll(annotator.annotate(h2));
		px.getH().add(at);
		return px;
	}

	@XmlTransient
	private Map<String, String> attributes;
	@XmlAttribute
	protected String entailment;
	@XmlElement
	protected List<AnnotatedText> h;
	@XmlAttribute
	protected String id;

	@XmlTransient
	private int order;

	@XmlElement
	protected List<AnnotatedText> t;

	@XmlAttribute
	protected String task;

	public AnnotatedEntailmentPair() {
		t = Lists.newArrayList();
		h = Lists.newArrayList();
		attributes = Maps.newHashMap();
	}

}
