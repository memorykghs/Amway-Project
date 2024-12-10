package com.exam.project.lottery.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author memorykghs
 * @date 2024/12/8
 */
@Data
@AllArgsConstructor
public class LotteryReq {
    private String userId;
    private String lotteryId;
}
