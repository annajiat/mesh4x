package org.mesh4j.ektoo.ui.component.treetable.xmltreetable;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import org.mesh4j.ektoo.ui.component.treetable.TreeTableModel;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

 
public class JXmlTreeTableModel implements TreeTableModel{ 
    private Node node; 
 
    public JXmlTreeTableModel(InputSource is) throws Exception{ 
        this(TreeElementProvider.createDocument(is).getDocumentElement()); 
    } 
 
    public JXmlTreeTableModel(Node node){ 
        this.node = node; 
    } 
 
    public Object getRoot(){ 
        return node; 
    } 
 
    public Object getChild(Object parent, int index){ 
        Node node = (Node)parent; 
        NamedNodeMap attrs = node.getAttributes(); 
        int attrCount = attrs!=null ? attrs.getLength() : 0; 
        if(index<attrCount) 
            return attrs.item(index); 
        NodeList children = node.getChildNodes(); 
        return children.item(index - attrCount); 
    } 
 
    public int getChildCount(Object parent){ 
        Node node = (Node)parent; 
        NamedNodeMap attrs = node.getAttributes(); 
        int attrCount = attrs!=null ? attrs.getLength() : 0; 
        NodeList children = node.getChildNodes(); 
        int childCount = children!=null ? children.getLength() : 0; 
        if(childCount==1 && children.item(0).getNodeType()==Node.TEXT_NODE) 
            return attrCount; 
        else 
            return attrCount + childCount; 
    } 
 
    public boolean isLeaf(Object node){ 
        return getChildCount(node)==0; 
    } 
 
    public int getIndexOfChild(Object parent, Object child){ 
        Node node = (Node)parent; 
        Node childNode = (Node)child; 
 
        NamedNodeMap attrs = node.getAttributes(); 
        int attrCount = attrs!=null ? attrs.getLength() : 0; 
        if(childNode.getNodeType()==Node.ATTRIBUTE_NODE){ 
            for(int i=0; i<attrCount; i++){ 
                if(attrs.item(i)==child) 
                    return i; 
            } 
        }else{ 
            NodeList children = node.getChildNodes(); 
            int childCount = children!=null ? children.getLength() : 0; 
            for(int i=0; i<childCount; i++){ 
                if(children.item(i)==child) 
                    return attrCount + i; 
            } 
        } 
        throw new RuntimeException("this should never happen!"); 
    } 
 
    public void addTreeModelListener(TreeModelListener listener){ 
        // not editable 
    	//implement later
    } 
 
    public void removeTreeModelListener(TreeModelListener listener){ 
        // not editable 
    	//implement later
    } 
 
    public void valueForPathChanged(TreePath path, Object newValue){ 
        // not editable 
    	//implement later
    } 
 
    public int getColumnCount(){ 
        return 2; 
    } 
 
    public String getColumnName(int column){ 
        return column==0 ? "Node" : "Value"; 
    } 
 
    public Class getColumnClass(int column){ 
        return column==0 ? TreeTableModel.class : String.class; 
    } 
 
    public Object getValueAt(Object node, int column){ 
        if(column==0) 
            return node; 
        else{ 
            Node n = (Node)node; 
            if(n.getNodeType()==Node.ELEMENT_NODE){ 
                NodeList children = n.getChildNodes(); 
                int childCount = children!=null ? children.getLength() : 0; 
                if(childCount==1 && children.item(0).getNodeType()==Node.TEXT_NODE) 
                    return children.item(0).getNodeValue(); 
            } 
            return n.getNodeValue(); 
        } 
    } 
 
    public boolean isCellEditable(Object node, int column){ 
        return false; 
    } 
 
    public void setValueAt(Object aValue, Object node, int column){ 
        // not editable 
    } 
}



