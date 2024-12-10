package com.exam.project.lottery.service;

import com.exam.project.lottery.enums.PrizeEnum;
import com.exam.project.lottery.exception.LotteryProcessException;
import com.exam.project.lottery.vo.Prize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author memorykghs
 * @date 2024/12/8
 */
@SpringBootTest
class LotterySvcTest {

    private static final String USER_ID = "U123456";

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock userLock;

    @Mock
    private RLock prizeLock;

    @InjectMocks
    @Spy
    private LotterySvc lotterySvc;

    @BeforeEach
    void setUp() {
        when(redissonClient.getLock(anyString())).thenReturn(userLock);
        when(redissonClient.getSpinLock(anyString())).thenReturn(prizeLock);
    }

    @Test
    void testSuccessfulSpin() throws InterruptedException, LotteryProcessException {
        // Mock
        when(userLock.tryLock()).thenReturn(true);
        when(prizeLock.tryLock(3, 10, TimeUnit.SECONDS)).thenReturn(true);
        when(lotterySvc.random()).thenReturn(0.8d);

        Prize result = lotterySvc.spin(USER_ID);
        assertEquals("THANKS", result.getPrize());
    }

    @Test
    void testDuplicateSpin() throws LotteryProcessException {
        when(userLock.tryLock()).thenReturn(true);
        doThrow(new LotteryProcessException("You have got the prize.")).when(lotterySvc).isDuplicate(USER_ID);

        LotteryProcessException e = assertThrows(LotteryProcessException.class, () -> lotterySvc.spin(USER_ID));
        assertEquals("You have got the prize.", e.getMessage());
    }

    @Test
    void testConcurrentSpin() {
        when(userLock.tryLock()).thenReturn(false);
        LotteryProcessException e = assertThrows(LotteryProcessException.class, () -> lotterySvc.spin(USER_ID));
        assertEquals("You are in the spinning process", e.getMessage());
    }

    @Test
    void testNoAvailablePrizes() throws LotteryProcessException {
        when(userLock.tryLock()).thenReturn(true);

        Prize result = lotterySvc.spin(USER_ID);
        assertEquals("THANKS", result.getPrize());
    }

    @Test
    void testPrizeLockFailure() throws Exception {
        when(userLock.tryLock()).thenReturn(true);
        when(prizeLock.tryLock(3, 10, TimeUnit.SECONDS)).thenReturn(false);
        when(lotterySvc.random()).thenReturn(0.1d);

        LotteryProcessException e = assertThrows(LotteryProcessException.class, () -> lotterySvc.spin(USER_ID));
        assertEquals("Please retry after a few moment", e.getMessage());
    }
}