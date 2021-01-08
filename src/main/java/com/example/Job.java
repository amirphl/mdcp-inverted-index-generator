package com.example;

import java.io.IOException;

public class Job {
    public void start(String inputFileURL, String outputFilePath, int fraction, int totalFractions) throws IOException {
        Indexer.start(inputFileURL, outputFilePath, fraction, totalFractions);
    }
}
