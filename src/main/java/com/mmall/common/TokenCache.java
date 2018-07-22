package com.mmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by mjl on 18-7-22.
 */
public class TokenCache {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(TokenCache.class);
    public static final String TOKEN_PREFIX = "token_";
    //LRU算法
    private static LoadingCache<String, String> localcache = CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(10000)
            .expireAfterAccess(12, TimeUnit.HOURS).build(
                    new CacheLoader<String, String>() {
                        //默认数据加载，当调用get方法取值时，若key没有对应的值，就使用这个方法进行加载
                        @Override
                        public String load(String s) throws Exception {
                            return "null";
                        }
                    }
            );

    public static void setKey(String key, String value)
    {
        localcache.put(key, value);
    }

    public static String getKey(String key)
    {
        String value = null;
        try {
            value =  localcache.get(key);
            if("null".equals(value))
            {
                return null;
            }
            return value;
        } catch (ExecutionException e) {
            logger.error("localCache get error", e);
        }
        return null;
    }
}
