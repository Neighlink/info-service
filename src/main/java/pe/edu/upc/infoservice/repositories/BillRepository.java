package pe.edu.upc.infoservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upc.infoservice.entities.Bill;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    @Query("SELECT b FROM Bill b WHERE b.departmentId = :departmentId AND b.isDelete = false ")
    Optional<List<Bill>> getAllByDepartment(@Param("departmentId") Long departmentId );
    @Query("SELECT b FROM Bill b WHERE b.condominiumId = :condominiumId AND b.isDelete = false ")
    Optional<List<Bill>> getAllByCondominium(@Param("condominiumId") Long condominiumId);
    @Query("UPDATE Bill b SET b.isDelete = true WHERE b.id = :billId")
    Optional<List<Bill>> deleteBill(@Param("billId") Long billId);
}
