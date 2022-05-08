package com.luke.spring.boot.autoconfigure.datasource;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TableUpdateMeta extends TableMeta{

    private List<String> updateColumns;

}
