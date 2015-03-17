package org.edits.etaf;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Annotation", propOrder = {})
@XmlRootElement(name = "annotation")
public class Annotation {

	@XmlAttribute
	private String cpostag;
	@XmlAttribute
	private String deprel;
	@XmlAttribute
	private int end;
	@XmlAttribute
	private String feats;
	@XmlAttribute
	private String form;
	@XmlAttribute
	private String head;
	@XmlAttribute
	private String id;
	@XmlAttribute
	private String lemma;
	@XmlAttribute
	private String postag;
	@XmlAttribute
	private int start;

}
