package mat.dao.impl;

import java.util.ArrayList;
import java.util.List;

import mat.DTO.AuthorDTO;
import mat.dao.search.GenericDAO;
import mat.model.Author;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class AuthorDAO extends GenericDAO<Author, String> implements mat.dao.AuthorDAO {
	
	private static final Log logger = LogFactory.getLog(AuthorDAO.class);
	
	public List<AuthorDTO> getAllAuthors(){
		
		List<AuthorDTO> AuthorDTOList = new ArrayList<AuthorDTO>();
		logger.info("Getting all the rows from the Steward table");
		Session session = getSessionFactory().getCurrentSession();

		@SuppressWarnings("unchecked")
		List<Author> AuthorList = session.createCriteria(Author.class).list();
		for(Author author: AuthorList){
			AuthorDTO authorDTO =  new AuthorDTO();			
			authorDTO.setAuthorName(author.getAuthorName());
			authorDTO.setId(author.getId());
			AuthorDTOList.add(authorDTO);
		}
		return AuthorDTOList;
	}
}
