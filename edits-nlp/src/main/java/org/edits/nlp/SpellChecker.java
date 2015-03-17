package org.edits.nlp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import lombok.extern.log4j.Log4j;
import opennlp.tools.util.Span;

import org.edits.MapInteger;

import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.engine.Word;

@Log4j
public class SpellChecker {

	private static SpellChecker instance;

	public static SpellChecker getInstance() {
		if (instance == null) {
			try {
				instance = new SpellChecker(OpenNLP.getInstance());
			} catch (Exception e) {
				log.debug(e);
				throw new RuntimeException("Could not load spell checker modules");
			}
		}
		return instance;
	}

	private final SpellDictionary dict;

	private final OpenNLP opennlp;

	private SpellChecker(OpenNLP opennlp_) throws Exception {
		opennlp = opennlp_;
		InputStream im = this.getClass().getClassLoader().getResource("words.txt").openStream();
		dict = new SpellDictionaryHashMap(new InputStreamReader(im));
	}

	public String correct(String text) throws Exception {
		return correct(text, new MapInteger());
	}

	public String correct(String text, MapInteger context) throws Exception {
		StringBuilder b = new StringBuilder();
		Span[] sp = opennlp.tokenize(text);
		int pos = 0;
		for (Span label : sp) {
			if (label.getStart() > pos)
				b.append(text.substring(pos, label.getStart()));
			pos = label.getEnd();
			String word = text.substring(label.getStart(), label.getEnd());
			if (dict.isCorrect(word) || word.trim().length() == 1) {
				b.append(word);
				continue;
			}
			boolean ok = true;
			for (int i = 0; i < word.length(); i++) {
				if (!Character.isLetter(word.charAt(i))) {
					ok = false;
					break;
				}
			}
			if (!ok) {
				b.append(word);
				continue;
			}
			@SuppressWarnings("unchecked")
			List<Word> aa = dict.getSuggestions(word, 1);
			String best = null;
			int score = 0;

			for (Word o : aa) {
				if (o.getCost() > 100)
					continue;
				String w = o.getWord();
				int s = context.get(w);
				if (best == null || s > score) {
					best = w;
					score = s;
				}
			}
			if (best != null)
				word = best;
			b.append(word);
		}
		return b.toString().replace("``", "'").replace("`", "'").replace("  ", " ").replace("  ", " ")
				.replace("..", ".").trim();
	}

	public String simple(String text) throws IOException {
		return text.replace("\"", "'").replace("wasnt", "wasn't").replace("dont", "don't").replace("  ", " ")
				.replace("  ", " ");
	}
}
