package Event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component
public class EventDao implements AutoCloseable, IEventLog {

    private final Connection connection;
    private final static String sql = "INSERT INTO event (id, duration, type, host, alert)  VALUES (?, ?, ?, ?, ?)";

    @Autowired
    public EventDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean save(EventLog event) {

        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, event.getId());
            statement.setLong(2, event.getDuration());
            statement.setString(3, event.getType());
            statement.setString(4, event.getHost());
            statement.setBoolean(5, event.isAlert());
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }
}