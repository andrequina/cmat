package mat.dao.impl.clause;

import java.util.List;

import mat.dao.search.GenericDAO;
import mat.model.clause.AttributeDetails;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class AttributeDetailsDAO extends GenericDAO<AttributeDetails, String> implements mat.dao.clause.AttributeDetailsDAO {


	public AttributeDetails findByName(String attrName) {
		
		Session session = getSessionFactory().getCurrentSession();
		Criteria criteria = session.createCriteria(AttributeDetails.class);
		criteria.add(Restrictions.eq("attrName", attrName));
		List<AttributeDetails> attributeDetailsList = criteria.list(); 
		if(attributeDetailsList.isEmpty())
			return null;
		return attributeDetailsList.get(0);
	}
	
}
