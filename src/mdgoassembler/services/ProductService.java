package mdgoassembler.services;

import mdgoassembler.dao.ProductsDao;
import mdgoassembler.models.Product;
import mdgoassembler.utils.CustomException;

import java.util.List;

public class ProductService {
    private ProductsDao dao = new ProductsDao();

    public ProductService() {
    }

    public boolean saveProduct(Product product) throws CustomException {
        return dao.save(product);
    }

    public boolean updateProduct(Product product) throws CustomException {
        return dao.update(product);
    }

    public boolean deleteProduct(Product product) throws CustomException {
        return dao.delete(product);
    }

    public Product findByName(String name) throws CustomException {
        return dao.findByName(name);
    }

    public List findAll() throws CustomException {
        return dao.findAll();
    }
}
