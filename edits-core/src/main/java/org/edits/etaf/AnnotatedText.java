package org.edits.etaf;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;

import com.google.common.collect.Lists;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AnnotatedText", propOrder = { "annotation" })
public class AnnotatedText {

	@XmlElement
	protected List<Annotation> annotation;

	public AnnotatedText() {
		annotation = Lists.newArrayList();
	}
}
