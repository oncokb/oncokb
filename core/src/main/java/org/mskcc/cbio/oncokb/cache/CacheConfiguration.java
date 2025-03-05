package org.mskcc.cbio.oncokb.cache;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.cache.keygenerator.ConcatGenerator;
import org.mskcc.cbio.oncokb.model.OncoKBInfo;
import org.mskcc.cbio.oncokb.util.CacheUtils;
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
import org.springframework.cache.interceptor.CacheErrorHandler;
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
        String redisSlaveConnectionMinimumIdleSize = PropertiesUtils.getProperties("redis.slaveConnectionMinimumIdleSize");
        String redisSlaveConnectionPoolSize = PropertiesUtils.getProperties("redis.slaveConnectionPoolSize");
        String redisMasterConnectionMinimumIdleSize = PropertiesUtils.getProperties("redis.masterConnectionMinimumIdleSize");
        String redisMasterConnectionPoolSize = PropertiesUtils.getProperties("redis.masterConnectionPoolSize");

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
            setRedisConnectionPoolSize(sentinelConfig, redisSlaveConnectionMinimumIdleSize, redisSlaveConnectionPoolSize, redisMasterConnectionMinimumIdleSize, redisMasterConnectionPoolSize);
            setRedisClientName(sentinelConfig);
        } else if (redisType.equals(RedisType.CLUSTER.getType())) {
            ClusterServersConfig clusterConfig = 
                config
                    .useClusterServers()
                    .addNodeAddress(redisAddress)
                    .setPassword(redisPassword);
            setRedisConnectionPoolSize(clusterConfig, redisSlaveConnectionMinimumIdleSize, redisSlaveConnectionPoolSize, redisMasterConnectionMinimumIdleSize, redisMasterConnectionPoolSize);
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

    private void setRedisConnectionPoolSize(BaseMasterSlaveServersConfig baseMasterSlaveServersConfig, String redisSlaveConnectionMinimumIdleSize, String redisSlaveConnectionPoolSize, String redisMasterConnectionMinimumIdleSize, String redisMasterConnectionPoolSize) {
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
        String appNameProperty = PropertiesUtils.getProperties("app.name");

        StringBuilder appName = new StringBuilder();
        appName.append(StringUtils.isNotEmpty(appNameProperty) ? appNameProperty : "oncokb-core");

        OncoKBInfo oncoKBInfo = new OncoKBInfo();
        appName.append("-app:");
        appName.append(oncoKBInfo.getAppVersion().getVersion());
        appName.append("-data:");
        appName.append(oncoKBInfo.getDataVersion().getVersion());

        // Use a versioned cache key prefix to prevent conflicts during deployments.
        // In Kubernetes, old pods may still process requests and write stale data to Redis 
        // while new pods are starting up. By including the software and data version in 
        // the cache key, we ensure that each deployment is fetching the correct values from Redis.
        baseConfig.setClientName(appName.toString());
    }

    @Bean
    public CacheErrorHandler cacheErrorHandler() {
        return new LoggingCacheErrorHandler();
    }

    @Bean
    public org.springframework.cache.CacheManager cacheManager(
        RedissonClient redissonClient,
        CacheNameResolver cacheNameResolver,
        CacheErrorHandler cacheErrorHandler
    ) {
        Integer redisExpiration = Integer.parseInt(PropertiesUtils.getProperties("redis.expiration"));
        CustomRedisCacheManager cm = new CustomRedisCacheManager(redissonClient, redisExpiration == null ? DEFAULT_TTL : redisExpiration, cacheNameResolver, cacheErrorHandler);
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
