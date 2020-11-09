package mdgoassembler.utils;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.jdbc.Work;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class HibernateUtils implements MsgBox {
    
	private static SessionFactory sessionFactory;
	private static final ThreadLocal<Session> threadlocal=new ThreadLocal<Session>();

	static{
		try {
			sessionFactory = HibernateSessionFactoryUtil.getSessionFactory();
		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public static Session getSession() throws HibernateException{
		Session session=threadlocal.get();
		if (session==null || !session.isOpen()) {
			if (sessionFactory==null) {
				rebuildSessionFactory();
			}
			session=(sessionFactory!=null)?sessionFactory.openSession():null;
			threadlocal.set(session);
		}
		return session;  
	}

	public static void closeSession() throws HibernateException{
		Session session=threadlocal.get();
		threadlocal.set(null);
		if (session!=null) {
			session.close();
		}
	}
	
	public static void rebuildSessionFactory(){
		try {
		 Configuration configuration= new Configuration().configure();
		 sessionFactory=configuration.buildSessionFactory();
		} catch (Exception e) {
			MsgBox.msgException(e);
		}
	}
	
	public static List<?> findAllRecords(Object o) {	
		Session session = getSession();
	    CriteriaBuilder cb = session.getCriteriaBuilder();
	    CriteriaQuery<Object> cq = (CriteriaQuery<Object>) cb.createQuery(o.getClass());
	    Root<Object> rootEntry = (Root<Object>) cq.from(o.getClass());
	    CriteriaQuery<Object> all = cq.select(rootEntry); 
	    TypedQuery<Object> allQuery = session.createQuery(all);
	    return allQuery.getResultList();
	}

	public static void shutdown(){
		getSessionFactory().close();
	}

	
}
