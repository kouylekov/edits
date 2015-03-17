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

import lombok.extern.log4j.Log4j;

/**
 * 
 * @author Milen Kouylekov
 * 
 */
@Log4j
public class Worker<E> extends Thread {

	private final EditsThread<E> engine;

	public Worker(EditsThread<E> engine_) {
		engine = engine_;
	}

	@Override
	public void run() {
		engine.increment();
		while (true) {
			try {
				E pk = engine.next();
				if (pk == null)
					break;
				engine.process(pk);
			} catch (Exception e) {
				e.printStackTrace();
				log.debug("error", e);
				System.exit(0);

			}
		}
		engine.finnished();
	}
}
