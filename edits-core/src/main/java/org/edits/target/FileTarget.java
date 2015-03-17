package org.edits.target;

import java.io.File;
import java.io.PrintStream;

public class FileTarget<E> implements Target<E> {

	private final PrintStream ps;

	public FileTarget(String filename) throws Exception {
		ps = new PrintStream(new File(filename));
	}

	@Override
	public void close() {
		ps.close();
	}

	@Override
	public void handle(E res) {
		ps.println(res);
	}

}
