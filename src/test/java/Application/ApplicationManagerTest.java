package Application;

import Event.EventDao;
import Exception.LogFileProcessException;
import Processor.Processor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationManagerTest {

    @Mock
    private EventDao eventDao;

    @Mock
    private Logger logger;

    private Processor processor;

    @Before
    public void setup() {
        initMocks(this);
        processor = new Processor(eventDao, logger);
    }

    @Test(expected = Exception.class)
    public void testRun_EmptyArgs() throws IOException {
        String[] args = {};
        processor.run(args);
    }

    @Test(expected = FileNotFoundException.class)
    public void testRun_EmptyFilePath() throws IOException {
        String[] args = {""};
        processor.run(args);
    }

    @Test(expected = FileNotFoundException.class)
    public void testRun_InvalidFilePath() throws IOException {
        String[] args = {"invalid"};
        processor.run(args);
    }

    @Test(expected = LogFileProcessException.class)
    public void testRun_ValidJsonMissingRequiredField() throws IOException {
        String[] args = {"src/test/resources/missing_required_fields.txt"};
        processor.run(args);
    }

    @Test(expected = LogFileProcessException.class)
    public void testRun_InvalidJson() throws IOException {
        String[] args = {"src/test/resources/invalid_json.txt"};
        processor.run(args);
    }
}
