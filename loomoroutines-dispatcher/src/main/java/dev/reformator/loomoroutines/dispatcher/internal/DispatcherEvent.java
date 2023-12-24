package dev.reformator.loomoroutines.dispatcher.internal;

public sealed interface DispatcherEvent permits AwaitDispatcherEvent, DelayDispatcherEvent, SwitchDispatcherEvent { }
