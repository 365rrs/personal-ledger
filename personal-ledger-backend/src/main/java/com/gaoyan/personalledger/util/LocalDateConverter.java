package com.gaoyan.personalledger.util;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * LocalDate类型转换器
 */
public class LocalDateConverter implements Converter<LocalDate> {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @Override
    public Class<?> supportJavaTypeKey() {
        return LocalDate.class;
    }
    
    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }
    
    @Override
    public LocalDate convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        return LocalDate.parse(cellData.getStringValue(), FORMATTER);
    }
    
    @Override
    public WriteCellData<?> convertToExcelData(LocalDate value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        return new WriteCellData<>(value.format(FORMATTER));
    }
}
