/*
 *  Copyright 2018 jd.com
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.yihaodian.architecture.kira.manager.util.excel;

import com.yihaodian.architecture.kira.common.exception.KiraHandleException;
import com.yihaodian.architecture.kira.manager.util.excel.bean.ExcelCellDescriptor;
import com.yihaodian.architecture.kira.manager.util.excel.bean.ExcelExportDataHolder;
import com.yihaodian.architecture.kira.manager.util.excel.bean.ExcelSheetData;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ExcelUtil {

  private static final Logger LOG = LoggerFactory.getLogger(ExcelUtil.class);
  private static final String ERROR_MESSAGE_READ_UPLOADED_EXCEL = "Error reading excel file. The file uploaded must be in an Excel file format.";
  private OutputStream os;
  private HSSFSheet sheet;
  private HSSFWorkbook wb;
  //Need to cache HSSFCellStyles to avoid exception. (The maximum number of cell styles was exceeded. You can define up to 4000 styles in a .xls workbook)
  private Map<Short, HSSFCellStyle> alignmentHSSFCellStyleMap = new LinkedHashMap<Short, HSSFCellStyle>();
  private String sheetNamePrefix;
  private int currentLineNumber;
  private int pageLength = 65000;
  private int pageNo = 1;
  private int pageNoToDisplay = 1;
  private List<String> headerLineList;
  private String space = " ";

  private ExcelUtil() {
  }

  private ExcelUtil(List<String> headerLineList, String sheetNamePrefix, OutputStream os) {
    this.headerLineList = headerLineList;
    this.sheetNamePrefix = sheetNamePrefix;
    this.os = os;
  }

  private ExcelUtil(List<String> headerLineList, String sheetNamePrefix, OutputStream os,
      int pageLength) {
    this(headerLineList, sheetNamePrefix, os);
    this.pageLength = pageLength;
  }

  public static List<List<Cell>> readExcelFile(InputStream inputStream) throws KiraHandleException {
    List<List<Cell>> excelData = new ArrayList<List<Cell>>();
    try {
      Workbook workBook = WorkbookFactory.create(inputStream);
      Sheet sheet = workBook.getSheetAt(0);
      for (Row row : sheet) {
        List<Cell> cellData = new ArrayList<Cell>();
        for (int i = 0; i < row.getLastCellNum(); i++) {
          cellData.add(row.getCell(i));
        }
        excelData.add(cellData);
      }
    } catch (Exception e) {
      throw new KiraHandleException(ERROR_MESSAGE_READ_UPLOADED_EXCEL);
    }
    return excelData;
  }

  /**
   * @return the Map<lineNumber, Map<fieldName, fieldValue>> where lineNumber is 1 based. And only
   * contain lines with contents.
   */
  public static Map<Integer, Map<String, String>> readExcelFile(InputStream inputStream,
      Set<String> fieldNameSet) throws KiraHandleException {
    Map<Integer, Map<String, String>> lineNumberRowDataMap = new LinkedHashMap<Integer, Map<String, String>>();
    try {
      Workbook workBook = WorkbookFactory.create(inputStream);
      Sheet sheet = workBook.getSheetAt(0);
      Map<String, Integer> headerColumnNameIndexMap = new LinkedHashMap<String, Integer>();
      boolean isHasData = false;
      int lastRowNum = sheet.getLastRowNum();
      for (int i = 0; i <= lastRowNum; i++) {
        Row row = sheet.getRow(i);
        if (null != row) {
          if (MapUtils.isEmpty(headerColumnNameIndexMap)) {
            headerColumnNameIndexMap = getHeaderColumnNameIndexMap(row, fieldNameSet);
          } else {
            Map<String, String> fieldNameFieldValueMap = new LinkedHashMap<String, String>();
            boolean isEmptyContentRow = true;
            for (String fieldName : fieldNameSet) {
              int columnIndex = headerColumnNameIndexMap.get(fieldName).intValue();
              Cell cell = row.getCell(columnIndex);
              String cellValueAsString = getCellValueAsString(cell);
              if (isEmptyContentRow) {
                if (StringUtils.isNotEmpty(cellValueAsString)) {
                  isEmptyContentRow = false;
                }
              }
              fieldNameFieldValueMap.put(fieldName, cellValueAsString);
            }
            if (!isEmptyContentRow) {
              lineNumberRowDataMap.put(Integer.valueOf(i + 1), fieldNameFieldValueMap);
              if (!isHasData) {
                isHasData = true;
              }
            }
          }
        }
      }
      if (MapUtils.isEmpty(headerColumnNameIndexMap)) {
        throw new KiraHandleException("The file you uploaded should not be empty.");
      }
      if (!isHasData) {
        throw new KiraHandleException("Please provide the data to be uploaded.");
      }
    } catch (KiraHandleException be) {
      throw be;
    } catch (Exception e) {
      LOG.error("Error while readExcelFile: ", e);
      throw new KiraHandleException(ERROR_MESSAGE_READ_UPLOADED_EXCEL);
    }
    return lineNumberRowDataMap;
  }

  private static Map<String, Integer> getHeaderColumnNameIndexMap(Row row, Set<String> fieldNameSet)
      throws KiraHandleException {
    Map<String, Integer> headerColumnNameIndexMap = new LinkedHashMap<String, Integer>();
    if (null != row) {
      boolean isEmptyContentRow = true;
      int firstCellNum = row.getFirstCellNum();
      if (-1 != firstCellNum) {
        int lastCellNum = row.getLastCellNum();
        for (int colIx = firstCellNum; colIx < lastCellNum; colIx++) {
          Cell cell = row.getCell(colIx);
          if (null == cell) {
            continue;
          }
          String cellValueAsString = getCellValueAsString(cell);
          if (StringUtils.isNotEmpty(cellValueAsString)) {
            headerColumnNameIndexMap.put(cellValueAsString.trim().toUpperCase(),
                Integer.valueOf(cell.getColumnIndex()));
            if (isEmptyContentRow) {
              isEmptyContentRow = false;
            }
          }
        }
        if (!isEmptyContentRow) {
          headerColumnNameIndexMap = getCheckedHeaderColumnNameIndexMap(headerColumnNameIndexMap,
              fieldNameSet);
          if (MapUtils.isEmpty(headerColumnNameIndexMap)) {
            throw new KiraHandleException(
                "Can not find the expected excel file header, please check your uploaded excel file again.");
          }
        }
      }
    }
    return headerColumnNameIndexMap;
  }

  private static Map<String, Integer> getCheckedHeaderColumnNameIndexMap(
      Map<String, Integer> headerColumnNameIndexMap, Set<String> fieldNameSet) {
    Map<String, Integer> checkedHeaderColumnNameIndexMap = new LinkedHashMap<String, Integer>();
    if (headerColumnNameIndexMap.size() == fieldNameSet.size()) {
      for (String fieldName : fieldNameSet) {
        Integer columnIndexValue = headerColumnNameIndexMap.get(fieldName.toUpperCase());
        if (null == columnIndexValue) {
          checkedHeaderColumnNameIndexMap.clear();
          break;
        } else {
          checkedHeaderColumnNameIndexMap.put(fieldName, columnIndexValue);
        }
      }
    }
    return checkedHeaderColumnNameIndexMap;
  }

  private static String getCellValueAsString(Cell cell) {
    String returnValue = "";
    if (null != cell) {
      int cellType = cell.getCellType();
      switch (cellType) {
        case Cell.CELL_TYPE_NUMERIC:
          returnValue = new BigDecimal(cell.getNumericCellValue()).toString();
          break;
        default:
          returnValue = cell.getStringCellValue();
      }
    }
    return returnValue;
  }

  public static void write(List<ExcelSheetData> excelSheetDataList, OutputStream os)
      throws IOException {
    if (!CollectionUtils.isEmpty(excelSheetDataList)) {
      ExcelUtil writer = null;
      try {
        for (ExcelSheetData oneExcelSheetData : excelSheetDataList) {
          String sheetNamePrefix = oneExcelSheetData.getSheetNamePrefix();
          List<ExcelExportDataHolder> excelExportDataHolderList = oneExcelSheetData
              .getExcelExportDataHolderList();
          if (!CollectionUtils.isEmpty(excelExportDataHolderList)) {
            int length = excelExportDataHolderList.size();
            ExcelExportDataHolder excelExportDataHolder = null;
            for (int i = 0; i < length; i++) {
              excelExportDataHolder = excelExportDataHolderList.get(i);
              if (null != excelExportDataHolder) {
                List<String> headerLineList = excelExportDataHolder.getHeaderLineList();
                if (!CollectionUtils.isEmpty(headerLineList)) {
                  if (null == writer) {
                    writer = new ExcelUtil(headerLineList, sheetNamePrefix, os);
                    writer.init();
                  } else {
                    if (0 == i) {
                      writer.workOnNextSheet(sheetNamePrefix, headerLineList);
                    } else {
                      //One sheet have more than one part.
                      writer.workOnNextPart(headerLineList);
                    }
                  }
                  List<List<String>> dataList = excelExportDataHolder.getDataList();
                  if (!CollectionUtils.isEmpty(dataList)) {
                    List<ExcelCellDescriptor> dataCellDescriptorList = excelExportDataHolder
                        .getDataCellDescriptorList();
                    writer.writeDataList(dataList, dataCellDescriptorList);
                  }
                }
              }
            }
          }
        }
      } catch (Exception e) {
        LOG.error("Error occurs for write", e);
      } finally {
        if (null != writer) {
          writer.completeFileWriting();
          os.flush();
        }
      }
    }
  }

  public static byte[] getExcelFileByteData(List<ExcelSheetData> excelSheetDataList)
      throws Exception {
    byte[] returnValue = null;
    if (!CollectionUtils.isEmpty(excelSheetDataList)) {
      ByteArrayOutputStream byteArrayOutputStream = null;
      try {
        byteArrayOutputStream = new ByteArrayOutputStream();
        ExcelUtil.write(excelSheetDataList, byteArrayOutputStream);
        returnValue = byteArrayOutputStream.toByteArray();
      } finally {
        if (null != byteArrayOutputStream) {
          try {
            byteArrayOutputStream.close();
          } catch (IOException e) {
            LOG.error("IOException occurs for getExcelFileByteData.close().", e);
          }
        }
      }
    }
    return returnValue;
  }

  public static void write(ExcelExportDataHolder excelExportDataHolder, String sheetNamePrefix,
      OutputStream os) throws IOException {
    if (null != excelExportDataHolder) {
      List<String> headerLineList = excelExportDataHolder.getHeaderLineList();
      List<List<String>> dataList = excelExportDataHolder.getDataList();
      write(headerLineList, dataList, sheetNamePrefix, os);
    }
  }

  public static void write(List<String> headerLineList, List<List<String>> dataList,
      String sheetNamePrefix, OutputStream os) throws IOException {
    ExcelUtil writer = null;
    try {
      writer = new ExcelUtil(headerLineList, sheetNamePrefix, os);
      writer.init();
      if (null != dataList) {
        for (List<String> dataLineList : dataList) {
          if (null != dataLineList) {
            writer.writeNextDataLine(dataLineList, null);
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Error occurs for write", e);
    } finally {
      if (null != writer) {
        writer.completeFileWriting();
        os.flush();
      }
    }
  }

  public static void write(List<String> headerLineList, List<List<String>> dataList,
      String sheetNamePrefix, OutputStream os, int pageLength) throws IOException {
    ExcelUtil writer = null;
    try {
      writer = new ExcelUtil(headerLineList, sheetNamePrefix, os, pageLength);
      writer.init();
      if (null != dataList) {
        for (List<String> dataLineList : dataList) {
          if (null != dataLineList) {
            writer.writeNextDataLine(dataLineList, null);
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Error occurs for write", e);
    } finally {
      if (null != writer) {
        writer.completeFileWriting();
        os.flush();
      }
    }
  }

  private void writeDataList(List<List<String>> dataList,
      List<ExcelCellDescriptor> dataCellDescriptorList) {
    if (!CollectionUtils.isEmpty(dataList)) {
      for (List<String> dataLineList : dataList) {
        if (null != dataLineList) {
          this.writeNextDataLine(dataLineList, dataCellDescriptorList);
        }
      }
    }
  }

  private void init() {
    wb = new HSSFWorkbook();
    sheet = wb.createSheet(sheetNamePrefix);
    writeHeaderLine();
  }

  private void workOnNextSheet(String sheetNamePrefix, List<String> headerLineList) {
    this.sheetNamePrefix = sheetNamePrefix;
    pageNo++;
    pageNoToDisplay = 1;
    currentLineNumber = 0;
    this.headerLineList = headerLineList;
    sheet = wb.createSheet();
    setSheetName();
    writeHeaderLine();
  }

  private void setSheetName() {
    if (pageNoToDisplay > 1) {
      wb.setSheetName(pageNo - 1, sheetNamePrefix + space + pageNoToDisplay);
      if (2 == pageNoToDisplay) {
        wb.setSheetName(pageNo - 2, sheetNamePrefix + space + 1);
      }
    } else {
      wb.setSheetName(pageNo - 1, sheetNamePrefix);
    }
  }

  private void workOnNextPart(List<String> headerLineList) {
    this.headerLineList = headerLineList;
    //Parts are seperated with one empty line.
    writeEmptyLine();
    writeHeaderLine();
  }

  private void writeEmptyLine() {
    if (currentLineNumber == pageLength) {
      this.autoSizeColumns();
      return;
    }
    HSSFRow row = sheet.createRow(currentLineNumber);
    HSSFCell cell = row.createCell(0);
    cell.setCellValue("");
    cell.setCellType(HSSFCell.CELL_TYPE_BLANK);
    currentLineNumber++;
    this.autoSizeColumns();
  }

  private void workOnNewSheetIfNeeded() {
    if (currentLineNumber == pageLength) {
      pageNo++;
      pageNoToDisplay++;
      currentLineNumber = 0;
      sheet = wb.createSheet(sheetNamePrefix + space + pageNoToDisplay);
      setSheetName();
      writeHeaderLine();
    }
  }

  private void writeHeaderLine() {
    if (!CollectionUtils.isEmpty(headerLineList)) {
      workOnNewSheetIfNeeded();
      HSSFRow row = sheet.createRow(currentLineNumber);
      for (int i = 0; i < headerLineList.size(); i++) {
        HSSFCell cell = row.createCell(i);
        HSSFRichTextString str;
        if (headerLineList.get(i) != null) {
          str = new HSSFRichTextString(headerLineList.get(i));
        } else {
          str = new HSSFRichTextString("");
        }
        cell.setCellValue(str);
      }
      currentLineNumber++;

      this.autoSizeColumns();
    }
  }

  private void writeNextDataLine(List<String> dataLineList,
      List<ExcelCellDescriptor> dataCellDescriptorList) {
    workOnNewSheetIfNeeded();
    HSSFRow row = sheet.createRow(currentLineNumber);
    boolean canDataCellDescriptorListApplied = canDataCellDescriptorListApplied(dataLineList,
        dataCellDescriptorList);
    for (int i = 0; i < dataLineList.size(); i++) {
      HSSFCell cell = row.createCell(i);
      if (canDataCellDescriptorListApplied) {
        applyCellDescriptor(dataCellDescriptorList.get(i), cell);
      }
      HSSFRichTextString str;
      if (dataLineList.get(i) != null) {
        str = new HSSFRichTextString(dataLineList.get(i));
      } else {
        str = new HSSFRichTextString("");
      }
      cell.setCellValue(str);
    }
    currentLineNumber++;

    this.autoSizeColumns();
  }

  private void autoSizeColumns() {
    if (null != this.headerLineList) {
      for (int i = 0; i < this.headerLineList.size(); i++) {
        if (null != this.sheet) {
          this.sheet.autoSizeColumn((short) i);
        }
      }
    }
  }

  private void applyCellDescriptor(ExcelCellDescriptor excelCellDescriptor, HSSFCell cell) {
    if (null != excelCellDescriptor) {
      Integer cellType = excelCellDescriptor.getCellType();
      if (null != cellType) {
        cell.setCellType(cellType.intValue());
      }
      Short alignment = excelCellDescriptor.getAlignment();
      HSSFCellStyle cellStyle = null;
      if (null != alignment) {
        cellStyle = this.alignmentHSSFCellStyleMap.get(alignment);
        if (null == cellStyle) {
          cellStyle = this.wb.createCellStyle();
          cellStyle.setAlignment(alignment.shortValue());
          //Cache it to avoid limit exception.
          this.alignmentHSSFCellStyleMap.put(alignment, cellStyle);
        }
      }
      if (null != cellStyle) {
        cell.setCellStyle(cellStyle);
      }
    }
  }

  private boolean canDataCellDescriptorListApplied(List<String> dataLineList,
      List<ExcelCellDescriptor> dataCellDescriptorList) {
    boolean returnValue = false;
    if (!CollectionUtils.isEmpty(dataLineList) && !CollectionUtils
        .isEmpty(dataCellDescriptorList)) {
      returnValue = dataCellDescriptorList.size() == dataCellDescriptorList.size();
    }
    return returnValue;
  }

  private void completeFileWriting() throws IOException {
    wb.write(os);
  }
}
