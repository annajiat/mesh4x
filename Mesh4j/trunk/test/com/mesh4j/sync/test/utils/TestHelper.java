package com.mesh4j.sync.test.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.mesh4j.sync.adapters.kml.KmlNames;
import com.mesh4j.sync.utils.IdGenerator;
import com.mesh4j.sync.utils.XMLHelper;
import com.mesh4j.sync.utils.ZipUtils;
import com.mesh4j.sync.validations.MeshException;

public class TestHelper {

	private static long LAST_ID = 0;
	private static Random ID_GENERATOR = new Random();

	public synchronized static String newID() {
		int currentID = random();
		while (LAST_ID == currentID) {
			currentID = random();
		}
		LAST_ID = currentID;
		return String.valueOf(LAST_ID);
	}

	private static int random() {
		int i = ID_GENERATOR.nextInt();
		if (i < 0) {
			i = i * -1;
		}
		return i;
	}

	public static Element makeElement(String xmlAsString) {
		Document doc;
		try {
			doc = DocumentHelper.parseText(xmlAsString);
		} catch (DocumentException e) {
			throw new IllegalArgumentException(e);
		}
		return doc.getRootElement();
	}

	public static Date now() {
		return new Date();
	}

	public static Date nowSubtractMinutes(int i) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, (-1 * i));
		return cal.getTime();
	}

	public static Date nowSubtractHours(int i) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, (-1 * i));
		return cal.getTime();
	}

	public static Date nowAddMinutes(int i) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, i);
		return cal.getTime();
	}

	public static Date nowAddDays(int i) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, i);
		return cal.getTime();
	}

	public static Date nowAddSeconds(int i) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, i);
		return cal.getTime();
	}

	public static Date nowSubtractSeconds(int i) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, (-1 * i));
		return cal.getTime();
	}

	public static Date makeDate(int year, int month, int day, int hour,
			int minute, int second, int millisecond) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DATE, day);
		cal.set(Calendar.HOUR, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MILLISECOND, millisecond);
		return cal.getTime();
	}

	public static Date nowSubtractDays(int i) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, (-1 * i));
		return cal.getTime();
	}

	public static void sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static String fileName(String name) {
		return "D:\\temp_dev\\mesh4j\\" + name;
	}

	public static File makeNewXMLFile(String xml) {
		return makeNewXMLFile(xml, ".xml");
	}
	
	public static File makeNewXMLFile(String xml, String fileExtension) {
		File file = new File(TestHelper.fileName(IdGenerator.newID() + fileExtension));
		XMLHelper.write(xml, file);
		return file;
	}

	public static String readFileContent(String fileName) throws IOException {

		StringBuffer contents = new StringBuffer();

		BufferedReader input = new BufferedReader(new FileReader(new File(
				fileName)));
		try {
			String line = null;
			while ((line = input.readLine()) != null) {
				contents.append(line);
				contents.append(System.getProperty("line.separator"));
			}
		} finally {
			input.close();
		}
		return contents.toString();

	}
	
	public static byte[] readFileBytes(String fileName) throws IOException {
		InputStream reader = new FileInputStream(fileName);
		BufferedInputStream bis = new BufferedInputStream(reader);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
	    
	    final int N = 1024;
	    byte buf[] = new byte[N];
	    int ln = 0;
	    while ((ln = bis.read(buf, 0, N)) != -1) {
	        os.write(buf, 0, ln);
	    }
	    bis.close();
		reader.close();
		os.flush();
		return os.toByteArray();
	}

	public static void writeFile(String fileName, String contents) throws IOException{
		Writer output = new BufferedWriter(new FileWriter(new File(fileName)));
		try {
			output.write(contents);
		} finally {
			output.close();
		}
	}

	public static File makeNewKMZFile(String xml) {
		File file = new File(fileName(IdGenerator.newID() + ".kmz"));
		try {
			ZipUtils.write(file, KmlNames.KMZ_DEFAULT_ENTRY_NAME_TO_KML, xml);
		} catch (IOException e) {
			throw new MeshException(e);
		}
		return file;
	}

	public static Document readKMZDocument(File file) {
		try{
			String xml = ZipUtils.getTextEntryContent(file, KmlNames.KMZ_DEFAULT_ENTRY_NAME_TO_KML);
			return DocumentHelper.parseText(xml);
		} catch(Exception e){
			throw new MeshException(e);
		}
	}
}
