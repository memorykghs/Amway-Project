package com.exam.project.lottery.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author memorykghs
 * @date 2024/12/7
 */
@Data
@Builder
public class Prize {
    public Prize(String prize, double probability, int quantity) {
        this.prize = prize;
        this.probability = probability;
        this.quantity = quantity;
    }

    private String prize;
    private double probability;
    private int quantity;

    public void decreaseQuantity() {
        if (quantity > 0) {
            quantity--;
        }
    }
}
