module dev.reformator.loomoroutines.common {
    requires static kotlin.stdlib;
    requires org.slf4j;

    exports dev.reformator.loomoroutines.common;

    exports dev.reformator.loomoroutines.common.internal to dev.reformator.loomoroutines.dispatcher;
    exports dev.reformator.loomoroutines.common.internal.kotlinstdlibstub to dev.reformator.loomoroutines.dispatcher;
}
