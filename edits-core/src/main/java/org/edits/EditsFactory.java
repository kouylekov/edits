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

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * 
 * @author Milen Kouylekov
 * 
 */
public class EditsFactory {

	public Object load(String filename) throws Exception {
		FileTools.checkFile(filename);
		Unmarshaller unmarshaller =
				JAXBContext.newInstance(this.getClass().getPackage().getName()).createUnmarshaller();
		JAXBElement<?> el = (JAXBElement<?>) unmarshaller.unmarshal(new File(filename));
		return el.getValue();
	}

	public String marshal(Object object) throws Exception {
		StringWriter writer = new StringWriter();
		Marshaller marshaller = JAXBContext.newInstance(this.getClass().getPackage().getName()).createMarshaller();
		marshaller.setProperty("jaxb.formatted.output", true);
		marshaller.marshal(object, writer);
		return writer.toString();
	}

	public void marshal(String filename, Object object, boolean overwrite) throws Exception {
		FileTools.checkOutput(filename, overwrite);
		FileOutputStream fos = new FileOutputStream(filename);
		Marshaller marshaller = JAXBContext.newInstance(this.getClass().getPackage().getName()).createMarshaller();
		marshaller.setProperty("jaxb.formatted.output", true);
		marshaller.marshal(object, fos);
		fos.close();
	}

	public Object read(String text) throws Exception {
		Unmarshaller unmarshaller =
				JAXBContext.newInstance(this.getClass().getPackage().getName()).createUnmarshaller();
		JAXBElement<?> el = (JAXBElement<?>) unmarshaller.unmarshal(new StringReader(text));
		return el.getValue();
	}

}
