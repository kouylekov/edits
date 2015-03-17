package org.edits;

public class Progress {

	private int c;
	private final long start;
	private final int wc;

	public Progress(int outof_) {
		start = System.currentTimeMillis();
		c = 0;
		wc = outof_ / 20;
	}

	public void complete() {
		long end = System.currentTimeMillis();
		print(start, end);
	}

	public synchronized void increment() {
		c++;
		if (c == wc) {
			if (!Edits.isVerbose())
				System.out.print("*");
			c = 0;
		}
	}

	public static void main(String[] args) throws InterruptedException {
		Progress p = new Progress(1000);

		for (int i = 0; i < 1000; i++) {
			p.increment();
			Thread.sleep(10);
		}
		p.complete();
	}

	public static void print(long start, long end) {

		long running = end - start;

		long seconds = running / 1000;

		long minutes = seconds / 60;
		seconds = seconds - minutes * 60;

		long hours = minutes / 60;
		minutes = minutes - hours * 60;

		StringBuilder b = new StringBuilder();

		if (hours > 0)
			b.append(hours + " Hours ");

		if (minutes > 0)
			b.append(minutes + " Minutes ");

		if (seconds > 0)
			b.append(seconds + " Seconds ");

		System.out.print("\n" + (b.length() > 0 ? "Time: " + b.toString() + "\n" : ""));
	}
}
