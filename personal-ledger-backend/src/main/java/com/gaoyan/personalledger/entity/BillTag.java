package com.gaoyan.personalledger.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("bill_tag")
public class BillTag {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String color;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
