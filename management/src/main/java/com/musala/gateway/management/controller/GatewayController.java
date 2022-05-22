package com.musala.gateway.management.controller;

import com.musala.gateway.management.exception.DeviceLimitException;
import com.musala.gateway.management.exception.DeviceNotFoundException;
import com.musala.gateway.management.exception.GatewayNotFoundException;
import com.musala.gateway.management.exception.NotValidGatewayException;
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
    public ResponseEntity<?> createGateway(@Valid @RequestBody Gateway gateway) {
        try {
            Gateway record = gatewayService.create(gateway);
            return new ResponseEntity<>(record, HttpStatus.CREATED);
        } catch (NotValidGatewayException e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
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
    public ResponseEntity<?> updateGateway(@Valid @RequestBody Gateway gateway, @PathVariable long id) {
        try {
            logger.info("/gateway/update Requested");
            Gateway updated = gatewayService.updateGateway(gateway, id);
            logger.info("/gateway/update responded OK");
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (DeviceLimitException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (GatewayNotFoundException e) {
            gateway.setId(id);
            Gateway created = null;
            try {
                created = gatewayService.create(gateway);
            } catch (NotValidGatewayException ex) {
                logger.error(ex.getMessage(), ex);
            }
            logger.error("/gateway/update responded CREATED", e);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (NotValidGatewayException e) {
            logger.error("/gateway/update responded BAD_REQUEST", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/attach/{gateway_id}/{device_id}")
    public ResponseEntity<?> attachDevice(@PathVariable long gateway_id, @PathVariable long device_id) {
        try {
            logger.info("/gateway/attach requested");
            Gateway updated = gatewayService.attachDevice(gateway_id, device_id);
            logger.info("/gateway/attach responded OK");
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (GatewayNotFoundException | DeviceNotFoundException | DeviceLimitException e) {
            logger.warn("/gateway/attach responded BAD_REQUEST", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/detach/{gateway_id}/{device_id}")
    public ResponseEntity<?> detachDevice(@PathVariable long gateway_id, @PathVariable long device_id) {
        try {
            logger.info("/gateway/detach requested");
            Gateway updated = gatewayService.detachDevice(gateway_id, device_id);
            logger.info("/gateway/detach responded");
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (GatewayNotFoundException | DeviceNotFoundException e) {
            logger.warn("/gateway/detach responded BAD_REQUEST", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/devices/{id}")
    public ResponseEntity<?> gatewayDevices(@PathVariable long id) {
        try {
            logger.info("/gateway/devices requested");
            List<Device> devices = gatewayService.gatewayDevices(id);
            logger.info("/gateway/devices responded OK");
            return new ResponseEntity<>(devices.toArray(new Device[0]), HttpStatus.OK);
        } catch (GatewayNotFoundException e) {
            logger.warn("/gateway/devices responded BAD REQUEST", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Although must exceptions are captured within the endpoints others like model validation exceptions may not be
     * captured. This function will ensure a proper response is given in such cases.
     *
     * @param ex exception thrown
     * @return a map with the fields that could not pass validation tests and the corresponding error message.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler( {MethodArgumentNotValidException.class})
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}
