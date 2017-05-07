package com.wengyingjian.excel.db.test;

import com.wengyingjian.excel.db.ExcelDbDao;

import java.util.List;

/**
 * Created by hzwengyingjian on 2017/5/7.
 */
public class TestDao extends ExcelDbDao<Test> {

    public static void main(String[] args) {
        Test test = new Test();
        test.setA("11");
        TestDao dao = new TestDao();
        dao.insert(test);
        dao.insert(test);

        List<Test> list = dao.query(new Test());
        System.out.println("result=" + list);
    }

}
