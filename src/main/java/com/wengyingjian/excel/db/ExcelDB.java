package com.wengyingjian.excel.db;

import java.util.List;

/**
 * Created by hzwengyingjian on 2017/5/7.
 */
public class ExcelDB {

    private String database;

    private List<List<String>> values;

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public List<List<String>> getValues() {
        return values;
    }

    public void setValues(List<List<String>> values) {
        this.values = values;
    }
}
