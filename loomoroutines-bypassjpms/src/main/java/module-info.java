module dev.reformator.loomoroutines.bypassjpms {
    requires static kotlin.stdlib;
    requires dev.reformator.loomoroutines.common;
    requires io.github.toolfactory.jvm;

    provides dev.reformator.loomoroutines.common.internal.CoroutineFactory with dev.reformator.loomoroutines.bypassjpms.internal.BypassJpmsCoroutineFactory;
}
