package pe.edu.upc.infoservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.infoservice.entities.Bill;
import pe.edu.upc.infoservice.entities.Payment;
import pe.edu.upc.infoservice.entities.PaymentCategory;
import pe.edu.upc.infoservice.models.RequestPaySave;
import pe.edu.upc.infoservice.models.Response;
import pe.edu.upc.infoservice.models.ResponseAuth;
import pe.edu.upc.infoservice.services.BillService;
import pe.edu.upc.infoservice.services.PaymentService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@RestController
@RequestMapping("/infos")
public class InfoController {

    InfoController() {
        response = new Response();
        responseAuth = new ResponseAuth();
    }


    @Autowired
    private BillService billService;
    @Autowired
    private PaymentService paymentService;

    private final static String URL_PROFILE = "http://localhost:8094/profiles";
    private final static Logger LOGGER = Logger.getLogger("bitacora.subnivel.Control");
    HttpStatus status;

    Response response = new Response();
    ResponseAuth responseAuth = new ResponseAuth();

    private ResponseAuth authToken(String token) {
        try {
            var values = new HashMap<String, String>() {{
            }};
            var objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(values);
            String url = URL_PROFILE + "/authToken";
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .setHeader("Authorization", token)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject responseAPI = new JSONObject(response.body());
            var status = responseAPI.getInt("status");
            if (status != 200) {
                var message = responseAPI.getString("message");
                responseAuth.initError(false, message);
                return responseAuth;
            }
            JSONObject result = responseAPI.getJSONObject("result");
            responseAuth.init(result.getLong("id"), result.getString("userType"), result.getBoolean("authorized"), "");
            return responseAuth;
        } catch (Exception e) {
            responseAuth.initError(false, e.getMessage());
            return responseAuth;
        }
    }

    public void unauthorizedResponse() {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setMessage("UNAUTHORIZED USER");
        status = HttpStatus.UNAUTHORIZED;
    }

    public void notFoundResponse() {
        response.setStatus(HttpStatus.NOT_FOUND.value());
        response.setMessage("ENTITY NOT FOUND");
        status = HttpStatus.NOT_FOUND;
    }

    public void okResponse(Object result) {
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("SERVICE SUCCESS");
        response.setResult(result);
        status = HttpStatus.OK;
    }

    public void conflictResponse(String message) {
        response.setStatus(HttpStatus.CONFLICT.value());
        response.setMessage(message);
        status = HttpStatus.CONFLICT;
    }

    public void internalServerErrorResponse(String message) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setMessage(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase() + " => " + message);
    }

    @GetMapping(path = "/departments/{id}/bills", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> getBillsByDepartment(@PathVariable(name = "id") Long id, @RequestHeader String Authorization) {
        try {
            ResponseAuth authToken = authToken(Authorization);
            if (!authToken.isAuthorized()) {
                unauthorizedResponse();
                return new ResponseEntity<>(response, status);
            }
            Optional<List<Bill>> bills = billService.getAllByDepartment(id);
            LOGGER.info(String.valueOf(bills));
            if (bills.isEmpty()) {
                notFoundResponse();
            } else {
                okResponse(bills.get());
            }
            LOGGER.info(String.valueOf(response));
            LOGGER.info(String.valueOf(status));
            return new ResponseEntity<>(response, status);
        } catch
        (Exception e) {
            internalServerErrorResponse(e.getMessage());
            return new ResponseEntity<>(response, status);
        }
    }

    @PostMapping(path = "/departments/{departmentId}/bills/{billId}/pay", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> savePayment(@PathVariable("departmentId") Long departmentId, @PathVariable("billId") Long billId, @RequestBody RequestPaySave requestPaySave, @RequestHeader String Authorization) {
        try {
            ResponseAuth userAuth = authToken(Authorization);
            LOGGER.info("RESPONSE AUTH => " + userAuth);
            if (!userAuth.isAuthorized()) {
                unauthorizedResponse();
                return new ResponseEntity<>(response, status);
            }
            Optional<Bill> bill = billService.findById(billId);
            if (bill.isEmpty()) {
                notFoundResponse();
                return new ResponseEntity<>(response, status);
            }
            if (bill.get().getPayment() != null) {
                conflictResponse("Ya hay un pago registrado con anterioridad");
            } else {
                Payment payment = new Payment();
                payment.init(new Date(), requestPaySave.getAmount(), userAuth.getId());
                Payment paymentSaved = paymentService.save(payment);
                bill.get().setPayment(paymentSaved);
                Bill billSaved = billService.save(bill.get());
                LOGGER.info("paymentsaved => " + paymentSaved);
                LOGGER.info("billSaved => " + billSaved);
                if (paymentSaved.getId() == 0) {
                    conflictResponse("Ocurrio un error al guardar");
                } else {
                    okResponse(paymentSaved);
                }
            }
            return new ResponseEntity<>(response, status);
        } catch (Exception e) {
            internalServerErrorResponse(e.getMessage());
            return new ResponseEntity<>(response, status);
        }
    }
}
