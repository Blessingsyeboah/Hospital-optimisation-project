package com.hospitaloptimizer.shared.model;

/** One empirical timing measurement, logged for the efficiency lab (Section 9 of the brief). */
public class AlgorithmRun {
    private int runId;
    private String algorithmName;
    private int inputSize;
    private long timeNs;
    private long memoryKb;
    private String dateRun;

    public AlgorithmRun() {}

    public AlgorithmRun(String algorithmName, int inputSize, long timeNs, long memoryKb, String dateRun) {
        this.algorithmName = algorithmName;
        this.inputSize = inputSize;
        this.timeNs = timeNs;
        this.memoryKb = memoryKb;
        this.dateRun = dateRun;
    }

    public int getRunId() { return runId; }
    public void setRunId(int runId) { this.runId = runId; }
    public String getAlgorithmName() { return algorithmName; }
    public void setAlgorithmName(String algorithmName) { this.algorithmName = algorithmName; }
    public int getInputSize() { return inputSize; }
    public void setInputSize(int inputSize) { this.inputSize = inputSize; }
    public long getTimeNs() { return timeNs; }
    public void setTimeNs(long timeNs) { this.timeNs = timeNs; }
    public long getMemoryKb() { return memoryKb; }
    public void setMemoryKb(long memoryKb) { this.memoryKb = memoryKb; }
    public String getDateRun() { return dateRun; }
    public void setDateRun(String dateRun) { this.dateRun = dateRun; }
}
