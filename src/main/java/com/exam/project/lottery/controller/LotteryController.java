package com.exam.project.lottery.controller;

import com.exam.project.lottery.exception.LotteryProcessException;
import com.exam.project.lottery.service.LotterySvc;
import com.exam.project.lottery.vo.Prize;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author memorykghs
 * @date 2024/12/8
 */
@RestController
@RequestMapping("/lottery")
@Slf4j
public class LotteryController {

    @Autowired
    private LotterySvc lotterySvc;

    @PostMapping("/spin")
    public Prize spin(@RequestBody String userId) throws LotteryProcessException {
        log.info("user {} spin start", userId);
        Prize prize = lotterySvc.spin(userId);
        log.info("user {} spin end", userId);

        return prize;
    }
}
