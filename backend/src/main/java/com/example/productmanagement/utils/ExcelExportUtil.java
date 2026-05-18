package com.example.productmanagement.utils;

import com.example.productmanagement.dto.SalesDTO;
import com.example.productmanagement.dto.SalesRankDTO;
import com.example.productmanagement.dto.SearchKeywordDTO;
import com.example.productmanagement.dto.UserProfileDTO;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Excel导出工具类
 */
public class ExcelExportUtil {

    /**
     * 导出销量排行数据
     */
    public static void exportSalesRank(List<SalesRankDTO> data, OutputStream outputStream) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("销量排行");

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"排名", "书籍ID", "书籍名称", "ISBN", "作者", "出版社", "分类", "销售数量", "销售金额"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(getHeaderStyle(workbook));
        }

        // 填充数据
        for (int i = 0; i < data.size(); i++) {
            SalesRankDTO dto = data.get(i);
            Row row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(dto.getRank());
            row.createCell(1).setCellValue(dto.getBookId());
            row.createCell(2).setCellValue(dto.getBookName());
            row.createCell(3).setCellValue(dto.getIsbn());
            row.createCell(4).setCellValue(dto.getAuthor());
            row.createCell(5).setCellValue(dto.getPublisher());
            row.createCell(6).setCellValue(dto.getCategory());
            row.createCell(7).setCellValue(dto.getTotalQuantity());
            row.createCell(8).setCellValue(dto.getTotalSales().doubleValue());
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(outputStream);
        workbook.close();
    }

    /**
     * 导出销售数据
     */
    public static void exportSalesData(List<SalesDTO> data, OutputStream outputStream) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("销售数据统计");

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"统计日期", "订单数量", "总金额", "实收金额"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(getHeaderStyle(workbook));
        }

        // 填充数据
        for (int i = 0; i < data.size(); i++) {
            SalesDTO dto = data.get(i);
            Row row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(dto.getStatDate());
            row.createCell(1).setCellValue(dto.getOrderCount());
            row.createCell(2).setCellValue(dto.getTotalSales().doubleValue());
            row.createCell(3).setCellValue(dto.getActualSales().doubleValue());
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(outputStream);
        workbook.close();
    }

    /**
     * 导出用户画像数据
     */
    public static void exportUserProfile(List<UserProfileDTO> data, OutputStream outputStream) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("用户画像统计");

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"分类名称", "用户数量"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(getHeaderStyle(workbook));
        }

        // 填充数据
        for (int i = 0; i < data.size(); i++) {
            UserProfileDTO dto = data.get(i);
            Row row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(dto.getCategoryName());
            row.createCell(1).setCellValue(dto.getUserCount());
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(outputStream);
        workbook.close();
    }

    /**
     * 导出分类销量排行数据
     */
    public static void exportCategoryRank(List<Map<String, Object>> data, OutputStream outputStream) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("分类销量排行");

        Row headerRow = sheet.createRow(0);
        String[] headers = {"排名", "分类名称", "销售数量", "销售金额", "涉及书籍数"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(getHeaderStyle(workbook));
        }

        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> map = data.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(i + 1);
            row.createCell(1).setCellValue(String.valueOf(map.get("category")));
            Object totalQuantity = map.get("total_quantity");
            row.createCell(2).setCellValue(totalQuantity != null ? ((Number) totalQuantity).intValue() : 0);
            Object totalSales = map.get("total_sales");
            row.createCell(3).setCellValue(totalSales != null ? ((Number) totalSales).doubleValue() : 0);
            Object bookCount = map.get("book_count");
            row.createCell(4).setCellValue(bookCount != null ? ((Number) bookCount).intValue() : 0);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(outputStream);
        workbook.close();
    }

    /**
     * 导出分类销量趋势数据
     */
    public static void exportCategoryTrend(List<Map<String, Object>> data, OutputStream outputStream) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("分类销量趋势");

        Row headerRow = sheet.createRow(0);
        String[] headers = {"月份", "分类名称", "销售数量", "销售金额"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(getHeaderStyle(workbook));
        }

        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> map = data.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(String.valueOf(map.get("stat_month")));
            row.createCell(1).setCellValue(String.valueOf(map.get("category")));
            Object totalQuantity = map.get("total_quantity");
            row.createCell(2).setCellValue(totalQuantity != null ? ((Number) totalQuantity).intValue() : 0);
            Object totalSales = map.get("total_sales");
            row.createCell(3).setCellValue(totalSales != null ? ((Number) totalSales).doubleValue() : 0);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(outputStream);
        workbook.close();
    }

    /**
     * 导出搜索关键词数据
     */
    public static void exportSearchKeywords(List<SearchKeywordDTO> data, OutputStream outputStream) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("热门搜索关键词");

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"排名", "关键词", "搜索次数", "总结果数"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(getHeaderStyle(workbook));
        }

        // 填充数据
        for (int i = 0; i < data.size(); i++) {
            SearchKeywordDTO dto = data.get(i);
            Row row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(dto.getRank());
            row.createCell(1).setCellValue(dto.getKeyword());
            row.createCell(2).setCellValue(dto.getSearchCount());
            row.createCell(3).setCellValue(dto.getTotalResults());
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(outputStream);
        workbook.close();
    }

    /**
     * 获取标题样式
     */
    private static CellStyle getHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}
