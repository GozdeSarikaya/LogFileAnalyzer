package Processor;

import Event.EventDao;
import Event.EventLog;
import Event.EventLogFileDto;
import Exception.DatabaseInsertException;
import Exception.LogFileProcessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static Processor.ProcessorEnums.State.STARTED;

@Component
public class Processor implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {

        try {
            logger.info("Application started...");
            ProcessorParams params = new ProcessorParamsValidator(args).getEventParameters();
            logger.info("Application parameters validated! FilePath: " + params.getFilePath() + ", NumOfThreads: " + params.getNumberOfThreads());
            logger.info("Application is processing log file...");
            ProcessLogFile(params);
            ProcessEventData(startedMap.keySet());
            logger.info("Application finished...");
        } catch (Exception ex) {
            logger.error("Application finished with error. Exception: " + ex);
        }
    }

    @Autowired
    public Processor(EventDao eventDao, Logger logger) {
        this.eventDao = eventDao;
        this.logger = logger;
    }


    @Autowired private ObjectMapper objectMapper;
    @Autowired private JSONParser jsonParser;
    private final Logger logger;
    private final EventDao eventDao;

    private final Map<String, EventLogFileDto> startedMap = new ConcurrentHashMap<>();
    private final Map<String, EventLogFileDto> finishedMap = new ConcurrentHashMap<>();

    private void ProcessLogFile(ProcessorParams processorParams) {

        try {

            logger.info("Log file processing step is started...");
            File file = new File(processorParams.getFilePath());
            logger.debug("Log file processing step is started...");
            Reader is = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(is);

            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null) {
                ProcessCurrentLine(currentLine);
            }

        } catch (Exception ex) {
            logger.error("Failure reading the file, exiting...", ex);
            throw new LogFileProcessException(ex);
        }


    }

    private void ProcessCurrentLine(String currentLine) {

        try {
            logger.debug("Log file current line: " + currentLine);
            JSONObject logLine = (JSONObject) jsonParser.parse(currentLine);
            logger.debug("Current line is parsed to json successfully. JsonString: " + logLine.toJSONString());
            EventLogFileDto event = objectMapper.readValue(logLine.toJSONString(), EventLogFileDto.class);
            logger.debug("JsonString is mapped to EventLogFileDto class successfully.");
            if (event.getState().equals(STARTED)) {
                startedMap.put(event.getId(), event);
                logger.info("Event is put the started map. EventID: " + event.getId());
            } else {
                finishedMap.put(event.getId(), event);
                logger.info("Event is put the finished map. EventID: " + event.getId());
            }
        } catch (Exception ex) {
            logger.error("Failure reading the file, exiting...", ex);
            throw new LogFileProcessException(ex);
        }

    }

    private void ProcessEventData(Set<String> ids) {
        try {
            for (String id : ids) {
                logger.info("EventLog will be generated! EventID: " + id);
                EventLogFileDto startEvent = startedMap.get(id);
                EventLogFileDto finishEvent = finishedMap.get(id);
                if (startEvent != null && finishEvent != null) {
                    logger.debug("EventLog is converting.");
                    boolean result = eventDao.save(EventLogFileDTOToEventLog(startEvent, finishEvent));
                    logger.info("EventLog save result: " + result);
                }

            }
        } catch (DatabaseInsertException ex) {
            logger.error("Failure processing event data", ex);
            throw new DatabaseInsertException(ex);
        }
    }

    private EventLog EventLogFileDTOToEventLog(EventLogFileDto startEvent, EventLogFileDto finishEvent) {
        long duration = finishEvent.getTimestamp().getTime() - startEvent.getTimestamp().getTime();
        boolean isAlert = duration > 4;
        logger.info("EventLog is converted. Duration: " + duration + ", Alert: " + isAlert);
        return new EventLog(startEvent.getId(), duration, startEvent.getType(), startEvent.getHost(), isAlert);
    }


}
