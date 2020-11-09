package mdgoassembler.dao;

import mdgoassembler.models.Assembly;
import mdgoassembler.models.Product;
import mdgoassembler.utils.CustomException;
import mdgoassembler.utils.HibernateSessionFactoryUtil;
import org.hibernate.Session;

import javax.persistence.NoResultException;
import java.util.List;

public class ProductsDao implements UniversalDao {
    public Product findById(int id) throws CustomException {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Product product = session.get(Product.class, id);
        session.close();
        return product;
    }

    public Product findByName(String name) throws CustomException {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        try {
            String q = String.format("SELECT * FROM public.product WHERE name = '%s'", name);
            System.out.println(q);
            Product product = (Product) session.createSQLQuery(q)
                    .addEntity(Product.class).getSingleResult();
            session.close();
            return product;
        } catch (NoResultException e){
            return null;
        }
    }

    public List findAll() throws CustomException{
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        List productList = session.createQuery("From Product").list();
        session.close();
        return productList;
    }
}
