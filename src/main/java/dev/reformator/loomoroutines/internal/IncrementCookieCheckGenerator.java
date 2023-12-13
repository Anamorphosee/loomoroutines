package dev.reformator.loomoroutines.internal;

public class IncrementCookieCheckGenerator implements CookieCheckGenerator {
    @Override
    public Object getNewCookieCheck() {
        return 0;
    }

    @Override
    public Object getNextCookieCheck(Object cookieCheck) {
        return (Integer) cookieCheck + 1;
    }
}
