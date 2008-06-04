package com.mesh4j.sync.adapters.dom.parsers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.QName;

import com.mesh4j.sync.adapters.dom.IMeshDOM;
import com.mesh4j.sync.adapters.dom.MeshDOM;
import com.mesh4j.sync.adapters.dom.MeshNames;
import com.mesh4j.sync.parsers.IXMLViewElement;
import com.mesh4j.sync.utils.XMLHelper;
import com.mesh4j.sync.validations.Guard;

public class FileXMLViewElement implements IXMLViewElement, IDOMRequied{

	// MODEL VARIABLES
	private IMeshDOM dom;
	private IFileManager fileManager;
	
	// BUSINESS METHODS
	public FileXMLViewElement(IFileManager fileManager)	{
		Guard.argumentNotNull(fileManager, "fileManager");
		this.fileManager = fileManager;
	}
	
	@Override
	public Element add(Document document, Element newElement) {
		Guard.argumentNotNull(dom, "dom");
		Guard.argumentNotNull(document, "document");
		Guard.argumentNotNull(newElement, "element");
		
		Element syncRepo = this.dom.getSyncRepository(document);
		Guard.argumentNotNull(syncRepo, "syncRepo");		
		
		String syncID = newElement.attributeValue(MeshNames.MESH_QNAME_SYNC_ID);
		Guard.argumentNotNullOrEmptyString(syncID, "syncID");
		
		String fileName = newElement.attributeValue(MeshNames.MESH_QNAME_FILE_ID);
		Guard.argumentNotNull(fileName, "fileName");
		
		Element elementToAdd = newElement.createCopy();		
		Element fileContent = elementToAdd.element(MeshNames.MESH_QNAME_FILE_CONTENT);
		Guard.argumentNotNull(fileContent, "fileContent");
		
		elementToAdd.remove(fileContent);
		fileManager.setFileContent(fileName, fileContent.getText());
		syncRepo.add(elementToAdd);
		return newElement;
	}

	@Override
	public void delete(Document document, Element element) {
		Guard.argumentNotNull(document, "document");
		Guard.argumentNotNull(element, "element");
		
		String fileName = element.attributeValue(MeshNames.MESH_QNAME_FILE_ID);
		Guard.argumentNotNull(fileName, "fileName");
		Guard.argumentNotNull(element.getParent(), "parent");
		
		fileManager.removeFileContent(fileName);
		element.getParent().remove(element);
	}

	@Override
	public List<Element> getAllElements(Document document) {
		Guard.argumentNotNull(dom, "dom");
		Guard.argumentNotNull(document, "document");
		Guard.argumentNotNull(document.getRootElement(), "document.root");
		
		ArrayList<Element> result = new ArrayList<Element>(); 
		
		Map<String, String> files = fileManager.getFileContents();
		
		List<Element> fileElements = XMLHelper.selectElements("//mesh4x:file", document.getRootElement(), MeshDOM.SEARCH_NAMESPACES);
		for (Element element : fileElements) {
			String fileName = element.attributeValue(MeshNames.MESH_QNAME_FILE_ID);
			if(fileName == null){
				element.getParent().remove(element);
			} else {
				Element refreshElement = this.refresh(document, element);
				if(refreshElement != null){
					result.add(refreshElement);
				}
				files.remove(fileName);
			}
		}
	
		Element elementRoot = this.dom.getSyncRepository(document);
		for (String fileName : files.keySet()) {
			Element newFileElement = elementRoot.addElement(MeshNames.MESH_QNAME_FILE);
			newFileElement.addAttribute(MeshNames.MESH_QNAME_SYNC_ID, this.dom.newID());
			newFileElement.addAttribute(MeshNames.MESH_QNAME_FILE_ID, fileName);
			
			Element refreshElement = this.refresh(document, newFileElement);
			if(refreshElement != null){
				result.add(refreshElement);
			}
		}
		
		return result;
	}

	@Override
	public String getName() {
		return MeshNames.MESH_QNAME_FILE.getName();
	}

	@Override
	public QName getQName() {
		return MeshNames.MESH_QNAME_FILE;
	}

	@Override
	public boolean isValid(Document document, Element element) {
		Guard.argumentNotNull(document, "document");
		Guard.argumentNotNull(element, "element");

		String fileName = element.attributeValue(MeshNames.MESH_QNAME_FILE_ID);
		return MeshNames.MESH_QNAME_FILE.equals(element.getQName()) && fileName != null;
	}

	@Override
	public Element normalize(Element element) {
		if(element != null && !this.getQName().equals(element.getQName())){
			return null;
		} else {
			return element;
		}
	}

	@Override
	public Element refresh(Document document, Element element) {
		Guard.argumentNotNull(document, "document");
		Guard.argumentNotNull(element, "element");
		
		String fileName = element.attributeValue(MeshNames.MESH_QNAME_FILE_ID);
		Guard.argumentNotNull(fileName, "fileName");

		String fileContent = fileManager.getFileContent(fileName);		
		if(fileContent == null){
			this.delete(document, element);
			return null;
		} else { 
			String actualFileContent = element.elementText(MeshNames.MESH_QNAME_FILE_CONTENT);
			if(!fileContent.equals(actualFileContent)){
				Element elementCopy = element.createCopy();
				Element fileContentElement = elementCopy.element(MeshNames.MESH_QNAME_FILE_CONTENT);
				if(fileContentElement == null){
					fileContentElement = elementCopy.addElement(MeshNames.MESH_QNAME_FILE_CONTENT);
				}
				fileContentElement.setText(fileContent);
				return elementCopy;
			} else {
				return element;
			}
		}
	}

	@Override
	public Element update(Document document, Element element, Element newElement) {
		Guard.argumentNotNull(document, "document");
		Guard.argumentNotNull(element, "element");
		Guard.argumentNotNull(newElement, "newElement");
		
		String fileName = newElement.attributeValue(MeshNames.MESH_QNAME_FILE_ID);
		Guard.argumentNotNull(fileName, "fileName");

		Element newFileContent = newElement.element(MeshNames.MESH_QNAME_FILE_CONTENT);
		Guard.argumentNotNull(newFileContent, "newFileContent");
		Guard.argumentNotNullOrEmptyString(newFileContent.getText(), "newFileContent");
		
		Attribute fileIDAttr = element.attribute(MeshNames.MESH_QNAME_FILE_ID);					
		if(fileIDAttr == null){
			element.addAttribute(MeshNames.MESH_QNAME_FILE_ID, fileName);
		} else if(!fileIDAttr.getValue().equals(fileName)){
			fileIDAttr.setValue(fileName);
		}
				
		fileManager.setFileContent(fileName, newFileContent.getText());
		
		return newElement;
	}

	@Override
	public void setDOM(IMeshDOM dom) {
		this.dom = dom;		
	}	
}
