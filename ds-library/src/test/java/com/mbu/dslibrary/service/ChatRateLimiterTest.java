package com.mbu.dslibrary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import com.mbu.dslibrary.service.ChatRateLimiter.Decision;

class ChatRateLimiterTest {

    /** A clock the test can move forward by hand. */
    static class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-10-11T10:00:00Z");
        void advanceMinutes(long m) { now = now.plusSeconds(m * 60); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    @Test
    void eachVisitorGetsALimitedNumberOfQuestionsPerHour() {
        MutableClock clock = new MutableClock();
        ChatRateLimiter limiter = new ChatRateLimiter(3, 1000, clock);
        for (int i = 0; i < 3; i++) assertEquals(Decision.OK, limiter.check("1.1.1.1"));
        assertEquals(Decision.VISITOR_LIMIT, limiter.check("1.1.1.1"));
        assertEquals(Decision.OK, limiter.check("2.2.2.2"), "another visitor is not affected");

        clock.advanceMinutes(61);
        assertEquals(Decision.OK, limiter.check("1.1.1.1"), "the hour has passed, so the visitor can ask again");
    }

    @Test
    void theWholeSiteHasADailyCapThatResetsTomorrow() {
        MutableClock clock = new MutableClock();
        ChatRateLimiter limiter = new ChatRateLimiter(100, 4, clock);
        assertEquals(Decision.OK, limiter.check("a"));
        assertEquals(Decision.OK, limiter.check("b"));
        assertEquals(Decision.OK, limiter.check("c"));
        assertEquals(Decision.OK, limiter.check("d"));
        assertEquals(Decision.DAILY_LIMIT, limiter.check("e"));
        assertEquals(Decision.DAILY_LIMIT, limiter.check("a"));

        clock.advanceMinutes(24 * 60);
        assertEquals(Decision.OK, limiter.check("e"), "a new day starts with a fresh count");
    }

    @Test
    void refusedQuestionsDoNotUseUpTheQuota() {
        ChatRateLimiter limiter = new ChatRateLimiter(1, 2, new MutableClock());
        assertEquals(Decision.OK, limiter.check("a"));
        for (int i = 0; i < 5; i++) assertEquals(Decision.VISITOR_LIMIT, limiter.check("a"));
        assertEquals(Decision.OK, limiter.check("b"), "the refused attempts above did not eat the daily cap");
    }
}
