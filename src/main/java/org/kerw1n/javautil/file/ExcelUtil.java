package org.kerw1n.javautil.file;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.kerw1n.javautil.format.ReflectionUtil;
import org.springframework.util.Assert;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Excel 操作
 *
 * @author kerw1n
 */
public class ExcelUtil {

    /**
     * 单表最大写入行数，不包含标题行
     */
    private static final int MAX_ROW = 65535;
    /**
     * 初始页码
     */
    private static final int INIT_PAGE = 1;
    /**
     * 2003 版后缀名称
     */
    private static final String SUFFIX_2003 = ".xls";
    /**
     * 2007 版后缀名称
     */
    private static final String SUFFIX_2007 = ".xlsx";
    /**
     * 默认表名称
     */
    private static final String DEFAULT_SHEET_NAME = "sheet";

    private ExcelUtil() {

    }

    /**
     * 读取 Excel 文件
     * <p>
     * 需含有标题行,返回 {@link List} 列表，{@link HashMap} 对应的键值分别为标题、值。
     * 列表顺序与表格顺序相同;
     *
     * @param file Excel 文件
     * @return {@code List<HashMap<String, Object>> }
     * @throws Exception
     */
    public static List<HashMap<String, String>> read(File file) throws IOException {
        return read(new FileInputStream(file));
    }

