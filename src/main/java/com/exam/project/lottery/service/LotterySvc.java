package com.exam.project.lottery.service;

import com.exam.project.lottery.enums.PrizeEnum;
import com.exam.project.lottery.exception.LotteryProcessException;
import com.exam.project.lottery.vo.Prize;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author memorykghs
 * @date 2024/12/7
 */
@Service
@Slf4j
public class LotterySvc {

    private static final String LOTTERY_LOCK_PREFIX = "lottery:lock:";
    private static final String LOTTERY_PRIZE_PREFIX = "lottery:prize:";
    private static final String THANKS = "THANKS"; // 銘謝惠顧
    private final Map<String, String> userLotteryHistory = new ConcurrentHashMap<>(); // 用戶抽獎紀錄
    private Map<String, Prize> prizeMap; // 獎品類別

    private final RedissonClient redissonClient;

    public LotterySvc(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        initPrize();
    }

    public Prize spin(String userId) throws LotteryProcessException {
        RLock lock = redissonClient.getLock(LOTTERY_LOCK_PREFIX + userId);
        try {
            if (!lock.tryLock()) {
                throw new LotteryProcessException("You are in the spinning process");
            }

            isDuplicate(userId); // 檢查是否已經中獎過
            Prize prize = executeLottery(); // 執行抽獎
            updateUserLotteryHistory(userId, prize); // 更新用戶抽獎紀錄
            return prize;

        } finally {
            // 解鎖
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void initPrize() {

        prizeMap = Arrays.stream(PrizeEnum.values())
                .map(prizeEnum ->
                        Prize.builder()
                                .prize(prizeEnum.getPrize())
                                .probability(prizeEnum.getProbability())
                                .quantity(prizeEnum.getQuantity())
                                .build()
                )
                .sorted(Comparator.comparingDouble(Prize::getProbability))
                .collect(Collectors.toMap(
                        Prize::getPrize,
                        prize -> prize,
                        (prize1, prize2) -> prize1.getProbability() <= prize2.getProbability() ? prize1 : prize2,
                        LinkedHashMap::new));

        // 計算獎品的機率，不足的全部設為銘謝惠顧
        double totalProbability = prizeMap.keySet()
                .stream()
                .mapToDouble(key -> prizeMap.get(key).getProbability())
                .sum();

        // 加入銘謝惠顧
        if (totalProbability < 1.0) {
            prizeMap.put(THANKS, Prize.builder()
                    .prize(THANKS)
                    .probability(1.0 - totalProbability)
                    .quantity(Integer.MAX_VALUE)
                    .build()
            );
        }
    }

    protected void isDuplicate(String userId) throws LotteryProcessException {
        if (userLotteryHistory.containsKey(userId)) {
            log.error("The user has got the prize.");
            throw new LotteryProcessException("You have got the prize.");
        }
    }

    /**
     * 執行抽獎邏輯
     *
     * @return
     */
    private Prize executeLottery() throws LotteryProcessException {
        double random = random();
        double cumulativeProbability = 0.0;

        for (String key : prizeMap.keySet()) {
            Prize prize = prizeMap.get(key);
            cumulativeProbability += prize.getProbability();

            // 看抽中哪個獎品，更新剩餘數量
            // 銘謝惠顧則不需要更新
            if (random <= cumulativeProbability && prize.getQuantity() > 0 && !THANKS.equals(prize.getPrize())) {
                log.info("current prize: {}, remain quantity: {}", prize.getPrize(), prize.getQuantity());

                // 針對每個不同獎項加鎖以提高效率
                String prizeKey = LOTTERY_LOCK_PREFIX + prize.getPrize();
                RLock lock = redissonClient.getSpinLock(prizeKey);
                try {
                    boolean getLock = lock.tryLock(3, 10, TimeUnit.SECONDS);
                    if (!getLock) {
                        log.info("System busy.");
                        throw new LotteryProcessException("Please retry after a few moment");
                    }

                    // 再次確認是否還有剩餘獎品
                    if (prize.getQuantity() == 0) {
                        log.info("prize {} remain quantity is 0", prize.getPrize());
                        return prizeMap.get(THANKS);
                    }

                    // 更新剩餘數量
                    prize.decreaseQuantity();

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
        return prizeMap.get(THANKS);
    }

    protected double random() {
        return Math.random();
    }

    /**
     * 更新用戶抽獎紀錄
     *
     * @param userId
     * @param prize
     */
    private void updateUserLotteryHistory(String userId, Prize prize) {
        log.info("update user lottery history, userId: {}, prize: {}", userId, prize.getPrize());
        // 銘謝惠顧則不紀錄
        if (THANKS.equals(prize.getPrize())) {
            return;
        }
        userLotteryHistory.put(userId, prize.getPrize());
    }
}
