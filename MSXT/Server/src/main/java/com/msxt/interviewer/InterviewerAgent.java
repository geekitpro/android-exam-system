package com.msxt.interviewer;

import java.util.List;

import javax.faces.bean.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.xml.stream.XMLStreamException;

import org.jboss.seam.international.status.Messages;
import org.jboss.seam.international.status.builder.BundleKey;

import com.msxt.model.Interview;
import com.msxt.model.Interview_;
import com.msxt.model.Interviewer;
import com.msxt.model.Interviewer_;

@Named
@RequestScoped
public class InterviewerAgent {
	@PersistenceContext
    private EntityManager em;
	
	@Inject
	private Messages messages;
	
	private Interviewer selectedInterviewer  = new Interviewer();
	
	public void selectInterviewer(String id){
		selectedInterviewer = em.find( Interviewer.class, id );
	}

	public Interviewer getSelectedInterviewer() {
		return selectedInterviewer;
	}
	
	public String save() throws XMLStreamException {
		if( verifyIdCodeIsAvailable() ) {
			Interviewer ie = em.find( Interviewer.class, selectedInterviewer.getId() );
			ie.setName( selectedInterviewer.getName() );
			ie.setIdCode( selectedInterviewer.getIdCode() );
			ie.setPhone( selectedInterviewer.getPhone() );
			ie.setAge( selectedInterviewer.getAge() );
			em.persist( ie );

			return "search?faces-redirect=true";
		} else {
			return null;
		}
	}
	
	public String delete(String id){
		Interviewer iver = em.find( Interviewer.class, id );
		
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Interview> cq = cb.createQuery( Interview.class );
		Root<Interview> rp =  cq.from( Interview.class );
		cq.select( rp ).where( cb.equal( rp.get( Interview_.interviewer), iver) );
		
		List<Interview> result = em.createQuery( cq ).getResultList();
		if( result.size()==0 ) {
			em.remove( iver );
			return "search?faces-redirect=true";
		} else { 
			messages.error( new BundleKey("messages", "msxt_interviewer_exist_interview") )
                    .params( iver.getName() );
			return "search";
		}
	}
	
	private boolean verifyIdCodeIsAvailable() {
    	CriteriaBuilder cb = em.getCriteriaBuilder();
    	
    	CriteriaQuery<Interviewer> cq = cb.createQuery(Interviewer.class);
    	Root<Interviewer> fr = cq.from(Interviewer.class);
    	cq.select( fr ).where( cb.notEqual(fr.get(Interviewer_.id), selectedInterviewer.getId() ), 
    			               cb.equal( fr.get(Interviewer_.idCode), selectedInterviewer.getIdCode() ) );
    	
    	TypedQuery<Interviewer> q = em.createQuery( cq );
    	List<Interviewer> existing = q.getResultList();
    	
        if ( existing != null && existing.size()>0 ) {
            messages.error( new BundleKey("messages", "account_usernameTaken") )
                    .params( selectedInterviewer.getName() );
            return false;
        }

        return true;
    }
}
