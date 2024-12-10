package com.exam.project.calculator.controller.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author memorykghs
 * @date 2024/12/7
 */
@Data
@Builder
public class CalculatorReq {
    private BigDecimal num1;
    private BigDecimal num2;
}
