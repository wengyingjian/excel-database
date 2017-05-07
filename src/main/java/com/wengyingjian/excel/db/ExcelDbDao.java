package com.wengyingjian.excel.db;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by hzwengyingjian on 2017/5/7.
 */
public class ExcelDbDao<T> {
    private static Memory2ExcelService memory2ExcelService = new Memory2ExcelService();

    private static String filePath;
    static {
        System.out.println("init start");
        ExcelDBGlobal.databases = new ConcurrentHashMap<String, ExcelDB>();
        List<ExcelDB> excelDBList = memory2ExcelService.excel2Memory(getFilePath());
        for (ExcelDB excelDB : excelDBList) {
            ExcelDBGlobal.databases.put(excelDB.getDatabase(), excelDB);
        }
        System.out.println("init done");
    }

    private static String getFilePath() {
        try {
            if (filePath == null) {
                Properties properties = new Properties();
                properties
                        .load(Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties"));
                filePath = String.valueOf(properties.get("exceldb.filepath"));
            }
            return filePath;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    interface Function {
        boolean matches(List<String> list, Object obj);
    }

    private Function defaultMatch = new Function() {
        public boolean matches(List<String> list, Object obj) {
            List<String> objectFieldValues = obj2StringValues(obj);
            for (int i = 0; i < list.size(); i++) {
                if ("".equals(objectFieldValues.get(i))) {
                    continue;
                }
                if (!objectFieldValues.get(i).equals(list.get(i))) {
                    return false;
                }
            }
            return true;
        }
    };

    private int getAutoIncrement(String tableName) {
        ExcelDB excelDB = ExcelDBGlobal.databases.get(tableName);
        if (excelDB == null) {
            throw new RuntimeException("table " + tableName + " not exists!");
        }
        int lastLineNum = excelDB.getValues().size() - 1;
        String lastLineId = excelDB.getValues().get(lastLineNum).get(0);
        return Integer.valueOf(lastLineId) + 1;
    }

    private String getTableName(Class<?> clazz) {
        return clazz.getSimpleName();
    }

    // ===database
    // 插入
    public int insert(Object obj) {
        List<String> stringValues = obj2StringValues(obj);
        String tableName = getTableName(obj.getClass());
        int id = getAutoIncrement(tableName);
        stringValues.set(0, String.valueOf(id));
        ExcelDBGlobal.databases.get(tableName).getValues().add(stringValues);
        memory2ExcelService.flush(ExcelDBGlobal.databases.values(), getFilePath());
        return id;
    }

    private List<String> obj2StringValues(Object obj) {
        try {
            List<String> cellValues = new ArrayList<String>();
            Class<?> tClass = obj.getClass();
            Field[] fields = tClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object cellValue = field.get(obj);
                if (cellValue == null) {
                    cellValues.add("");
                } else {
                    cellValues.add(cellValue.toString());
                }
            }
            return cellValues;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // ===查询
    public List<T> query(T t) {
        List<List<String>> matchedRows = query(t, defaultMatch);
        List<T> list = new ArrayList<T>(matchedRows.size());
        for (List<String> lineValue : matchedRows) {
            list.add(stringValues2Obj(lineValue, t.getClass()));
        }
        return list;
    }

    private List<List<String>> query(Object obj, Function function) {
        List<List<String>> queryLines = new ArrayList<List<String>>();
        ExcelDB excelDB = ExcelDBGlobal.databases.get(getTableName(obj.getClass()));
        // 第一栏标题不计
        for (int i = 1; i < excelDB.getValues().size(); i++) {
            if (function.matches(excelDB.getValues().get(i), obj)) {
                queryLines.add(excelDB.getValues().get(i));
            }
        }
        return queryLines;
    }

    private T stringValues2Obj(List<String> stringValues, Class<?> tClass) {
        try {
            T obj = (T) tClass.newInstance();
            Field[] fields = tClass.getDeclaredFields();
            for (int i = 0; i < stringValues.size(); i++) {
                Field field = fields[i];
                field.setAccessible(true);
                field.set(obj, stringValues.get(i));
            }
            return obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
