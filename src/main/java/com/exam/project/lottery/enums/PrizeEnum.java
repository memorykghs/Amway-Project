package com.exam.project.lottery.enums;

/**
 * @author memorykghs
 * @date 2024/12/7
 */
public enum PrizeEnum {
    GOLDEN("GOLDEN", 0.01d, 1),
    SILVER("SILVER", 0.05d, 10),
    BRONZE("BRONZE", 0.1d, 20);

    PrizeEnum(String prize, double probability, int quantity) {
        this.prize = prize;
        this.probability = probability;
        this.quantity = quantity;
    }

    private String prize;
    private double probability;
    private int quantity;

    public String getPrize() {
        return this.prize;
    }

    public double getProbability() {
        return this.probability;
    }

    public int getQuantity() {
        return this.quantity;
    }
}
