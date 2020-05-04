package me.chanjar.weixin.open.api.impl;

import me.chanjar.weixin.common.util.ConfigUtils;
import redis.clients.jedis.Jedis;
import redis.clients.util.Pool;

/**
 * @deprecated 未实现分布式锁，建议使用 <code>WxOpenInRedissonConfigStorage</code>
 * @author <a href="https://github.com/007gzs">007</a>
 */
@Deprecated
public class WxOpenInRedisConfigStorage extends AbstractWxOpenInRedisConfigStorage {

  protected final Pool<Jedis> jedisPool;

  public WxOpenInRedisConfigStorage(Pool<Jedis> jedisPool, String componentAppId, String componentAppSecret, String componentToken, String componentAesKey) {
    this(jedisPool, componentAppId, componentAppSecret, componentToken, componentAesKey, null);
  }

  public WxOpenInRedisConfigStorage(Pool<Jedis> jedisPool, String componentAppId, String componentAppSecret, String componentToken, String componentAesKey, String keyPrefix) {
    super(componentAppId, componentAppSecret, componentToken, componentAesKey, keyPrefix);
    this.jedisPool = jedisPool;
  }

  @Override
  public String getComponentVerifyTicket() {
    try (Jedis jedis = this.jedisPool.getResource()) {
      return jedis.get(this.componentVerifyTicketKey);
    }
  }

  @Override
  public void setComponentVerifyTicket(String componentVerifyTicket) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      jedis.set(this.componentVerifyTicketKey, componentVerifyTicket);
    }
  }

  @Override
  public String getComponentAccessToken() {
    try (Jedis jedis = this.jedisPool.getResource()) {
      return jedis.get(this.componentAccessTokenKey);
    }
  }

  @Override
  public boolean isComponentAccessTokenExpired() {
    try (Jedis jedis = this.jedisPool.getResource()) {
      return jedis.ttl(this.componentAccessTokenKey) < 2;
    }
  }

  @Override
  public void expireComponentAccessToken() {
    try (Jedis jedis = this.jedisPool.getResource()) {
      jedis.expire(this.componentAccessTokenKey, 0);
    }
  }

  @Override
  public void updateComponentAccessToken(String componentAccessToken, int expiresInSeconds) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      jedis.setex(this.componentAccessTokenKey, expiresInSeconds - 200, componentAccessToken);
    }
  }

  @Override
  public String getAuthorizerRefreshToken(String appId) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      return jedis.get(ConfigUtils.redisKey(this.authorizerRefreshTokenKeyPrefix, appId));
    }
  }

  @Override
  public void setAuthorizerRefreshToken(String appId, String authorizerRefreshToken) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      jedis.set(ConfigUtils.redisKey(this.authorizerRefreshTokenKeyPrefix, appId), authorizerRefreshToken);
    }
  }

  @Override
  public String getAuthorizerAccessToken(String appId) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      return jedis.get(ConfigUtils.redisKey(this.authorizerAccessTokenKeyPrefix, appId));
    }
  }

  @Override
  public boolean isAuthorizerAccessTokenExpired(String appId) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      return jedis.ttl(ConfigUtils.redisKey(this.authorizerAccessTokenKeyPrefix, appId)) < 2;
    }
  }

  @Override
  public void expireAuthorizerAccessToken(String appId) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      jedis.expire(ConfigUtils.redisKey(this.authorizerAccessTokenKeyPrefix, appId), 0);
    }
  }

  @Override
  public void updateAuthorizerAccessToken(String appId, String authorizerAccessToken, int expiresInSeconds) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      jedis.setex(ConfigUtils.redisKey(this.authorizerAccessTokenKeyPrefix, appId), expiresInSeconds - 200, authorizerAccessToken);
    }
  }

  @Override
  public String getJsapiTicket(String appId) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      return jedis.get(ConfigUtils.redisKey(this.jsapiTicketKeyPrefix, appId));
    }
  }

  @Override
  public boolean isJsapiTicketExpired(String appId) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      return jedis.ttl(ConfigUtils.redisKey(this.jsapiTicketKeyPrefix, appId)) < 2;
    }
  }

  @Override
  public void expireJsapiTicket(String appId) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      jedis.expire(ConfigUtils.redisKey(this.jsapiTicketKeyPrefix, appId), 0);
    }
  }

  @Override
  public void updateJsapiTicket(String appId, String jsapiTicket, int expiresInSeconds) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      jedis.setex(ConfigUtils.redisKey(this.jsapiTicketKeyPrefix, appId), expiresInSeconds - 200, jsapiTicket);
    }
  }

  @Override
  public String getCardApiTicket(String appId) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      return jedis.get(ConfigUtils.redisKey(this.cardApiTicketKeyPrefix, appId));
    }
  }

  @Override
  public boolean isCardApiTicketExpired(String appId) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      return jedis.ttl(ConfigUtils.redisKey(this.cardApiTicketKeyPrefix, appId)) < 2;
    }
  }

  @Override
  public void expireCardApiTicket(String appId) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      jedis.expire(ConfigUtils.redisKey(this.cardApiTicketKeyPrefix, appId), 0);
    }
  }

  @Override
  public void updateCardApiTicket(String appId, String cardApiTicket, int expiresInSeconds) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      jedis.setex(ConfigUtils.redisKey(this.cardApiTicketKeyPrefix, appId), expiresInSeconds - 200, cardApiTicket);
    }
  }
}
