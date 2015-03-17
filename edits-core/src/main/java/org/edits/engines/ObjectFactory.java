package org.edits.engines;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.namespace.QName;

public class ObjectFactory {
	private final static QName _EVALUATION_RESULT_QNAME = new QName("", "result");

	@XmlElementDecl(namespace = "", name = "result")
	public JAXBElement<EvaluationResult> createEntailmentCorpus(EvaluationResult value) {
		return new JAXBElement<EvaluationResult>(_EVALUATION_RESULT_QNAME, EvaluationResult.class, null, value);
	}

	public EvaluationResult createEvaluationResult() {
		return new EvaluationResult();
	}
}
