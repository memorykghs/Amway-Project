package com.exam.project.calculator.service;

import com.exam.project.calculator.enums.BiasEnum;
import com.exam.project.calculator.enums.OperateEnum;

import java.math.BigDecimal;

/**
 * @author memorykghs
 * @date 2024/12/7
 */
public interface IOperation {

    BigDecimal operate(BigDecimal num1, BigDecimal num2);

    BigDecimal operateWithBias(BigDecimal num1, BigDecimal num2, BiasEnum biasEnum);

    OperateEnum getOperateEnum();
}
