package pe.edu.upc.infoservice.services;

import pe.edu.upc.infoservice.entities.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentService extends CrudService<Payment, Long> {
    Optional<List<Payment>> historyByDepartment(Long deparmentId);
}
