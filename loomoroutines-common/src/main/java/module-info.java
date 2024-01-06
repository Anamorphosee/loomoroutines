module dev.reformator.loomoroutines.common {
    requires static kotlin.stdlib;
    requires org.slf4j;

    uses dev.reformator.loomoroutines.common.internal.CoroutineFactory;

    exports dev.reformator.loomoroutines.common;

    exports dev.reformator.loomoroutines.common.internal to dev.reformator.loomoroutines.dispatcher, dev.reformator.loomoroutines.bypassjpms;
    exports dev.reformator.loomoroutines.common.internal.kotlinstdlibstub to dev.reformator.loomoroutines.dispatcher, dev.reformator.loomoroutines.bypassjpms;
}
