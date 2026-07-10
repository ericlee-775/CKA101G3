package com.farmily;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//加入spring快取，跨交易整個伺服器
@Configuration
public class CacheConfig {
	@Bean
	public CacheManager cacheManager() {
		return new ConcurrentMapCacheManager("categories");
	}

}
