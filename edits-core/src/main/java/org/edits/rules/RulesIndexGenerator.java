package org.edits.rules;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import lombok.extern.log4j.Log4j;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.edits.engines.thread.EditsThread;
import org.edits.etaf.AnnotatedEntailmentPair;
import org.edits.etaf.AnnotatedText;
import org.edits.etaf.Annotation;

import com.google.common.collect.Sets;

@Log4j
public class RulesIndexGenerator extends EditsThread<AnnotatedEntailmentPair> {
	private final Set<String> cache;
	private final boolean extractRules;
	private final RulesSource rulesSource;
	private IndexWriter writer;

	public RulesIndexGenerator(RulesSource rulesSource_, boolean extractRules_) {
		cache = Sets.newHashSet();
		rulesSource = rulesSource_;
		extractRules = extractRules_;
	}

	private void addRule(Annotation at, Annotation ah) {
		String key = generateKey(at, ah);
		if (cache.contains(key))
			return;
		cache.add(key);

		double prob = rulesSource.probability(at, ah);

		if (prob == 0)
			return;

		addRule(at, ah, prob);
	}

	private void addRule(Annotation at, Annotation ah, double prob) {
		Document doc = new Document();
		for (String u : rulesSource.uses()) {
			String v1 = IndexRulesSource.value(u, at);
			String v2 = IndexRulesSource.value(u, ah);
			doc.add(new StringField(u + "-1", v1, Store.YES));
			doc.add(new StringField(u + "-2", v2, Store.YES));
		}

		doc.add(new StringField(IndexRulesSource.VALUE_FIELD, "" + prob, Store.YES));
		synchronized (writer) {
			try {
				writer.addDocument(doc);
			} catch (IOException e) {
				log.debug(e);
			}
		}
	}

	private void addRules(AnnotatedText t, AnnotatedText h) {
		List<Rule> ss = rulesSource.extractRules(t, h);
		for (Rule s : ss) {
			if (s instanceof AnnotationRule) {
				AnnotationRule ar = (AnnotationRule) s;
				String key = generateKey(ar.getT(), ar.getH());
				if (cache.contains(key))
					continue;
				cache.add(key);
				addRule(ar.getT(), ar.getH(), ar.getProbability());
			}
		}
	}

	public void generateIndex(String path, List<AnnotatedEntailmentPair> aps) throws Exception {

		log.info("Rules extraction started.");
		IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_47, new WhitespaceAnalyzer(Version.LUCENE_47));
		conf.setOpenMode(OpenMode.CREATE);
		writer = new IndexWriter(FSDirectory.open(new File(path)), conf);
		Document doc = new Document();
		doc.add(new StringField(IndexRulesSource.TERMDOC_FIELD, "true", Store.YES));
		for (String u : rulesSource.uses())
			doc.add(new StringField(IndexRulesSource.USES_FIELD, u, Store.YES));
		writer.addDocument(doc);
		start(aps.iterator());
		writer.waitForMerges();
		writer.close(true);
		log.info(cache.size() + " rules extracted!");

	}

	private String generateKey(Annotation at, Annotation ah) {
		StringBuilder b = new StringBuilder();
		for (String u : rulesSource.uses()) {
			b.append(IndexRulesSource.value(u, at) + "###");
			b.append(IndexRulesSource.value(u, ah) + "###");
		}
		return b.toString();
	}

	@Override
	public void process(AnnotatedEntailmentPair p) throws Exception {
		for (AnnotatedText t : p.getT()) {
			for (AnnotatedText h : p.getH()) {
				if (extractRules) {
					addRules(t, h);
				} else {
					for (Annotation at : t.getAnnotation()) {
						for (Annotation ah : h.getAnnotation()) {
							addRule(at, ah);
						}
					}
				}
			}
		}
	}
}
