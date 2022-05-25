package com.musala.gateway.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musala.gateway.management.exception.GatewayNotFoundException;
import com.musala.gateway.management.exception.NotValidGatewayException;
import com.musala.gateway.management.model.Device;
import com.musala.gateway.management.model.DeviceStatus;
import com.musala.gateway.management.model.Gateway;
import com.musala.gateway.management.repository.DeviceRepository;
import com.musala.gateway.management.repository.GatewayRepository;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.sql.Date;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@ExtendWith(SpringExtension.class)
class GatewayIntegrationTest {
    @Autowired
    private WebApplicationContext applicationContext;
    @Autowired
    private GatewayRepository gatewayRepository;
    @Autowired
    DeviceRepository deviceRepository;
    @Autowired
    private ObjectMapper objectMapper;
    private MockMvc mockMvc;

    @BeforeEach
    void initialize() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

    /**
     * Checks ip validation upon gateway create request
     *
     * @throws Exception
     */
    @Test
    void createGatewayFailsOnInvalidIp() throws Exception {
        String gateway = objectMapper.writeValueAsString(notValidIpGateway());
        mockMvc.perform(post("/gateway/create").contentType(MediaType.APPLICATION_JSON).content(gateway))
               .andExpect(status().isBadRequest());
    }

    /**
     * Checks unique constraints error handling upon gateway create request.
     *
     * @throws Exception
     */
    @Test
    void createGatewayFailsOnUniqueConstraint() throws Exception {
        Gateway gateway = validIpGateway();
        String gatewayStr = objectMapper.writeValueAsString(gateway);
        mockMvc.perform(post("/gateway/create").contentType(MediaType.APPLICATION_JSON).content(gatewayStr))
               .andExpect(status().isCreated());
        Gateway created = gatewayRepository.findBySerialNumber(gateway.getSerialNumber()).orElse(null);
        assertThat(created).isNotNull();
        mockMvc.perform(post("/gateway/create").contentType(MediaType.APPLICATION_JSON).content(gatewayStr))
               .andExpect(status().isBadRequest());
    }

    /**
     * Checks if the creation of a gateway resource is successful with valid input.
     *
     * @throws Exception
     */
    @Test
    void createGatewaySucceeds() throws Exception {
        Gateway gateway = validIpGateway();
        String gatewayStr = objectMapper.writeValueAsString(gateway);
        mockMvc.perform(post("/gateway/create").contentType(MediaType.APPLICATION_JSON).content(gatewayStr))
               .andExpect(status().isCreated());
        Gateway created = gatewayRepository.findBySerialNumber(gateway.getSerialNumber()).orElse(null);
        assertThat(created).isNotNull();
        assertThat(created.getSerialNumber()).isEqualTo(gateway.getSerialNumber());
    }

    /**
     * Checks if updating a gateway with an existing serial number fails.
     *
     * @throws Exception
     */
    @Test
    void updateGatewayFailsOnUniqueSerialNumber() throws Exception {
        //First a gateway resource is created
        Gateway gateway = validIpGateway();
        String gatewayStr = objectMapper.writeValueAsString(gateway);
        mockMvc.perform(post("/gateway/create").contentType(MediaType.APPLICATION_JSON).content(gatewayStr))
               .andExpect(status().isCreated());
        Gateway created1 = gatewayRepository.findBySerialNumber(gateway.getSerialNumber()).orElse(null);
        assertThat(created1).isNotNull();
        Gateway gateway2 = validIpGateway();
        gateway2.setName("test_gw2");
        String gateway2Str = objectMapper.writeValueAsString(gateway2);
        //A second gateway resource is created so it can be modified later
        mockMvc.perform(post("/gateway/create").contentType(MediaType.APPLICATION_JSON).content(gateway2Str))
               .andExpect(status().isCreated());
        Gateway created2 = gatewayRepository.findBySerialNumber(gateway2.getSerialNumber()).orElse(null);
        assertThat(created2).isNotNull();
        //Change the serial number of second gateway to the first gateway serial number
        created2.setSerialNumber(created1.getSerialNumber());
        String updGW2 = objectMapper.writeValueAsString(created2);
        Map<String, String> expectedContent = new HashMap<>();
        expectedContent.put(NotValidGatewayException.class.getSimpleName(),
                            "A gateway with serial number: " + created1.getSerialNumber() + " already exist");
        //when the update operation is performed it should fail because of unique constraints.
        mockMvc.perform(
                       put("/gateway/update/" + created2.getId()).contentType(MediaType.APPLICATION_JSON).content(updGW2))
               .andExpect(status().isBadRequest())
               .andExpect(content().string(objectMapper.writeValueAsString(expectedContent)));


    }

