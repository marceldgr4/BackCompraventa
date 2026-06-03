package com.CompraVenta.Backend.Config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;


import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> RT = new RedisTemplate<>();
        RT.setConnectionFactory(redisConnectionFactory);
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer(
                buildObjectMapper()
        );
        RT.setKeySerializer(keySerializer);
        RT.setHashKeySerializer(keySerializer);
        RT.setValueSerializer(valueSerializer);
        RT.setHashValueSerializer(valueSerializer);
        RT.afterPropertiesSet();
        return RT;
    }
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(buildObjectMapper())))
                .disableCachingNullValues();
        Map<String, RedisCacheConfiguration> cacheConfigs = Map.of(
                "sessions",   config.entryTtl(Duration.ofHours(1)),
                "articles",   config.entryTtl(Duration.ofMinutes(5)),
                "employees",  config.entryTtl(Duration.ofMinutes(30)),
                "clientes",   config.entryTtl(Duration.ofMinutes(5)),
                "dashboard",  config.entryTtl(Duration.ofMinutes(2))
        );
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .withInitialCacheConfigurations(cacheConfigs).build();
    }

    public ObjectMapper buildObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return mapper;
    }
}
