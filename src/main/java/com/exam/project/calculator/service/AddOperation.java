package com.exam.project.calculator.service;

import com.exam.project.calculator.enums.BiasEnum;
import com.exam.project.calculator.enums.OperateEnum;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * @author memorykghs
 * @date 2024/12/7
 */
@Component
public class AddOperation implements  IOperation{

    @Override
    public BigDecimal operate(BigDecimal num1, BigDecimal num2) {
        return num1.add(num2);
    }

    @Override
    public BigDecimal operateWithBias(BigDecimal num1, BigDecimal num2, BiasEnum biasEnum){
        return num1.add(num2).multiply(biasEnum.getBias());
    }

    @Override
    public OperateEnum getOperateEnum() {
        return OperateEnum.ADD;
    }
}