    /**
     * Checks if updating a gateway fails when the specified gateway does not exist.
     *
     * @throws Exception
     */
    @Test
    void updateGatewayFailsOnGatewayNotFound() throws Exception {
        Gateway gateway = validIpGateway();
        String gatewayStr = objectMapper.writeValueAsString(gateway);
        mockMvc.perform(post("/gateway/create").contentType(MediaType.APPLICATION_JSON).content(gatewayStr))
               .andExpect(status().isCreated());
        Gateway createdGW = gatewayRepository.findBySerialNumber(gateway.getSerialNumber()).orElse(null);
        assertThat(createdGW).isNotNull();
        createdGW.setName("updated_name");
        int non_existingId = 100;
        Map<String, String> expectedContent = new HashMap<>();
        expectedContent.put(GatewayNotFoundException.class.getSimpleName(),
                            "Gateway not found with ID: " + non_existingId);
        mockMvc.perform(put("/gateway/update/" + non_existingId).contentType(MediaType.APPLICATION_JSON)
                                                                .content(objectMapper.writeValueAsString(createdGW)))
               .andExpect(status().isBadRequest())
               .andExpect(content().string(objectMapper.writeValueAsString(expectedContent)));

    }


    @Test
    void attachDeviceSucceeds() throws Exception {
        Gateway gateway = validIpGateway();
        String gatewayStr = objectMapper.writeValueAsString(gateway);
        //Create a gateway instance
        mockMvc.perform(post("/gateway/create").contentType(MediaType.APPLICATION_JSON).content(gatewayStr))
               .andExpect(status().isCreated());
        gateway = gatewayRepository.findBySerialNumber(gateway.getSerialNumber()).orElse(null);
        assertThat(gateway).isNotNull();
        Device device = getTestDevice();
        String deviceStr = objectMapper.writeValueAsString(device);
        mockMvc.perform(post("/device/create").contentType(MediaType.APPLICATION_JSON).content(deviceStr))
               .andExpect(status().isCreated());
        device = deviceRepository.findByUID(device.getUid()).orElse(null);
        assertThat(device).isNotNull();
        mockMvc.perform(put("/gateway/" + gateway.getId() + "/attach/" + device.getId())).andExpect(status().isOk());
        Device updatedDevice = deviceRepository.findByUID(device.getUid()).orElse(null);
        assertThat(updatedDevice).isNotNull().has(new Condition<>(new Predicate<Device>() {
            private Gateway gateway = null;

            @Override
            public boolean test(Device device) {
                return device.getGateway().getId() == gateway.getId();
            }

            public Predicate<Device> setGateway(Gateway gateway) {
                this.gateway = gateway;
                return this;
            }
        }.setGateway(gateway), "Checks if contains the gateway"));
    }

    @Test
    void detachDeviceSuccessful() throws Exception {
        Device device = getTestDevice();
        Gateway gateway = validIpGateway();
        //Creates the gateway that will have the device attached
        Gateway createdGw = gatewayRepository.save(gateway);
        assertThat(createdGw).isNotNull();
        device.setGateway(gateway);
        //Create a device with the associated gateway
        Device createdDev = deviceRepository.save(device);
        assertThat(createdDev).isNotNull();
        mockMvc.perform(put("/gateway/" + createdGw.getId() + "/detach/" + createdDev.getId()))
               .andExpect(status().isOk());
        Gateway updated = gatewayRepository.findById(createdGw.getId()).orElse(null);
        assertThat(updated).isNotNull().doesNotHave(new Condition<>(new Predicate<Gateway>() {
            private Device device;
            @Override
            public boolean test(Gateway gateway) {
                for (Device d : gateway.getDevices()) {
                    if (d.getId() == device.getId()) {
                        return true;
                    }
                }
                return false;
            }

            Predicate<Gateway> setDevice(Device device) {
                this.device = device;
                return this;
            }
        }.setDevice(createdDev), "Checks if the device was detached"));

    }

    Gateway validIpGateway() {
        return new Gateway(UUID.randomUUID().toString(), "test_gw", "10.8.6.50");
    }

    Gateway notValidIpGateway() {
        return new Gateway(UUID.randomUUID().toString(), "test_gw", "not.valid.ip.address");
    }

    Device getTestDevice() {
        return new Device(new Random().nextLong(), "Huawei", Date.from(Instant.now()), DeviceStatus.ONLINE);
    }


}

