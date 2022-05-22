package com.musala.gateway.management.service;

import com.musala.gateway.management.exception.DeviceLimitException;
import com.musala.gateway.management.exception.DeviceNotFoundException;
import com.musala.gateway.management.exception.GatewayNotFoundException;
import com.musala.gateway.management.exception.NotValidGatewayException;
import com.musala.gateway.management.model.Device;
import com.musala.gateway.management.model.Gateway;
import com.musala.gateway.management.repository.GatewayRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
@Service
public class GatewayService {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private GatewayRepository gatewayRepository;

    @Autowired
    private DeviceService deviceService;
    @Value("${musala.max.gateway.devices}")
    private int maxDevices;

    /**
     * Default constructor
     */
    public GatewayService() {
        maxDevices = 10;
    }

    /**
     * Parametrized constructor. Since this class utilizes field injection this constructor is only used for test
     * proposes (is more convenient to create manually the instance than set up an entire spring context just to test
     * this class)
     *
     * @param gatewayRepository the repository for crud operations
     * @param deviceService     a device service instance for device management
     * @param maxDevices        max amount of devices allowed in a gateway
     */
    public GatewayService(GatewayRepository gatewayRepository, DeviceService deviceService, int maxDevices) {
        this();
        this.gatewayRepository = gatewayRepository;
        this.deviceService = deviceService;
        this.maxDevices = maxDevices;
    }

    /**
     * Creates a Gateway record.
     *
     * @param gateway Gateway information
     * @return The created record
     */
    public Gateway create(Gateway gateway) throws NotValidGatewayException {
        if (gateway.isIPAddressValid()) {
            logger.info("Gateway record created");
            return gatewayRepository.save(gateway);
        }
        NotValidGatewayException e = new NotValidGatewayException("Provided IP address is not valid");
        logger.error(e.getMessage(), e);
        throw e;
    }

    /**
     * Retrieves from the database the Gateway record specified by the id parameter.
     *
     * @param id Id of the gateway
     * @return The retrieved Gateway record
     * @throws RuntimeException thrown when the specified Gateway record does not exist
     */
    public Gateway gatewayById(long id) throws GatewayNotFoundException {
        Optional<Gateway> byId = gatewayRepository.findById(id);
        return byId.orElseThrow(() -> {
            logger.warn("Requested nonexistent gateway of id: " + id);
            return new GatewayNotFoundException("Gateway not found with ID: " + id);
        });
    }

    /**
     * Retrieves all Gateway records from the database
     *
     * @return List of all existing Gateway records
     */
    public List<Gateway> list() {
        logger.info("Listed all devices");
        return (List<Gateway>) gatewayRepository.findAll();
    }

    /**
     * Updates a specified Gateway record.
     *
     * @param gateway Information to be updated
     * @param id      id of the record to update
     * @return Updated Gateway record
     * @throws DeviceLimitException     thrown when the amount of devices in the Gateway record exceeds the predefined
     *                                  amount.
     * @throws GatewayNotFoundException thrown if the specified Gateway record to update does not exist
     */
    public Gateway updateGateway(Gateway gateway, long id)
            throws DeviceLimitException, GatewayNotFoundException, NotValidGatewayException {
        Gateway gwRecord = gatewayById(id);
        if(gwRecord!=null){
            if(gateway.isIPAddressValid()){
                gwRecord.setName(gateway.getName());
                gwRecord.setIpAddress(gateway.getIpAddress());
                gwRecord.setSerialNumber(gateway.getSerialNumber());
                logger.info("The gateway of id: " + id + " was updated");
                return gatewayRepository.save(gwRecord);
            }
            NotValidGatewayException notValidGatewayException =
                    new NotValidGatewayException("Provided IP address is not valid");
            logger.error(notValidGatewayException.getMessage(),notValidGatewayException);
            throw notValidGatewayException;
        }else{
            GatewayNotFoundException gatewayNotFoundException =
                    new GatewayNotFoundException("Gateway of id:" + id + " not found at update");
            logger.error(gatewayNotFoundException.getMessage(),gatewayNotFoundException);
            throw gatewayNotFoundException;
        }

    }

