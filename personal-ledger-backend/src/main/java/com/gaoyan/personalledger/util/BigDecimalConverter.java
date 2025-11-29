package com.gaoyan.personalledger.util;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.math.BigDecimal;

/**
 * BigDecimal转换器 - 精确保持原始金额，不做四舍五入
 */
public class BigDecimalConverter implements Converter<BigDecimal> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return BigDecimal.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public BigDecimal convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
                                        GlobalConfiguration globalConfiguration) {
        // 优先使用字符串值，避免浮点数精度问题
        if (cellData.getStringValue() != null && !cellData.getStringValue().trim().isEmpty()) {
            try {
                return new BigDecimal(cellData.getStringValue().trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        // 如果是数字类型，转为字符串再创建BigDecimal
        if (cellData.getNumberValue() != null) {
            return new BigDecimal(cellData.getNumberValue().toPlainString());
        }
        return null;
    }

    @Override
    public WriteCellData<?> convertToExcelData(BigDecimal value, ExcelContentProperty contentProperty,
                                                GlobalConfiguration globalConfiguration) {
        if (value == null) {
            return new WriteCellData<>("");
        }
        // 使用字符串形式输出，保持精度
        return new WriteCellData<>(value.toPlainString());
    }
}
