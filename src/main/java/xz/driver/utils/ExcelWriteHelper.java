package xz.driver.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelWriteHelper {

	private FileOutputStream fileOut = null;
	private Workbook workBook = null;

	public ExcelWriteHelper(String filePath) throws IOException {
		File file = new File(filePath);
		file.createNewFile();
		fileOut = new FileOutputStream(file);
		workBook = new XSSFWorkbook();
	}

	public ExcelWriteHelper buildExcelSheet(String sheetName, String[] titles,
			List<? extends Object> dataSet) throws IOException {
		this.buildExcelSheet(sheetName, titles, dataSet, new WriteRowMapper() {
			@SuppressWarnings("unchecked")
			@Override
			public List<String> handleData(Object param) {
				return (List<String>) param;
			}
		});
		return this;
	}

	public ExcelWriteHelper buildExcelSheet(String sheetName, String[] titles,
			List<? extends Object> dataSet, WriteRowMapper rowMapper)
			throws IOException {
		Sheet sheet = workBook.createSheet(sheetName);
		sheet.setVerticallyCenter(true);
		Row row = sheet.createRow(0);
		for (int i = 0; i < titles.length; i++) {
			sheet.setColumnWidth(i, titles[i].length() * 255 * 5);
			Cell cell = row.createCell(i);
			cell.setCellValue(titles[i]);
			cell.setCellStyle(this.initTitleCellStyle());
		}
		for (int i = 0; i < dataSet.size(); i++) {
			row = sheet.createRow(i + 1);
			List<String> values = rowMapper.handleData(dataSet.get(i));
//			if (values.size() != titles.length) {
//				throw new IOException("转换后的列表长度与表头数组长度不一致");
//			}
			for (int j = 0; j < values.size(); j++) {
				Cell cell = row.createCell(j);
				cell.setCellValue(String.valueOf(values.get(j)));
				//cell.setCellStyle(this.initContentCellStyle());
			}
		}
		return this;
	}

	public void write() {
		try {
			if (fileOut != null) {
				workBook.write(fileOut);
				fileOut.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*private CellStyle initContentCellStyle() {
		CellStyle cell_Style = workBook.createCellStyle();// 设置字体样式
		cell_Style.setAlignment(HorizontalAlignment.CENTER);
		cell_Style.setVerticalAlignment(VerticalAlignment.CENTER);// 垂直对齐居中
		cell_Style.setWrapText(true); // 设置为自动换行
		Font cell_Font = workBook.createFont();
		cell_Font.setFontName("宋体");
		cell_Font.setFontHeightInPoints((short) 15);
		cell_Style.setFont(cell_Font);
		cell_Style.setBorderBottom(BorderStyle.THIN); // 下边框
		cell_Style.setBorderLeft(BorderStyle.THIN);// 左边框
		cell_Style.setBorderTop(BorderStyle.THIN);// 上边框
		cell_Style.setBorderRight(BorderStyle.THIN);// 右边框
		return cell_Style;
	}*/

	private CellStyle initTitleCellStyle() {
		CellStyle headerStyle = workBook.createCellStyle();// 创建标题样式
//		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 设置垂直居中
//		headerStyle.setAlignment(HorizontalAlignment.CENTER); // 设置水平居中
		Font headerFont = workBook.createFont(); // 创建字体样式
		headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD); // 字体加粗
//		headerFont.setBold(true);
		headerFont.setFontName("Times New Roman"); // 设置字体类型
		headerFont.setFontHeightInPoints((short) 15); // 设置字体大小
		headerStyle.setFont(headerFont); // 为标题样式设置字体样式
		return headerStyle;
	}

	public static interface WriteRowMapper {
		List<String> handleData(Object param);
	}
}
