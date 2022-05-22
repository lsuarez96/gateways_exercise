package com.musala.gateway.management.config;

import com.musala.gateway.management.model.Device;
import com.musala.gateway.management.model.DeviceStatus;
import com.musala.gateway.management.model.Gateway;
import com.musala.gateway.management.repository.DeviceRepository;
import com.musala.gateway.management.repository.GatewayRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Date;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Configuration
public class InitializerCfg {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    GatewayRepository gatewayRepository;
    @Autowired
    DeviceRepository deviceRepository;

    @Bean
    public CommandLineRunner dbInitializer() {
        return args -> {
            Gateway gateway0 = new Gateway("gw0", "gateway0", "10.8.6.50");
            Gateway gateway1 = new Gateway("gw1", "gateway0", "10.8.6.51");
            Gateway gateway2 = new Gateway("gw2", "gateway0", "10.8.6.52");
            List<Gateway> gatewayList =
                    (List<Gateway>) gatewayRepository.saveAll(Arrays.asList(gateway0, gateway1, gateway2));
            logger.info(gatewayList.toString());

            List<Device> devices = (List<Device>) deviceRepository.saveAll(
                    Arrays.asList(new Device(1, "Sony", Date.from(Instant.now()), DeviceStatus.ONLINE, gateway0),
                                  new Device(2, "Apple", Date.from(Instant.now()), DeviceStatus.ONLINE, gateway0),
                                  new Device(3, "Cisco", Date.from(Instant.now()), DeviceStatus.ONLINE, gateway0),
                                  new Device(4, "Huawei", Date.from(Instant.now()), DeviceStatus.ONLINE, gateway0),
                                  new Device(5, "Panasonic", Date.from(Instant.now()), DeviceStatus.ONLINE, gateway0),
                                  new Device(6, "LG", Date.from(Instant.now()), DeviceStatus.ONLINE, gateway0),
                                  new Device(7, "Samsung", Date.from(Instant.now()), DeviceStatus.ONLINE, gateway0),
                                  new Device(8, "Logitech", Date.from(Instant.now()), DeviceStatus.ONLINE, gateway0),
                                  new Device(9, "TP-Link", Date.from(Instant.now()), DeviceStatus.ONLINE, gateway0),
                                  new Device(10, "Hawlett-Packard", Date.from(Instant.now()), DeviceStatus.ONLINE,
                                             gateway0),
                                  new Device(11, "IBM", Date.from(Instant.now()), DeviceStatus.ONLINE),
                                  new Device(12, "AMD", Date.from(Instant.now()), DeviceStatus.ONLINE),
                                  new Device(13, "Intel", Date.from(Instant.now()), DeviceStatus.ONLINE)));
            logger.info(devices.toString());
        };

    }
}
