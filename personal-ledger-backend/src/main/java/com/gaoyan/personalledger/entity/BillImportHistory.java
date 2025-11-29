package com.gaoyan.personalledger.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 账单导入历史记录实体
 */
@Data
@TableName("bill_import_history")
public class BillImportHistory {
    
    @TableId(type = IdType.INPUT)
    private Long id;
    
    /**
     * 导入批次名称
     */
    private String importName;
    
    /**
     * 源文件名
     */
    private String sourceFile;
    
    /**
     * 文件类型：CSV/EXCEL
     */
    private String fileType;
    
    /**
     * 导入时间
     */
    private LocalDateTime importTime;
    
    /**
     * 记录数量
     */
    private Integer recordCount;
    
    /**
     * 新增记录数
     */
    private Integer newCount;
    
    /**
     * 更新记录数
     */
    private Integer updateCount;
    
    /**
     * 重复记录数
     */
    private Integer duplicateCount;
    
    /**
     * 账户号码
     */
    private String accountNumber;
    
    /**
     * 账单起始日期
     */
    private LocalDate periodStart;
    
    /**
     * 账单结束日期
     */
    private LocalDate periodEnd;
    
    /**
     * 导入状态：SUCCESS/FAILED/PARTIAL
     */
    private String importStatus;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
