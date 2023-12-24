module dev.reformator.loomoroutines.common {
    requires org.jetbrains.annotations;

    exports dev.reformator.loomoroutines.common;
    exports dev.reformator.loomoroutines.utils;

    exports dev.reformator.loomoroutines.common.internal.utils to dev.reformator.loomoroutines.dispatcher;
}
