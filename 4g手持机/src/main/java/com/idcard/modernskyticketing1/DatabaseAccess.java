package com.idcard.modernskyticketing1;

/**
 * Created by daxu on 5/11/17.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class DatabaseAccess {


    private static String OperationMode = "sqlite"; //mssql
    //    private static String OperationMode = "mssql"; //mssql
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private static DatabaseAccess instance;


    private SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd日 hh:mm:ss a");// 设置日期格式


    /**
     * Private constructor to aboid object creation from outside classes.
     *
     * @param context
     */
    private DatabaseAccess(Context context) {
        this.openHelper = new DatabaseOpenHelper(context);
    }

    /**
     * Return a singleton instance of DatabaseAccess.
     *
     * @param context the Context
     * @return the instance of DabaseAccess
     */
    public static DatabaseAccess getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseAccess(context);
        }
        return instance;
    }

    /**
     * Open the database connection.
     */
    public void open() {

        if (OperationMode.equals("sqlite")) {
            this.database = openHelper.getWritableDatabase();
        }
        init();
    }


    private void executeNonQuery(String sql) {
        if (OperationMode.equals("mssql")) {

            MSSqlConnectClass db_conn = new MSSqlConnectClass();

            try {
                Connection conn = db_conn.CONN();

                if (conn != null) {
                    Statement stmt = conn.createStatement();

                    stmt.execute(sql);
                }

            } catch (Exception e) {


            }
        }
    }

    private ResultSet executeQuery(String sql) {

        ResultSet rs = null;
        if (OperationMode.equals("mssql")) {

            MSSqlConnectClass db_conn = new MSSqlConnectClass();

            try {
                Connection conn = db_conn.CONN();

                if (conn != null) {
                    Statement stmt = conn.createStatement();

                    rs = stmt.executeQuery(sql);
                }

            } catch (Exception e) {


            }
        }

        return rs;
    }

    private void init() {

        if (OperationMode.equals("mssql")) {

            String check_in_sql = "if object_id(N'dbo.CheckIns') IS  NULL Begin create table CheckIns (ID nvarchar(50) NOT NULL, Event_Date nvarchar(50), CheckIn_Time nvarchar(50)) End";
            String VR_check_in_sql = "if object_id(N'dbo.VRCheckIns') IS  NULL Begin create table VRCheckIns (ID nvarchar(50) NOT NULL, Event_Date nvarchar(50), CheckIn_Time nvarchar(50)) End";

            executeNonQuery(check_in_sql);
            executeNonQuery(VR_check_in_sql);

        } else {
            database.execSQL("CREATE TABLE IF NOT EXISTS CheckIns ( ID NOT NULL, Event_Date, CheckIn_Time ,isGet)");
            database.execSQL("CREATE TABLE IF NOT EXISTS VRCheckIns ( ID NOT NULL, Event_Date, CheckIn_Time )");

        }
    }


    /**
     * Close the database connection.
     */
    public void close() {
        if (database != null && OperationMode.equals("sqlite")) {
            this.database.close();
        }
    }

    public String getOperationMode() {

        return OperationMode;
    }


    public List<String> doMsSqlFindTickets(String id, int day) {

        List<String> list = new ArrayList<>();
        if (OperationMode.equals("mssql")) {

            MSSqlConnectClass db_conn = new MSSqlConnectClass();


            try {

                Connection conn = db_conn.CONN();

                if (conn != null) {
//                    Cursor cursor = database.rawQuery("SELECT 收货人, 商品数量, 金额, 订单号, 露营VR FROM " + table_name + " WHERE 身份证='" + id + "'", null);
                    String msg = "";

                    Log.d("Day=======", "==" + day);
                    String sql = "Select 收货人, 金额, 商品数量, 订单号,露营VR FROM View_Day_" + day + " WHERE 身份证='" + id + "'";
                    Statement stmt = conn.createStatement();

                    ResultSet rs = stmt.executeQuery(sql);
//                    ContentValues values = new ContentValues();
//                    values.put("露营VR", "已取票");
//                    stmt.executeQuery("View_Day_"+day, values, "身份证=?", new String[]{id});
                    switch (day) {

                        case 1:
                            if (!TextUtils.isEmpty(rs.getString(5))) {
                                msg = rs.getString(5);
                            } else {
                                msg = "第一日的票";
                            }
                            break;
                        case 2:
                            msg = "第二日的票";
                            break;
                        case 3:
                            msg = "第三日的票";
                            break;
                    }
                    while (rs.next()) {

                        list.add("订单号:[" + rs.getString(4) + "]" +
                                rs.getString(1) + " [" + rs.getString(2) + " | " + rs.getString(3) + "]" + msg);

                    }
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        return list;
    }

    public List<String> doMsSqlCheckin(String id, String event_date, int ticket_count, int force_check_in) {

        List<String> list = new ArrayList<>();

        if (OperationMode.equals("mssql")) {

            MSSqlConnectClass db_conn = new MSSqlConnectClass();
            String sql = "EXEC CheckInTickets_Day ?, ?, ?, ?";
            try {

                Connection conn = db_conn.CONN();
                if (conn != null) {

                    CallableStatement cstmt = conn.prepareCall("{call CheckInTickets_Day(?,?,?,?)}");

                    cstmt.setString(1, id);
                    cstmt.setString(2, event_date);
                    cstmt.setInt(3, ticket_count);
                    cstmt.setInt(4, force_check_in);

                    boolean bResult = cstmt.execute();
                    ResultSet rs;
                    if (bResult) {
                        rs = cstmt.getResultSet();

                        rs.next();
                        int avail_ct = rs.getInt(1);

                        while (rs.next()) {
                            list.add(rs.getString(2));

                        }
                    }
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        return list;
    }


    public int doMsSqlFindAvailTickets(String id, int day) {


        if (OperationMode.equals("mssql")) {

            MSSqlConnectClass db_conn = new MSSqlConnectClass();

            try {

                Connection conn = db_conn.CONN();

                if (conn != null) {

                    CallableStatement cstmt = conn.prepareCall("{call FindAvailTickets(?,?,?)}");

                    cstmt.setString("MY_ID", id);
                    cstmt.setInt("DAY", day);

                    cstmt.registerOutParameter("TICKET_COUNT", java.sql.Types.INTEGER);


                    boolean bResult = cstmt.execute();

                    return cstmt.getInt("TICKET_COUNT");


                }
            } catch (Exception e) {

                e.printStackTrace();
            }
        }
        return -1;
    }

    public void doCheckIn(String id, String event_date, String isGet) {

        Date dNow = new Date();

        String now_date = df.format(dNow);

        ContentValues values = new ContentValues();

        values.put("ID", id);
        values.put("Event_Date", event_date);
        values.put("CheckIn_Time", now_date + isGet);
        values.put("isGet", isGet);

        //Cursor cursor =  database.rawQuery("INSERT INTO CheckIns (ID, Event_Date, CheckIn_Time) VALUES ('" + id + "', '" + event_date + "', '" + now_date + "') ", null);

        database.insert("CheckIns", null, values);


    }

    public void doVRCheckIn(String id, String event_date) {

        Date dNow = new Date();

        String now_date = df.format(dNow);

        // Cursor cursor =  database.rawQuery("INSERT INTO VRCheckIns (ID, Event_Date, CheckIn_Time) VALUES ('" + id + "', '" + event_date + "', '" + now_date + "') ", null);

        ContentValues values = new ContentValues();

        values.put("ID", id);
        values.put("Event_Date", event_date);
        values.put("CheckIn_Time", now_date);

        database.insert("VRCheckIns", null, values);

    }


    public List<String> findCheckIn(String event_date, String id) {

        Cursor cursor = database.rawQuery("SELECT Event_Date, CheckIn_Time, isGet FROM CheckIns WHERE ( Event_Date='" + event_date + "' OR Event_Date='0' ) AND ID='" + id + "'", null);


        List<String> list = new ArrayList<>();

        if (cursor == null) {
            return list;
        }


        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {

            list.add(cursor.getString(0) + cursor.getString(1));
            cursor.moveToNext();
        }

        return list;

    }


    public List<String> findVRCheckIn(String event_date, String id) {

        Cursor cursor = database.rawQuery("SELECT Event_Date, CheckIn_Time FROM VRCheckIns WHERE ID='" + id + "'", null);

        cursor.moveToFirst();

        List<String> list = new ArrayList<>();

        while (!cursor.isAfterLast()) {

            list.add(cursor.getString(0) + cursor.getString(1));
            cursor.moveToNext();
        }

        return list;

    }


    public List<TicketInfo> findTicket(String table_name, String id) {

        List<TicketInfo> list = new ArrayList<>();

        Cursor cursor = database.rawQuery("SELECT 收货人, 商品数量, 金额, 订单号, 露营VR,票名称,备注 FROM " + table_name + " WHERE 身份证='" + id + "'", null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {

            TicketInfo info = new TicketInfo();

            info.setId(id);
            info.setCustomer_name(cursor.getString(0));
            info.setProduct_count(Integer.parseInt(cursor.getString(1)));
            info.setTotal_price(Integer.parseInt(cursor.getString(2)));
            info.setOrder_num(cursor.getString(3));
            String isGet = "";
            if (cursor.getString(6) != null)
                isGet = cursor.getString(6);
            info.setMisc_info(cursor.getString(5) + isGet);
//            switch (table_name) {
//                case "Day1":
//                    info.setMisc_info("第一日的票" + isGet);
//                    break;
//                case "Day2":
//                    info.setMisc_info("第二日的票" + isGet);
//                    break;
//                case "Day3":
//                    info.setMisc_info("第三日的票" + isGet);
//                    break;
//                case "AllPass":
//                    info.setMisc_info("通票" + isGet);
//                    break;
//            }
            ContentValues values = new ContentValues();
            values.put("备注", isGet + "已取票");
            database.update(table_name, values, "身份证=?", new String[]{id});
            list.add(info);
            cursor.moveToNext();

        }

        return list;
    }


    private List<TableInfo> processTableInfo(Cursor cursor) {

        cursor.moveToFirst();

        List<TableInfo> list = new ArrayList<>();

        while (!cursor.isAfterLast()) {

            TableInfo info = new TableInfo();

            info.setTableName(cursor.getString(0));
            info.setDay(Integer.parseInt(cursor.getString(1)));
            info.setDate(cursor.getString((2)));
            info.setName(cursor.getString(3));

            list.add(info);

            cursor.moveToNext();

        }

        return list;
    }

    private List<TableInfo> processTableInfoRS(ResultSet rs) {


        List<TableInfo> list = new ArrayList<>();

        try {
            while (rs.next()) {

                TableInfo info = new TableInfo();

                info.setTableName(rs.getString(1));
                info.setDay(Integer.parseInt(rs.getString(2)));
                info.setDate(rs.getString((3)));
                info.setName(rs.getString(4));

                list.add(info);


            }
        } catch (Exception e) {

            e.printStackTrace();
        }


        return list;
    }


    public List<TableInfo> getShowDates() {
        String sql = "Select * FROM Info WHERE Day > 0 ORDER BY Day ASC";

        if (OperationMode.equals("sqlite")) {
            Cursor cursor = database.rawQuery(sql, null);


            return processTableInfo(cursor);

        } else if (OperationMode.equals("mssql")) {
            ResultSet rs = executeQuery(sql);

            if (rs != null) {

                return processTableInfoRS(rs);
            }

        }

        /// Bad coding and logic. This is a patch job
        return new ArrayList<>();


    }

    public List<TableInfo> getAllPass() {

        String sql = "SELECT * FROM Info WHERE Day = 0";

        if (OperationMode.equals("sqlite")) {
            Cursor cursor = database.rawQuery(sql, null);


            return processTableInfo(cursor);
        } else if (OperationMode.equals("mssql")) {

            ResultSet rs = executeQuery(sql);

            if (rs != null) {

                return processTableInfoRS(rs);
            }
        }

        return new ArrayList<>();
    }


    public List<String> getTables() {

        List<String> list = new ArrayList<String>();

        if (OperationMode.equals("sqlite")) {
            Cursor cursor = database.rawQuery("SELECT * FROM Info WHERE Day > 0 ORDER BY Day ASC", null);
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {

                list.add(cursor.getString(0) + " - " + cursor.getString(1) + " - " + cursor.getString(2) + " - " + cursor.getString(3));
                cursor.moveToNext();
            }
            cursor.close();

        } else if (OperationMode.equals("mssql")) {
            String sql = "Select * FROM Info WHERE Day > 0 ORDER BY Day ASC";
            ResultSet rs = executeQuery(sql);

            if (rs != null) {

                try {
                    while (rs.next()) {
                        list.add(rs.getString(1) + " - " + rs.getString(2) + " - " + rs.getString(3) + " - " + rs.getString(4));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }


        return list;
    }

}
