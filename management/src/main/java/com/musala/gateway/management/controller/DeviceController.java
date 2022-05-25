package com.musala.gateway.management.controller;

import com.musala.gateway.management.exception.DeviceNotFoundException;
import com.musala.gateway.management.exception.NotValidDeviceException;
import com.musala.gateway.management.model.Device;
import com.musala.gateway.management.service.DeviceService;
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
@RequestMapping("/device")
public class DeviceController {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    DeviceService deviceService;

    /**
     * Retrieves all devices. In case no record exists a 204 NO_CONTENT response will be returned. Otherwise, a 200 OK
     * status code will be returned along with the list of devices.
     *
     * @return List of devices.
     */
    @GetMapping("/list")
    public ResponseEntity<?> list() {
        logger.info("Request at: /device/list");
        List<Device> list = deviceService.list();
        if (!list.isEmpty()) {
            logger.info("/device/list responded OK");
            return new ResponseEntity<>(list.toArray(new Device[0]), HttpStatus.OK);
        }
        logger.info("/device/list did not yield any results");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Retrieves a device from the database given an id. If the requested device does not exist a 204 NO_CONTENT
     * responses will be issued.
     *
     * @param id Identifier of the requested device
     * @return The requested device in case it exists.
     */
    @GetMapping("/view/{id}")
    public ResponseEntity<?> viewDevice(@PathVariable long id) {
        try {
            logger.info("Request at: /device/view/" + id);
            Device device = deviceService.deviceById(id);
            logger.info("/device/view/" + id + " responded OK");
            return new ResponseEntity<>(device, HttpStatus.OK);
        } catch (DeviceNotFoundException e) {
            logger.error("/device/view/" + id + " responded NO CONTENT", e);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    /**
     * Creates a new device
     *
     * @param device Information of the device to be created
     * @return The device stored in the database.
     */
    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody Device device) throws NotValidDeviceException {
        Device created = deviceService.createDevice(device);
        return new ResponseEntity<>(created, HttpStatus.CREATED);

    }

    /**
     * Endpoint for updating the information of a device. If device identifiable by the supplied id does not exist a
     * new one with the specified id will be created.
     *
     * @param device Device object encapsulating the device information
     * @param id     Identifier of the device to be updated
     * @return The resulting device from the update (or newly created device in case the specified one did not exist).
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody Device device, @PathVariable long id)
            throws DeviceNotFoundException, NotValidDeviceException {
        logger.info("Requested /device/update/" + id);
        Device updated = deviceService.updateDevice(device, id);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) {
        logger.info("Requested /device/delete/" + id);
        boolean deleted = deviceService.deleteDevice(id);
        logger.info("Device of id: " + id + (deleted ? " deleted, /device/delete/" + id + " responded OK" :
                                             " could not be deleted, /device/delete/" + id
                                             + " responded NO_CONTENT status"));
        return new ResponseEntity<>(deleted ? HttpStatus.OK : HttpStatus.NO_CONTENT);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler( {
            MethodArgumentNotValidException.class, DeviceNotFoundException.class, NotValidDeviceException.class
    })
    @ResponseBody
    public Map<String, String> handleValidationExceptions(Exception ex) {
        Map<String, String> errors = new HashMap<>();
        if (ex instanceof MethodArgumentNotValidException) {
            handleModelValidationsErrors((MethodArgumentNotValidException) ex, errors);
        } else {
            errors.put(ex.getClass().getSimpleName(), ex.getMessage());
        }
        return errors;
    }

    private void handleModelValidationsErrors(MethodArgumentNotValidException ex, Map<String, String> errors) {
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
    }
}
