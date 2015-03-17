package org.edits.nlp;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Sequence;
import opennlp.tools.util.Span;

import org.apache.lucene.queryparser.classic.ParseException;
import org.edits.etaf.Annotation;
import org.tartarus.snowball.ext.EnglishStemmer;

@Log4j
public class OpenNLP implements org.edits.EditsTextAnnotator {
	private static OpenNLP instance;

	public static OpenNLP getInstance() {
		if (instance == null) {
			try {
				instance = new OpenNLP();
			} catch (Exception e) {
				log.debug(e);
				throw new RuntimeException("Could not load OpenNLP models");
			}
		}
		return instance;
	}

	private final Parser parser;
	private final EnglishStemmer stemmer;

	private final POSTagger tagger;

	private final Tokenizer tokenizer;

	public OpenNLP() throws Exception {
		tagger = new POSTaggerME(new POSModel(this.getClass().getClassLoader()
				.getResourceAsStream("models/en-pos-maxent.bin")));
		parser = ParserFactory.create(new ParserModel(this.getClass().getClassLoader()
				.getResourceAsStream("models/en-parser-chunking.bin")));
		tokenizer = new TokenizerME(new TokenizerModel(this.getClass().getClassLoader()
				.getResourceAsStream("models/en-token.bin")));
		stemmer = new EnglishStemmer();
		log.info("OpenNLP models loaded");
	}

	@Override
	public List<Annotation> annotate(String text) throws ParseException {
		List<Annotation> out = new ArrayList<Annotation>();
		String[] spans = tokenizer.tokenize(text);
		Sequence s = tagger.topKSequences(spans)[0];
		int i = 0;
		for (String outcome : s.getOutcomes()) {
			Annotation l = new Annotation();
			stemmer.setCurrent(spans[i]);
			stemmer.stem();
			l.setForm(spans[i]);
			l.setLemma(stemmer.getCurrent());
			l.setCpostag(ClassicTreeTagger.toGeneral(outcome));
			l.setPostag(outcome);

			out.add(l);
			i++;
		}
		return out;
	}

	public int count(Parse p) {
		int count = 0;
		if (p.getType().equals("PP") || p.getType().equals("VP") || p.getType().equals("NP"))
			count++;

		for (Parse pp : p.getChildren())
			count += count(pp);
		return count;
	}

	private boolean dontCut(Parse p) {
		boolean has = false;
		for (Parse pp : p.getChildren()) {
			int count = count(pp);
			boolean dontCount = dontCut(pp);
			if (!dontCount)
				return dontCount;
			if (count > 1) {
				if (has)
					return false;
				has = true;
			}
		}
		return true;
	}

	private void extract(StringBuilder b, Parse p, String s, boolean top, boolean dontCut) {
		String str = s.substring(p.getSpan().getStart(), p.getSpan().getEnd());
		if (!top && !dontCut && (p.getType().equals("PP") || p.getType().equals("VP")))
			return;
		if (p.isFlat() && !p.isChunk() && p.getParent() != null && p.getParent().isPosTag())
			b.append(str + " ");
		for (Parse pp : p.getChildren())
			extract(b, pp, s, false, dontCut);
	}

	public List<Annotation> parse(String text) {
		Span[] spans = tokenizer.tokenizePos(text);
		Parse p = new Parse(text, new Span(0, text.length()), AbstractBottomUpParser.INC_NODE, 0, 0);
		int i = 0;
		for (Span s : spans) {
			p.insert(new Parse(text, s, AbstractBottomUpParser.TOK_NODE, 0, i));
			i++;
		}
		p = parser.parse(p);
		List<String> phrases = new ArrayList<String>();
		sepratePhrases(p, phrases, text);

		List<String> np = new ArrayList<String>();
		for (String s : phrases) {
			boolean ok = true;
			int replace = -1;
			int k = 0;
			for (String ss : np) {
				if (ss.contains(s))
					ok = false;
				if (s.contains(ss)) {
					replace = k;
				}
				k++;
			}
			if (ok) {
				if (replace != -1)
					np.set(replace, s);
				else
					np.add(s);
			}
		}

		System.out.println("************************");
		for (String t : np)
			System.out.println(t);
		return null;
	}

	public void populate(List<Annotation> out, Parse p, String s) {
		String str = s.substring(p.getSpan().getStart(), p.getSpan().getEnd());
		if (p.isFlat() && !p.isChunk() && p.getParent() != null && p.getParent().isPosTag()) {
			Annotation l = new Annotation();
			stemmer.setCurrent(str);
			stemmer.stem();
			l.setForm(str);
			l.setLemma(stemmer.getCurrent());
			l.setCpostag(ClassicTreeTagger.toGeneral(p.getParent().getType()));
			l.setPostag(p.getParent().getType());
			out.add(l);
		}
		for (Parse pp : p.getChildren())
			populate(out, pp, s);
	}

	public void sepratePhrases(Parse p, List<String> phrases, String text) {
		if (p.getType().equals("PP") || p.getType().equals("VP") || p.getType().equals("NP")) {
			StringBuilder b = new StringBuilder();
			boolean dontCut = dontCut(p);
			extract(b, p, text, true, dontCut);
			if (b.length() != 0)
				phrases.add(b.toString().trim());
		}
		for (Parse x : p.getChildren())
			sepratePhrases(x, phrases, text);
	}

	public Span[] tokenize(String text) throws ParseException {
		return tokenizer.tokenizePos(text);
	}
}
