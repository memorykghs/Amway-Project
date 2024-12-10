package com.exam.project.calculator.exception;

/**
 * @author memorykghs
 * @date 2024/12/8
 */
public class CalculateException extends Exception{

    public CalculateException() {
    }

    public CalculateException(String message) {
        this.message = message;
    }

    private String message;

    public String getMessage(){
        return message;
    }
}
