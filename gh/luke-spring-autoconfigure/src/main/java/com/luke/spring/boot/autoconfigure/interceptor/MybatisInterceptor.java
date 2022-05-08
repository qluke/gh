package com.luke.spring.boot.autoconfigure.interceptor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.sql.parser.ParserException;
import com.luke.spring.boot.autoconfigure.annotation.GlobalHistory;
import com.luke.spring.boot.autoconfigure.datasource.TableUpdateMeta;
import com.luke.spring.boot.autoconfigure.datasource.UpdateExecutor;
import com.luke.spring.boot.autoconfigure.util.SqlTools;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;


/**
 * mybatis拦截器
 */
@Intercepts({@Signature(
        type = Executor.class,
        method = "update",
        args = {MappedStatement.class, Object.class})})
@SuppressWarnings({"unchecked", "rawtypes"})
public class MybatisInterceptor implements Interceptor {

    private final JdbcTemplate jdbcTemplate;

    private final JdbcTemplate jdbcTemplateWrite;

    public MybatisInterceptor(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcTemplateWrite = jdbcTemplate;
    }

    public MybatisInterceptor(JdbcTemplate jdbcTemplate, JdbcTemplate jdbcTemplateWrite){
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcTemplateWrite = jdbcTemplateWrite;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        String cmdType = null;
        List<TableUpdateMeta> updateMetas = new ArrayList<>();
        try {
            // 获取xml中的一个select/update/insert/delete节点，是一条SQL语句
            MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
            // id为执行的mapper方法的全路径名
            String id = mappedStatement.getId();
            // 注解逻辑判断 添加注解了才拦截
            Class<?> classType = Class.forName(id.substring(0, mappedStatement.getId().lastIndexOf(".")));
            if (!classType.isAnnotationPresent(GlobalHistory.class)) {
                return invocation.proceed();
            }
            GlobalHistory globalHistory = classType.getAnnotation(GlobalHistory.class);

            Object parameter = null;
            if (invocation.getArgs().length > 1) {
                parameter = invocation.getArgs()[1];
            }

            // BoundSql就是封装myBatis最终产生的sql类
            BoundSql boundSql = mappedStatement.getBoundSql(parameter);

            // 获取节点的配置
            Configuration configuration = mappedStatement.getConfiguration();

            // 获取到最终的sql语句
            String sql = SqlTools.showSql(configuration, boundSql);

            // 判断sql操作类型 ：update和delete的时候才需要去查询修改前的值
            cmdType = mappedStatement.getSqlCommandType().name().toLowerCase(Locale.ROOT);

            switch (cmdType){
                case "update":
                    updateMetas = new UpdateExecutor(globalHistory.onlyCareUpdateColumns()).getBeforeImage(sql);
                    query(updateMetas);
                    break;
                case "delete":
                    updateMetas = new UpdateExecutor(false).getBeforeImage(sql);
                    query(updateMetas);
                    break;
            }
        } catch (ParserException p) {
            System.out.println("syntax error : ");
        }catch (Exception e) {
            e.printStackTrace();
        }
        // 执行完上面的任务后，不改变原有的sql执行过程
        Object proceed = invocation.proceed();

        if(cmdType == null){
            return proceed;
        }
        try {
            switch (cmdType){
                case "update":
                    query(updateMetas);
                    save(updateMetas);
                case "delete":
                    break;
            }
        }catch (ParserException p) {
            System.out.println("syntax error : ");
        }catch (Exception e) {
            e.printStackTrace();
        }
        return proceed;
    }


    public void query(TableUpdateMeta tableUpdateMeta) {
        String querySql = tableUpdateMeta.getQuerySql();
        List<Map<String, Object>> beforeImage = tableUpdateMeta.getBeforeImage();
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(querySql);
        if(CollUtil.isEmpty(beforeImage)){
            tableUpdateMeta.setBeforeImage(resultList);
        }else {
            tableUpdateMeta.setAfterImage(resultList);
        }
    }

    public void query(List<TableUpdateMeta> tableUpdateMetas) {
        for (TableUpdateMeta tableUpdateMeta : tableUpdateMetas) {
            query(tableUpdateMeta);
        }
    }

    public void save(List<TableUpdateMeta> tableUpdateMetas) {
        for (TableUpdateMeta tableUpdateMeta : tableUpdateMetas) {
            jdbcTemplateWrite.update("insert into tb_update_history(before_image, after_image, update_columns, table_name, original_sql, query_sql) values(?,?,?,?,?,?)",
                    JSONUtil.toJsonStr(tableUpdateMeta.getBeforeImage()),
                    JSONUtil.toJsonStr(tableUpdateMeta.getAfterImage()),
                    JSONUtil.toJsonStr(tableUpdateMeta.getUpdateColumns()),
                    tableUpdateMeta.getTableName(),
                    tableUpdateMeta.getOriginalSql(),
                    tableUpdateMeta.getQuerySql());
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
