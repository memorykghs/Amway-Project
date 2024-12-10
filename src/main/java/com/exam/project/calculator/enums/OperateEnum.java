package com.exam.project.calculator.enums;

/**
 * @author memorykghs
 * @date 2024/12/7
 */
public enum OperateEnum {
    ADD("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    MODULAR("%");

    OperateEnum(String operator){
        this.operator = operator;
    }

    private String operator;

    public String getOperator(){
        return this.operator;
    }
}
