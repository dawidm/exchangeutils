package com.dawidmotyka.exchangeutils.db;

import com.dawidmotyka.exchangeutils.exchangespecs.ExchangeSpecs;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by dawid on 5/9/17.
 */
public class DbWriter {

    Logger logger = Logger.getLogger(DbWriter.class.getName());

    private Connection connection = null;
    private ExchangeSpecs exchangeSpecs;

    public DbWriter(ExchangeSpecs exchangeSpecs) {
        this.exchangeSpecs=exchangeSpecs;
    }

    private void connectDb() {
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql:polo";
            connection = DriverManager.getConnection(url,"polo","polo");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "when connecting db",e);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "when connecting db",e);
        }
    }

    public void connect() {
        connectDb();
        try {
            createBalanceTable();
            createTransactionsTable();
            createPricesTable();
            createDepthsTable();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "when creating tables",e);
        }
    }

    public void createBalanceTable() throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("create table if not exists franek_" +
                exchangeSpecs.getName() +
                "_balances(timestamp integer NOT NULL, balance numeric(10,5) NULL);");
        statement.close();
    }
    public void insertBalanceValue(int timestamp, double balance) throws SQLException{
        String insertString = "insert into franek_"+ exchangeSpecs.getName() +"_balances values(?,?);";
        PreparedStatement insertStatement = connection.prepareStatement(insertString);
        insertStatement.setInt(1,timestamp);
        insertStatement.setDouble(2, balance);
        insertStatement.execute();
        insertStatement.close();
    }

    public void createTransactionsTable() throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("create table if not exists user_stats(" +
                "account_identifier text NOT NULL, " +
                "pair text NOT NULL, " +
                "buy_threshold numeric(2,2) NOT NULL," +
                "buy_amoumt numeric (10,10) NOT NULL," +
                "profit_divider NUMERIC (2,2) NOT NULL, " +
                "timestamp integer NOT NULL," +
                "buy_timestamp integer NULL, " +
                "sell_timestamp integer NULL);");
        statement.close();
    }
    public void insertTransactionValue(String accountIdentifier, String pair, double buyThreshold, double buyAmount, double profitDivier, int timestamp, int buyTimestamp, int sellTimestamp) throws SQLException{
        String insertString = "insert into user_stats values(?,?,?,?,?,?,?,?);";
        PreparedStatement insertStatement = connection.prepareStatement(insertString);
        insertStatement.setNString(1,accountIdentifier);
        insertStatement.setNString(2,pair);
        insertStatement.setDouble(3,buyThreshold);
        insertStatement.setDouble(4,buyAmount);
        insertStatement.setDouble(5,profitDivier);
        insertStatement.setInt(6,timestamp);
        insertStatement.setInt(7,buyTimestamp);
        insertStatement.setInt(8,sellTimestamp);
        insertStatement.execute();
        insertStatement.close();
    }

    public void createPricesTable() throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("create table if not exists " + exchangeSpecs.getName() + "_prices (" +
                "timestamp integer NOT NULL, " +
                "pair text NOT NULL, " +
                "price double precision NOT NULL);");
    }

    public void insertPriceSnapshot(int timestamp, String pair, double price) throws SQLException {
        String insertString = "insert into " + exchangeSpecs.getName() + "_prices values(?,?,?);";
        PreparedStatement insertStatement = connection.prepareStatement(insertString);
        insertStatement.setInt(1, timestamp);
        insertStatement.setString(2, pair);
        insertStatement.setDouble(3,price);
        insertStatement.execute();
        insertStatement.close();
    }

    public List<PriceSnapshot> readPriceSnapshots(int beginTimestamp, int endTimestamp) throws SQLException {
        List<PriceSnapshot> priceSnapshotList = new LinkedList<>();
        String sqlString = "select timestamp, pair, price from " + exchangeSpecs.getName() +"_prices where timestamp>? and timestamp<?;";
        PreparedStatement preparedStatement = connection.prepareStatement(sqlString);
        preparedStatement.setInt(1,beginTimestamp);
        preparedStatement.setInt(2,endTimestamp);
        ResultSet resultSet = preparedStatement.executeQuery();
        while(resultSet.next()) {
            priceSnapshotList.add(new PriceSnapshot(resultSet.getInt(1),
                    resultSet.getString(2),
                    resultSet.getDouble(3)));
        }
        return priceSnapshotList;
    }

    public void createDepthsTable() throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("create table if not exists " + exchangeSpecs.getName() + "_depths (" +
                "timestamp integer NOT NULL, " +
                "pair text NOT NULL, " +
                "full_ask_depth double precision NOT NULL," +
                "full_bid_depth double precision NOT NULL," +
                "reduced_depth_price_divider_exponent decimal(4,2) NOT NULL," +
                "reduced_ask_depth double precision not null," +
                "reduced_bid_depth double precision not null );");
    }

    public void insertDepth(int timestamp, String pair, double fullAskDepth, double fullBidDepth, double priceDividerExponent, double reducedAskDepth, double reducedBidDepth) throws SQLException {
        String insertString = "insert into " + exchangeSpecs.getName() + "_depths values(?,?,?,?,?,?,?);";
        PreparedStatement insertStatement = connection.prepareStatement(insertString);
        insertStatement.setInt(1, timestamp);
        insertStatement.setString(2, pair);
        insertStatement.setDouble(3, fullAskDepth);
        insertStatement.setDouble(4, fullBidDepth);
        insertStatement.setBigDecimal(5, new BigDecimal(priceDividerExponent, new MathContext(2, RoundingMode.DOWN)));
        insertStatement.setDouble(6, reducedAskDepth);
        insertStatement.setDouble(7, reducedBidDepth);
        insertStatement.execute();
        insertStatement.close();
    }

    public Connection getConnection() {
        return connection;
    }

    private void checkConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connectDb();
        }
    }

    public void insertOrderBookSnifferResult(String tableSuffix, long timestamp, double value) {
        try {
            checkConnection();
        }
        catch (SQLException e) {
            logger.log(Level.SEVERE, "when checking if db is connected");
            return;
        }
        try {
            String tableName = String.format("order_size_averages_%s",tableSuffix);
            PreparedStatement preparedStatement = connection.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS %s (timestamp BIGINT , value DOUBLE PRECISION );",tableName));
            preparedStatement.execute();
            preparedStatement.close();
            preparedStatement = connection.prepareStatement(String.format("INSERT INTO %s VALUES (?,?)",tableName));
            preparedStatement.setLong(1,timestamp);
            preparedStatement.setDouble(2,value);
            System.out.println(preparedStatement.toString());
            preparedStatement.execute();
            preparedStatement.close();
        }
        catch (SQLException e) {
            logger.log(Level.SEVERE, "when inserting value", e);
        }
    }

}
