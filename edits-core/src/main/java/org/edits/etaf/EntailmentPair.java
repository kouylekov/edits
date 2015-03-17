package org.edits.etaf;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;

import com.google.common.collect.Lists;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EntailmentPair", propOrder = { "t", "h" })
public class EntailmentPair {

	@XmlAttribute
	protected String entailment;

	@XmlElement
	protected List<String> h;
	@XmlAttribute
	protected String id;
	@XmlElement
	protected List<String> t;

	@XmlAttribute
	protected String task;

	public EntailmentPair() {
		t = Lists.newArrayList();
		h = Lists.newArrayList();
	}

}
