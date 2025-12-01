package com.gaoyan.personalledger.util;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * LocalTime类型转换器
 */
public class LocalTimeConverter implements Converter<LocalTime> {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    @Override
    public Class<?> supportJavaTypeKey() {
        return LocalTime.class;
    }
    
    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }
    
    @Override
    public LocalTime convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        return LocalTime.parse(cellData.getStringValue(), FORMATTER);
    }
    
    @Override
    public WriteCellData<?> convertToExcelData(LocalTime value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        return new WriteCellData<>(value.format(FORMATTER));
    }
}
