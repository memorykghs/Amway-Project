package com.exam.project.calculator.enums;

import java.math.BigDecimal;

/**
 * @author memorykghs
 * @date 2024/12/7
 */
public enum BiasEnum {

    AMERICA(new BigDecimal("32")),
    JAPANE(new BigDecimal("0.23"));

    BiasEnum(BigDecimal bias){
        this.bias = bias;
    }

    private BigDecimal bias;

    public BigDecimal getBias(){
        return bias;
    }
}
