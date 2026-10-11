package com.mbu.dslibrary.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Keeps the AI bill predictable now that the portal has no login:
 * each visitor (by address) gets a limited number of questions per hour, and the whole site has a daily cap.
 * Counts live in memory, so they reset when the app restarts.
 */
@Component
public class ChatRateLimiter {

    public enum Decision { OK, VISITOR_LIMIT, DAILY_LIMIT }

    private static final long HOUR_MS = 60L * 60 * 1000;

    private final int perVisitorPerHour;
    private final int dailyLimit;
    private final Clock clock;
    private final Map<String, Deque<Long>> hits = new HashMap<>();
    private LocalDate today;
    private int todayCount;

    @Autowired
    public ChatRateLimiter(@Value("${chat.per-visitor-per-hour:30}") int perVisitorPerHour,
                           @Value("${chat.daily-limit:500}") int dailyLimit) {
        this(perVisitorPerHour, dailyLimit, Clock.systemDefaultZone());
    }

    ChatRateLimiter(int perVisitorPerHour, int dailyLimit, Clock clock) {
        this.perVisitorPerHour = perVisitorPerHour;
        this.dailyLimit = dailyLimit;
        this.clock = clock;
    }

    /** Counts this question if it is allowed. */
    public synchronized Decision check(String visitor) {
        long now = clock.millis();
        LocalDate day = LocalDate.now(clock);
        if (!day.equals(today)) {
            today = day;
            todayCount = 0;
        }
        if (todayCount >= dailyLimit) return Decision.DAILY_LIMIT;

        Deque<Long> recent = hits.computeIfAbsent(visitor == null ? "unknown" : visitor, k -> new ArrayDeque<>());
        while (!recent.isEmpty() && now - recent.peekFirst() > HOUR_MS) recent.pollFirst();
        if (recent.size() >= perVisitorPerHour) return Decision.VISITOR_LIMIT;

        recent.addLast(now);
        todayCount++;
        if (hits.size() > 5000) hits.values().removeIf(d -> d.isEmpty() || now - d.peekLast() > HOUR_MS);   // tidy up
        return Decision.OK;
    }

    public int getPerVisitorPerHour() { return perVisitorPerHour; }
}
