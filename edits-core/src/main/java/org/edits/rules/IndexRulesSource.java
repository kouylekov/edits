package org.edits.rules;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.edits.etaf.AnnotatedText;
import org.edits.etaf.Annotation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@Log4j
@Data
@EqualsAndHashCode(callSuper = false)
public class IndexRulesSource extends RulesSource {

	private static Map<String, IndexSearcher> searchers;

	private static final long serialVersionUID = 1L;
	public static final String TERMDOC_FIELD = "value";
	public static final String USES_FIELD = "uses";

	public static final String VALUE_FIELD = "termsDoc";

	static {
		searchers = Maps.newHashMap();
	}

	public static String value(String f, Annotation a1) {

		if (f.equals("form"))
			return a1.getForm();

		if (f.equals("lemma"))
			return a1.getLemma();

		if (f.equals("cpostag"))
			return a1.getCpostag();

		if (f.equals("postag"))
			return a1.getPostag();

		return null;
	}

	private Set<String> fields;

	private String indexPath;
	private transient IndexSearcher searcher;

	public IndexRulesSource() {

	}

	public IndexRulesSource(String indexPath_) {
		indexPath = indexPath_;
		fields = Sets.newHashSet();
		try {
			Query q = new TermQuery(new Term(TERMDOC_FIELD, "true"));
			TopDocs td = searcher().search(q, 1);
			Document doc = searcher().doc(td.scoreDocs[0].doc);
			for (String d : doc.getValues(USES_FIELD))
				fields.add(d);
		} catch (Exception e) {
			log.debug(e);
			throw new RuntimeException("Could not load index from " + indexPath);
		}
	}

	@Override
	public RulesSource clone() {
		IndexRulesSource rs = new IndexRulesSource();
		rs.setFields(fields);
		rs.setIndexPath(indexPath);
		return rs;
	}

	@Override
	public void close() {
		try {
			searcher().getIndexReader().close();
		} catch (Exception e) {
			log.debug(e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<Rule> extractRules(AnnotatedText t, AnnotatedText h) {
		List<Rule> out = Lists.newArrayList();
		for (Annotation a1 : t.getAnnotation()) {
			for (Annotation a2 : h.getAnnotation()) {
				double probability = probability(a1, a2);
				if (probability == 0)
					continue;
				AnnotationRule ar = new AnnotationRule();
				ar.setH(a2);
				ar.setT(a1);
				ar.setProbability(probability);
				out.add(ar);
			}
		}

		return out;
	}

	@Override
	public double probability(Annotation a1, Annotation a2) {

		BooleanQuery b = new BooleanQuery();

		for (String f : fields) {
			String v1 = value(f, a1);
			String v2 = value(f, a2);

			if (v1 == null || v2 == null)
				return 0;
			b.add(new TermQuery(new Term(f + "-1", v1)), Occur.MUST);
			b.add(new TermQuery(new Term(f + "-2", v2)), Occur.MUST);
		}
		try {
			TopDocs td = searcher.search(b, 1);
			if (td.scoreDocs.length == 0)
				return 0;
			return Double.parseDouble(searcher.doc(td.scoreDocs[0].doc).get(VALUE_FIELD));
		} catch (Exception e) {
			log.debug(e.getMessage());
		}
		return 0;
	}

	public IndexSearcher searcher() {
		try {
			if (searcher != null)
				return searcher;
			searcher = searchers.get(indexPath);
			if (searcher != null)
				return searcher;
			searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(new File(indexPath))));
			searchers.put(indexPath, searcher);
		} catch (Exception e) {
			log.debug(e);
			throw new RuntimeException("Could not load index from " + indexPath);
		}
		return searcher;
	}

	@Override
	public Set<String> uses() {
		return fields;
	}

}
