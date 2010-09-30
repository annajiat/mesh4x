package org.mesh4j.sync.adapters.hibernate.mapping;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.List;

import org.mesh4j.sync.payload.schema.rdf.IRDFSchema;

public class MappingGenerator {
	
	public static void createMapping(IRDFSchema rdfSchema, String mappingFileName) throws Exception{

		FileWriter writer = new FileWriter(mappingFileName);
		
		MappingGenerator.writeHeader(writer);
		MappingGenerator.writerClass(writer, rdfSchema.getOntologyNameSpace(), rdfSchema.getOntologyNameSpace());
		
		List<String> ids = rdfSchema.getIdentifiablePropertyNames();
		
		if(ids.size() == 1){
			String idColumnName = ids.get(0);
			String propertyType = rdfSchema.getPropertyType(idColumnName);
			MappingGenerator.writeID(writer, idColumnName, idColumnName, getHibernateTypeFromXSD(propertyType));
		} else if (ids.size() > 1){
			writer.write("\n");
			writer.write("\t\t");
			writer.write("<composite-id name=\"id\">");	
			
			for (String id : ids) {
				String propertyType = rdfSchema.getPropertyType(id);
				String idType = getHibernateTypeFromXSD(propertyType);
				writer.write("\n");
				writer.write("\t\t\t");
				writer.write(MessageFormat.format("<key-property name=\"{0}\" node=\"{0}\" type=\"{1}\">", id, idType));
				writer.write("\n");
				writer.write("\t\t\t\t");
				writer.write(MessageFormat.format("<column name=\"{0}\"/>", id));
				writer.write("\n");
				writer.write("\t\t\t");
				writer.write("</key-property>");
			}
			writer.write("\n");
			writer.write("\t\t");
			writer.write("</composite-id>");	
		}
		
		int size = rdfSchema.getPropertyCount();
		String propertyName, propertyType;
		
		for (int i = 0; i < size; i++) {
			propertyName = rdfSchema.getPropertyName(i);
			if (!ids.contains(propertyName)) {
				propertyType = rdfSchema.getPropertyType(propertyName);
				MappingGenerator.writeProperty(writer, propertyName, propertyName, getHibernateTypeFromXSD(propertyType));
			}
		}
		MappingGenerator.writerFooter(writer);
		
		try{
			writer.flush();
		}finally{
			writer.close();
		}
	}
	
	public static String getHibernateTypeFromXSD(String propertyType) {
		if(IRDFSchema.XLS_STRING.equals(propertyType)){
			return "string";
		}else if(IRDFSchema.XLS_BOOLEAN.equals(propertyType)){
			return "byte";
		} else if(IRDFSchema.XLS_INTEGER.equals(propertyType)){
			return "integer";
		}  else if(IRDFSchema.XLS_LONG.equals(propertyType)){
			return "long";
		}  else if(IRDFSchema.XLS_DOUBLE.equals(propertyType)){
			return "double";
		} else if(IRDFSchema.XLS_DATETIME.equals(propertyType)){
			return "timestamp";
		} else {
			return null;
		}
	}
	
	public static void createSyncInfoMapping(String mappingFileName, String tableName) throws IOException{

		StringWriter writer = new StringWriter();
		writeHeader(writer);
		writerClass(writer, tableName, tableName);
		writeID(writer, "sync_id", "sync_id", "string");
		writeProperty(writer, "entity_name", "entity_name", "string");
		writeProperty(writer, "entity_id", "entity_id", "string");
		writeProperty(writer, "entity_version", "entity_version", "string");
		writeProperty(writer, "sync_data", "sync_data", "string");
		writerFooter(writer);
				
		File mappingFile = new File(mappingFileName);
		FileWriter fileWriter = new FileWriter(mappingFile);
		try{
			writer.flush();
			fileWriter.write(writer.toString());
		} finally{
			fileWriter.close();
		}
	}

	public static void writeHeader(Writer writer) throws IOException {
		writer.write("<?xml version=\"1.0\"?><!DOCTYPE hibernate-mapping PUBLIC");
		writer.write(" \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\" ");
		writer.write("\"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">");
		writer.write("\n");
		writer.write("<hibernate-mapping>");
	}

	public static void writerClass(Writer writer, String entityName, String tableName) throws IOException {
		writer.write("\n");
		writer.write("\t");
		writer.write(MessageFormat.format("<class entity-name=\"{0}\" node=\"{0}\" table=\"{1}\">", entityName, tableName));
	}
	
	public static void writeID(Writer writer, String idName, String idColumnName, String idType) throws IOException {
		writer.write("\n");
		writer.write("\t\t");
		writer.write(MessageFormat.format("<id name=\"{0}\" type=\"{1}\" column=\"{2}\">", idName, idType, idColumnName));
		writer.write("\n");
		writer.write("\t\t\t");
		writer.write("<generator class=\"assigned\"/>");
		writer.write("\n");
		writer.write("\t\t");
		writer.write("</id>");
	}
	
	public static void writeProperty(Writer writer, String name, String column, String type) throws IOException {
		writer.write("\n");
		writer.write("\t\t");
		writer.write(MessageFormat.format("<property name=\"{0}\" column=\"{1}\" node=\"{0}\" type=\"{2}\"/>", name, column, type));
	}

	public static void writeProperty(Writer writer, String name, String column, String type, String length) throws IOException {
		writer.write("\n");
		writer.write("\t\t");
		writer.write(MessageFormat.format("<property name=\"{0}\" column=\"{1}\" node=\"{0}\" type=\"{2}\" length=\"{3}\"/>", name, column, type, length));
	}
	
	public static void writerFooter(Writer writer) throws IOException {
		writer.write("\n");
		writer.write("\t");
		writer.write("</class>");
		writer.write("\n");
		writer.write("</hibernate-mapping>");
	}
}
