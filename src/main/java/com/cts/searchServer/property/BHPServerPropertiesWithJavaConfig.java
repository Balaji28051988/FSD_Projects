package com.cts.searchServer.property;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:BHPServers.properties")
public class BHPServerPropertiesWithJavaConfig {

	public BHPServerPropertiesWithJavaConfig() {
		super();
		
	}

	
}
