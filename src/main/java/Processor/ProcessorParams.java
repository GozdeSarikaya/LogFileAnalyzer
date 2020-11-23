package Processor;

public class ProcessorParams {

    //region Members
    private final String filePath;
    private final int numberOfThreads;
    //endregion

    //region Constructor
    private ProcessorParams(String filePath, int numberOfThreads) {
        this.filePath = filePath;
        this.numberOfThreads = numberOfThreads;
    }

    public static ProcessorParams newInstance(String filePath, int numberOfThreads) {
        return new ProcessorParams(filePath, numberOfThreads);
    }
    //endregion

    //region Getters
    public String getFilePath() {
        return filePath;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }
    //endregion
}
