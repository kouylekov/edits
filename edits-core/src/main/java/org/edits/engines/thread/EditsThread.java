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
package org.edits.engines.thread;

import java.util.Iterator;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

/**
 * 
 * @author Milen Kouylekov
 * 
 */
@Log4j
public abstract class EditsThread<E> {

	private boolean finnished = false;
	private Iterator<E> iterator;
	@Getter
	@Setter
	private int threads;
	private int workers = 0;

	public EditsThread() {
		Runtime runtime = Runtime.getRuntime();
		threads = runtime.availableProcessors();
		if (threads > 24)
			threads = 24;
	}

	public EditsThread(int threads_) {
		threads = threads_;
	}

	public synchronized void finnished() {
		workers--;
		if (workers == 0) {
			finnished = true;
		}
	}

	public synchronized void increment() {
		workers++;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public synchronized E next() throws Exception {

		if (iterator != null && iterator.hasNext())
			return iterator.next();
		return null;
	}

	public abstract void process(E object) throws Exception;

	public void start(Iterator<E> iterator_) {
		iterator = iterator_;
		log.debug("Started with " + threads + " threads");
		for (int i = 0; i < threads; i++)
			new Worker<E>(this).start();
		finnished = false;
		try {
			while (finnished == false)
				Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		iterator = null;
	}

}
