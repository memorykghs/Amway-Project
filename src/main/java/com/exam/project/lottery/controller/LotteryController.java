package com.exam.project.lottery.controller;

import com.exam.project.lottery.controller.dto.LotteryReq;
import com.exam.project.lottery.controller.dto.LotteryResp;
import com.exam.project.lottery.exception.LotteryProcessException;
import com.exam.project.lottery.service.LotterySpinSvc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author memorykghs
 * @date 2024/12/8
 */
@RestController
@RequestMapping("/lottery")
@Slf4j
public class LotteryController {

    @Autowired
    private LotterySpinSvc lotterySvc;

    @PostMapping("/spin")
    public LotteryResp spin(@RequestBody LotteryReq req) throws LotteryProcessException {
        String userId = req.getUserId();

        log.info("user {} spin start", userId);
        LotteryResp lotteryResult = lotterySvc.spin(userId, req.getLotteryId());
        log.info("user {} spin end", userId);

        return lotteryResult;
    }

    @GetMapping("/init")
    public LotteryResp initLottery(@RequestBody LotteryReq req) throws LotteryProcessException {
        String lotteryId = req.getLotteryId();

        log.info("lottery {} init start", lotteryId);
        LotteryResp lotteryResult = lotterySvc.spin(lotteryId, req.getLotteryId());
        log.info("lotteryId {} init end", lotteryId);

        return lotteryResult;
    }
}
