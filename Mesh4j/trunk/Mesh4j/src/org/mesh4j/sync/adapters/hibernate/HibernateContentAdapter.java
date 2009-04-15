package org.mesh4j.sync.adapters.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dom4j.Element;
import org.hibernate.EntityMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.mesh4j.sync.ISupportSchema;
import org.mesh4j.sync.adapters.hibernate.mapping.IHibernateToXMLMapping;
import org.mesh4j.sync.adapters.split.IIdentifiableContentAdapter;
import org.mesh4j.sync.model.IContent;
import org.mesh4j.sync.payload.schema.ISchema;
import org.mesh4j.sync.validations.Guard;
import org.mesh4j.sync.validations.MeshException;

public class HibernateContentAdapter implements IIdentifiableContentAdapter, ISupportSchema {

	// MODEL VARIABLES
	private IHibernateToXMLMapping mapping;
	private SessionFactory sessionFactory;
	private String entityName;
	
	// BUSINESS METHODS
	
	public HibernateContentAdapter(IHibernateSessionFactoryBuilder sessionFactoryBuilder, String entityName){
		Guard.argumentNotNull(sessionFactoryBuilder, "sessionFactoryBuilder");
		Guard.argumentNotNullOrEmptyString(entityName, "entityName");
		
		this.entityName = entityName;
		initializeSessionFactory(sessionFactoryBuilder);
	}

	public void initializeSessionFactory(IHibernateSessionFactoryBuilder sessionFactoryBuilder) {
		if(this.sessionFactory != null){
			this.sessionFactory.close();
		}
		
		this.sessionFactory = sessionFactoryBuilder.buildSessionFactory();
		this.mapping = sessionFactoryBuilder.buildMeshMapping(this.entityName);
	}

	@Override
	public EntityContent get(String entityId) {
		Session session = this.sessionFactory.openSession();
		Session dom4jSession = session.getSession(EntityMode.DOM4J);
		Element entityElement = (Element) dom4jSession.get(this.getType(), entityId);
		session.close();
		
		if(entityElement == null){
			return null;
		} else {
			return new EntityContent(convertRowToXML(entityId, entityElement), this.getType(), entityId);
		}
	}

	public void save(IContent content) {
	
		Session session =  this.sessionFactory.openSession();
		Transaction tx = null;
		try{
			tx = session.beginTransaction();
			Session dom4jSession = session.getSession(EntityMode.DOM4J);
			dom4jSession.saveOrUpdate(convertXMLToRow(content.getPayload()));
			tx.commit();
		}catch (RuntimeException e) {
			if (tx != null) {
				tx.rollback();
			}
			throw e;
		}finally{
			session.close();
		}
	}

	public void delete(IContent content) {
		Session session =  this.sessionFactory.openSession();
		Session dom4jSession = session.getSession(EntityMode.DOM4J);
		Element entityElement = (Element) dom4jSession.get(this.getType(), content.getId());
		if(entityElement == null){
			session.close();
			return;
		}
		
		Transaction tx = null;
		try{
			tx = session.beginTransaction();
			dom4jSession.delete(this.getType(), entityElement);
			tx.commit();
		}catch (RuntimeException e) {
			if (tx != null) {
				tx.rollback();
			}
			throw e;
		}finally{
			session.close();
		}
	}

	@SuppressWarnings("unchecked")
	public List<IContent> getAll(Date since) {
		String hqlQuery ="FROM " + this.getType();
		Session session = this.sessionFactory.openSession();
		Session dom4jSession = session.getSession(EntityMode.DOM4J);		
		List<Element> entities = dom4jSession.createQuery(hqlQuery).list();
		session.close();
		
		ArrayList<IContent> result = new ArrayList<IContent>();
		for (Element entityElement : entities) {
			String entityID = entityElement.element(getEntityIdNode()).getText();
			EntityContent entity = new EntityContent(convertRowToXML(entityID, entityElement), this.getType(), entityID);
			result.add(entity);
		}
		return result;
	}
	
	public String getType() {
		return this.entityName;
	}
	
	private Element convertXMLToRow(Element payload) {
		try{
			return this.mapping.convertXMLToRow(payload.createCopy());
		}catch (Exception e) {
			throw new MeshException(e);
		}
	}

	private Element convertRowToXML(String id, Element entityElement){
		try{
			return this.mapping.convertRowToXML(id, entityElement);
		}catch (Exception e) {
			throw new MeshException(e);
		}
	}
	
	private String getEntityIdNode() {
		return this.mapping.getIDNode();
	}

	public List<IContent> getAll() {
		return getAll(null);
	}

	@Override
	public String getID(IContent content) {
		EntityContent entityContent = EntityContent.normalizeContent(content, this.getType(), this.getEntityIdNode());
		if(entityContent == null){
			return null;
		} else {
			return entityContent.getId();
		}
	}

	public IHibernateToXMLMapping getMapping() {
		return mapping;		
	}

	@Override
	public ISchema getSchema() {
			return this.mapping.getSchema();
	}

}
