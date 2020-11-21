package Processor;

public class ProcessorParams {

    private final String filePath;
    private final int numberOfThreads;

    private ProcessorParams(String filePath, int numberOfThreads) {
        this.filePath = filePath;
        this.numberOfThreads = numberOfThreads;
    }

    public static ProcessorParams newInstance(String filePath, int numberOfThreads) {
        return new ProcessorParams(filePath, numberOfThreads);
    }

    public String getFilePath() {
        return filePath;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }
}
