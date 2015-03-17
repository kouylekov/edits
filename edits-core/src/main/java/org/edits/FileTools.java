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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;

/**
 * @author Milen Kouylekov
 */
public class FileTools {

	public static void checkDirectory(String filename) throws Exception {
		File file = new File(filename);
		if (!validFileName(file))
			throw new Exception("Filename not acceptable!");
		if (!file.exists())
			throw new Exception("The directory " + filename + " does not exist!");
		if (!file.canRead())
			throw new Exception("The system can not read from directory " + filename + "!");
	}

	public static void checkFile(String filename) {
		File file = new File(filename);
		if (!validFileName(file))
			throw new RuntimeException("Filename not acceptable!");
		if (!file.exists())
			throw new RuntimeException("The file " + filename + " does not exist!");
		if (!file.canRead())
			throw new RuntimeException("The system can not read from file " + filename + "!");
	}

	public static void checkOutput(String filename, boolean overwrite) {
		File file = new File(filename);
		if (!overwrite && file.exists())
			throw new RuntimeException("The file " + filename + " already exists!");
		if (file.exists() && (file.isDirectory() || !file.canWrite()))
			throw new RuntimeException("The system can not write in " + filename + "!");
	}

	public static void delete(File f) {
		for (File ff : f.listFiles()) {
			if (ff.isDirectory())
				delete(ff);
			else
				ff.delete();
		}
		f.delete();
	}

	private static void getAllFiles(File file, List<String> all) {
		if (!validFileName(file))
			return;
		FileTools.checkFile(file.getAbsolutePath());
		if (file.isDirectory()) {
			for (File f : file.listFiles())
				getAllFiles(f, all);
			return;
		}
		all.add(file.getAbsolutePath());
	}

	public static List<String> inputFiles(List<String> all) {
		List<String> out = Lists.newArrayList();
		for (String s : all)
			getAllFiles(new File(s), out);
		return out;
	}

	public static List<String> inputFiles(String[] all) {
		List<String> out = Lists.newArrayList();
		for (String s : all)
			getAllFiles(new File(s), out);
		return out;
	}

	public static List<String> loadList(String filename, String encoding) throws Exception {
		checkFile(filename);

		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename), encoding));
		String line = null;
		List<String> vud = Lists.newArrayList();

		while ((line = in.readLine()) != null) {
			vud.add(line);
		}
		in.close();
		return vud;
	}

	public static Map<String, Double> loadNumberMap(InputStream url) throws Exception {

		Map<String, Double> out = new HashMap<String, Double>();

		BufferedReader in = new BufferedReader(new InputStreamReader(url, "UTF-8"));
		String line = null;
		while ((line = in.readLine()) != null) {
			if (line.length() == 0)
				continue;
			line = line.trim();
			String key = line.substring(0, line.indexOf("\t"));
			String value = line.substring(line.indexOf("\t") + 1);
			out.put(key, new Double(value));
		}
		in.close();
		return out;
	}

	public static Map<String, Double> loadNumberMap(String filename, String encoding) throws Exception {
		checkFile(filename);

		Map<String, Double> out = new HashMap<String, Double>();

		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename), encoding));
		String line = null;
		while ((line = in.readLine()) != null) {
			if (line.length() == 0)
				continue;
			line = line.trim();
			String key = line.substring(0, line.indexOf("\t"));
			String value = line.substring(line.indexOf("\t") + 1);
			out.put(key, new Double(value));
		}
		in.close();
		return out;
	}

	public static Set<String> loadSet(InputStream stream) throws Exception {
		BufferedReader in = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
		String line = null;
		Set<String> vud = new HashSet<String>();

		while ((line = in.readLine()) != null) {
			vud.add(line);
		}
		in.close();

		return vud;
	}

	public static Set<String> loadSet(String filename, String encoding) throws Exception {
		checkFile(filename);
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename), encoding));
		String line = null;
		Set<String> vud = new HashSet<String>();
		while ((line = in.readLine()) != null) {
			vud.add(line.trim());
		}
		in.close();
		return vud;
	}

	public static String loadString(String filename) {
		return loadString(filename, "UTF8");
	}

	public static String loadString(String filename, String encoding) {
		checkFile(filename);

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename), encoding));
			String line = null;
			StringBuilder vud = new StringBuilder();

			while ((line = in.readLine()) != null) {
				vud.append(line + "\n");
			}
			in.close();
			return vud.toString();
		} catch (Exception e) {
			throw new RuntimeException("Could not load file " + filename);
		}
	}

	public static Map<String, String> loadStringMap(String filename, String encoding) throws Exception {

		Map<String, String> out = new HashMap<String, String>();

		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename), encoding));
		String line = null;
		while ((line = in.readLine()) != null) {
			if (line.length() == 0)
				continue;
			if (line.contains("\"User Rate Limit Exceeded\"") || !line.contains("\t"))
				continue;
			line = line.trim();
			String key = line.substring(0, line.indexOf("\t"));
			String value = line.substring(line.indexOf("\t") + 1);
			out.put(key, value);
		}
		in.close();
		return out;
	}

	public static Object read(String file) throws Exception {
		FileTools.checkFile(file);
		ObjectInputStream objIn = new ObjectInputStream(new FileInputStream(file));
		Object o = objIn.readObject();
		objIn.close();
		return o;
	}

	public static void saveString(String filename, String s, boolean overwrite) {
		saveString(filename, s, overwrite, "UTF-8");
	}

	public static void saveString(String filename, String s, boolean overwrite, String encoding) {
		try {
			checkOutput(filename, overwrite);
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), encoding));
			out.write(s);
			out.close();
		} catch (IOException e) {
			throw new RuntimeException("The system can not write in " + filename + " because:\n" + e.getMessage());
		}
	}

	public static boolean validFileName(File file) {
		return !(file.getName().startsWith(".") || file.getName().endsWith(".save") || file.getName().endsWith("#") || file
				.getName().endsWith("~"));
	}

	public static void write(Object o, String file, boolean overwrite) throws Exception {
		FileTools.checkOutput(file, overwrite);
		ObjectOutputStream objOut = new ObjectOutputStream(new FileOutputStream(file));
		objOut.writeObject(o);
		objOut.close();
	}
}
