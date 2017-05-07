package com.wengyingjian.excel.db;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by hzwengyingjian on 2017/5/7.
 */
class Memory2ExcelService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    void flush(Collection<ExcelDB> databases, String filePath) {
        memory2Excel(databases, filePath);
    }

    private synchronized void memory2Excel(final Collection<ExcelDB> excelDBList, final String filePath) {
        executorService.submit(new Runnable() {
            public void run() {
                try {
                    File file = new File(filePath);
                    String tmpFileName = file.getParent() + "/." + file.getName();
                    File tmpFile = new File(tmpFileName);
                    if (tmpFile.exists() && !tmpFile.delete()) {
                        throw new RuntimeException("failed to clean tmp file " + tmpFileName);
                    }

                    XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
                    for (ExcelDB excelDB : excelDBList) {
                        XSSFSheet sheet = xssfWorkbook.createSheet(excelDB.getDatabase());
                        for (int line = 0; line < excelDB.getValues().size(); line++) {
                            List<String> lineValue = excelDB.getValues().get(line);
                            XSSFRow row = sheet.createRow(line);
                            stringValues2Row(lineValue, row);
                        }
                    }

                    FileOutputStream out = new FileOutputStream(tmpFileName);
                    xssfWorkbook.write(out);
                    out.close();

                    logger.info("replacing file {} -> {}", filePath, tmpFileName);
                    if (!tmpFile.renameTo(file)) {
                        logger.info("failed to replace file {} -> {}", filePath, tmpFileName);
                    }
                } catch (IOException e) {
                    logger.error("error occurred when save file {}", filePath, e);
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void stringValues2Row(List<String> lineValue, XSSFRow row) {
        for (int i = 0; i < lineValue.size(); i++) {
            row.createCell(i).setCellValue(lineValue.get(i));
        }
    }

    List<ExcelDB> excel2Memory(String filePath) {
        try {
            List<ExcelDB> excelDBList = new ArrayList<ExcelDB>();
            XSSFWorkbook workbook = new XSSFWorkbook(filePath);
            int sheetCount = workbook.getNumberOfSheets();
            for (int sheetIndex = 0; sheetIndex < sheetCount; sheetIndex++) {
                XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
                List<List<String>> values = new ArrayList<List<String>>();
                for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    values.add(row2StringList(sheet.getRow(rowIndex)));
                }
                ExcelDB excelDB = new ExcelDB();
                excelDB.setDatabase(sheet.getSheetName());
                excelDB.setValues(values);
                excelDBList.add(excelDB);
            }
            return excelDBList;
        } catch (IOException e) {
            logger.error("error occurred when read excel {}", filePath, e);
            throw new RuntimeException(e);
        }
    }

    private List<String> row2StringList(XSSFRow row) {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < row.getLastCellNum(); i++) {
            list.add(row.getCell(i).getStringCellValue());
        }
        return list;
    }
}
