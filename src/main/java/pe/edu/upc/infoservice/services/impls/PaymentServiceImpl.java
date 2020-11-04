package pe.edu.upc.infoservice.services.impls;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.edu.upc.infoservice.entities.Payment;
import pe.edu.upc.infoservice.repositories.PaymentRepository;
import pe.edu.upc.infoservice.services.PaymentService;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public Payment save(Payment entity) throws Exception {
        return paymentRepository.save(entity);
    }

    @Override
    public List<Payment> findAll() throws Exception {
        return paymentRepository.findAll();
    }

    @Override
    public Optional<Payment> findById(Long aLong) throws Exception {
        return paymentRepository.findById(aLong);
    }

    @Override
    public Payment update(Payment entity) throws Exception {
        return paymentRepository.save(entity);
    }

    @Override
    public void deleteById(Long aLong) throws Exception {
        paymentRepository.deleteById(aLong);
    }
}
