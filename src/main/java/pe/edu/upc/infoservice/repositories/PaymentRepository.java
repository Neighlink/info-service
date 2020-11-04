package pe.edu.upc.infoservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.infoservice.entities.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
