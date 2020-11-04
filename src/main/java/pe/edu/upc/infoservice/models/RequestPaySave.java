package pe.edu.upc.infoservice.models;

import lombok.Data;

import java.util.Date;

@Data
public class RequestPaySave {
    private Long id;
    private Date paymentDate;
    private Float amount;
    private boolean confirmPaid;
    private Long residentId;
}
