package org.edits.etaf;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

import org.edits.EditsFactory;

@XmlRegistry
public class ObjectFactory extends EditsFactory {

	private final static QName _EntailmentCorpus_QNAME = new QName("", "entailment-corpus");

	public EntailmentCorpus createEntailmentCorpus() {
		return new EntailmentCorpus();
	}

	@XmlElementDecl(namespace = "", name = "entailment-corpus")
	public JAXBElement<EntailmentCorpus> createEntailmentCorpus(EntailmentCorpus value) {
		return new JAXBElement<EntailmentCorpus>(_EntailmentCorpus_QNAME, EntailmentCorpus.class, null, value);
	}

}
