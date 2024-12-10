package com.exam.project.calculator.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author memorykghs
 * @date 2024/12/7
 */
@SpringBootTest
class MultiplyOperationTest {

    private final BigDecimal num1 = BigDecimal.TEN;

    private final BigDecimal num2 = BigDecimal.ONE;

    @Autowired
    private MultiplyOperation multiplyOperation;

    @Test
    void add() {
        assertEquals(new BigDecimal("10"), multiplyOperation.operate(num1, num2));
    }
}