package com.spring.shiro.redis.cache;

import java.io.Serializable;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.CachingSessionDAO;

public class ShiroRedisSessionDao extends CachingSessionDAO{

	
	private ShiroRedisCacheManager shiroRedisCacheManager = null;
	
	public ShiroRedisCacheManager getShiroRedisCacheManager() {
		return shiroRedisCacheManager;
	}

	public void setShiroRedisCacheManager(ShiroRedisCacheManager shiroRedisCacheManager) {
		this.shiroRedisCacheManager = shiroRedisCacheManager;
		this.setCacheManager(shiroRedisCacheManager);
	}


	@Override
	protected void doUpdate(Session session) {
		getCacheManager().getCache(getActiveSessionsCacheName()).put(session.getId(), session);
	}

	@Override
	protected void doDelete(Session session) {
		getCacheManager().getCache(getActiveSessionsCacheName()).remove(session.getId());
	}

	@Override
	protected Serializable doCreate(Session session) {
		Serializable sessionId = this.generateSessionId(session);  
        this.assignSessionId(session, sessionId);
        getCacheManager().getCache(getActiveSessionsCacheName()).put(session.getId(), session);
		return sessionId;
	}

	@Override
	protected Session doReadSession(Serializable sessionId) {
		return (Session) getCacheManager().getCache(getActiveSessionsCacheName()).get(sessionId);
	}

}
