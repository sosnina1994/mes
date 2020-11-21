package ru.pnppk.mes.db;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DbConnectionPool {
    private static DbConnectionPool ourInstance = new DbConnectionPool();

    private DataSource dataSource;

    private DbConnectionPool() {
        try {
            InitialContext ic = new InitialContext();
            this.dataSource = (DataSource) ic.lookup("java:/comp/env/jdbc/dbpoolconnection");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public static DbConnectionPool getInstance() {
        return ourInstance;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
