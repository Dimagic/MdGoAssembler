package mdgoassembler.services;

import mdgoassembler.dao.AssHistoryDao;
import mdgoassembler.models.AssHistory;
import mdgoassembler.models.Assembly;
import mdgoassembler.utils.CustomException;

import java.util.List;

public class AssHistoryService {
    private AssHistoryDao dao = new AssHistoryDao();

    public boolean save(AssHistory assHistory) throws CustomException {
        return dao.save(assHistory);
    }

    public boolean saveList(List<Object> historyList) throws CustomException {
        return dao.saveList(historyList);
    }

    public void delete(AssHistory assHistory) throws CustomException {
        dao.delete(assHistory);
    }

    public void update(AssHistory assHistory) throws CustomException {
        dao.update(assHistory);
    }

    public List findAssHistoryByAssembly(Assembly assembly) throws CustomException {
        return dao.findAssHistoryByAssembly(assembly);
    }

    public AssHistory find(int id) throws CustomException {
        return dao.findById(id);
    }
}
