package com.gaoyan.personalledger.util;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.List;

/**
 * Easy Excel导出工具
 *
 * @author gaomingxi@haier.com
 * @version 1.0
 * @date 2020/10/20 0020 16:07
 */
@Slf4j
public class EasyExcelExportUtil {

    public static HorizontalCellStyleStrategy generatorHorizontalCellStyleStrategy() {
        // 头的策略
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        // 背景设置
        headWriteCellStyle.setFillPatternType(FillPatternType.NO_FILL);

        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setFontHeightInPoints((short) 10);
        headWriteFont.setBold(false);
        headWriteFont.setFontName("Arial");
        headWriteCellStyle.setWriteFont(headWriteFont);
        // 内容的策略
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        // 这里需要指定 FillPatternType 为FillPatternType.SOLID_FOREGROUND 不然无法显示背景颜色.头默认了 FillPatternType所以可以不指定
        // 背景
        WriteFont contentWriteFont = new WriteFont();
        contentWriteFont.setFontHeightInPoints((short) 10);
        contentWriteFont.setBold(false);
        contentWriteFont.setFontName("Arial");
        contentWriteCellStyle.setWriteFont(contentWriteFont);
        contentWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
        contentWriteCellStyle.setBorderBottom(BorderStyle.THIN);
        contentWriteCellStyle.setBorderLeft(BorderStyle.THIN);
        contentWriteCellStyle.setBorderRight(BorderStyle.THIN);
        contentWriteCellStyle.setBorderTop(BorderStyle.THIN);
        contentWriteCellStyle.setShrinkToFit(false);
        // 这个策略是 头是头的样式 内容是内容的样式 其他的策略可以自己实现
        return new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
    }

    /**
     * 导出Excel
     *
     * @param response
     * @param fileName
     * @param sheetName
     * @param head
     * @param list
     */
    public static void exportExcel(HttpServletResponse response, String fileName, String sheetName, List<List<String>> head, List<List<Object>> list) {
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String excelFileName = null;
        try {
            excelFileName = URLEncoder.encode(fileName + "-", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + excelFileName + DateUtil.format(new DateTime(), DatePattern.PURE_DATETIME_MS_FORMAT) + ".xlsx");
            ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).registerWriteHandler(EasyExcelExportUtil.generatorHorizontalCellStyleStrategy()).build();
            //这里 需要指定写用哪个class去写
            WriteSheet writeSheet = EasyExcel.writerSheet(0, sheetName).head(head).build();
            excelWriter.write(list, writeSheet);
            //千万别忘记finish 会帮忙关闭流
            excelWriter.finish();
        } catch (Exception e) {
            log.error("EasyExcel导出util异常，异常信息：", e);
        }
    }
}
