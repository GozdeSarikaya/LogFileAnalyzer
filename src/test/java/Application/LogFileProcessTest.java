package Application;

import Event.EventDao;
import Processor.Processor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.parser.JSONParser;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class LogFileProcessTest {

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS event (id VARCHAR(20), duration INTEGER, type VARCHAR(50), host VARCHAR(50), alert BOOLEAN)";

    @InjectMocks
    private Processor processor;

    @BeforeClass
    public static void init() throws SQLException, ClassNotFoundException {
        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(CREATE_TABLE);
            connection.commit();
        }
    }

    @AfterClass
    public static void destroy() throws SQLException {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP TABLE event");
            connection.commit();
            statement.executeUpdate("SHUTDOWN");
            connection.commit();
        }
    }

    @Before
    public void setup() throws SQLException, NoSuchFieldException, IllegalAccessException {
        initMocks(this);
        Logger logger = LoggerFactory.getLogger("LogFileAnalyzer");
        ObjectMapper objectMapper = new ObjectMapper();
        JSONParser jsonParser = new JSONParser();
        EventDao eventDao = new EventDao(getConnection(), logger);
        processor = new Processor(eventDao, logger);

        Field objectMapperField = processor.getClass().getDeclaredField("objectMapper");
        objectMapperField.setAccessible(true);
        objectMapperField.set(processor, objectMapper);

        Field jsonParserField = processor.getClass().getDeclaredField("jsonParser");
        jsonParserField.setAccessible(true);
        jsonParserField.set(processor, jsonParser);
    }

    @After
    public void cleanup() throws SQLException {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM event");
            connection.commit();
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:hsqldb:file:eventdbTest;ifexists=false", "user", "");
    }

    @Test
    public void testRun__ProcessesLogFileAndSavesResultsToDatabase() throws Exception {
        String[] args = {"src/test/resources/test.txt"};
        processor.run(args);

        try (Connection assertConnection = getConnection(); Statement statement = assertConnection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS total FROM event");

            assertEquals("Should save 3 records", 3, getSize(resultSet));

            ResultSet result1 = statement.executeQuery("SELECT COUNT(*) AS total FROM event WHERE id='scsmbstgra' AND duration=5 AND type='APPLICATION_LOG' AND host='12345' AND alert=true");
            assertEquals("Should have 1 record", 1, getSize(result1));

            ResultSet result2 = statement.executeQuery("SELECT COUNT(*) AS total FROM event WHERE id='scsmbstgrb' AND duration=3 AND alert=false");
            assertEquals("Should have 1 record", 1, getSize(result2));

            ResultSet result3 = statement.executeQuery("SELECT COUNT(*) AS total FROM event WHERE id='scsmbstgrc' AND duration=8 AND alert=true");
            assertEquals("Should save 1 record", 1, getSize(result3));
        }
    }

    @Test
    public void testRun__ProcessJsonWithAdditionalFieldsAndSavesResultsToDatabase() throws Exception {
        String[] args = {"src/test/resources/test_additional_fields.txt"};
        processor.run(args);

        try (Connection assertConnection = getConnection(); Statement statement = assertConnection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS total FROM event");

            assertEquals("Should save 3 records", 3, getSize(resultSet));

            ResultSet result1 = statement.executeQuery("SELECT COUNT(*) AS total FROM event WHERE id='scsmbstgra' AND duration=5 AND type='APPLICATION_LOG' AND host='12345' AND alert=true");
            assertEquals("Should have 1 record", 1, getSize(result1));

            ResultSet result2 = statement.executeQuery("SELECT COUNT(*) AS total FROM event WHERE id='scsmbstgrb' AND duration=3 AND alert=false");
            assertEquals("Should have 1 record", 1, getSize(result2));

            ResultSet result3 = statement.executeQuery("SELECT COUNT(*) AS total FROM event WHERE id='scsmbstgrc' AND duration=8 AND alert=true");
            assertEquals("Should save 1 record", 1, getSize(result3));
        }
    }

    private int getSize(ResultSet resultSet) throws SQLException {
        resultSet.next();
        return resultSet.getInt("total");
    }

}
