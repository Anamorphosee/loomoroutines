package dev.reformator.loomoroutines.internal;

public interface CookieCheckGenerator {
    Object getNewCookieCheck();

    Object getNextCookieCheck(Object cookieCheck);
}
