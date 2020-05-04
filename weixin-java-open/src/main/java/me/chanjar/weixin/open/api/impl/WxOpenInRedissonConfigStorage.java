package me.chanjar.weixin.open.api.impl;

import cn.binarywang.wx.miniapp.config.WxMaConfig;
import me.chanjar.weixin.common.util.ConfigUtils;
import me.chanjar.weixin.mp.config.WxMpConfigStorage;
import me.chanjar.weixin.mp.enums.TicketType;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * @author yangyidian
 * @date 2020/01/06
 **/
public class WxOpenInRedissonConfigStorage extends AbstractWxOpenInRedisConfigStorage {

  private final RedissonClient redissonClient;
  private final RBucket<String> componentVerifyTicketBucket;
  private final RBucket<String> componentAccessTokenBucket;

  private final Lock componentAccessTokenLock;

  public WxOpenInRedissonConfigStorage(String componentAppId, String componentAppSecret, String componentToken, String componentAesKey, RedissonClient redissonClient) {
    this(componentAppId, componentAppSecret, componentToken, componentAesKey, redissonClient, null);
  }

  public WxOpenInRedissonConfigStorage(String componentAppId, String componentAppSecret, String componentToken, String componentAesKey, RedissonClient redissonClient, String redisKeyPrefix) {
    super(componentAppId, componentAppSecret, componentToken, componentAesKey, redisKeyPrefix);
    this.redissonClient = redissonClient;
    componentVerifyTicketBucket = redissonClient.getBucket(componentVerifyTicketKey);
    componentAccessTokenBucket = redissonClient.getBucket(componentAccessTokenKey);
    componentAccessTokenLock = redissonClient.getLock(componentAccessTokenLockKey);
  }

  @Override
  public String getComponentVerifyTicket() {
    return componentVerifyTicketBucket.get();
  }

  @Override
  public void setComponentVerifyTicket(String componentVerifyTicket) {
    componentVerifyTicketBucket.set(componentVerifyTicket);
  }

  @Override
  public String getComponentAccessToken() {
    return componentAccessTokenBucket.get();
  }

  @Override
  public boolean isComponentAccessTokenExpired() {
    return ConfigUtils.isTTLExpired(componentAccessTokenBucket.remainTimeToLive());
  }

  @Override
  public void expireComponentAccessToken() {
    componentAccessTokenBucket.delete();
  }

  @Override
  public void updateComponentAccessToken(String componentAccessToken, int expiresInSeconds) {
    componentAccessTokenBucket.set(componentAccessToken, ConfigUtils.expiresTimestamp(expiresInSeconds), TimeUnit.MILLISECONDS);
  }

  @Override
  public Lock getComponentAccessTokenLock() {
    return componentAccessTokenLock;
  }

  @Override
  public String getAuthorizerRefreshToken(String appId) {
    return getBucket(authorizerRefreshTokenKeyPrefix, appId).get();
  }

  @Override
  public void setAuthorizerRefreshToken(String appId, String authorizerRefreshToken) {
    getBucket(authorizerRefreshTokenKeyPrefix, appId).set(authorizerRefreshToken);
  }

  @Override
  public String getAuthorizerAccessToken(String appId) {
    return getBucket(authorizerAccessTokenKeyPrefix, appId).get();
  }

  @Override
  public boolean isAuthorizerAccessTokenExpired(String appId) {
    return ConfigUtils.isTTLExpired(getBucket(authorizerAccessTokenKeyPrefix, appId).remainTimeToLive());
  }

  @Override
  public void expireAuthorizerAccessToken(String appId) {
    getBucket(authorizerAccessTokenKeyPrefix, appId).delete();
  }

  @Override
  public void updateAuthorizerAccessToken(String appId, String authorizerAccessToken, int expiresInSeconds) {
    getBucket(authorizerAccessTokenKeyPrefix, appId).set(authorizerAccessToken, ConfigUtils.expiresTimestamp(expiresInSeconds), TimeUnit.MILLISECONDS);
  }

