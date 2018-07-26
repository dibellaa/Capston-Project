package com.udacity.adibella.whatsinmyfridge;

import android.app.Application;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import timber.log.Timber;

public class WhatsInMyFridgeApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree() {
            @Override
            protected @Nullable String createStackElementTag(@NotNull StackTraceElement element) {
                return String.format("[%1$s][%4$s.%2$s(%3$s)]",
                        "WHATSINMYFRIDGE DEBUG",
                        element.getMethodName(),
                        element.getLineNumber(),
                        super.createStackElementTag(element));
            }
        });
    }
}
