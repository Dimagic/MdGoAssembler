package mdgoassembler.dao;

import mdgoassembler.utils.CustomException;

import mdgoassembler.utils.HibernateSessionFactoryUtil;
import mdgoassembler.utils.MsgBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.postgresql.util.PSQLException;

import javax.persistence.PersistenceException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public interface UniversalDao {
    Logger LOGGER = LogManager.getLogger(UniversalDao.class.getName());

    default boolean save(Object obj) throws CustomException {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        try {
            session.save(obj);
            tx1.commit();
            session.close();
            return true;
        } catch (Exception e){
            tx1.rollback();
            session.close();
            if (e.getCause() instanceof PSQLException){
                throw new CustomException(e.getCause().getMessage());
            }
            throw new CustomException(e);
        }
    }

    default boolean saveList(List<Object> objectList) throws CustomException {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        try {
            for (Object o: objectList){
                session.save(o);
            }
            tx1.commit();
            session.close();
            return true;
        } catch (Exception e){
            tx1.rollback();
            session.close();
            if (e.getCause() instanceof PSQLException){
                throw new CustomException(e.getCause().getMessage());
            }
            throw new CustomException(e);
        }
    }

    default boolean update(Object obj) throws CustomException {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        try {
            session.update(obj);
            tx1.commit();
            session.close();
            return true;
        } catch (Exception e){
            tx1.rollback();
            session.close();
            if (e.getCause() instanceof PSQLException){
                throw new CustomException(e.getCause().getMessage());
            }
            throw new CustomException(e);
        }
    }

    default void saveOrUpdate(Object obj) throws CustomException {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        session.saveOrUpdate(obj);
        tx1.commit();
        session.close();
    }

    default boolean delete(Object obj) throws CustomException {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        session.delete(obj);
        try {
            tx1.commit();
            session.close();
            return true;
        } catch (PersistenceException hibernateEx) {
            LOGGER.error("Exception", hibernateEx);
            try {
                tx1.rollback();
                MsgBox.msgWarning("Delete operation", "Can't delete this object.\nFor more information see log file");
            } catch (RuntimeException runtimeEx) {
                MsgBox.msgException("Couldn’t Roll Back Transaction", runtimeEx);
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return false;
    }

    default Date convertDatePeriod(Date date, int format){
        SimpleDateFormat formatter =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            if (format == 0){
                return formatter.parse(String.format("%s 00:00:00", date));
            } else if (format == 1){
                return formatter.parse(String.format("%s 23:59:59", date));
            } else {
                throw new CustomException("Incorrect format number");
            }
        } catch (ParseException | CustomException e) {
            LOGGER.error("Exception", e);
            MsgBox.msgException(e);
            return null;
        }
    }

}
