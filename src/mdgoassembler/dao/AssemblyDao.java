package mdgoassembler.dao;

import mdgoassembler.models.Assembly;
import mdgoassembler.models.Product;
import mdgoassembler.utils.CustomException;
import mdgoassembler.utils.HibernateSessionFactoryUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.persistence.NoResultException;
import java.util.Date;
import java.util.List;

public class AssemblyDao implements UniversalDao{
    public Assembly findById(int id) throws CustomException {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Assembly firstAssembly = session.get(Assembly.class, id);
        session.close();
        return firstAssembly;
    }

    public Assembly findByBoard(String boardsn) throws CustomException {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        try {
            Assembly assembly = (Assembly) session.createSQLQuery(
                    "SELECT * FROM public.assembly WHERE boardsn = :boardsn")
                    .addEntity(Assembly.class)
                    .setParameter("boardsn", boardsn).getSingleResult();
            session.close();
            return assembly;
        } catch (NoResultException e){
            return null;
        }
    }

    public Assembly findBySim(String simSn) throws CustomException {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        try {
            Assembly assembly = (Assembly) session.createSQLQuery(
                    "SELECT * FROM public.assembly WHERE simsn = :simsn")
                    .addEntity(Assembly.class)
                    .setParameter("simsn", simSn).getSingleResult();
            session.close();
            return assembly;
        } catch (NoResultException e){
            return null;
        }
    }

    public Assembly findByCase(String caseSn) throws CustomException {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        try {
            Assembly assembly = (Assembly) session.createSQLQuery(
                    "SELECT * FROM public.assembly WHERE casesn = :casesn")
                    .addEntity(Assembly.class)
                    .setParameter("casesn", caseSn).getSingleResult();
            session.close();
            return assembly;
        } catch (NoResultException e){
            return null;
        }
    }

    public Assembly findByAws(String aws) throws CustomException {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        try {
            Assembly assembly = (Assembly) session.createSQLQuery(
                    "SELECT * FROM public.assembly WHERE qrawsid = :aws")
                    .addEntity(Assembly.class)
                    .setParameter("aws", aws).getSingleResult();
            session.close();
            return assembly;
        } catch (NoResultException e){
            return null;
        }
    }

    public List<Assembly> getAssemblyAfterTest() throws CustomException{
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        List list = session.createSQLQuery("SELECT * FROM public.assembly WHERE casesn is null")
                .addEntity(Assembly.class).list();
        session.close();
        return list;
    }

    public List<Assembly> getCompleteAssembly() throws CustomException{
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        List list = session.createSQLQuery("SELECT * FROM public.assembly WHERE casesn is not null")
                .addEntity(Assembly.class).list();
        session.close();
        return list;
    }

    public Long getTotalCount() throws CustomException {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Query query = session.createQuery("SELECT count(*) FROM Assembly");
        Long count = (Long) query.uniqueResult();
        session.close();
        return count;
    }

    public Long getCompleteCount() throws CustomException {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Query query = session.createQuery("SELECT count(*) FROM Assembly WHERE casesn IS NOT NULL");
        Long count = (Long) query.uniqueResult();
        session.close();
        return count;
    }

    public Long getIncompleteCount() throws CustomException {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Query query = session.createQuery("SELECT count(*) FROM Assembly WHERE casesn IS NULL");
        Long count = (Long) query.uniqueResult();
        session.close();
        return count;
    }

    public List getCompleteBetweenDate(Date start, Date stop) throws CustomException {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        String q = String.format("select * from public.assembly where casesn is not null " +
                "and assemblydate between '%s 00:00:00' and '%s 23:59:59' order by assemblydate desc", start, stop);
        List assemblyList = session.createSQLQuery(q).addEntity(Assembly.class).list();
        session.close();
        return assemblyList;
    }

    public List getIncompleteBetweenDate(Date start, Date stop) throws CustomException {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        String q = String.format("select * from public.assembly where casesn is null " +
                "and testdate between '%s 00:00:00' and '%s 23:59:59' order by testdate desc", start, stop);
        List assemblyList = session.createSQLQuery(q).addEntity(Assembly.class).list();
        session.close();
        return assemblyList;
    }

    public List getByProduct(Product product) throws CustomException {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        String q = String.format("select * from public.assembly where productpn = %s", product.getName());
        List assemblyList = session.createSQLQuery(q).addEntity(Assembly.class).list();
        return assemblyList;
    }

    public List findAll() throws CustomException{
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        List assemblyList = session.createQuery("From Assembly").list();
        session.close();
        return assemblyList;
    }
}
