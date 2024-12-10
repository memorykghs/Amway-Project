package com.exam.project.calculator.service;

import com.exam.project.calculator.exception.CalculateException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author memorykghs
 * @date 2024/12/7
 */
@SpringBootTest
class DivideOperationTest {
    private final BigDecimal num1 = BigDecimal.TEN;

    private final BigDecimal num2 = BigDecimal.ONE;

    @Autowired
    private DivideOperation divideOperation;

    @Test
    void divide() {
        assertEquals(BigDecimal.TEN, divideOperation.operate(num1, num2));
    }

    @Test
    void divideByZero() {
        Exception e = assertThrows(CalculateException.class, () -> divideOperation.operate(num1, BigDecimal.ZERO));
        assertEquals("Cannot divide by zero.", e.getMessage());
    }
}