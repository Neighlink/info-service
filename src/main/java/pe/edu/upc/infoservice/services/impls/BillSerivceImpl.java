package pe.edu.upc.infoservice.services.impls;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.edu.upc.infoservice.entities.Bill;
import pe.edu.upc.infoservice.entities.PaymentCategory;
import pe.edu.upc.infoservice.repositories.BillRepository;
import pe.edu.upc.infoservice.repositories.PaymentCategoryRepository;
import pe.edu.upc.infoservice.repositories.PaymentRepository;
import pe.edu.upc.infoservice.services.BillService;

import java.util.List;
import java.util.Optional;

@Service
public class BillSerivceImpl implements BillService {

    @Autowired
    private BillRepository billRepository;
    @Autowired
    private PaymentCategoryRepository paymentCategoryRepository;

    @Override
    public Bill save(Bill entity) throws Exception {
        return billRepository.save(entity);
    }

    @Override
    public List<Bill> findAll() throws Exception {
        return billRepository.findAll();
    }

    @Override
    public Optional<Bill> findById(Long aLong) throws Exception {
        return billRepository.findById(aLong);
    }

    @Override
    public Bill update(Bill entity) throws Exception {
        return billRepository.save(entity);
    }

    @Override
    public void deleteById(Long aLong) throws Exception {
        billRepository.deleteById(aLong);
    }

    @Override
    public Optional<List<Bill>> getAllByDepartment(Long departmentId) {
        return billRepository.getAllByDepartment(departmentId);
    }

    @Override
    public Optional<PaymentCategory> getPaymentCategoryById(Long id) {
        return paymentCategoryRepository.findById(id);
    }
}