    /**
     * Attaches an existing device to a specified gateway. In case either the specified Gateway or Device are not
     * found an
     * exception is thrown. The specified devices will only be attached if the amount of currently attached devices
     * does not exceed the limit, otherwise an exception will be thrown.
     *
     * @param gatewayId Identifier of the Gateway to which the device will be attached
     * @param deviceId  Identifier of the Device to be attached
     * @return Updated Gateway record with the new Device attached
     * @throws GatewayNotFoundException thrown when the specified Gateway record is not found
     * @throws DeviceNotFoundException  thrown when the specified Device record is not found.
     * @throws DeviceLimitException     thrown when the specified Gateway has the maximum amount of devices attached.
     */
    public Gateway attachDevice(long gatewayId, long deviceId)
            throws GatewayNotFoundException, DeviceNotFoundException, DeviceLimitException {
        Gateway gateway;
        try {//Search for the specified gateway
            gateway = gatewayById(gatewayId);
        } catch (Throwable e) {
            logger.error("Could not attach device due to gateway not found", e);
            throw new GatewayNotFoundException("Gateway of id: " + gatewayId + " could not be found");
        }
        Device device;
        try {//Search for the specified device
            device = deviceService.deviceById(deviceId);
        } catch (Throwable e) {
            logger.error("Could not attach device to gateway due to device not found", e);
            throw new DeviceNotFoundException("Device of id: " + deviceId + " could not be found");
        }
        //Add this point both gateway and device have been found
        if (!gateway.getDevices().contains(
                device)) {//if the gateway already has de device attached is not necessary to perform any operation
          logger.info("Devices in gateway "+gateway.getDevices().size());
            if (gateway.getDevices().size()
                < maxDevices) {//Check if the amount of attached devices is less than the configured limit
                device.setGateway(gateway);
                deviceService.updateDevice(device,
                                           deviceId);//Since the device is the owner of the gateway the device record
                // is the one updated
                logger.info("Device attached to gateway, attempting to save the change");
            } else {
                throw new DeviceLimitException(
                        "The amount of devices exceeds the predefined limit of " + maxDevices + " devices");
            }
        }
        return gatewayById(gatewayId);
    }

    /**
     * Detaches the specified Device from the specified Gateway. If either the specified Gateway or Device don't
     * exist an exception will be raised. Furthermore, if the specified Device is not attached to the specified
     * Gateway an exception will also be thrown.
     *
     * @param gatewayId Gateway from which the Device will be detached.
     * @param deviceId  Device to be detached from the Gateway.
     * @return The updated Gateway record with the specified Device detached.
     * @throws GatewayNotFoundException thrown if the specified Gateway record does not exist.
     * @throws DeviceNotFoundException  thrown if the specified Device record does not exist or the Device is not
     *                                  attached to the Gateway.
     */
    public Gateway detachDevice(long gatewayId, long deviceId)
            throws GatewayNotFoundException, DeviceNotFoundException {
        Gateway gateway;
        try {//Search for the specified gateway
            gateway = gatewayById(gatewayId);
        } catch (Throwable e) {
            logger.error("Could not detach device due to gateway not found", e);
            throw new GatewayNotFoundException("Gateway of id: " + gatewayId + " could not be found");
        }
        Device device;
        try {//Search for the specified device
            device = deviceService.deviceById(deviceId);
        } catch (Throwable e) {
            logger.error("Could not detach device to gateway due to device not found", e);
            throw new DeviceNotFoundException("Device of id: " + deviceId + " could not be found");
        }

        if (device.getGateway()==null||device.getGateway().getId() != gatewayId) {
            logger.error("The specified device is not attached to the gateway");
            throw new DeviceNotFoundException(
                    "The specified device of id: " + deviceId + " is not attached to the specified gateway");
        }
        logger.info("Device detached from gateway");
        device.setGateway(null);
        deviceService.updateDevice(device, deviceId);
        return gatewayById(gatewayId);
    }

    /**
     * Retrieves the devices attached to a specified Gateway. If the specified Gateway does not exist an exception is
     * raised.
     *
     * @param gatewayId Gateway identifier
     * @return List of Devices attached to the Gateway.
     * @throws GatewayNotFoundException thrown if the specified Gateway does not exist.
     */
    public List<Device> gatewayDevices(long gatewayId) throws GatewayNotFoundException {
        Gateway gateway;
        try {//Search for the specified gateway
            gateway = gatewayById(gatewayId);
        } catch (Throwable e) {
            logger.error("Gateway of id:" + gatewayId + " not found", e);
            throw new GatewayNotFoundException("Gateway of id: " + gatewayId + " could not be found");
        }
        return gateway.getDevices();
    }


    /**
     * Attempts to delete a Gateway record.
     *
     * @param id Gateway to be deleted
     * @return True if the Gateway exists and therefore is deleted, False otherwise.
     */
    public boolean deleteGateway(long id) {
        if (gatewayRepository.existsById(id)) {
            gatewayRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }
}
