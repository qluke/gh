package com.luke.spring.boot.autoconfigure.datasource;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.JdbcConstants;
import lombok.extern.slf4j.Slf4j;

import java.util.*;


@Slf4j
public class UpdateExecutor implements AbstractDMLBaseExecutor {

    private MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();

    private boolean ONLY_CARE_UPDATE_COLUMNS;

    public UpdateExecutor() {
    }

    public UpdateExecutor(boolean onlyCareUpdateColumns) {
        this.ONLY_CARE_UPDATE_COLUMNS = onlyCareUpdateColumns;
    }

    public List<TableUpdateMeta> getBeforeImage(String sql) {
        List<TableUpdateMeta> tableUpdateMetas = new ArrayList<>();
        List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, JdbcConstants.MYSQL);
        for (SQLStatement stmt : stmtList) {
            MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
            stmt.accept(visitor);
            TableUpdateMeta updateMeta = new TableUpdateMeta();
            updateMeta.setOriginalSql(sql);
            buildBeforeImageSQL(visitor, updateMeta);
            tableUpdateMetas.add(updateMeta);
        }
        return tableUpdateMetas;
    }

    public void buildBeforeImageSQL(SchemaStatVisitor schemaStatVisitor, TableUpdateMeta tableMeta) {
        String tableName = getTableName(schemaStatVisitor);
        String prefix = "SELECT ";
        StringBuilder suffix = new StringBuilder(" FROM ").append(tableName);

        String whereCondition = buildCondition(schemaStatVisitor, "=", "IN");
        String orderByCondition = buildCondition(schemaStatVisitor, "LIKE");
        String limitCondition = buildCondition(schemaStatVisitor, "LIMIT");
        if (StrUtil.isNotBlank(whereCondition)) {
            suffix.append(" WHERE ").append(whereCondition);
        }
        if (StrUtil.isNotBlank(orderByCondition)) {
            suffix.append(" ").append(orderByCondition);
        }
        if (StrUtil.isNotBlank(limitCondition)) {
            suffix.append(" ").append(limitCondition);
        }

        StringJoiner selectSQLJoiner = new StringJoiner(", ", prefix, suffix.toString().replaceFirst(" AND ", " "));
        List<String> updateColumns = getUpdateColumns(schemaStatVisitor);
        if (ONLY_CARE_UPDATE_COLUMNS) {
            for (String updateColumn : updateColumns) {
                selectSQLJoiner.add(updateColumn);
            }
        }else{
            selectSQLJoiner.add(" * ");
        }
        tableMeta.setUpdateColumns(updateColumns);
        tableMeta.setTableName(tableName);
        tableMeta.setQuerySql(selectSQLJoiner.toString());
    }

    private List<String> getUpdateColumns(SchemaStatVisitor visitor) {
        List<String> updateColumns = new ArrayList<>();
        // 获取查询列名
        Collection<TableStat.Column> columns = visitor.getColumns();
        for (TableStat.Column column : columns) {
            if (column.isUpdate()) {
                updateColumns.add(column.getName());
            }
        }
        return updateColumns;
    }
    private String getTableName(SchemaStatVisitor visitor) {
        Map<TableStat.Name, TableStat> tables = visitor.getTables();
        for (TableStat.Name name : tables.keySet()) {
            return name.getName();
        }
        return null;
    }


    private String buildCondition(SchemaStatVisitor visitor, String ... operatorName) {
        StringBuilder whereCondition = new StringBuilder();
        // 获取查询条件
        List<TableStat.Condition> conditions = visitor.getConditions();
        for (TableStat.Condition condition : conditions) {
            TableStat.Column column = condition.getColumn();
            String columnName = column.getName();
            String operator = condition.getOperator();// 比如=、IN、LIKE、LIMIT
            List<Object> values = condition.getValues();
            for (String op : operatorName) {
                if(op.equals(operator)){
                    for (Object value : values) {
                        whereCondition.append(" AND ")
                                .append(columnName).append(" ")
                                .append(operator).append(" ")
                                .append("'").append(value).append("'");
                    }
                }
            }
        }
        return whereCondition.toString();
    }
}
