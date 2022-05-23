package com.musala.gateway.management.service;

import com.musala.gateway.management.exception.DeviceNotFoundException;
import com.musala.gateway.management.exception.NotValidDeviceException;
import com.musala.gateway.management.model.Device;
import com.musala.gateway.management.repository.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceService {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    DeviceRepository deviceRepository;

    public DeviceService() {
    }

    /**
     * Parametrized constructor. Since this class utilizes field injection this constructor is only used for test
     * proposes (is more convenient to create manually the instance than set up an entire spring context just
     * to test
     * this class)
     *
     * @param deviceRepository crud repository
     */
    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    /**
     * List all Device records stored in the database.
     *
     * @return List of all Device records
     */
    public List<Device> list() {
        return (List<Device>) deviceRepository.findAll();
    }

    /**
     * Retrieves the specified Device record from the database. If the specified Device does not exist an exception is
     * raised.
     *
     * @param id Identifier of the Device to retrieve.
     * @return Device record corresponding to the specified id.
     * @throws DeviceNotFoundException thrown if a Device record with the specified id does not exist.
     */
    public Device deviceById(long id) throws DeviceNotFoundException {
        return deviceRepository.findById(id).orElseThrow(() -> {
            logger.warn("Requested nonexistent device of id: " + id);
            return new DeviceNotFoundException("Device not found with ID: " + id);
        });
    }

    /**
     * Creates a new Device record.
     *
     * @param device Device information.
     * @return Stored Device Record
     */
    public Device createDevice(Device device) throws NotValidDeviceException {
        Device byUid = deviceRepository.findByUID(device.getUid()).orElse(null);
        if (byUid == null) {
            Device save = deviceRepository.save(device);
            logger.info("Device with id: " + save.getId() + " created");
            return save;
        }
        throw new NotValidDeviceException("A device with the specified uid already exist");
    }

    /**
     * Modifies the specified Device record.
     *
     * @param device Device information to perform the update.
     * @param id     specified Device record to be updated
     * @return Modified Device record.
     */
    public Device updateDevice(Device device, long id) throws DeviceNotFoundException, NotValidDeviceException {
        Device update = deviceRepository.findById(id).orElse(null);
        if (update == null) {
            throw new DeviceNotFoundException(
                    "The specified Device with id: " + id + " could not be modified because it does not exist");
        }
        Device byUid = deviceRepository.findByUID(device.getUid()).orElse(null);
        if (byUid == null || byUid.getId() == id) {
            update.setDeviceStatus(device.getDeviceStatus());
            if (device.getCreatedAt() != null) {
                update.setCreatedAt(device.getCreatedAt());
            }
            update.setUid(device.getUid());
            update.setVendor(device.getVendor());
            logger.info("Device of id: " + id + " updated");
            return deviceRepository.save(update);
        }
        throw new NotValidDeviceException("The specified uid is asociated to another device");
    }

    /**
     * Attempts to delete a Device record.
     *
     * @param id Identifier of the device to be deleted.
     * @return True if the specified Device exists, False otherwise.
     */
    public boolean deleteDevice(long id) {
        if (deviceRepository.existsById(id)) {
            deviceRepository.deleteById(id);
            logger.info("Device of id: " + id + " deleted");
            return true;
        }
        logger.warn("Device of id: " + id + " could not be deleted because it does not exist");
        return false;
    }

    public void setDeviceRepository(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }
}
