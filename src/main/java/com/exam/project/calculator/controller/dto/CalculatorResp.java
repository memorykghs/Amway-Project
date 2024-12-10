package com.exam.project.calculator.controller.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author memorykghs
 * @date 2024/12/8
 */
@Data
@Builder
public class CalculatorResp {

    private String status;
    private String message;
    private String result;
}
