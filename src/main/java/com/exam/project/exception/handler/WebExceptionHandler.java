package com.exam.project.exception.handler;

import com.exam.project.calculator.controller.dto.CalculatorResp;
import com.exam.project.calculator.exception.CalculateException;
import com.exam.project.lottery.controller.dto.LotteryResp;
import com.exam.project.lottery.exception.LotteryProcessException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author memorykghs
 * @date 2024/12/8
 */
@RestControllerAdvice
public class WebExceptionHandler {

    @ExceptionHandler(LotteryProcessException.class)
    public LotteryResp handleLotteryException(LotteryProcessException e) {
        return LotteryResp.builder()
                .status("FAIL")
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler(CalculateException.class)
    public CalculatorResp handleCalculateException(CalculateException e) {
        return CalculatorResp.builder()
                .status("FAIL")
                .message(e.getMessage())
                .build();
    }
}
