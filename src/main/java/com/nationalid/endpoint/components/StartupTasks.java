package com.nationalid.endpoint.components;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import nationalid.loggers.ConsoleLogger;
import nationalid.loggers.FileLogger;
import nationalid.loggers.LogManager;
import nationalid.models.Calculators.GlobalCodeCalculator;
import nationalid.models.Calculators.LithuanianCodeCalculator;

@Component
public class StartupTasks {

    @EventListener(ContextRefreshedEvent.class)
    public void Startup() {
        LogManager.getGlobalInstance().addLogger(new FileLogger());
        LogManager.getGlobalInstance().addLogger(new ConsoleLogger());
        LogManager.getGlobalInstance().LogMessage("Starting Up...");
        GlobalCodeCalculator.setGlobalInstance(new LithuanianCodeCalculator());

    }
}