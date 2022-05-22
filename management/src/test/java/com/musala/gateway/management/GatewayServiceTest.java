package com.musala.gateway.management;

import com.musala.gateway.management.exception.DeviceLimitException;
import com.musala.gateway.management.exception.DeviceNotFoundException;
import com.musala.gateway.management.exception.GatewayNotFoundException;
import com.musala.gateway.management.exception.NotValidGatewayException;
import com.musala.gateway.management.model.Device;
import com.musala.gateway.management.model.DeviceStatus;
import com.musala.gateway.management.model.Gateway;
import com.musala.gateway.management.repository.DeviceRepository;
import com.musala.gateway.management.repository.GatewayRepository;
import com.musala.gateway.management.service.DeviceService;
import com.musala.gateway.management.service.GatewayService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GatewayServiceTest {

    @Mock
    private GatewayRepository gatewayRepository;
    @Mock
    private DeviceRepository deviceRepository;
    private GatewayService gatewayService;

    @BeforeEach
    void initialize() {
        DeviceService deviceService = new DeviceService(deviceRepository);
        gatewayService = new GatewayService(gatewayRepository, deviceService, 2);
    }

    /**
     * Tests whether ip validation detects mal formed IP addresses and the creation of a Gateway with bad IP address
     * fails.
     */
    @Test
    void gatewayCreationFailsOnNotValidIP() {
        Gateway gateway = notValidIpGateway();
        assertThat(gateway.isIPAddressValid()).isFalse().withFailMessage("IP validation not working");
        Assertions.assertThrows(NotValidGatewayException.class, () -> gatewayService.create(gateway));
        assertThat(new Gateway("sn", "gw", "256.0.0.255").isIPAddressValid()).isFalse().withFailMessage(
                "IP validation not working");
    }

    /**
     * Tests whether the creation of a Gateway with valid IP address succeeds.
     * @throws NotValidGatewayException
     */
    @Test
    void gatewayCreationSucceedsOnValidIP() throws NotValidGatewayException {
        Gateway gateway = validIpGateway();
        assertThat(gateway.isIPAddressValid()).isTrue();
        when(gatewayRepository.save(any(Gateway.class))).thenReturn(gateway);
        assertThat(gatewayService.create(gateway)).isEqualTo(gateway);
    }

    /**
     * Tests whether a GatewayNotFoundException is raised when a non-existing Gateway is requested
     */
    @Test
    void requestForNonExistingGatewayRaisesException() {
        long id = 0;
        when(gatewayRepository.findById(id)).thenReturn(Optional.empty());
        Assertions.assertThrows(GatewayNotFoundException.class, () -> gatewayService.gatewayById(id));
    }

    /**
     * Tests whether gateway update operation fails when the specified gateway does not exist
     */
    @Test
    void gatewayUpdateFailsOnGatewayNotFound() {
        long id = 0;
        when(gatewayRepository.findById(id)).thenReturn(Optional.empty());
        Assertions.assertThrows(GatewayNotFoundException.class,
                                () -> gatewayService.updateGateway(validIpGateway(), id));

    }

    /**
     * Tests if a gateway update fails when a bad ip address is specified.
     */
    @Test
    void gatewayUpdateFailsOnBadIP() {
        long id = 0;
        Gateway gateway = notValidIpGateway();
        gateway.setId(id);
        when(gatewayRepository.findById(id)).thenReturn(Optional.of(maxDevicesGateway()));
        Assertions.assertThrows(NotValidGatewayException.class, () -> gatewayService.updateGateway(gateway, id));
    }

    /**
     * Tests whether the attach device operation fails when the device amount limit is exceeded.
     */
    @Test
    void gatewayAttachDeviceFailsBecauseDeviceLimit() {
        long gwId = 0;
        long devId = 3;
        when(gatewayRepository.findById(gwId)).thenReturn(Optional.of(maxDevicesGateway()));
        when(deviceRepository.findById(devId)).thenReturn(Optional.of(getTestDevice()));
        Assertions.assertThrows(DeviceLimitException.class, () -> gatewayService.attachDevice(gwId, devId));
    }

    @Test
    void gatewayDetachDeviceFailsBecauseDeviceNotAssigned() {
        long gwId = 0;
        long devId = 3;
        when(gatewayRepository.findById(gwId)).thenReturn(Optional.of(maxDevicesGateway()));
        when(deviceRepository.findById(devId)).thenReturn(Optional.of(getTestDevice()));
        DeviceNotFoundException ex =
                Assertions.assertThrows(DeviceNotFoundException.class, () -> gatewayService.detachDevice(gwId, devId));
        assertThat(ex.getMessage()).isEqualTo(
                "The specified device of id: " + devId + " is not attached to the specified gateway");

    }

    Device getTestDevice() {
        return new Device(3, "Huawei", Date.from(Instant.now()), DeviceStatus.ONLINE);
    }

    Gateway validIpGateway() {
        return new Gateway("SN", "test_gw", "10.8.6.50");
    }

    Gateway notValidIpGateway() {
        return new Gateway("SN", "test_gw", "not.valid.ip.address");
    }

    Gateway maxDevicesGateway() {
        Gateway gateway = new Gateway("SN", "test_gw", "10.8.6.50");
        gateway.getDevices().addAll(Arrays.asList(new Device(1, "Sony", Date.from(Instant.now()), DeviceStatus.ONLINE),
                                                  new Device(2, "Panasonic", Date.from(Instant.now()),
                                                             DeviceStatus.ONLINE)));
        return gateway;
    }
}
