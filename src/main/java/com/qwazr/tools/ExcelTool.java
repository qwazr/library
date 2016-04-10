package com.qwazr.tools;

import com.datastax.driver.core.ResultSet;
import com.qwazr.library.AbstractLibrary;
import com.qwazr.utils.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class ExcelTool extends AbstractLibrary {

	public final String default_date_format = null;
	public final String default_number_format = null;

	@Override
	public void load(File data_directory) throws IOException {
	}

	/**
	 * Create a new Excel document builder
	 *
	 * @param xlsx       true to create a XLSX document, false to create a legacy XLS document
	 * @param closeables an optional autocloseable context
	 * @return a new builder
	 */
	public ExcelBuilder getNewBuilder(boolean xlsx, IOUtils.CloseableContext closeables) {
		ExcelBuilder builder = new ExcelBuilder(xlsx);
		if (closeables != null)
			closeables.add(builder);
		if (default_date_format != null)
			builder.setDefaultDateFormat(default_date_format);
		if (default_number_format != null)
			builder.setDefaultNumberFormat(default_number_format);
		return builder;
	}

	public static class ExcelBuilder implements Closeable {

		private final Workbook workbook;
		private final AtomicInteger xpos = new AtomicInteger();
		private final AtomicInteger ypos = new AtomicInteger();
		private Sheet currentSheet;
		private Row currentRow;
		private Short defaultDateFormat;
		private Short defaultNumberFormat;
		private CellStyle defaultDateCellStyle;
		private CellStyle defaultNumberCellStyle;

		/**
		 * The builder creator
		 *
		 * @param xlsx true to create a XLSX document, false to create a legacy XLS document
		 */
		public ExcelBuilder(final boolean xlsx) {
			workbook = xlsx ? new XSSFWorkbook() : new HSSFWorkbook();
			currentSheet = null;
			currentRow = null;
		}

		/**
		 * Get the number of a given format. If the format does not exist, a new one is created
		 *
		 * @param format the string representation of the format
		 * @return
		 */
		public short getFormat(String format) {
			return workbook.createDataFormat().getFormat(format);
		}

		/**
		 * Define the default format for dates
		 *
		 * @param dateFormat the string representation of the format
		 */
		public void setDefaultDateFormat(String dateFormat) {
			if (dateFormat == null) {
				defaultDateFormat = null;
				defaultDateCellStyle = null;
				return;
			}
			defaultDateFormat = getFormat(dateFormat);
			defaultDateCellStyle = workbook.createCellStyle();
			defaultDateCellStyle.setDataFormat(defaultDateFormat);
		}

		/**
		 * Define the default format for numbers
		 *
		 * @param numberFormat the string representation of the format
		 */
		public void setDefaultNumberFormat(String numberFormat) {
			if (numberFormat == null) {
				defaultNumberFormat = null;
				defaultNumberCellStyle = null;
				return;
			}
			defaultNumberFormat = getFormat(numberFormat);
			defaultNumberCellStyle = workbook.createCellStyle();
			defaultNumberCellStyle.setDataFormat(defaultNumberFormat);
		}

		/**
		 * Create a new sheet or activate the existing one and set the cursor position
		 *
		 * @param sheetName the name of the sheet
		 * @param xpos      the initial horizontal cursor position
		 * @param ypos      the initial vertical cursor position
		 * @return the sheet
		 */
		public Sheet activeSheetAndSetPos(final String sheetName, final int xpos, final int ypos) {
			Sheet sheet = workbook.getSheet(sheetName);
			if (sheet == null)
				sheet = workbook.createSheet(sheetName);
			this.xpos.set(xpos);
			this.ypos.set(ypos);
			return currentSheet = sheet;
		}

		/**
		 * Return the row at the current cursor position and increment the vertical position of the cursor.
		 * If the row does not exist, a new one is created.
		 *
		 * @return the row
		 */
		public Row addRow() {
			Row row = currentSheet.getRow(ypos.get());
			if (row == null)
				row = currentSheet.createRow(ypos.get());
			xpos.set(0);
			ypos.incrementAndGet();
			return currentRow = row;
		}

		/**
		 * Create a new cell at the current cursor position. The horizontal position of the cursor is incremented.
		 *
		 * @param object the content of the cell
		 * @return the created cell
		 */
		public Cell addOneCell(Object object) {
			if (object == null) {
				incCell(1);
				return null;
			}
			Cell cell = currentRow.getCell(xpos.get());
			if (cell == null)
				cell = currentRow.createCell(xpos.get());
			if (object instanceof Calendar) {
				cell.setCellValue((Calendar) object);
				if (defaultDateCellStyle != null)
					cell.setCellStyle(defaultDateCellStyle);
			} else if (object instanceof Date) {
				cell.setCellValue((Date) object);
				if (defaultDateCellStyle != null)
					cell.setCellStyle(defaultDateCellStyle);
			} else if (object instanceof Number)
				cell.setCellValue(((Number) object).doubleValue());
			if (defaultNumberCellStyle != null)
				cell.setCellStyle(defaultNumberCellStyle);
			else if (object instanceof Boolean)
				cell.setCellValue((Boolean) object);
			else
				cell.setCellValue(object.toString());
			xpos.incrementAndGet();
			return cell;
		}

		/**
		 * Create a collection of cell at the current row position.
		 *
		 * @param objects a collection of values
		 */
		public void addCell(Object... objects) {
			if (objects == null || objects.length == 0)
				return;
			for (Object object : objects)
				addOneCell(object);
		}

		/**
		 * Increment the horizontal position of the cursor
		 *
		 * @param inc the incrementation step
		 * @return the new position of the cursor
		 */
		public int incCell(int inc) {
			return xpos.addAndGet(inc);
		}

		/**
		 * Fill the content of a sheet with a Cassandra resultSet. The fisrt row show the column definition.
		 *
		 * @param sheetName the name of the sheet
		 * @param resultSet the rows to copy
		 */
		public void createSheetAndFill(final String sheetName, final ResultSet resultSet) {

			activeSheetAndSetPos(sheetName, 0, 0);

			addRow();
			resultSet.getColumnDefinitions().forEach(colDef -> addCell(colDef.getName()));

			resultSet.forEach(row -> {
				addRow();
				xpos.set(0);
				row.getColumnDefinitions().forEach(colDef -> {
					final String colName = colDef.getName();
					if (row.isNull(colName))
						xpos.incrementAndGet();
					else
						addCell(row.getObject(colName));
				});
			});
		}

		/**
		 * Send the content of the Excel file using the right mime type.
		 *
		 * @param fileName the virtual name
		 * @param response
		 * @throws IOException
		 */
		public void send(String fileName, HttpServletResponse response) throws IOException {
			String mime = workbook instanceof HSSFWorkbook ?
					"application/vnd.ms-excel" :
					"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
			response.setContentType(mime);
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
			workbook.write(response.getOutputStream());
		}

		/**
		 * Save the content of the file
		 *
		 * @param file the destination file
		 * @throws IOException
		 */
		public void saveFile(File file) throws IOException {
			try (FileOutputStream outputStream = new FileOutputStream(file)) {
				workbook.write(outputStream);
			}
		}

		/**
		 * Save the content of the file
		 *
		 * @param filePath the path of the destination file
		 * @throws IOException
		 */
		public void saveFile(String filePath) throws IOException {
			this.saveFile(new File(filePath));
		}

		@Override
		public void close() throws IOException {
			workbook.close();
		}
	}

}
