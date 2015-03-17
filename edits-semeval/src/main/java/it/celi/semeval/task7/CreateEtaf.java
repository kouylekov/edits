package it.celi.semeval.task7;

import it.celi.semeval.questions.ObjectFactory;
import it.celi.semeval.questions.Question;
import it.celi.semeval.questions.ReferenceAnswer;
import it.celi.semeval.questions.StudentAnswer;

import java.io.File;

import org.apache.log4j.LogManager;
import org.edits.MapInteger;
import org.edits.distance.match.DefaultMatcher;
import org.edits.etaf.EntailmentCorpus;
import org.edits.etaf.EntailmentPair;
import org.edits.example.RunExperiment;
import org.edits.nlp.SpellChecker;

import com.swabunga.spell.event.StringWordTokenizer;

public class CreateEtaf {

	public static MapInteger createContext(Question q) {
		MapInteger mip = new MapInteger();
		for (StudentAnswer a : q.getStudentAnswers().getStudentAnswer()) {

			StringWordTokenizer toker = new StringWordTokenizer(a.getContent());
			while (toker.hasMoreWords()) {
				String word = toker.nextWord();
				mip.increment(word);
			}
		}

		for (ReferenceAnswer a : q.getReferenceAnswers().getReferenceAnswer()) {

			StringWordTokenizer toker = new StringWordTokenizer(a.getContent());
			while (toker.hasMoreWords()) {
				String word = toker.nextWord();
				mip.increment(word);
			}
		}
		return mip;
	}

	public static void createEtaf(String path, String way, String type, SpellChecker check) throws Exception {

		ObjectFactory factory = new ObjectFactory();
		File f = new File(path + "task7/" + type + "/" + way + "/beetle");

		EntailmentCorpus ecCorpus = new EntailmentCorpus();

		for (File file : f.listFiles()) {
			handleFile(file, factory, ecCorpus, check);
		}

		org.edits.etaf.ObjectFactory ff = new org.edits.etaf.ObjectFactory();

		ff.marshal(path + "task7/edits/beetle-" + way + "-" + type + ".xml", ff.createEntailmentCorpus(ecCorpus), true);

		f = new File(path + "task7/" + type + "/" + way + "/sciEntsBank");

		EntailmentCorpus ecCorpus2 = new EntailmentCorpus();

		for (File file : f.listFiles()) {
			handleFile(file, factory, ecCorpus2, check);
		}
		ff.marshal(path + "task7/edits/bank-" + way + "-" + type + ".xml", ff.createEntailmentCorpus(ecCorpus2), true);
		ecCorpus.getPair().addAll(ecCorpus2.getPair());
		ff.marshal(path + "task7/edits/rte7-" + type + ".xml", ff.createEntailmentCorpus(ecCorpus), true);

	}

	public static void handleFile(File file, ObjectFactory factory, EntailmentCorpus ecCorpus, SpellChecker check)
			throws Exception {
		Question q = (Question) factory.load(file.getAbsolutePath());
		MapInteger context = createContext(q);
		for (StudentAnswer a : q.getStudentAnswers().getStudentAnswer()) {
			String orig = check.simple(a.getContent());
			String s = check.correct(orig, context);
			double min = 1;
			String s2 = null;
			for (ReferenceAnswer az : q.getReferenceAnswers().getReferenceAnswer()) {
				double d = DefaultMatcher.distance(s, az.getContent(), true);
				if (d <= min) {
					min = d;
					s2 = az.getContent();
				}
			}
			EntailmentPair p = new EntailmentPair();
			p.setId(a.getId());
			p.setEntailment(a.getAccuracy());
			p.getT().add(s2);
			p.getH().add(s);
			ecCorpus.getPair().add(p);

		}
	}

	public static void main(String[] args) throws Exception {
		System.setProperty("log4j.defaultInitOverride", "true");
		LogManager.resetConfiguration();
		SpellChecker check = SpellChecker.getInstance();
		String path = "/home/milen/export/semeval2013/";
		createEtaf(path, "5way", "training", check);
		path = path + "task7/edits/";
		new RunExperiment(path, false, true, true).train("rte7.xml");
	}

}
