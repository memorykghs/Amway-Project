package com.exam.project.calculator.service;

import com.exam.project.calculator.enums.BiasEnum;
import com.exam.project.calculator.enums.OperateEnum;
import com.exam.project.calculator.exception.CalculateException;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * @author memorykghs
 * @date 2024/12/7
 */
@Component
public class DivideOperation implements IOperation {

    @Override
    public BigDecimal operate(BigDecimal num1, BigDecimal num2) {
        validate(num2);
        return num1.divide(num2);
    }

    @Override
    public BigDecimal operateWithBias(BigDecimal num1, BigDecimal num2, BiasEnum biasEnum) {
        validate(num2);
        return num1.divide(num2).multiply(biasEnum.getBias());
    }

    @Override
    public OperateEnum getOperateEnum() {
        return OperateEnum.DIVIDE;
    }

    /**
     * 除數不可為 0
     *
     * @param num
     */
    @SneakyThrows
    private void validate(BigDecimal num) {
        if (num.compareTo(BigDecimal.ZERO) == 0) {
            throw new CalculateException("Cannot divide by zero.");
        }
    }
}
