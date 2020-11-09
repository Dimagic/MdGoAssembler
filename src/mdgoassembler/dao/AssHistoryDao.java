package mdgoassembler.dao;

import mdgoassembler.models.AssHistory;
import mdgoassembler.models.Assembly;
import mdgoassembler.utils.CustomException;
import mdgoassembler.utils.HibernateSessionFactoryUtil;
import org.hibernate.Session;

import java.util.List;

public class AssHistoryDao implements UniversalDao {
    public AssHistory findById(int id) throws CustomException {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        AssHistory envHistory = session.get(AssHistory.class, id);
        session.close();
        return envHistory;
    }

    public List findAssHistoryByAssembly(Assembly assembly) throws CustomException{
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        List journal = session.createSQLQuery(
                "select * from public.asshistory where assembly_id = :assembly_id order by date")
                .addEntity(AssHistory.class)
                .setParameter("assembly_id", assembly.getId()).list();
        session.close();
        return journal;
    }
}
