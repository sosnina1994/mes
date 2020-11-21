package ru.pnppk.mes.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.pnppk.mes.api.helper.ApiHelper;
import ru.pnppk.mes.db.DbConnectionPool;
import ru.pnppk.mes.pojo.journal.LogEntry;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/blanksjournal")
public class JournalApi extends HttpServlet {
    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter writer = resp.getWriter();
        String cmd = req.getParameter("cmd");
        int areaNumber = Integer.parseInt(req.getParameter("areaNumber"));

        try {
            List<LogEntry> logs = readBlanksJournalForAreas();
            logs = logs.stream()
                    .filter(logEntry -> logEntry.getAreaNumber() == areaNumber)
                    .collect(Collectors.toList());

            JsonNode node = mapper.valueToTree(logs);
            for (int i = 0; i < logs.size(); i++) {
                String date = logs.get(i).getDate().format(ApiHelper.DATE_TIME_FORMATTER);
                ((ObjectNode)node.get(i)).set("date", mapper.getNodeFactory().textNode(date));
            }

            if ("get".equals(cmd)) {
                writer.write(mapper.writeValueAsString(node));
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<LogEntry> readBlanksJournalForAreas() {
        ArrayList<LogEntry> logs = new ArrayList<>();

        try (Connection sql = DbConnectionPool.getInstance().getConnection()) {
            PreparedStatement pSt = sql.prepareStatement("SELECT date, cipher, name, route_map_number, " +
                    "order_number, count, area_number FROM blanks_journal");
            ResultSet rs = pSt.executeQuery();
            while (rs.next()) {
                LocalDateTime date = rs.getTimestamp("date").toLocalDateTime();
                String cipher = rs.getString("cipher");
                String name = rs.getString("name");
                String routMapNumber = rs.getString("route_map_number");
                long orderNumber = rs.getLong("order_number");
                int count = rs.getInt("count");
                int areaNumber = rs.getInt("area_number");

                LogEntry log = new LogEntry(date, cipher, name, routMapNumber, orderNumber, count, areaNumber);
                logs.add(log);
            }
            rs.close();
            pSt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
}
