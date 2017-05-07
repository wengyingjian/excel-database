package com.wengyingjian.excel.db.test;

/**
 * Created by hzwengyingjian on 2017/5/7.
 */
public class Test {
    private String id;

    private String a;

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    @Override
    public String toString() {
        return "Test{" + "id=" + id + ", a='" + a + '\'' + '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
