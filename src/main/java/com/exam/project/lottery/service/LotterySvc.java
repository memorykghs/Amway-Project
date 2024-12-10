package com.exam.project.lottery.service;

import com.exam.project.lottery.enums.PrizeEnum;
import com.exam.project.lottery.exception.LotteryProcessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author memorykghs
 * @date 2024/12/10
 */
@Component
@Slf4j
public class LotterySvc {

    private static final String LOTTERY_PRIZE_PREFIX = "lottery:prize:";
    private static final String LOTTERY_PRIZE_QUANTITY = "quantity:";
    private static final String LOTTERY_PRIZE_PROBABILITY = "probability";
    private static final String THANKS = "thanks"; // 銘謝惠顧
    private static final String LOTTERY_HISTORY = "history";

    private final RedisTemplate<String, Object> redisTemplate;

    public LotterySvc(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 初始化抽獎獎品
     *
     * @param lotteryId
     * @throws LotteryProcessException
     */
    public void initializePrizes(String lotteryId) throws LotteryProcessException {
        // 如果舊的還存在，就不能建立相同的抽獎活動
        String lotteryKey = getLotteryKey(lotteryId);
        validateLotteryEvent(lotteryKey);

        PrizeEnum[] prizes = PrizeEnum.values();
        Map<String, Double> prizeInfo = new HashMap<>();
        setPrize(lotteryKey, prizes, prizeInfo); // 儲存獎品資訊到 Redis

        // 儲存機率到 Redis
        setProbability(lotteryKey, prizes, prizeInfo); // 計算獎品的機率，不足的全部設為銘謝惠顧
        redisTemplate.opsForSet().add(lotteryKey + LOTTERY_PRIZE_PROBABILITY, prizeInfo);
        redisTemplate.opsForValue().set(lotteryKey, "alive");
    }

    /**
     * 檢查 user 中獎紀錄是否存在
     *
     * @param userId
     * @param lotteryId
     * @return
     */
    public boolean isUserHistoryExist(String userId, String lotteryId) {
        log.info("get user lottery history, userId: {}", userId);
        return redisTemplate.opsForHash().get(getLotteryKey(lotteryId) + LOTTERY_HISTORY, userId) == null;
    }

    /**
     * 更新用戶中獎紀錄
     *
     * @param userId
     * @param prize
     * @param lotteryId
     */
    public void updateUserLotteryHistory(String userId, String prize, String lotteryId) {
        log.info("update user lottery history, userId: {}, prize: {}", userId, prize);
        // 銘謝惠顧則不紀錄
        if (THANKS.equals(prize)) {
            return;
        }
        redisTemplate.opsForHash().put(getLotteryKey(lotteryId) + LOTTERY_HISTORY, userId, prize);
    }

    /**
     * 取得該 lottery 的個獎項機率
     *
     * @param lotteryId
     * @return
     */
    public LinkedHashMap<String, Double> getPrizeProbabilityFromRedis(String lotteryId) {
        String lotteryHisKey = getLotteryKey(lotteryId) + LOTTERY_PRIZE_PROBABILITY;
        return redisTemplate.opsForHash().entries(lotteryHisKey)
                .entrySet()
                .stream()
                .sorted(Comparator.comparingDouble(e -> Double.parseDouble((String) e.getValue())))
                .collect(Collectors.toMap(
                        e -> (String) e.getKey(),
                        e -> Double.valueOf((String) e.getValue()),
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * 取得該獎品剩餘數量
     *
     * @param lotteryId
     * @param prize
     * @return
     * @throws LotteryProcessException
     */
    public int getPrizeQuantity(String lotteryId, String prize) throws LotteryProcessException {
        String lotteryKey = getLotteryKey(lotteryId);
        validateLotteryEvent(lotteryKey);

        Object quantityObj = redisTemplate.opsForValue().get(lotteryKey + LOTTERY_PRIZE_QUANTITY + prize);
        return Integer.parseInt((String) quantityObj);
    }

    /**
     * 更新獎品數量
     *
     * @param lotteryId
     * @param prize
     * @throws LotteryProcessException
     */
    public void decreaseQuantity(String lotteryId, String prize) throws LotteryProcessException {
        String lotteryKey = getLotteryKey(lotteryId);
        validateLotteryEvent(lotteryKey);
        Object quantityObj = redisTemplate.opsForValue().decrement(lotteryKey + LOTTERY_PRIZE_QUANTITY + prize);
    }

    /**
     * 計算累計中獎機率
     *
     * @param prizes
     * @return
     */
    private double calculateTotalProbability(PrizeEnum[] prizes) {
        return Arrays.stream(prizes)
                .mapToDouble(PrizeEnum::getProbability)
                .sum();
    }

    /**
     * 儲存獎品資訊到 Redis
     *
     * @param lotteryKey
     * @param prizes
     * @param prizeInfo
     */
    private void setPrize(String lotteryKey, PrizeEnum[] prizes, Map<String, Double> prizeInfo) {
        for (PrizeEnum prizeEnum : prizes) {
            String prizeName = prizeEnum.getPrize();

            // 儲存獎品數量到 Redis
            redisTemplate.opsForValue().set(
                    lotteryKey + LOTTERY_PRIZE_QUANTITY + prizeName,
                    prizeEnum.getQuantity()
            );

            // 將獎品中獎機率放入 Map
            prizeInfo.put(prizeName, prizeEnum.getProbability());
        }
    }

    /**
     * 計算銘謝惠顧的機率
     *
     * @param lotteryKey
     * @param prizes
     * @param prizeInfo
     */
    private void setProbability(String lotteryKey, PrizeEnum[] prizes, Map<String, Double> prizeInfo) {
        double totalProbability = calculateTotalProbability(prizes);
        if (totalProbability < 1.0) {
            redisTemplate.opsForValue().set(
                    lotteryKey + THANKS + LOTTERY_PRIZE_QUANTITY,
                    Integer.MAX_VALUE
            );
            prizeInfo.put(THANKS, 1.0 - totalProbability);
        }
    }

    private void validateLotteryEvent(String lotteryKey) throws LotteryProcessException {
        String lotteryStatus = (String) redisTemplate.opsForValue().get(lotteryKey);
        if ("alive".equals(lotteryStatus)) {
            log.error("The lottery event is exist, lottery ID: {}", lotteryKey);
            throw new LotteryProcessException("The lottery event is exist.");
        }
    }

    private String getLotteryKey(String lotteryId) {
        return LOTTERY_PRIZE_PREFIX + lotteryId;
    }
}
