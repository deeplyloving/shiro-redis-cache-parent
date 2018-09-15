package com.spring.boot.starter.shiro;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.spring.shiro.redis.cache.ShiroRedisCacheManager;
import com.spring.shiro.redis.cache.ShiroRedisSessionDao;

@Configuration
@ConfigurationProperties(prefix = "spring.shiro")
public class ShiroAutoConfiguration {

	private long globalSessionTimeout = 30 * 60;

	private int cookieMaxAge = -1;
	private String cookiePath = null;
	
	private String sessionCacheName=null;
	private String authenticationCacheName=null;
	
	private String prefix = null;
	private String principalFiled = null;

	public long getGlobalSessionTimeout() {
		return globalSessionTimeout;
	}

	public void setGlobalSessionTimeout(long globalSessionTimeout) {
		this.globalSessionTimeout = globalSessionTimeout;
	}

	public int getCookieMaxAge() {
		return cookieMaxAge;
	}

	public void setCookieMaxAge(int cookieMaxAge) {
		this.cookieMaxAge = cookieMaxAge;
	}

	public String getCookiePath() {
		return cookiePath;
	}

	public void setCookiePath(String cookiePath) {
		this.cookiePath = cookiePath;
	}

	private Map<String, String> filterResource = new LinkedHashMap<String, String>();

	private String loginUrl = "/login";
	private String successUrl = "/index";

	public Map<String, String> getFilterResource() {
		return filterResource;
	}

	public void setFilterResource(Map<String, String> filterResource) {
		this.filterResource = filterResource;
	}

	public String getLoginUrl() {
		return loginUrl;
	}

	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}

	public String getSuccessUrl() {
		return successUrl;
	}

	public void setSuccessUrl(String successUrl) {
		this.successUrl = successUrl;
	}

	@Bean
	public ShiroFilterFactoryBean shiroFilter(org.apache.shiro.mgt.SecurityManager securityManager) {
		ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
		shiroFilterFactoryBean.setSecurityManager(securityManager);
		shiroFilterFactoryBean.setFilterChainDefinitionMap(filterResource);
		// 如果不设置默认会自动寻找Web工程根目录下的"/login.jsp"页面
		shiroFilterFactoryBean.setLoginUrl(loginUrl);
		// 登录成功后要跳转的链接
		shiroFilterFactoryBean.setSuccessUrl(successUrl);
		return shiroFilterFactoryBean;
	}


	@Bean(name = "shiroRedisTemplate")
	@ConditionalOnMissingBean
	public RedisTemplate<Object, Object> createRedisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<Object, Object>();
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setConnectionFactory(connectionFactory);
		return redisTemplate;
	}

	@Bean
	@ConditionalOnMissingBean
	public ShiroRedisCacheManager createShiroRedisCacheManager(
			@Qualifier("shiroRedisTemplate") RedisTemplate<Object, Object> redisTemplate) {
		ShiroRedisCacheManager shiroRedisCacheManager = new ShiroRedisCacheManager(redisTemplate, globalSessionTimeout,prefix,principalFiled);
		return shiroRedisCacheManager;
	}

	@Bean
	@ConditionalOnMissingBean
	public ShiroRedisSessionDao createRedisSessionDAO(ShiroRedisCacheManager shiroRedisCacheManager) {
		ShiroRedisSessionDao redisSessionDAO = new ShiroRedisSessionDao();
		redisSessionDAO.setCacheManager(shiroRedisCacheManager);
		if(sessionCacheName!=null && sessionCacheName.length()>0)
			redisSessionDAO.setActiveSessionsCacheName(sessionCacheName);
		return redisSessionDAO;
	}

	@Bean
	@ConditionalOnMissingBean
	public CredentialsMatcher myCredentialsMatcher() {
		HashedCredentialsMatcher credentialsMatcher = new HashedCredentialsMatcher();
		credentialsMatcher.setHashAlgorithmName("MD5");// MD5加密
		credentialsMatcher.setHashIterations(2);// 加密两次
		return credentialsMatcher;
	}

	@Bean
	@ConditionalOnMissingBean
	public SessionManager createDefaultWebSessionManager(SessionDAO sessionDAO) {
		DefaultWebSessionManager sessionManager = new CustomDefaultWebSessionManager();
		sessionManager.setSessionDAO(sessionDAO);
		sessionManager.setGlobalSessionTimeout(globalSessionTimeout * 1000);

		Cookie sessionIdCookie = sessionManager.getSessionIdCookie();
		sessionIdCookie.setMaxAge(cookieMaxAge);
		if (cookiePath != null)
			sessionIdCookie.setPath(cookiePath);
		sessionManager.setSessionIdCookie(sessionIdCookie);
		sessionManager.setDeleteInvalidSessions(true);
		return sessionManager;
	}
	
	@Bean
	@ConditionalOnMissingBean
	public org.apache.shiro.mgt.SecurityManager securityManager(AuthorizingRealm realm, CacheManager cacheManager,
			SessionManager sessionManager) {
		DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
		securityManager.setRealm(realm);

		securityManager.setCacheManager(cacheManager);
		securityManager.setSessionManager(sessionManager);
		if(authenticationCacheName!=null && authenticationCacheName.length()>0)
			realm.setAuthenticationCacheName(authenticationCacheName);
		
		return securityManager;
	}

	/**
	 * 开启注解
	 * 
	 * @param securityManager
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean
	public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(
			org.apache.shiro.mgt.SecurityManager securityManager) {
		AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
		authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
		return authorizationAttributeSourceAdvisor;
	}

	public static class CustomDefaultWebSessionManager extends DefaultWebSessionManager {
		@Override
		protected Session retrieveSession(SessionKey sessionKey) throws UnknownSessionException {
			Serializable sessionId = getSessionId(sessionKey);
			if (sessionId == null) {
				return super.retrieveSession(sessionKey);
			}
			ServletRequest request = WebUtils.getRequest(sessionKey);
			Object sessionObj = request.getAttribute(sessionId.toString());
			if (sessionObj != null) {
				return (Session) sessionObj;
			}
			Session s = super.retrieveSession(sessionKey);
			if (request != null && null != sessionId) {
				request.setAttribute(sessionId.toString(), s);
			}
			return s;
		}
	}
}
