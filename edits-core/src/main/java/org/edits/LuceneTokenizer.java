/**
 * Edits - Edit Distance Textual Entailment Suite Copyright (C) 2011 Milen
 * Kouylekov This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of the License,
 * or (at your option) any later version. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package org.edits;

import java.io.StringReader;
import java.util.List;

import lombok.extern.log4j.Log4j;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.EnglishMinimalStemFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;
import org.edits.etaf.Annotation;

import com.google.common.collect.Lists;

/**
 * @author Milen Kouylekov
 */
@Log4j
public class LuceneTokenizer implements EditsTextAnnotator {

	@Override
	public List<Annotation> annotate(String text) throws Exception {
		text = SimpleTokenizer.format(text);
		Analyzer analyser = new EnglishAnalyzer(Version.LUCENE_47, CharArraySet.EMPTY_SET);
		TokenFilter filter = new EnglishMinimalStemFilter(analyser.tokenStream("text", new StringReader(text)));
		List<Annotation> out = Lists.newArrayList();
		while (filter.incrementToken()) {
			CharTermAttribute az = filter.getAttribute(CharTermAttribute.class);
			OffsetAttribute o = filter.getAttribute(OffsetAttribute.class);
			String token = text.substring(o.startOffset(), o.endOffset());
			String lemma = az.toString();
			Annotation t = new Annotation();
			t.setForm(token);
			t.setLemma(lemma);
			out.add(t);
		}
		if (out.size() == 0) {
			log.debug("Input string is empty");
		}
		filter.close();
		analyser.close();
		return out;
	}

}
