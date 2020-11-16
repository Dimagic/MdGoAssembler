package mdgoassembler.services;

import mdgoassembler.dao.AssemblyDao;
import mdgoassembler.models.Assembly;
import mdgoassembler.models.Product;
import mdgoassembler.utils.CustomException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;

public class AssemblyService {
    private final Logger LOGGER = LogManager.getLogger(this.getClass().getName());

    private AssemblyDao dao = new AssemblyDao();

    public AssemblyService() {
    }

    public boolean saveAssembly(Assembly assembly) throws CustomException {
        return dao.save(assembly);
    }

    public boolean updateAssembly(Assembly assembly) throws CustomException {
        return dao.update(assembly);
    }

    public boolean deleteAssembly(Assembly assembly) throws CustomException {
        return dao.delete(assembly);
    }

    public Assembly findByBoard(String boardsn) throws CustomException {
        return dao.findByBoard(boardsn);
    }

    public Assembly findBySim(String simSn) throws CustomException {
        return dao.findBySim(simSn);
    }

    public Assembly findByCase(String caseSn) throws CustomException {
        return dao.findByCase(caseSn);
    }

    public Assembly findByAws(String aws) throws CustomException {
        return dao.findByAws(aws);
    }

    public List<Assembly> getAssemblyAfterTest() throws CustomException {
        return dao.getAssemblyAfterTest();
    }

    public List<Assembly> getCompleteAssembly() throws CustomException {
        return dao.getCompleteAssembly();
    }

    public Assembly findAssemblyById(int id) throws CustomException {
        return dao.findById(id);
    }

    public Long getTotalCount() throws CustomException {
        return dao.getTotalCount();
    }

    public Long getCompleteCount() throws CustomException {
        return dao.getCompleteCount();
    }

    public Long getIncompleteCount() throws CustomException {
        return dao.getIncompleteCount();
    }

    public List getCompleteBetweenDate(Date start, Date stop) throws CustomException {
        return dao.getCompleteBetweenDate(start, stop);
    }

    public List getIncompleteBetweenDate(Date start, Date stop) throws CustomException {
        return dao.getIncompleteBetweenDate(start, stop);
    }

    public List getByProduct(Product product) throws CustomException {
        return dao.getByProduct(product);
    }

    public List findAll() throws CustomException {
        return dao.findAll();
    }
}
