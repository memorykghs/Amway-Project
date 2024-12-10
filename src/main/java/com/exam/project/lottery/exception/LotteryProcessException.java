package com.exam.project.lottery.exception;

/**
 * @author memorykghs
 * @date 2024/12/8
 */
public class LotteryProcessException extends Exception {
    public LotteryProcessException() {
    }

    public LotteryProcessException(String message) {
        this.message = message;
    }

    private String message;

    public String getMessage(){
        return message;
    }
}
