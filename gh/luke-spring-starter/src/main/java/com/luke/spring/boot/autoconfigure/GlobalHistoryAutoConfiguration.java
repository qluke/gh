package com.luke.spring.boot.autoconfigure;

import com.luke.spring.boot.autoconfigure.config.GlobalHistoryProperties;
import com.luke.spring.boot.autoconfigure.interceptor.MybatisInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import static com.luke.spring.boot.autoconfigure.StarterConstants.LUKE_PREFIX;

@Configuration
@ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class })
@ConditionalOnProperty(prefix = LUKE_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class GlobalHistoryAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalHistoryAutoConfiguration.class);

    @Bean
    public MybatisInterceptor mybatisInterceptor(JdbcTemplate jdbcTemplate, @Qualifier("jdbcTemplateWrite") JdbcTemplate jdbcTemplateWrite,
                                                 GlobalHistoryProperties globalHistoryProperties) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Automatically configure globalHistory");
        }
        if(globalHistoryProperties.isUseDefaultDataSource()){
            return new MybatisInterceptor(jdbcTemplate, jdbcTemplateWrite);
        }
        return new MybatisInterceptor(jdbcTemplate);
    }
}
