package com.exam.project.lottery.service;

import com.exam.project.lottery.controller.dto.LotteryReq;
import com.exam.project.lottery.controller.dto.LotteryResp;
import com.exam.project.lottery.exception.LotteryProcessException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author memorykghs
 * @date 2024/12/7
 */
@Service
@Slf4j
public class LotterySpinSvc {

    private static final String LOTTERY_LOCK_PREFIX = "lottery:lock:";
    private static final String THANKS = "thanks"; // 銘謝惠顧
    private static final Map<String, LinkedHashMap<String, Double>> lotteryCollections = new HashMap<>();

    private final RedissonClient redissonClient;
    private final LotterySvc lotterySvc;

    public LotterySpinSvc(RedissonClient redissonClient,
                          LotterySvc lotterySvc) {
        this.redissonClient = redissonClient;
        this.lotterySvc = lotterySvc;
    }

    public LotteryResp spin(String userId, String lotteryId) throws LotteryProcessException {
        RLock lock = redissonClient.getLock(LOTTERY_LOCK_PREFIX + userId);
        try {
            if (!lock.tryLock()) {
                throw new LotteryProcessException("You are in the spinning process");
            }

            isDuplicate(userId, lotteryId); // 檢查是否已經中獎過
            String prize = executeLottery(lotteryId); // 執行抽獎
            lotterySvc.updateUserLotteryHistory(userId, prize, lotteryId); // 更新用戶抽獎紀錄
            return LotteryResp.builder()
                    .status("SUCCESS")
                    .prize(prize)
                    .build();

        } finally {
            // 解鎖
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 判斷是否已中獎
     *
     * @param userId
     * @throws LotteryProcessException
     */
    protected void isDuplicate(String userId, String lotteryId) throws LotteryProcessException {
        if (lotterySvc.isUserHistoryExist(userId, lotteryId)) {
            log.error("The user has got the prize.");
            throw new LotteryProcessException("You have got the prize.");
        }
    }

    /**
     * 執行抽獎邏輯
     *
     * @return
     */
    private String executeLottery(String lotteryId) throws LotteryProcessException {
        double random = random();
        double cumulativeProbability = 0.0;

        LinkedHashMap<String, Double> probabilityMap = getProbability(lotteryId);
        for (String prize : probabilityMap.keySet()) {
            cumulativeProbability += probabilityMap.get(prize);

            // 看抽中哪個獎品，更新剩餘數量
            // 銘謝惠顧則不需要更新
            int quantity = lotterySvc.getPrizeQuantity(lotteryId, prize);
            if (random <= cumulativeProbability && quantity > 0 && !THANKS.equals(prize)) {
                log.info("current prize: {}, remain quantity: {}", prize, quantity);

                // 針對每個不同獎項加鎖以提高效率
                String prizeKey = LOTTERY_LOCK_PREFIX + lotteryId + ':' + prize;
                RLock lock = redissonClient.getSpinLock(prizeKey);
                try {
                    boolean getLock = lock.tryLock(3, 10, TimeUnit.SECONDS);
                    if (!getLock) {
                        log.info("System busy.");
                        throw new LotteryProcessException("Please retry after a few moment");
                    }

                    // 再次確認是否還有剩餘獎品
                    quantity = lotterySvc.getPrizeQuantity(lotteryId, prize);
                    if (quantity == 0) {
                        log.info("prize {} remain quantity is 0", prize);
                        return THANKS;
                    }

                    // 更新剩餘數量
                    lotterySvc.decreaseQuantity(lotteryId, prize);

                } catch (InterruptedException e) {
                    log.error("get prize lock fail: {}", e.getMessage());
                    throw new LotteryProcessException("Please retry after a few moment");

                } finally {
                    // 解鎖
                    if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
                return prize;
            }
        }
        return THANKS;
    }

    private LinkedHashMap<String, Double> getProbability(String lotteryId) {
        if (lotteryCollections.get(lotteryId) == null) {
            lotteryCollections.put(lotteryId, lotterySvc.getPrizeProbabilityFromRedis(lotteryId));
        }
        return lotteryCollections.get(lotteryId);
    }

    protected double random() {
        return Math.random();
    }
}
