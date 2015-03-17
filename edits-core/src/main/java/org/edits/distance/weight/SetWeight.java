package org.edits.distance.weight;

import java.util.List;
import java.util.Set;

import lombok.Data;
import lombok.extern.log4j.Log4j;

import org.edits.FileTools;
import org.edits.etaf.AnnotatedText;
import org.edits.etaf.Annotation;

@Data
@Log4j
public class SetWeight implements NamedWeightCalculator {

	public static final String MONTH_NAME = "monthname";
	private static final long serialVersionUID = 1L;

	public static final String WEEKDAY_NAME = "weekdayname";

	private boolean ignoreCase = true;
	private String name;

	private Set<String> set;

	public SetWeight() {

	}

	public SetWeight(String name_) {
		name = name_;
		try {
			if (name.equals(WEEKDAY_NAME))
				set = FileTools.loadSet(this.getClass().getClassLoader().getResourceAsStream("week.txt"));
			if (name.equals(MONTH_NAME))
				set = FileTools.loadSet(this.getClass().getClassLoader().getResourceAsStream("month.txt"));
		} catch (Exception e) {
			log.debug(e);
			throw new RuntimeException("Could not load list " + name);
		}
	}

	public SetWeight(String name_, List<String> list) {
		name = name_;
	}

	@Override
	public WeightCalculator clone() {
		SetWeight st = new SetWeight();
		st.setName(name);
		st.setIgnoreCase(ignoreCase);
		st.setSet(set);
		return st;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public double weightH(Annotation node, AnnotatedText t, AnnotatedText h) {
		String s = ignoreCase ? node.getForm().toLowerCase() : node.getForm();
		return set.contains(s) ? 1 : 0;
	}

	@Override
	public double weightT(Annotation node, AnnotatedText t, AnnotatedText h) {
		String s = ignoreCase ? node.getForm().toLowerCase() : node.getForm();
		return set.contains(s) ? 1 : 0;
	}
}
