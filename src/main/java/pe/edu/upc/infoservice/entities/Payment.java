package pe.edu.upc.infoservice.entities;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "payments")
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date paymentDate;
    @Column(nullable = false)
    private float amount;
    @Column(nullable = false)
    private Long residentId;
    @Column(nullable = false)
    private String status;
    @Column(nullable = false)
    private boolean editable;
    @Column(nullable = false)
    private Long deparmentId;
    @Column(nullable = false)
    private String urlImage;
    @Column(nullable = false)
    private Long billId;
}
