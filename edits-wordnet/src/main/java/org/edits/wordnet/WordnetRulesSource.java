package org.edits.wordnet;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.log4j.Log4j;

import org.edits.FileTools;
import org.edits.etaf.AnnotatedText;
import org.edits.etaf.Annotation;
import org.edits.rules.AnnotationRule;
import org.edits.rules.Rule;
import org.edits.rules.RulesSource;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IPointer;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;

@Log4j
public class WordnetRulesSource extends RulesSource {

	private static transient Dictionary dictionary;
	private static final long serialVersionUID = 1L;

	private final String path;

	public WordnetRulesSource() {
		this("/usr/share/wordnet/");
	}

	public static void runWord(WordnetRulesSource wns, IIndexWord iw1,
			StringBuilder bs) {

		for (int i = 0; i < iw1.getWordIDs().size(); i++) {
			ISynsetID synsetID = iw1.getWordIDs().get(i).getSynsetID();
			ISynset synset = wns.dictionary().getSynset(synsetID);
			List<ISynsetID> hypernymsSynset = synset
					.getRelatedSynsets(Pointer.HYPONYM);

			for (ISynsetID sid : hypernymsSynset) {
				List<IWord> iws = wns.dictionary().getSynset(sid).getWords();
				for (IWord iw : iws) {
					List<IWordID> x = iw.getRelatedWords();
					for (IWordID xx : x)
						bs.append(xx.getLemma().replace("_", " ") + "\n");
					bs.append(iw.getLemma().replace("_", " ") + "\n");
				}
			}
		}
	}

	public static void main(String[] args) {
		WordnetRulesSource wns = new WordnetRulesSource();
		StringBuilder bs = new StringBuilder();

		IIndexWord iw1 = wns.dictionary().getIndexWord("person", POS.NOUN);
		runWord(wns, iw1, bs);

		iw1 = wns.dictionary().getIndexWord("adult", POS.NOUN);
		runWord(wns, iw1, bs);

		iw1 = wns.dictionary().getIndexWord("human", POS.NOUN);
		runWord(wns, iw1, bs);

		iw1 = wns.dictionary().getIndexWord("profession", POS.NOUN);
		runWord(wns, iw1, bs);

		FileTools.saveString("/home/milen/jobs.txt", bs.toString(), true);
		wns.close();
	}

	public WordnetRulesSource(String path_) {
		path = path_;
	}

	@Override
	public RulesSource clone() {
		return new WordnetRulesSource(path);
	}

	@Override
	public void close() {
		dictionary.close();
	}

	private Dictionary dictionary() {
		if (dictionary != null)
			return dictionary;
		dictionary = new Dictionary(new File(path));
		try {
			dictionary.open();
		} catch (IOException e) {
			log.debug(e);
			throw new RuntimeException("Could not open wordnet");
		}
		return dictionary;
	}

	@Override
	public List<Rule> extractRules(AnnotatedText t, AnnotatedText h) {
		List<Rule> out = Lists.newArrayList();
		for (Annotation a2 : h.getAnnotation()) {
			List<Annotation> a2s = generate(a2);
			if (a2s.size() == 0)
				continue;
			for (Annotation a22 : a2s) {
				for (Annotation a1 : t.getAnnotation()) {
					if (a1.getLemma().equalsIgnoreCase(a2.getLemma())
							&& a2.getCpostag().equals(a1.getCpostag()))
						continue;
					if (!a1.getLemma().equals(a22.getLemma()))
						continue;
					if (!a1.getCpostag().equals(a22.getCpostag()))
						continue;
					AnnotationRule ar = new AnnotationRule();
					ar.setH(a2);
					ar.setT(a1);
					ar.setProbability(1);
					out.add(ar);
				}
			}
		}
		return out;
	}

	public List<Annotation> generate(Annotation a) {
		List<Annotation> out = Lists.newArrayList();
		POS pos = toPOS(a.getCpostag());
		if (pos == null)
			return out;
		IIndexWord iw1 = dictionary().getIndexWord(a.getLemma(), pos);
		if (iw1 == null)
			return out;
		Set<String> cache = Sets.newHashSet();
		List<IWordID> list = iw1.getWordIDs();
		for (IWordID x : list) {
			processWordID(x, out, cache);
		}
		return out;
	}

	@Override
	public double probability(Annotation a1, Annotation a2) {
		return 0;
	}

	private void processWordID(IWordID x, List<Annotation> out, Set<String> seen) {
		String key = x.getLemma() + " " + x.getPOS() + " " + x.getWordNumber()
				+ " " + x.getSynsetID().getOffset();
		if (seen.contains(key))
			return;
		seen.add(key);

		Map<IPointer, List<IWordID>> o = dictionary().getWord(x)
				.getRelatedMap();
		for (List<IWordID> ww : o.values())
			for (IWordID w : ww)
				processWordID(w, out, seen);

		ISynset s = dictionary().getSynset(x.getSynsetID());
		if (!x.getLemma().equals("?"))
			out.add(toAnnotation(x));
		Map<IPointer, List<ISynsetID>> asda = s.getRelatedMap();
		for (IPointer p : asda.keySet()) {
			for (ISynsetID w : asda.get(p)) {
				ISynset ss = dictionary().getSynset(w);
				List<IWord> wxl = ss.getWords();
				for (IWord wx : wxl) {
					if (!x.getLemma().equals("?"))
						out.add(toAnnotation(wx));
				}
			}
		}
	}

	private Annotation toAnnotation(IWord x) {
		Annotation a = new Annotation();
		a.setLemma(x.getLemma());
		a.setCpostag(toString(x.getPOS()));
		return a;
	}

	private Annotation toAnnotation(IWordID x) {
		Annotation a = new Annotation();
		a.setLemma(x.getLemma());
		a.setCpostag(toString(x.getPOS()));
		return a;
	}

	public POS toPOS(String pos) {
		if (pos.equals("noun"))
			return POS.NOUN;
		if (pos.equals("verb"))
			return POS.VERB;
		if (pos.equals("adj"))
			return POS.ADJECTIVE;
		if (pos.equals("adv"))
			return POS.ADVERB;
		return null;
	}

	public String toString(POS pos) {
		if (pos.equals(POS.NOUN))
			return "noun";
		if (pos.equals(POS.VERB))
			return "verb";
		if (pos.equals(POS.ADJECTIVE))
			return "adj";
		if (pos.equals(POS.ADVERB))
			return "adv";
		return null;
	}

	@Override
	public Set<String> uses() {
		Set<String> out = Sets.newHashSet();
		out.add("lemma");
		out.add("cpostag");
		return out;
	}

}
