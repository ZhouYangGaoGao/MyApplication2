package com.idcard.modernskyticketing1;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import net.sourceforge.jtds.jdbc.Driver;

/**
 * Created by daxu on 6/8/17.
 */

public class MSSqlConnectClass {

    private String ip = "172.16.1.98";
    private String class_name = "net.sourceforge.jtds.jdbc.Driver";
    private String db = "Test2017_Tickets";
    private String user = "sa";
    private String pass = "pingguo123";


    @SuppressLint("NewApi")
    public Connection CONN() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        Connection conn = null;

        String ConnURL = null;

        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");

            ConnURL = "jdbc:jtds:sqlserver://" + ip + ";"
                    + "databaseName=" + db + ";user=" + user + ";password=" + pass + ";";

            conn = DriverManager.getConnection(ConnURL);

        } catch (SQLException se) {

            Log.e("SQL ERROR", se.getMessage());

        } catch (ClassNotFoundException e) {

            Log.e("ERROR", e.getMessage());

        } catch (Exception e) {

            Log.e("ERROR", e.getMessage());
        }
        return conn;
    }


}
