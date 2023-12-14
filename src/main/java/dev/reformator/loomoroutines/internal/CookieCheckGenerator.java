package dev.reformator.loomoroutines.internal;

import org.jetbrains.annotations.Nullable;

public interface CookieCheckGenerator {
    @Nullable Object getNewCookieCheck();

    @Nullable Object getNextCookieCheck(@Nullable Object cookieCheck);
}
