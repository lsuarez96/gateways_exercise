package com.musala.gateway.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musala.gateway.management.model.Gateway;
import com.musala.gateway.management.repository.GatewayRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    private ObjectMapper objectMapper;
    private MockMvc mockMvc;

    @BeforeEach
    void initialize() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

    @Test
    void createGatewayFailsOnInvalidIp() throws Exception {
        String gateway = objectMapper.writeValueAsString(notValidIpGateway());
        mockMvc.perform(post("/gateway/create").contentType(MediaType.APPLICATION_JSON).content(gateway))
               .andExpect(status().isBadRequest());
    }

    @Test
    void createGatewaySucceeds() throws Exception {
        String gateway = objectMapper.writeValueAsString(validIpGateway());
        mockMvc.perform(post("/gateway/create").contentType(MediaType.APPLICATION_JSON).content(gateway))
               .andExpect(status().isCreated());
        Gateway actual = gatewayRepository.findBySerialNumber(validIpGateway().getSerialNumber()).orElse(null);
        assertThat(actual).isNotNull();
        assertThat(actual.getSerialNumber()).isEqualTo(validIpGateway().getSerialNumber());
    }


    Gateway validIpGateway() {
        return new Gateway("SN", "test_gw", "10.8.6.50");
    }

    Gateway notValidIpGateway() {
        return new Gateway("SN", "test_gw", "not.valid.ip.address");
    }

}

