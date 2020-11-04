package pe.edu.upc.infoservice.services;

import pe.edu.upc.infoservice.entities.Bill;
import pe.edu.upc.infoservice.entities.PaymentCategory;

import java.util.List;
import java.util.Optional;

public interface BillService extends CrudService<Bill, Long> {
    Optional<List<Bill>> getAllByDepartment(Long departmentId);
    Optional<PaymentCategory> getPaymentCategoryById(Long id);
}