    /**
     * 读取Excel文件流
     * <p>
     * 需含有标题行,返回List列表,HashMap对应的键值分别为标题、值,列表顺序与表格顺序相同;
     *
     * @param inp 文件流
     * @return {@code List<HashMap<String, Object>> }
     * @throws Exception
     */
    public static List<HashMap<String, String>> read(InputStream inp) throws IOException {
        List<HashMap<String, String>> result = null;
        try {
            // 创建工作簿
            Workbook workbook = createWorkbook(inp);
            // 获取第一张表
            Sheet sheet = workbook.getSheetAt(0);

            // 标题行
            Row headerRow = sheet.getRow(0);
            int lastCellNum = headerRow.getLastCellNum();
            if (lastCellNum == 0) {
                return null;
            }
            String[] headerArray = new String[lastCellNum];
            int j = 0;
            while (j < lastCellNum) {
                String cellTitle = getCell(headerRow, j);
                if (StringUtils.isEmpty(cellTitle)) {
                    continue;
                }
                headerArray[j] = cellTitle;
                j++;
            }

            int lastRowNum = sheet.getLastRowNum();
            result = new ArrayList<>(lastRowNum);
            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                HashMap<String, String> data = new HashMap<>(lastCellNum);
                int k = 0;
                while (k < lastCellNum) {
                    data.put(headerArray[k], getCell(row, k));
                    result.add(data);
                    k++;
                }
            }
        } finally {
            IoUtil.close(inp);
        }
        return result;
    }

    /**
     * 写数据到 Excel，行数超过 {@link #MAX_ROW} 时分表
     *
     * @param sheetName 工作表名称
     * @param data      数据源，考虑到应用场景，目前仅支持 {@link List}
     * @param title     标题行<br>
     *                  key：指定字段名称，与 JavaBean 中对应<br>
     *                  value：显示的标题名称，为空则取字段名
     * @param path      生成的文件路径
     * @throws Exception
     */
    public static <T> void write(String sheetName, final List<T> data, final Map<String, String> title, String path) throws IOException, ReflectiveOperationException {
        Assert.isTrue((data != null && data.size() > 0), "Invalid data source.");
        Assert.isTrue((title != null && title.size() > 0), "Invalid column.");

        int total = data.size(), page = getPage(total);
        Workbook workbook = null;
        FileOutputStream os = null;
        try {
            workbook = createWorkbook(path);
            for (int l = 0; l < page; l++) {
                // 开始位置的下标、行号
                int beginIndex = l * MAX_ROW, rowNum = 0;
                // 创建表
                Sheet sheet = workbook.createSheet(getSheetName(sheetName, (l + 1)));
                // 创建标题行
                Row titleRow = sheet.createRow(0);
                for (int i = beginIndex; i < total; i++) {
                    if (rowNum == MAX_ROW) {
                        break;
                    }
                    T d = data.get(i);
                    if (d == null) {
                        continue;
                    }
                    Class<?> clazz = d.getClass();
                    Field[] fields = ReflectionUtil.getClassFields(clazz);
                    if (fields == null) {
                        continue;
                    }
                    int column = 0;
                    Row row = sheet.createRow(rowNum + 1);
                    for (Map.Entry<String, String> map : title.entrySet()) {
                        String key = map.getKey(), val = map.getValue();
                        // 写入标题
                        Cell titleCell = titleRow.createCell(column);
                        titleCell.setCellValue(null == val ? key : val);
                        if (StringUtils.isEmpty(key)) {
                            continue;
                        }
                        for (Field field : fields) {
                            String fieldName = field.getName();
                            if (fieldName.equalsIgnoreCase(key)) {
                                // 匹配字段赋值
                                String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                                Object value = clazz.getMethod(getMethodName).invoke(d);
                                Cell cell = row.createCell(column);
                                cell.setCellValue(value.toString());
                                break;
                            }
                        }
                        column++;
                    }
                    rowNum++;
                }
            }
            os = new FileOutputStream(path);
            workbook.write(os);
            os.flush();
        } finally {
            IoUtil.close(workbook, os);
        }
    }

    /**
     * 获取工作表名称
     *
     * @param name
     * @param index
     * @return 表名称
     */
    private static String getSheetName(String name, int index) {
        return StringUtils.isEmpty(name) ? DEFAULT_SHEET_NAME + index : name;
    }

    /**
     * 获取页数
     *
     * @param total 总条数
     * @return
     */
    private static int getPage(int total) {
        int page = INIT_PAGE;
        if (total > MAX_ROW) {
            page = total / MAX_ROW;
            if (total % MAX_ROW != 0) {
                page++;
            }
        }
        return page;
    }

    /**
     * 根据文件流创建工作簿对象
     * 创建不同版本Excel 的工作簿.
     *
     * @param inp 文件流
     * @return
     * @throws IOException
     */
    private static Workbook createWorkbook(InputStream inp) throws IOException {
        if (!inp.markSupported()) {
            inp = new PushbackInputStream(inp, 8);
        }
        if (POIFSFileSystem.hasPOIFSHeader(inp)) {
            return new HSSFWorkbook(inp);
        } else if (POIXMLDocument.hasOOXMLHeader(inp)) {
            return new XSSFWorkbook(inp);
        }
        throw new IllegalArgumentException("不支持的excel版本.");
    }

    /**
     * 根据文件名创建工作簿对象
     *
     * @param path
     * @return
     */
    private static Workbook createWorkbook(String path) {
        if (StringUtils.isNotEmpty(path)) {
            if (path.endsWith(SUFFIX_2003)) {
                return new HSSFWorkbook();
            } else if (path.endsWith(SUFFIX_2007)) {
                return new XSSFWorkbook();
            }
        }
        throw new IllegalArgumentException("不支持的excel版本.");
    }

    /**
     * 获取指定单元格的值
     * 均按照字符串处理，日期则格式化为{@link org.kerw1n.javautil.format.DateUtil.Format#FORMAT_03}
     *
     * @param row
     * @param cellNum
     * @return
     */
    private static String getCell(Row row, int cellNum) {
        Cell cell = row.getCell(cellNum);
        if (cell == null) {
            return null;
        }
        String value = "";
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            Date d = cell.getDateCellValue();
            value = org.kerw1n.javautil.format.DateUtil.formatDate(d, org.kerw1n.javautil.format.DateUtil.Format.FORMAT_03);
        } else {
            cell.setCellType(Cell.CELL_TYPE_STRING);
            value = cell.getStringCellValue();
        }
        return value;
    }
}
