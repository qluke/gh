package com.luke.spring.boot.autoconfigure.datasource;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Data
public abstract class TableMeta {

    private List<Map<String, Object>> beforeImage;

    private List<Map<String, Object>> afterImage;

    private String tableName;

    private String originalSql;

    private String querySql;
}
