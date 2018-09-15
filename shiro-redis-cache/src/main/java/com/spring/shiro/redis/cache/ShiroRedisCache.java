package com.spring.shiro.redis.cache;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.springframework.data.redis.core.RedisTemplate;

public class ShiroRedisCache implements Cache<Object, Object>{
	
	private String name;
	
	private RedisTemplate<Object, Object> redisTemplate;
	
	private long expire = 30*60;
	
	private String prefix = null;
	
	private String principalFiled = null;
	
	
	public ShiroRedisCache(String name, RedisTemplate<Object, Object> redisTemplate, long expire, String prefix,
			String principalFiled) {
		super();
		this.name = name;
		this.redisTemplate = redisTemplate;
		this.expire = expire;
		this.prefix = prefix;
		this.principalFiled = principalFiled;
	}







	public long getExpire() {
		return expire;
	}

	public void setExpire(long expire) {
		this.expire = expire;
	}

	public Object get(Object key) throws CacheException {
		return redisTemplate.opsForValue().get(createKey(key));
	}

	public Object put(Object key, Object value) throws CacheException {
		redisTemplate.opsForValue().set(createKey(key), value,getExpire(),TimeUnit.SECONDS);
		return value;
	}

	public Object remove(Object key) throws CacheException {
		redisTemplate.delete(createKey(key));
		return null;
	}

	public void clear() throws CacheException {
		redisTemplate.delete(keys());
	}

	public int size() {
		return keys().size();
	}

	public Set<Object> keys() {
		return redisTemplate.keys(prefix+"*");
	}

	public Collection<Object> values() {
		return redisTemplate.opsForValue().multiGet(keys());
	}

	
	private String createKey(Object key) {
		if(key instanceof SimplePrincipalCollection) {
			Object principalObject = ((PrincipalCollection)key).getPrimaryPrincipal();
			try {
				Field filed = principalObject.getClass().getField(principalFiled);
				filed.setAccessible(true);
				key = filed.get(principalObject);
			} catch (NoSuchFieldException e) {
				throw new ShiroRedisException("凭证取值唯一键",e);
			} catch (SecurityException e) {
				throw new ShiroRedisException("凭证取值唯一键",e);
			} catch (IllegalArgumentException e) {
				throw new ShiroRedisException("凭证取值唯一键",e);
			} catch (IllegalAccessException e) {
				throw new ShiroRedisException("凭证取值唯一键",e);
			}
		}
		return prefix+name+":"+key;
	}
	
}
