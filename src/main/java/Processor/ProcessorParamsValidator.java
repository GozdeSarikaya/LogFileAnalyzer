package Processor;

import Exception.FilePathInvalidException;

public class ProcessorParamsValidator {

    //region Members
    public static final int DEFAULT_THREAD_POOL_SIZE = 1;

    private static final int FILE_PATH_PARAMETER_INDEX = 0;
    private static final int NUMBER_OF_THREADS_PARAMETER_INDEX = 1;

    private final String[] vars;

    //endregion

    //region Constructor
    public ProcessorParamsValidator(String... vars) {
        this.vars = vars.clone();
    }
    //endregion

    //region Private Methods
    private String getFilePath() {
        try {
            return vars[FILE_PATH_PARAMETER_INDEX];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new FilePathInvalidException(e);
        }
    }

    private int getNumberOfThreadsParameter() {
        try {
            return Integer.parseInt(vars[NUMBER_OF_THREADS_PARAMETER_INDEX]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return DEFAULT_THREAD_POOL_SIZE;
        }

    }

    //endregion

    //region Public Methods
    public ProcessorParams getEventParameters() {
        return ProcessorParams.newInstance(getFilePath(), getNumberOfThreadsParameter());
    }

    //endregion
}
