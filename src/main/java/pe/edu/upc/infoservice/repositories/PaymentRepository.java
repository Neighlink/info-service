package pe.edu.upc.infoservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upc.infoservice.entities.Payment;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("SELECT p FROM Payment p WHERE p.billId = :billId ORDER BY p.paymentDate DESC")
    List<Payment> paymentsByBill(@Param("billId") Long billId);
}
