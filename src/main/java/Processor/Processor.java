package Processor;

import Event.EventDao;
import Event.EventLog;
import Event.EventLogFileDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import Exception.LogFileProcessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static Processor.ProcessorEnums.State.STARTED;

@Component
public class Processor implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        ProcessLogFile(new ProcessorParamsValidator(args).getEventParameters());
        ProcessEventData(startedMap.keySet());
    }

    @Autowired
    public Processor(ObjectMapper objectMapper, EventDao eventDao) {
        this.objectMapper = objectMapper;
        this.eventDao = eventDao;
    }

    private final ObjectMapper objectMapper;
    private final EventDao eventDao;

    private final Map<String, EventLogFileDto> startedMap = new ConcurrentHashMap<>();
    private final Map<String, EventLogFileDto> finishedMap = new ConcurrentHashMap<>();

    public void ProcessLogFile(ProcessorParams processorParams) {

        try {

            JSONParser parser = new JSONParser();

            File file = new File(processorParams.getFilePath());
            try (Reader is = new FileReader(file)) {

                BufferedReader bufferedReader = new BufferedReader(is);

                String currentLine;
                while ((currentLine = bufferedReader.readLine()) != null) {
                    JSONObject logLine = (JSONObject) parser.parse(currentLine);
                    EventLogFileDto event = objectMapper.readValue(logLine.toJSONString(), EventLogFileDto.class);

                    if (event.getState().equals(STARTED))
                        startedMap.put(event.getId(), event);
                    else
                        finishedMap.put(event.getId(), event);
                }


            } catch (IOException | ParseException e) {
                throw new IllegalArgumentException("Error with input file:" + e);
            }

        } catch (Exception ex) {
            throw new LogFileProcessException(ex);
        }


    }

    private void ProcessEventData(Set<String> ids) {
        //List<EventLog> eventLogsList = new ArrayList<>();
        for (String id : ids) {
            EventLogFileDto startEvent = startedMap.get(id);
            EventLogFileDto finishEvent = finishedMap.get(id);
            if (startEvent != null && finishEvent != null) {
                eventDao.save(EventLogFileDTOToEventLog(startEvent, finishEvent));
            }
            //eventLogsList.add(EventLogFileDTOToEventLog(startEvent, finishEvent));
        }
        //return eventLogsList;
    }

    public EventLog EventLogFileDTOToEventLog(EventLogFileDto startEvent, EventLogFileDto finishEvent) {
        long duration = finishEvent.getTimestamp().getTime() - startEvent.getTimestamp().getTime();
        boolean isAlert = duration > 4;
        return new EventLog(startEvent.getId(), duration, startEvent.getType(), startEvent.getHost(), isAlert);
    }


}
