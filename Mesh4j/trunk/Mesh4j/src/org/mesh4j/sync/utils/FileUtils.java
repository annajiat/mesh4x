package org.mesh4j.sync.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.mesh4j.sync.validations.MeshException;

public class FileUtils {

	
	public static byte[] read(String fileName) throws IOException {
		return read(new File(fileName));
	}

	public static byte[] read(File file) throws IOException {
		return read(new FileInputStream(file));
	}
	
	public static byte[] read(InputStream reader) throws IOException {
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

	public static void write(String fileName, byte[] bytes) throws IOException{		
		write(new File(fileName), bytes);
	}
	
	public static void write(File file, byte[] bytes) throws IOException{		
		FileOutputStream output = new FileOutputStream(file);
		try {
			output.write(bytes);
			output.flush();
		} finally {
			output.close();
		}
	}
	
	public static String getFileName(String folderName, String fileName) {
		if(folderName.endsWith(File.separator)){
			return folderName + fileName;
		} else {
			return folderName + File.separator + fileName;
		}
	}

	public static File getFile(String folderName, String fileName) {
		return new File(getFileName(folderName, fileName));
	}

	public static URL getResourceFileURL(String fileName) {
		return ClassLoader.getSystemClassLoader().getResource(fileName);
	}		
	
	public static void delete(String fileName) {
		delete(new File(fileName));
	}
	
	public static void delete(File file) {
		if(file.exists()){
			if(file.isDirectory()){
				File[] files = file.listFiles();
				for (File f : files) {
					delete(f);
				}
			}
			file.delete();
		} 
	}

	/**
	 * Cleanup files from a directory
	 * 
	 * @param dirName
	 */
	public static void cleanupDirectory(String dirName) {
		File directory = new File(dirName);
		if (directory.isDirectory()) {
			for (File file : directory.listFiles()) {
				if (file.isFile() && !file.delete()) {
					throw new MeshException("Failed to delete " + file + " in directory "+ dirName);
				}
			}
		}
	}	
	
	public static String getFileNameWithOutExtension(File file) {
		String name = file.getName();
		int i = name.indexOf(".");
		int size = name.length();
		
		if(i > -1){
			size = i;
		}
		return name.substring(0, size);
	}
	
	public static void copyFile(String sourceFileName, String targetFileName) throws IOException{
		byte[] bytes = FileUtils.read(sourceFileName);
		FileUtils.write(new File(targetFileName), bytes);
	}
}
