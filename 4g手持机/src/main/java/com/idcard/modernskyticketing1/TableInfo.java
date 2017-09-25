package com.idcard.modernskyticketing1;

import java.io.Serializable;

/**
 * Created by daxu on 5/11/17.
 */

public class TableInfo implements Serializable {

    private String tableName;
    private int day;
    private String date;
    private String name;


    public String getTableName() {
        return tableName;
    }


    public int getDay() {
        return day;
    }

    public String getDate() {
        return date;
    }

    public String getName() {
        return name;
    }


    public void setTableName(String tbname) {

        tableName = tbname;
    }

    public void setDay(int in_day) {
        day = in_day;
    }

    public void setDate(String in_date) {

        date = in_date;
    }

    public void setName(String in_name)  {

        name = in_name;

    }

    @Override
    public String toString() {


        return name + " - " + date;

    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;


        TableInfo tbinfo = (TableInfo) o;

        if(!tableName.equals(((TableInfo) o).getTableName())) return true;

        return true;
    }

    @Override
    public int hashCode() {
        return tableName.hashCode();
    }


}
