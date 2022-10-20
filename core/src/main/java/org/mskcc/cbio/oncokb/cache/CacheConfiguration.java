package org.mskcc.cbio.oncokb.cache;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.cache.keygenerator.ConcatGenerator;
import org.mskcc.cbio.oncokb.util.PropertiesUtils;
import org.mskcc.oncokb.meta.enumeration.RedisType;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.SnappyCodecV2;
import org.redisson.config.BaseConfig;
import org.redisson.config.BaseMasterSlaveServersConfig;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;
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
    private String redisSlaveConnectionMinimumIdleSize;
    private String redisSlaveConnectionPoolSize;
    private String redisMasterConnectionMinimumIdleSize;
    private String redisMasterConnectionPoolSize;
    private String redisClientName;

    @Bean
    public RedissonClient redissonClient()
        throws Exception {
        Config config = new Config();
        String redisType = PropertiesUtils.getProperties("redis.type");
        String redisPassword = PropertiesUtils.getProperties("redis.password");
        String redisAddress = PropertiesUtils.getProperties("redis.address");
        String redisMasterName = PropertiesUtils.getProperties("redis.masterName");
        redisSlaveConnectionMinimumIdleSize = PropertiesUtils.getProperties("redis.slaveConnectionMinimumIdleSize");
        redisSlaveConnectionPoolSize = PropertiesUtils.getProperties("redis.slaveConnectionPoolSize");
        redisMasterConnectionMinimumIdleSize = PropertiesUtils.getProperties("redis.masterConnectionMinimumIdleSize");
        redisMasterConnectionPoolSize = PropertiesUtils.getProperties("redis.masterConnectionPoolSize");
        redisClientName = PropertiesUtils.getProperties("redis.clientName");

        if (redisType.equals(RedisType.SINGLE.getType())) {
            SingleServerConfig singleServerConfig = config
                .useSingleServer()
                .setAddress(redisAddress)
                .setConnectionMinimumIdleSize(1)
                .setConnectionPoolSize(2)
                .setSubscriptionConnectionMinimumIdleSize(0)
                .setSubscriptionConnectionPoolSize(0)
                .setDnsMonitoringInterval(-1)
                .setPassword(redisPassword);
            setRedisClientName(singleServerConfig);
        } else if (redisType.equals(RedisType.SENTINEL.getType())) {
            SentinelServersConfig sentinelConfig = config
                .useSentinelServers()
                .setMasterName(redisMasterName)
                .setCheckSentinelsList(false)
                .setDnsMonitoringInterval(-1)
                .addSentinelAddress(redisAddress)
                .setPassword(redisPassword);
            setRedisConnectionPoolSize(sentinelConfig);
            setRedisClientName(sentinelConfig);
        } else if (redisType.equals(RedisType.CLUSTER.getType())) {
            ClusterServersConfig clusterConfig = 
                config
                    .useClusterServers()
                    .addNodeAddress(redisAddress)
                    .setPassword(redisPassword);
            setRedisConnectionPoolSize(clusterConfig);
            setRedisClientName(clusterConfig);
        } else {
            throw new Exception(
                "The redis type " +
                    redisType +
                    " is not supported. Only single, sentinel, and cluster are supported."
            );
        }

        // Instead of using GZip to compress data manually, we can configure Redisson to use
        // snappy codec. Redisson will serialize and compress our cache values.
        config.setCodec(new SnappyCodecV2());
        return Redisson.create(config);
    }

    private void setRedisConnectionPoolSize(BaseMasterSlaveServersConfig baseMasterSlaveServersConfig) {
        baseMasterSlaveServersConfig.setSubscriptionConnectionMinimumIdleSize(0);
        baseMasterSlaveServersConfig.setSubscriptionConnectionPoolSize(0);

        if (StringUtils.isNotEmpty(redisSlaveConnectionMinimumIdleSize)) {
            baseMasterSlaveServersConfig.setSlaveConnectionMinimumIdleSize(Integer.parseInt(redisSlaveConnectionMinimumIdleSize));
        }
        if (StringUtils.isNotEmpty(redisSlaveConnectionPoolSize)) {
            baseMasterSlaveServersConfig.setSlaveConnectionPoolSize(Integer.parseInt(redisSlaveConnectionPoolSize));
        }
        if (StringUtils.isNotEmpty(redisMasterConnectionMinimumIdleSize)) {
            baseMasterSlaveServersConfig.setMasterConnectionMinimumIdleSize(Integer.parseInt(redisMasterConnectionMinimumIdleSize));
        }
        if (StringUtils.isNotEmpty(redisMasterConnectionPoolSize)) {
            baseMasterSlaveServersConfig.setMasterConnectionPoolSize(Integer.parseInt(redisMasterConnectionPoolSize));
        }
    }

    private void setRedisClientName(BaseConfig baseConfig) {
        if (StringUtils.isNotEmpty(redisClientName)) {
            baseConfig.setClientName(redisClientName);
        } else {
            baseConfig.setClientName("oncokb-core-client");
        }
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
