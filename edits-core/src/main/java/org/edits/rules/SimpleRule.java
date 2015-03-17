package org.edits.rules;

import lombok.Data;

@Data
public class SimpleRule implements Rule {

	private String h;
	private double probability;
	private String t;

}
