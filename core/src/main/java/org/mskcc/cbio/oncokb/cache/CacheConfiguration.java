package org.mskcc.cbio.oncokb.cache;

import org.mskcc.cbio.oncokb.util.PropertiesUtils;
import org.mskcc.oncokb.meta.enumeration.RedisType;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfiguration {
    private final int DEFAULT_TTL = 60;
    @Bean
    public RedissonClient redissonClient()
        throws Exception {
        Config config = new Config();
        String redisType = PropertiesUtils.getProperties("redis.type");
        String redisPassword = PropertiesUtils.getProperties("redis.password");
        String redisAddress = PropertiesUtils.getProperties("redis.address");

        if (redisType.equals(RedisType.SINGLE.getType())) {
            config
                .useSingleServer()
                .setAddress(redisAddress)
                .setConnectionMinimumIdleSize(1)
                .setConnectionPoolSize(2)
                .setPassword(redisPassword);
        } else if (redisType.equals(RedisType.SENTINEL.getType())) {
            config
                .useSentinelServers()
                .setMasterName("oncokb-master")
                .setCheckSentinelsList(false)
                .addSentinelAddress(redisAddress)
                .setPassword(redisPassword);
        } else {
            throw new Exception(
                "The redis type " +
                    redisType +
                    " is not supported. Only single and sentinel are supported."
            );
        }
        return Redisson.create(config);
    }

    @Bean
    public org.springframework.cache.CacheManager cacheManager(
        RedissonClient redissonClient,
        CacheNameResolver cacheNameResolver
    ) {
        Integer redisExpiration = Integer.parseInt(PropertiesUtils.getProperties("redis.expiration"));
        CacheManager cm = new CustomRedisCacheManager(redissonClient, redisExpiration == null ? DEFAULT_TTL : redisExpiration, cacheNameResolver);
        return cm;
    }

    @Bean
    public CacheResolver generalCacheResolver(
        CacheManager cm
    ) {
        return new GeneralCacheResolver(cm);
    }
}
