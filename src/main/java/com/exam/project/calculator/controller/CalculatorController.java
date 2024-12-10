package com.exam.project.calculator.controller;

import com.exam.project.calculator.controller.dto.CalculatorReq;
import com.exam.project.calculator.controller.dto.CalculatorResp;
import com.exam.project.calculator.enums.OperateEnum;
import com.exam.project.calculator.service.CalculatorSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * @author memorykghs
 * @date 2024/12/7
 */
@RestController
@RequestMapping("/calculate")
public class CalculatorController {
    @Autowired
    private CalculatorSvc calculator;

    @GetMapping("/add")
    public CalculatorResp add(@RequestBody CalculatorReq calculatorDTO) {
        BigDecimal result = calculator.calculate(calculatorDTO, OperateEnum.ADD);
        return handleResult(result);
    }

    @GetMapping("/minus")
    public CalculatorResp minus(@RequestBody CalculatorReq calculatorDTO) {
        BigDecimal result = calculator.calculate(calculatorDTO, OperateEnum.MINUS);
        return handleResult(result);
    }

    @GetMapping("/multiply")
    public CalculatorResp multiply(@RequestBody CalculatorReq calculatorDTO) {
        BigDecimal result = calculator.calculate(calculatorDTO, OperateEnum.MULTIPLY);
        return handleResult(result);
    }

    @GetMapping("/divide")
    public CalculatorResp divide(@RequestBody CalculatorReq calculatorDTO) {
        BigDecimal result = calculator.calculate(calculatorDTO, OperateEnum.DIVIDE);
        return handleResult(result);
    }

    @GetMapping("/undo")
    public CalculatorResp undo() {
        return handleResult(calculator.undo());
    }

    @GetMapping("/redo")
    public CalculatorResp redo() {
        return handleResult(calculator.redo());
    }

    @GetMapping("/reset")
    public CalculatorResp reset() {
        calculator.reset();
        return CalculatorResp.builder()
                .status("SUCCESS")
                .message("reset success")
                .build();
    }

    private CalculatorResp handleResult(BigDecimal result) {
        return CalculatorResp.builder()
                .status("SUCCESS")
                .message("calculate success")
                .result(result.toPlainString())
                .build();
    }
}