  @Override
  public String getJsapiTicket(String appId) {
    return getBucket(jsapiTicketKeyPrefix, appId).get();
  }

  @Override
  public boolean isJsapiTicketExpired(String appId) {
    return ConfigUtils.isTTLExpired(getBucket(jsapiTicketKeyPrefix, appId).remainTimeToLive());
  }

  @Override
  public void expireJsapiTicket(String appId) {
    getBucket(jsapiTicketKeyPrefix, appId).delete();
  }

  @Override
  public void updateJsapiTicket(String appId, String jsapiTicket, int expiresInSeconds) {
    getBucket(jsapiTicketKeyPrefix, appId).set(jsapiTicket, ConfigUtils.expiresTimestamp(expiresInSeconds), TimeUnit.MILLISECONDS);
  }

  @Override
  public String getCardApiTicket(String appId) {
    return getBucket(cardApiTicketKeyPrefix, appId).get();
  }

  @Override
  public boolean isCardApiTicketExpired(String appId) {
    return ConfigUtils.isTTLExpired(getBucket(cardApiTicketKeyPrefix, appId).remainTimeToLive());
  }

  @Override
  public void expireCardApiTicket(String appId) {
    getBucket(cardApiTicketKeyPrefix, appId).delete();
  }

  @Override
  public void updateCardApiTicket(String appId, String cardApiTicket, int expiresInSeconds) {
    getBucket(cardApiTicketKeyPrefix, appId).set(cardApiTicket, ConfigUtils.expiresTimestamp(expiresInSeconds), TimeUnit.MILLISECONDS);
  }

  @Override
  public WxMpConfigStorage getWxMpConfigStorage(String appId) {
    return new WxOpenRedissonInnerConfig(this, appId);
  }

  @Override
  public WxMaConfig getWxMaConfig(String appId) {
    return new WxOpenRedissonInnerConfig(this, appId);
  }

  private RBucket<String> getBucket(String key, String appId) {
    return redissonClient.getBucket(ConfigUtils.redisKey(key, appId));
  }


  static class WxOpenRedissonInnerConfig extends WxOpenInnerConfigStorage {

    private final RedissonClient redissonClient;

    private final static String AUTHORIZER_REFRESH_TOKEN_LOCK_KEY = "wechat_authorizer_access_token_lock";
    private final static String JSAPI_TICKET_LOCK_KEY = "wechat_jsapi_ticket_lock";
    private final static String CARD_API_TICKET_LOCK_KEY = "wechat_card_api_ticket_lock";

    private final String authorizerAccessTokenLockKey;
    private final String jsapiTicketLockKey;
    private final String cardApiTicketLockKey;

    public WxOpenRedissonInnerConfig(WxOpenInRedissonConfigStorage config, String appId) {
      super(config, appId);
      redissonClient = config.redissonClient;

      authorizerAccessTokenLockKey = ConfigUtils.redisKey(config.redisKeyPrefix, AUTHORIZER_REFRESH_TOKEN_LOCK_KEY, appId);
      jsapiTicketLockKey = ConfigUtils.redisKey(config.redisKeyPrefix, JSAPI_TICKET_LOCK_KEY, appId);
      cardApiTicketLockKey = ConfigUtils.redisKey(config.redisKeyPrefix, CARD_API_TICKET_LOCK_KEY, appId);
    }

    @Override
    public Lock getAccessTokenLock() {
      return redissonClient.getLock(authorizerAccessTokenLockKey);
    }

    @Override
    public Lock getTicketLock(TicketType type) {
      return redissonClient.getLock(jsapiTicketLockKey);
    }

    @Override
    public Lock getJsapiTicketLock() {
      return redissonClient.getLock(jsapiTicketLockKey);
    }

    @Override
    public Lock getCardApiTicketLock() {
      return redissonClient.getLock(cardApiTicketLockKey);
    }

  }
}
