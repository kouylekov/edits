package org.edits.nlp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.edits.EditsTextAnnotator;
import org.edits.etaf.Annotation;

public class ClassicTreeTagger implements EditsTextAnnotator {
	public static void main(String[] args) throws Exception {
		System.out
				.println(new ClassicTreeTagger("it")
						.analyse("Wolfgang Amadeus Mozart, nome di battesimo Joannes Chrysostomus Wolfgangus Theophilus Mozart (Salisburgo, 27 gennaio 1756 – Vienna, 5 dicembre 1791), è stato un compositore, pianista, organista e violinista austriaco[1], a cui è universalmente riconosciuta la creazione di opere musicali di straordinario valore artistico."));
	}

	public static String toGeneral(String pos) {
		pos = pos.toUpperCase();
		if (pos.startsWith("N"))
			return "noun";
		if (pos.startsWith("V"))
			return "verb";
		if (pos.startsWith("JJ"))
			return "adj";
		if (pos.startsWith("ADJ"))
			return "adj";
		if (pos.startsWith("ART"))
			return "det";
		if (pos.startsWith("DT"))
			return "det";
		if (pos.startsWith("DET"))
			return "det";
		if (pos.startsWith("WDT"))
			return "det";
		if (pos.startsWith("CARD"))
			return "num";
		if (pos.startsWith("CC"))
			return "conj";
		if (pos.startsWith("R"))
			return "adv";
		if (pos.startsWith("WR"))
			return "adv";
		if (pos.startsWith("PRP"))
			return "prep";
		if (pos.startsWith("APP"))
			return "prep";
		if (pos.startsWith("IN"))
			return "prep";
		if (pos.startsWith("TO"))
			return "pron";
		if (pos.startsWith("PP"))
			return "pron";
		if (pos.startsWith("PRO"))
			return "pron";
		if (pos.startsWith("WP"))
			return "pron";
		if (pos.startsWith("PD"))
			return "pron";
		if (pos.startsWith("PI"))
			return "pron";
		if (pos.startsWith("PR"))
			return "pron";
		return pos;
	}

	private String script;

	public ClassicTreeTagger(String l) {
		if (l.equals("en"))
			script = "english";
		if (l.equals("fr"))
			script = "french";
		if (l.equals("it"))
			script = "italian";
		if (l.equals("de"))
			script = "german";
		if (l.equals("es"))
			script = "spanish";
	}

	public List<List<Annotation>> analyse(List<String> text) throws Exception {
		List<List<String[]>> x = process(text);
		List<List<Annotation>> out = new ArrayList<List<Annotation>>();
		for (List<String[]> xa : x)
			out.add(convert(xa));
		return out;
	}

	public List<Annotation> analyse(String text) throws Exception {
		List<String[]> list = process(text);
		return convert(list);
	}

	private List<Annotation> convert(List<String[]> output) {
		List<Annotation> out = new ArrayList<Annotation>();
		for (String[] x : output) {

			if (x[2].equals("<unknown>"))
				x[2] = x[0].toLowerCase();

			Annotation t = new Annotation();
			t.setId("" + (out.size() + 1));
			t.setForm(x[0]);
			t.setLemma(x[2]);
			t.setCpostag(toGeneral(x[1]));
			t.setPostag(x[1]);
			out.add(t);
		}
		return out;
	}

	private List<List<String[]>> process(List<String> text) throws Exception {
		List<List<String[]>> n = new ArrayList<List<String[]>>();
		StringBuilder b = new StringBuilder();
		for (String t : text)
			b.append(t + " <end> ");
		List<String[]> cx = process(b.toString());
		List<String[]> current = new ArrayList<String[]>();
		for (String[] c : cx) {
			if (c[0].equals("<end>")) {
				n.add(current);
				current = new ArrayList<String[]>();
			} else
				current.add(c);
		}
		return n;
	}

	private List<String[]> process(String text) throws Exception {
		List<String[]> out = new ArrayList<String[]>();
		String[] command = new String[] { "/opt/it.celi.research/tree-tagger/tree-tagger/cmd/tree-tagger-" + script };
		String cmd = Arrays.toString(command).replace("[", "").replace("]", "").replace(",", "");
		System.out.println(cmd);
		Process proc = Runtime.getRuntime().exec(command);

		PrintStream ps = new PrintStream(proc.getOutputStream());
		ps.println(text);
		ps.close();

		StringBuilder vud = new StringBuilder();

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = null;
			while ((line = in.readLine()) != null) {
				vud.append(line + "\n");
			}
			in.close();
		} catch (Exception e) {
			InputStream is = proc.getErrorStream();
			String line = null;

			BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			StringBuilder vv = new StringBuilder();
			while ((line = in.readLine()) != null) {
				vv.append(line + "\n");
			}
			in.close();
			System.out.println(vv);
			throw new RuntimeException("Can not parse text");
		}

		StringTokenizer toker = new StringTokenizer(vud.toString(), "\n", false);
		while (toker.hasMoreTokens())
			out.add(toker.nextToken().split("\t"));

		return out;

	}

	@Override
	public List<Annotation> annotate(String text) throws Exception {
		return null;
	}

}
