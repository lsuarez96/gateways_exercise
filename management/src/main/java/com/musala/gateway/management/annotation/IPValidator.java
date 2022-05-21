package com.musala.gateway.management.annotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IPValidator implements ConstraintValidator<IPConstraint, String> {
    @Override
    public void initialize(IPConstraint constraintAnnotation) {
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        //For each segment there are 255 possible values
        //Single decimal (Eg. 1.), 100 range (Eg192.), 200 range (Eg 243.), 250 range (Eg. 255.)
        String segment = "(\\d{1,2}|(0|1)\\d{2}|2[0-4]\\d|25[0-5])";
        //An IP address is composed of 4 segments, delimited by a dot (.)
        String ipRegex = segment + "\\." + segment + "\\." + segment + "\\." + segment;
        return s.matches(ipRegex);
    }
}
