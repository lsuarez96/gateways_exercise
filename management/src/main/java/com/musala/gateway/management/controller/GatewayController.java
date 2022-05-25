package com.musala.gateway.management.controller;

import com.musala.gateway.management.exception.*;
import com.musala.gateway.management.model.Device;
import com.musala.gateway.management.model.Gateway;
import com.musala.gateway.management.service.GatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/gateway")
public class GatewayController {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    GatewayService gatewayService;


    @GetMapping("/list")
    public ResponseEntity<?> listGateways() {
        logger.info("Request made to /gateway/list");
        List<Gateway> gateways = gatewayService.list();
        if (!gateways.isEmpty()) {
            logger.info("/gateway/list responded OK.");
            return new ResponseEntity<>(gateways.toArray(new Gateway[0]), HttpStatus.OK);
        }
        logger.info("/gateway/list responded NO_CONTENT");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<?> viewGateway(@PathVariable long id) {
        try {
            Gateway gateway = gatewayService.gatewayById(id);
            return new ResponseEntity<>(gateway, HttpStatus.OK);
        } catch (GatewayNotFoundException e) {
            logger.error("/gateway/view responded NO_CONTENT", e);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createGateway(@Valid @RequestBody Gateway gateway) throws NotValidGatewayException {
        Gateway record = gatewayService.create(gateway);
        return new ResponseEntity<>(record, HttpStatus.CREATED);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteGateway(@PathVariable long id) {
        boolean deleted = gatewayService.deleteGateway(id);
        logger.info("Gateway of id: " + id + (deleted ? " deleted, /gateway/delete/" + id + " responded OK" :
                                              " could not be deleted, /gateway/delete/" + id
                                              + " responded NO_CONTENT status"));
        return new ResponseEntity<>(deleted ? HttpStatus.OK : HttpStatus.NO_CONTENT);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateGateway(@Valid @RequestBody Gateway gateway, @PathVariable long id)
            throws NotValidGatewayException, GatewayNotFoundException {
        logger.info("/gateway/update Requested");
        Gateway updated = gatewayService.updateGateway(gateway, id);
        logger.info("/gateway/update responded OK");
        return new ResponseEntity<>(updated, HttpStatus.OK);

    }

    @PutMapping("/{gateway_id}/attach/{device_id}")
    public ResponseEntity<?> attachDevice(@PathVariable long gateway_id, @PathVariable long device_id)
            throws DeviceLimitException, DeviceNotFoundException, GatewayNotFoundException {
        logger.info("/gateway/attach requested");
        Gateway updated = gatewayService.attachDevice(gateway_id, device_id);
        logger.info("/gateway/attach responded OK");
        return new ResponseEntity<>(updated, HttpStatus.OK);

    }

    @PutMapping("/{gateway_id}/detach/{device_id}")
    public ResponseEntity<?> detachDevice(@PathVariable long gateway_id, @PathVariable long device_id)
            throws DeviceNotFoundException, GatewayNotFoundException {
        logger.info("/gateway/detach requested");
        Gateway updated = gatewayService.detachDevice(gateway_id, device_id);
        logger.info("/gateway/detach responded OK");
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @GetMapping("/devices/{id}")
    public ResponseEntity<?> gatewayDevices(@PathVariable long id) throws GatewayNotFoundException {
        logger.info("/gateway/devices requested");
        List<Device> devices = gatewayService.gatewayDevices(id);
        logger.info("/gateway/devices responded OK");
        return new ResponseEntity<>(devices.toArray(new Device[0]), HttpStatus.OK);
    }

    /**
     * Although must exceptions are captured within the endpoints others like model validation exceptions may not be
     * captured. This function will ensure a proper response is given in such cases.
     *
     * @param ex exception thrown
     * @return a map with the fields that could not pass validation tests and the corresponding error message.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler( {
            MethodArgumentNotValidException.class, DeviceLimitException.class, DeviceNotFoundException.class,
            GatewayNotFoundException.class, NotValidGatewayException.class, NotValidDeviceException.class
    })
    @ResponseBody
    public Map<String, String> handleValidationExceptions(Exception ex) {
        Map<String, String> errors = new HashMap<>();
        if (ex instanceof MethodArgumentNotValidException) {
            handleModelValidationExceptions((MethodArgumentNotValidException) ex, errors);
        } else {
            errors.put(ex.getClass().getSimpleName(), ex.getMessage());
        }
        return errors;
    }

    private void handleModelValidationExceptions(MethodArgumentNotValidException ex, Map<String, String> errors) {
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            logger.error("Gateway." + fieldName + ":" + errorMessage, ex);
            errors.put(fieldName, errorMessage);
        });
    }

}
