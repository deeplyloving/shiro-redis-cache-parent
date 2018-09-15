package com.spring.shiro.redis.cache;

import org.apache.shiro.cache.AbstractCacheManager;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.data.redis.core.RedisTemplate;

public class ShiroRedisCacheManager extends AbstractCacheManager{
	
	private RedisTemplate<Object, Object> redisTemplate;
	
	private long expire = 30*60;
	
	private String prefix = "shiro:";
	
	private String principalFiled = "id";
	

	public ShiroRedisCacheManager(RedisTemplate<Object, Object> redisTemplate, long expire, String prefix,
			String principalFiled) {
		super();
		this.redisTemplate = redisTemplate;
		this.expire = expire;
		if(prefix!=null&&prefix.length()>0)
			this.prefix = prefix;
		if(principalFiled!=null&&principalFiled.length()>0)
			this.principalFiled = principalFiled;
	}


	@Override
	protected Cache<Object,Object> createCache(String name) throws CacheException {
		return new ShiroRedisCache(name,redisTemplate,expire,prefix,principalFiled);
	}

}
