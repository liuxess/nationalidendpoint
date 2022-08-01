package com.nationalid.endpoint.loggers;

import java.util.ArrayList;

import nationalid.interfaces.Logable;

public class ErrorLogger implements Logable {

    private final ArrayList<String> problems;

    public ErrorLogger() {
        problems = new ArrayList<>();
    }

    @Override
    public Boolean CompareInstance(Logable other) {
        return other instanceof ErrorLogger;
    }

    @Override
    public void LogMessage(String message) throws Exception {
        problems.add(message);
    }

    public ArrayList<String> getLoggedProblems() {
        return problems;
    }

}
