package com.css;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class Css {
	FileReader fileReader = null;
	File file = null;
	BufferedReader br = null;
	static HashMap<String, HashMap<String, String>> elementMap = null;
	HashMap<String, String> elementPropertyMap = null;

	PrintWriter overrideFile = null;
	PrintWriter differenceFile = null;
	PrintWriter duplicateFile = null;

	Css(String cssFileName) {
		elementMap = new HashMap<String, HashMap<String, String>>();
		try {
			overrideFile = new PrintWriter(new BufferedWriter(new FileWriter(
					cssFileName+"_override.css", true)));
			differenceFile = new PrintWriter(new BufferedWriter(new FileWriter(
					cssFileName+"_difference.css", true)));
			duplicateFile = new PrintWriter(new BufferedWriter(new FileWriter(
					cssFileName+"_duplicate.css", true)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		String cssFileName = "css1.css";
		Css css = new Css(cssFileName);
		css.readCssFile(cssFileName);
	}

	private void readElementProperties(String masterKey, String line,
			int openBrace) {
		String masterValue = null;
		if (line.indexOf("}") != -1) {
			masterValue = line.substring(openBrace, line.length());
			masterValue = masterValue.substring(masterValue.indexOf("{") + 1,
					masterValue.indexOf("}"));
			// System.out.println("Master value:" + masterValue);
		}
	}

	private void readCssFile(String cssFileName) throws FileNotFoundException, IOException {
		File file = new File(cssFileName);
		// file = new File("css2.css");
		// System.out.println(file.getCanonicalPath());
		fileReader = new FileReader(file.getCanonicalPath());
		br = new BufferedReader(fileReader);
		String line;
		// String masterValue = null;
		String masterKey = null;
		boolean bMasterKey = false;
		boolean bDifference = false;
		boolean bDuplicate = false;
		boolean bOverride = false;

		while ((line = br.readLine()) != null) {
			if (line.length() == 0)
				continue;
			line = line.trim();
			if (line.startsWith("/*"))
				continue;
			// System.out.println(line);
			int openBrace = line.indexOf("{");
			if (openBrace != -1) { // if open brace found means with element
				masterKey = line.substring(0, openBrace);
				int closeBrace = line.indexOf("}");
				if (closeBrace != -1) // means the properties also in the same
										// line
				{
					readElementProperties(masterKey, line, openBrace);
				} else {
					// if element already found then get those properties
					if (elementMap.get(masterKey) != null) {
						elementPropertyMap = elementMap.get(masterKey);
						bMasterKey = true;
					} else {
						elementPropertyMap = new HashMap<String, String>();
						bMasterKey = false;
					}
				}
				// System.out.println("Master Key : " + masterKey);
			} else {
				int closeBrace = line.indexOf("}");
				if (closeBrace == -1) {
					// open brace is not found
					String[] properties = line.split(";");
					// System.out.println(line);
					if (properties.length == 1) { // only one property value per
													// line
						String[] keyValue = properties[0].split(":");

						if (elementPropertyMap.containsKey(keyValue[0])) {
							String tmpValue = elementPropertyMap
									.get(keyValue[0]);
							if (!tmpValue.equals(keyValue[1]) && bMasterKey) {
								if (!bOverride) {
									bOverride = true;
									overrideFile.println(masterKey + "{");
								}
								// if both values are not equal then add it into
								// override.css
								// System.out.println("Override :> Element :"+
								// masterKey + "{" + keyValue[0] + ":" +
								// keyValue[1] +"}");
								overrideFile.println(keyValue[0] + ": "
										+ keyValue[1] + ";");
							} else {
								if (!bDuplicate) {
									bDuplicate = true;
									duplicateFile.println(masterKey + "{");
								}
								duplicateFile.println(keyValue[0] + ": "
										+ keyValue[1] + ";");
							}
						} else if (bMasterKey) {
							// System.out.println("Difference :> Element :"+
							// masterKey + "{" + keyValue[0] + ":" + keyValue[1]
							// +"}");
							if (!bDifference) {
								bDifference = true;
								differenceFile.println(masterKey + "{");
							}
							differenceFile.println(keyValue[0] + ": "
									+ keyValue[1] + ";");
						} else {
							// if key is not present already then simply add it
							elementPropertyMap.put(keyValue[0], keyValue[1]);
						}
					}
				} else {
					// if close brace found means
					elementMap.put(masterKey, elementPropertyMap);
					if (bDifference) {
						differenceFile.println("}");
						bDifference = false;
					}

					if (bDuplicate) {
						duplicateFile.println("}");
						bDuplicate = false;
					}

					if (bOverride) {
						overrideFile.println("}");
						bOverride = false;
					}

				}
			}
		}
		System.out.println("Element Map " + elementMap);
		System.out.println("Element Property Map " + elementPropertyMap);
		br.close();
		overrideFile.close();
		differenceFile.close();
		duplicateFile.close();
	}
}
