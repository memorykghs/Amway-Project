package com.exam.project.calculator.service;

import com.exam.project.calculator.controller.dto.CalculatorReq;
import com.exam.project.calculator.enums.OperateEnum;
import com.exam.project.calculator.exception.CalculateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author memorykghs
 * @date 2024/12/7
 */
@SpringBootTest
class CalculatorTest {

    @Autowired
    private CalculatorSvc calculator;

    @BeforeEach
    void setUp() {
        calculator.reset();
    }

    @Test
    void calculate() {
        CalculatorReq calculatorDTO = CalculatorReq.builder()
                .num1(new BigDecimal("10"))
                .num2(new BigDecimal("20"))
                .build();

        assertEquals(new BigDecimal("30"), calculator.calculate(calculatorDTO, OperateEnum.ADD));
        assertEquals(new BigDecimal("-10"), calculator.calculate(calculatorDTO, OperateEnum.MINUS));
        assertEquals(new BigDecimal("200"), calculator.calculate(calculatorDTO, OperateEnum.MULTIPLY));
        assertEquals(new BigDecimal("0.5"), calculator.calculate(calculatorDTO, OperateEnum.DIVIDE));
    }

    @Test
    void calculateWithIllegalOperator() {
        CalculatorReq calculatorDTO = CalculatorReq.builder()
                .num1(new BigDecimal("10"))
                .num2(new BigDecimal("20"))
                .build();

        Exception e = assertThrows(CalculateException.class, () -> calculator.calculate(calculatorDTO, OperateEnum.MODULAR));
        assertEquals("This operation haven't been implement.", e.getMessage());
    }

    @Test
    void undo() {
        CalculatorReq calculatorDTO = CalculatorReq.builder()
                .num1(BigDecimal.TEN)
                .num2(new BigDecimal("20"))
                .build();

        calculator.calculate(calculatorDTO, OperateEnum.ADD);
        assertEquals(BigDecimal.ZERO, calculator.undo());
    }

    @Test
    void redo() {
        CalculatorReq calculatorDTO = CalculatorReq.builder()
                .num1(BigDecimal.TEN)
                .num2(new BigDecimal("20"))
                .build();

        BigDecimal result = calculator.calculate(calculatorDTO, OperateEnum.ADD);
        assertEquals(new BigDecimal("30"), result);
        assertEquals(BigDecimal.ZERO, calculator.undo());
        assertEquals(new BigDecimal("30"), calculator.redo());
    }
}