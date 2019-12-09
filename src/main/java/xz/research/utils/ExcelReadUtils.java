package xz.research.utils;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * excel读取工具
 *
 * @author baoy
 */
public class ExcelReadUtils {

    public static Workbook getWorkbook(String excelFile) throws IOException {
        return getWorkbook(new FileInputStream(excelFile));
    }

    public static Workbook getWorkbook(InputStream is) throws IOException {

        Workbook wb = null;

        ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[512];
            int count = -1;
            while ((count = is.read(buffer)) != -1)
                byteOS.write(buffer, 0, count);
//            byteOS.close();
            byte[] allBytes = byteOS.toByteArray();

            try {
                wb = new XSSFWorkbook(new ByteArrayInputStream(allBytes));
            } catch (Exception ex) {
                ex.printStackTrace();
                wb = new HSSFWorkbook(new ByteArrayInputStream(allBytes));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            byteOS.close();
        }

        return wb;
    }

    public static ArrayList<ArrayList<Object>> readAllRows(String excelFile) throws IOException {
        return readAllRows(new FileInputStream(excelFile));
    }

    public static ArrayList<ArrayList<Object>> readAllRows(File file) throws IOException {
        return readAllRows(FileUtils.openInputStream(file));
    }

    public static ArrayList<ArrayList<Object>> readAllRows(InputStream is) throws IOException {
        ArrayList<ArrayList<Object>> rowList = new ArrayList<ArrayList<Object>>();
        try {
            Workbook wb = getWorkbook(is);

            for (int i = 0; i < wb.getNumberOfSheets(); i++) {//获取每个Sheet表
                Sheet sheet = wb.getSheetAt(i);
                if (sheet.getLastRowNum() > 0) {
                    rowList.addAll(readRows(sheet));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(is != null)
                is.close();
        }

        return rowList;
    }

    public static ArrayList<ArrayList<Object>> readAllRows(InputStream is, int sheetIndex) throws IOException {
        Workbook wb = getWorkbook(is);
        ArrayList<ArrayList<Object>> rowList = new ArrayList<ArrayList<Object>>();
        if (sheetIndex < wb.getNumberOfSheets()) {
            Sheet sheet = wb.getSheetAt(sheetIndex);
            if (sheet.getLastRowNum() > 0) {
                rowList.addAll(readRows(sheet));
            }
        }
        return rowList;
    }

    public static ArrayList<ArrayList<Object>> readAllRows(InputStream is, String sheetName) throws IOException {
        Workbook wb = getWorkbook(is);
        ArrayList<ArrayList<Object>> rowList = new ArrayList<ArrayList<Object>>();
        Sheet sheet = wb.getSheet(sheetName);
        if (sheet != null) {
            if (sheet.getLastRowNum() > 0) {
                rowList.addAll(readRows(sheet));
            }
        }
        return rowList;
    }

    public static ArrayList<ArrayList<Object>> readRows(String excelFile,
                                                        int startRowIndex, int rowCount) throws IOException {
        return readRows(new FileInputStream(excelFile), startRowIndex, rowCount);
    }

    public static ArrayList<ArrayList<Object>> readRows(String excelFile) throws IOException {
        return readRows(new FileInputStream(excelFile));
    }

    public static ArrayList<ArrayList<Object>> readRows(String excelFile, int i) throws IOException {
        return readRows(new FileInputStream(excelFile), i);
    }

    public static ArrayList<ArrayList<Object>> readRows(InputStream is,
                                                        int startRowIndex, int rowCount) throws IOException {
        Workbook wb = getWorkbook(is);
        Sheet sheet = wb.getSheetAt(0);

        return readRows(sheet, startRowIndex, rowCount);
    }

    public static ArrayList<ArrayList<Object>> readRows(InputStream is) throws IOException {
        Workbook wb = getWorkbook(is);
        Sheet sheet = wb.getSheetAt(0);
        return readRows(sheet);
    }

    public static ArrayList<ArrayList<Object>> readRows(InputStream is, int i) throws IOException {
        Workbook wb = getWorkbook(is);
        Sheet sheet = wb.getSheetAt(i);
        return readRows(sheet);
    }

    /**
     * 判断行是不是为空
     * @param row
     * @return
     */
    public static boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK){
                return false;
            }
        }
        return true;
    }

    public static ArrayList<ArrayList<Object>> readRows(Sheet sheet,
                                                        int startRowIndex, int rowCount) {
        ArrayList<ArrayList<Object>> rowList = new ArrayList<ArrayList<Object>>();
        int totalCellNum = sheet.getRow(0).getLastCellNum();
        for (int i = 0; i <= (startRowIndex + rowCount); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                break;
            }
            if(isRowEmpty(row)){
                break;
            }
            ArrayList<Object> cellList = new ArrayList<Object>();
//			for (Cell cell : row) {
//				cellList.add(readCell(cell));
//			}
            for (int j = 0; j < totalCellNum; j++) {
                cellList.add(readCell(row.getCell(j)));
            }

            rowList.add(cellList);
        }

        return rowList;
    }

    public static ArrayList<ArrayList<Object>> readRows(Sheet sheet) {
        int rowCount = sheet.getLastRowNum();
        return readRows(sheet, 0, rowCount);
    }

    private static Object readCell(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                String str = cell.getRichStringCellValue().getString();
                return str == null ? "" : str.trim();

            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    DecimalFormat formatter = new DecimalFormat("########.##");
                    return formatter.format(cell.getNumericCellValue());
                }

            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue();

            case Cell.CELL_TYPE_FORMULA:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    return cell.getCellFormula();
                }

            case Cell.CELL_TYPE_BLANK:
                return "";

            default:
                System.err.println("Data error for cell of excel: " + cell.getCellType());
                return "";
        }

    }

    public static int getLastRowNum(String path) throws IOException {
        Workbook wb = getWorkbook(path);
        Sheet sheet = wb.getSheetAt(0);
        return sheet.getLastRowNum();
    }
}
