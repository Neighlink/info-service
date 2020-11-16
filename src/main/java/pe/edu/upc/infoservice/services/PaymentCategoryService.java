package pe.edu.upc.infoservice.services;

import pe.edu.upc.infoservice.entities.PaymentCategory;

import java.util.List;
import java.util.Optional;

public interface PaymentCategoryService extends CrudService<PaymentCategory, Long> {
    Optional<List<PaymentCategory>> findAllByCondominiumId(Long condominiumId);
}
