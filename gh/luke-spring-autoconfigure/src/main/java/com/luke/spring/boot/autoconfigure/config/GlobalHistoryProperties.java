package com.luke.spring.boot.autoconfigure.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import static com.luke.spring.boot.autoconfigure.StarterConstants.LUKE_PREFIX;

@Data
@Component
@ConfigurationProperties(prefix = LUKE_PREFIX)
public class GlobalHistoryProperties {
    /**
     * whether enable auto configuration
     */
    private boolean enabled = true;

    /**
     * Whether use default datasource bean
     */
    private boolean useDefaultDataSource= true;
    /**
     * The scan packages. If empty, will scan all beans.
     */
    private String[] scanPackages = {};


}
