package com.musala.gateway.management;

import com.musala.gateway.management.model.Gateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class GatewayValidatorTest {
    private Validator validator;

    @BeforeEach
    void initialize() {
        try (ValidatorFactory vf = Validation.buildDefaultValidatorFactory()) {
            validator = vf.getValidator();
        }
    }

    @Test
    void badIpAddress() {
        Gateway gateway = notValidIpGateway();
        Set<ConstraintViolation<Gateway>> violations = validator.validate(gateway);
        assertThat(violations.isEmpty()).isFalse();
        boolean matchIPValidationMessage = violations.stream().anyMatch(
                gatewayConstraintViolation -> gatewayConstraintViolation.getMessage()
                                                                        .equalsIgnoreCase("Invalid IP Address"));
        assertThat(matchIPValidationMessage).isTrue();
    }

    @Test
    void goodIpAddress(){
        Gateway gateway=validIpGateway();
        Set<ConstraintViolation<Gateway>> violations = validator.validate(gateway);
        assertThat(violations.isEmpty()).isTrue();
    }

    Gateway validIpGateway() {
        return new Gateway("SN", "test_gw", "10.8.6.50");
    }

    Gateway notValidIpGateway() {
        return new Gateway("SN", "test_gw", "not.valid.ip.address");
    }

}
