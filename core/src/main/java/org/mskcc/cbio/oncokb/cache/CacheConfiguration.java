package org.mskcc.cbio.oncokb.cache;

import org.mskcc.cbio.oncokb.cache.keygenerator.ConcatGenerator;
import org.mskcc.cbio.oncokb.util.PropertiesUtils;
import org.mskcc.oncokb.meta.enumeration.RedisType;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.SnappyCodecV2;
import org.redisson.config.Config;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.*;

@Configuration
@EnableCaching
@Conditional(EnableCacheCondition.class)
public class CacheConfiguration {
    private final int DEFAULT_TTL = 60;
    @Bean
    public RedissonClient redissonClient()
        throws Exception {
        Config config = new Config();
        String redisType = PropertiesUtils.getProperties("redis.type");
        String redisPassword = PropertiesUtils.getProperties("redis.password");
        String redisAddress = PropertiesUtils.getProperties("redis.address");
        String redisMasterName = PropertiesUtils.getProperties("redis.masterName");

        if (redisType.equals(RedisType.SINGLE.getType())) {
            config
                .useSingleServer()
                .setAddress(redisAddress)
                .setConnectionMinimumIdleSize(1)
                .setConnectionPoolSize(2)
                .setDnsMonitoringInterval(-1)
                .setPassword(redisPassword);
        } else if (redisType.equals(RedisType.SENTINEL.getType())) {
            config
                .useSentinelServers()
                .setMasterName(redisMasterName)
                .setCheckSentinelsList(false)
                .setDnsMonitoringInterval(-1)
                .addSentinelAddress(redisAddress)
                .setPassword(redisPassword);
        } else if (redisType.equals(RedisType.CLUSTER.getType())) {
            config
                .useClusterServers()
                .addNodeAddress(redisAddress)
                .setPassword(redisPassword);
        } else {
            throw new Exception(
                "The redis type " +
                    redisType +
                    " is not supported. Only single, sentinel, and cluster are supported."
            );
        }
        // Instead of using GZip to compress data manually, we can use configure Redisson to use
        // snappy codec. Redisson will serialize and compress our cache values.
        config.setCodec(new SnappyCodecV2());
        return Redisson.create(config);
    }

    @Bean
    public org.springframework.cache.CacheManager cacheManager(
        RedissonClient redissonClient,
        CacheNameResolver cacheNameResolver
    ) {
        Integer redisExpiration = Integer.parseInt(PropertiesUtils.getProperties("redis.expiration"));
        CustomRedisCacheManager cm = new CustomRedisCacheManager(redissonClient, redisExpiration == null ? DEFAULT_TTL : redisExpiration, cacheNameResolver);
        cm.clearAll();
        return cm;
    }

    @Bean
    public CacheResolver generalCacheResolver(
        CacheManager cm
    ) {
        return new GeneralCacheResolver(cm);
    }

    @Bean
    public KeyGenerator concatKeyGenerator(){
        return new ConcatGenerator();
    }
}
