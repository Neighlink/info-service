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
import pe.edu.upc.infoservice.models.*;
import pe.edu.upc.infoservice.services.BillService;
import pe.edu.upc.infoservice.services.PaymentCategoryService;
import pe.edu.upc.infoservice.services.PaymentService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
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
    @Autowired
    private PaymentCategoryService paymentCategoryService;

    private final static String URL_PROFILE = "http://localhost:8092/profiles";
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

    // START CRUD BILL
    @GetMapping(path = "/condominiums/{condominiumId}/departments/{departmentId}/bills", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> getBillsByDepartment(@PathVariable("condominiumId") Long condominiumId, @PathVariable("departmentId") Long departmentId, @RequestHeader String Authorization) {
        response = new Response();
        try {
            ResponseAuth authToken = authToken(Authorization);
            if (!authToken.isAuthorized()) {
                unauthorizedResponse();
                return new ResponseEntity<>(response, status);
            }
            Optional<List<Bill>> bills = billService.getAllByDepartment(departmentId);
            if (bills.isEmpty()) {
                okResponse(new ArrayList<>());
            } else {
                okResponse(bills.get());
            }
            return new ResponseEntity<>(response, status);
        } catch
        (Exception e) {
            internalServerErrorResponse(e.getMessage());
            return new ResponseEntity<>(response, status);
        }
    }

    @GetMapping(path = "/condominiums/{condominiumId}/bills", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> getBillsByCondominium(@PathVariable("condominiumId") Long condominiumId, @RequestHeader String Authorization) {
        response = new Response();
        try {
            ResponseAuth authToken = authToken(Authorization);
            if (!authToken.isAuthorized()) {
                unauthorizedResponse();
                return new ResponseEntity<>(response, status);
            }
            Optional<List<Bill>> bills = billService.getAllByCondominium(condominiumId);
            if (bills.isEmpty()) {
                okResponse(new ArrayList<>());
            } else {
                okResponse(bills.get());
            }
            return new ResponseEntity<>(response, status);
        } catch
        (Exception e) {
            internalServerErrorResponse(e.getMessage());
            return new ResponseEntity<>(response, status);
        }
    }

    @DeleteMapping(path = "/condominiums/{condominiumId}/departments/{departmentId}/bills/{billId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> deleteBillsByCondominium(@PathVariable("condominiumId") Long condominiumId, @PathVariable("departmentId") Long departmentId, @PathVariable("billId") Long billId, @RequestHeader String Authorization) {
        response = new Response();
        try {
            ResponseAuth authToken = authToken(Authorization);
            if (!authToken.isAuthorized()) {
                unauthorizedResponse();
                return new ResponseEntity<>(response, status);
            }

            Optional<Bill> bill = billService.findById(billId);
            if (bill.isEmpty()) {
                notFoundResponse();
                return new ResponseEntity<>(response, status);
            }

            bill.get().setDelete(true);
            billService.save(bill.get());
            okResponse(null);
            return new ResponseEntity<>(response, status);
        } catch
        (Exception e) {
            internalServerErrorResponse(e.getMessage());
            return new ResponseEntity<>(response, status);
        }
    }


    @PostMapping(path = "/condominiums/{condominiumId}/departments/{departmentId}/bills", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> postBillByDepartment(@PathVariable("condominiumId") Long condominiumId, @PathVariable("departmentId") Long departmentId, @RequestHeader String Authorization, @RequestBody RequestBill requestBill) {
        response = new Response();
        try {
            ResponseAuth authToken = authToken(Authorization);
            if (!authToken.isAuthorized()) {
                unauthorizedResponse();
                return new ResponseEntity<>(response, status);
            }

            Optional<PaymentCategory> paymentCategory = billService.getPaymentCategoryById(requestBill.getPaymentCategoryId());
            if (paymentCategory.isEmpty()) {
                notFoundResponse();
                return new ResponseEntity<>(response, status);
            }

            Bill bill = new Bill();
            bill.setAmount(requestBill.getAmount());
            bill.setCategory(paymentCategory.get());
            bill.setCondominiumId(condominiumId);
            bill.setDepartmentId(departmentId);
            bill.setName(requestBill.getName());
            bill.setDescription(requestBill.getDescription());
            bill.setStartDate(requestBill.getStartDate());
            bill.setEndDate(requestBill.getEndDate());

            Bill billSave = billService.save(bill);

            okResponse(billSave);
            return new ResponseEntity<>(response, status);
        } catch
        (Exception e) {
            internalServerErrorResponse(e.getMessage());
            return new ResponseEntity<>(response, status);
        }
    }

    @PutMapping(path = "/condominiums/{condominiumId}/departments/{departmentId}/bills/{billId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> putBillByDepartment(@PathVariable("condominiumId") Long condominiumId, @PathVariable("departmentId") Long departmentId, @PathVariable("billId") Long billId, @RequestHeader String Authorization, @RequestBody RequestBill requestBill) {
        response = new Response();
        try {
            ResponseAuth authToken = authToken(Authorization);
            if (!authToken.isAuthorized()) {
                unauthorizedResponse();
                return new ResponseEntity<>(response, status);
            }

            Optional<PaymentCategory> paymentCategory = billService.getPaymentCategoryById(requestBill.getPaymentCategoryId());
            if (paymentCategory.isEmpty()) {
                notFoundResponse();
                return new ResponseEntity<>(response, status);
            }

            Optional<Bill> bill = billService.findById(billId);
            if (bill.isEmpty()) {
                notFoundResponse();
                return new ResponseEntity<>(response, status);
            }

            bill.get().setAmount(requestBill.getAmount());
            bill.get().setCategory(paymentCategory.get());
            bill.get().setCondominiumId(condominiumId);
            bill.get().setDepartmentId(departmentId);
            bill.get().setName(requestBill.getName());
            bill.get().setDescription(requestBill.getDescription());
            bill.get().setStartDate(requestBill.getStartDate());
            bill.get().setEndDate(requestBill.getEndDate());

            Bill billSave = billService.save(bill.get());

            okResponse(billSave);
            return new ResponseEntity<>(response, status);
        } catch
        (Exception e) {
            internalServerErrorResponse(e.getMessage());
            return new ResponseEntity<>(response, status);
        }
    }
    // END CRUD BILL

    // START CRUD PAY
    @PostMapping(path = "/departments/{departmentId}/bills/{billId}/pays", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> savePayment(@PathVariable("departmentId") Long departmentId, @PathVariable("billId") Long billId, @RequestBody RequestPayment requestPayment, @RequestHeader String Authorization) {
        response = new Response();
        try {
            ResponseAuth userAuth = authToken(Authorization);
            if (!userAuth.isAuthorized()) {
                unauthorizedResponse();
                return new ResponseEntity<>(response, status);
            }
            Optional<Bill> bill = billService.findById(billId);
            if (bill.isEmpty()) {
                notFoundResponse();
                return new ResponseEntity<>(response, status);
            }

            Payment payment = new Payment();
            payment.setAmount(requestPayment.getAmount());
            payment.setEditable(true);
            payment.setStatus("PENDIENTE");
            payment.setUrlImage(requestPayment.getUrlImage());
            payment.setPaymentDate(new Date());
            payment.setResidentId(userAuth.getId());
            payment.setDeparmentId(departmentId);

            Payment paymentSaved = paymentService.save(payment);
            okResponse(paymentSaved);

            return new ResponseEntity<>(response, status);
        } catch (Exception e) {
            internalServerErrorResponse(e.getMessage());
            return new ResponseEntity<>(response, status);
        }
    }

    @PutMapping(path = "/departments/{departmentId}/bills/{billId}/pays/{payId}/accept", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> acceptPayment(@PathVariable("departmentId") Long departmentId, @PathVariable("billId") Long billId, @PathVariable("payId") Long payId, @RequestHeader String Authorization) {
        response = new Response();
        try {
            ResponseAuth userAuth = authToken(Authorization);
            if (!userAuth.isAuthorized()) {
                unauthorizedResponse();
                return new ResponseEntity<>(response, status);
            }
            Optional<Bill> bill = billService.findById(billId);
            if (bill.isEmpty()) {
                notFoundResponse();
                return new ResponseEntity<>(response, status);
            }
            Optional<Payment> payment = paymentService.findById(payId);
            if (payment.isEmpty()) {
                notFoundResponse();
                return new ResponseEntity<>(response, status);
            }

            payment.get().setStatus("ACEPTADO");
            payment.get().setEditable(false);

            Payment paymentSaved = paymentService.save(payment.get());
            okResponse(paymentSaved);

            return new ResponseEntity<>(response, status);
        } catch (Exception e) {
            internalServerErrorResponse(e.getMessage());
            return new ResponseEntity<>(response, status);
        }
    }

    @PutMapping(path = "/departments/{departmentId}/bills/{billId}/pays/{payId}/denny", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> dennytPayment(@PathVariable("departmentId") Long departmentId, @PathVariable("billId") Long billId, @PathVariable("payId") Long payId, @RequestHeader String Authorization) {
        response = new Response();
        try {
            ResponseAuth userAuth = authToken(Authorization);
            if (!userAuth.isAuthorized()) {
                unauthorizedResponse();
                return new ResponseEntity<>(response, status);
            }
            Optional<Bill> bill = billService.findById(billId);
            if (bill.isEmpty()) {
                notFoundResponse();
                return new ResponseEntity<>(response, status);
            }
            Optional<Payment> payment = paymentService.findById(payId);
            if (payment.isEmpty()) {
                notFoundResponse();
                return new ResponseEntity<>(response, status);
            }

            payment.get().setStatus("RECHAZADO");
            payment.get().setEditable(false);

            Payment paymentSaved = paymentService.save(payment.get());
            okResponse(paymentSaved);

            return new ResponseEntity<>(response, status);
        } catch (Exception e) {
            internalServerErrorResponse(e.getMessage());
            return new ResponseEntity<>(response, status);
        }
    }

    @GetMapping(path = "/departments/{departmentId}/bills/{billId}/pays", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> historyPayByBill(@PathVariable("departmentId") Long departmentId, @PathVariable("billId") Long billId, @RequestHeader String Authorization) {
        response = new Response();
        try {
            ResponseAuth userAuth = authToken(Authorization);
            if (!userAuth.isAuthorized()) {
                unauthorizedResponse();
                return new ResponseEntity<>(response, status);
            }

            Optional<Bill> bill = billService.findById(billId);
            if (bill.isEmpty()) {
                notFoundResponse();
                return new ResponseEntity<>(response, status);
            }

            List<Payment> payments = paymentService.paymentsByBill(departmentId);
            okResponse(payments);

            return new ResponseEntity<>(response, status);
        } catch (Exception e) {
            internalServerErrorResponse(e.getMessage());
            return new ResponseEntity<>(response, status);
        }
    }
    // END CRUD PAY

    //INIT PAYMENT CATEGORY
    @GetMapping(path = "/condominiums/{condominiumId}/paymentCategories", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> getPaymentCategoryByDepartment(@PathVariable("condominiumId") Long condominiumId, @RequestHeader String Authorization) {
        try {
            ResponseAuth authToken = authToken(Authorization);
            if (!authToken.isAuthorized()) {
                unauthorizedResponse();
                return new ResponseEntity<>(response, status);
            }
            Optional<List<PaymentCategory>> paymentCategories = paymentCategoryService.findAllByCondominiumId(condominiumId);
            if (paymentCategories.isEmpty()) {
                okResponse(new ArrayList<>());
            } else {
                okResponse(paymentCategories.get());
            }
            return new ResponseEntity<>(response, status);
        } catch
        (Exception e) {
            internalServerErrorResponse(e.getMessage());
            return new ResponseEntity<>(response, status);
        }
    }

    @PostMapping(path = "/condominiums/{condominiumId}/paymentCategories", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> postPaymentCategoryByCondominium(@PathVariable("condominiumId") Long condominiumId, @RequestHeader String Authorization, @RequestBody RequestPaymentCategory requestPaymentCategory) {
        try {
            ResponseAuth authToken = authToken(Authorization);
            if (!authToken.isAuthorized()) {
                unauthorizedResponse();
                return new ResponseEntity<>(response, status);
            }
            PaymentCategory paymentCategory = new PaymentCategory();
            paymentCategory.setCondominiumId(condominiumId);
            paymentCategory.setName(requestPaymentCategory.getName());
            paymentCategory.setDescription(requestPaymentCategory.getDescription());
            paymentCategory.setDelete(false);
            PaymentCategory paymentCategorySaved = paymentCategoryService.save(paymentCategory);
            okResponse(paymentCategorySaved);
            return new ResponseEntity<>(response, status);
        } catch
        (Exception e) {
            internalServerErrorResponse(e.getMessage());
            return new ResponseEntity<>(response, status);
        }
    }

    @PutMapping(path = "/condominiums/{condominiumId}/paymentCategories/{paymentCategoryId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> updatePaymentCategoryByDepartment(@PathVariable("condominiumId") Long condominiumId, @PathVariable("paymentCategoryId") Long paymentCategoryId, @RequestHeader String Authorization, @RequestBody RequestPaymentCategory requestPaymentCategory) {
        try {
            ResponseAuth authToken = authToken(Authorization);
            if (!authToken.isAuthorized()) {
                unauthorizedResponse();
                return new ResponseEntity<>(response, status);
            }
            Optional<PaymentCategory> paymentCategory = paymentCategoryService.findById(paymentCategoryId);
            if (paymentCategory.isEmpty()) {
                notFoundResponse();
                return new ResponseEntity<>(response, status);
            }

            if (!requestPaymentCategory.getName().isEmpty())
                paymentCategory.get().setName(requestPaymentCategory.getName());
            if (!requestPaymentCategory.getDescription().isEmpty())
                paymentCategory.get().setDescription(requestPaymentCategory.getDescription());

            PaymentCategory paymentCategorySaved = paymentCategoryService.save(paymentCategory.get());
            okResponse(paymentCategorySaved);
            return new ResponseEntity<>(response, status);
        } catch
        (Exception e) {
            internalServerErrorResponse(e.getMessage());
            return new ResponseEntity<>(response, status);
        }
    }

    @DeleteMapping(path = "/condominiums/{condominiumId}/paymentCategories/{paymentCategoryId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> deletePaymentCategoryByCondominium(@PathVariable("condominiumId") Long condominiumId, @PathVariable("paymentCategoryId") Long paymentCategoryId, @RequestHeader String Authorization) {
        try {
            ResponseAuth authToken = authToken(Authorization);
            LOGGER.info(String.valueOf(authToken));
            if (!authToken.isAuthorized()) {
                unauthorizedResponse();
                return new ResponseEntity<>(response, status);
            }
            Optional<PaymentCategory> paymentCategory = paymentCategoryService.findById(paymentCategoryId);
            if (paymentCategory.isEmpty()) {
                notFoundResponse();
                return new ResponseEntity<>(response, status);
            }
            paymentCategory.get().setDelete(true);
            paymentCategoryService.save(paymentCategory.get());
            okResponse(null);
            return new ResponseEntity<>(response, status);
        } catch
        (Exception e) {
            internalServerErrorResponse(e.getMessage());
            return new ResponseEntity<>(response, status);
        }
    }
    //FINISH PAYMENT CATEGORY
}
