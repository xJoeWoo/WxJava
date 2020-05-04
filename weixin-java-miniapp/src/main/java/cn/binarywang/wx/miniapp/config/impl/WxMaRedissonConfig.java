package cn.binarywang.wx.miniapp.config.impl;

import me.chanjar.weixin.common.util.ConfigUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.concurrent.locks.Lock;

public class WxMaRedissonConfig extends WxMaDefaultConfigImpl {

  public static final String DEFAULT_REDIS_KEY_PREFIX = "maConfig";
  private static final String ACCESS_TOKEN_KEY = "accessToken";
  private static final String JSAPI_TICKET_KEY = "jsapiTicket";
  private static final String CARD_API_TICKET_KEY = "cardApiTicket";

  private static final String ACCESS_TOKEN_LOCK_KEY = "accessTokenLock";
  private static final String JSAPI_TICKET_LOCK_KEY = "jsapiTicketLock";
  private static final String CARD_API_TICKET_LOCK_KEY = "cardApiTicketLock";

  private static final String HASH_VALUE_FIELD = "value";
  private static final String HASH_EXPIRE_FIELD = "expire";

  private final Lock accessTokenLock;
  private final Lock jsapiTicketLock;
  private final Lock cardApiTicketLock;

  private final RMap<String, String> accessTokenRMap;
  private final RMap<String, String> jsApiRMap;
  private final RMap<String, String> cardApiRMap;

  public WxMaRedissonConfig(RedissonClient redissonClient) {
    this(null, redissonClient);
  }

  /**
   * @param appId          可空。设置微信小程序的 Id，可以自定义也可直接使用微信提供的 Id，用于拼接 Redis Hash Key。为多个小程序提供服务时必须设置，避免配置冲突。
   * @param redissonClient Redisson Client
   */
  public WxMaRedissonConfig(String appId, RedissonClient redissonClient) {
    this(appId, redissonClient, null);
  }

  /**
   * @param appId          设置微信小程序的 Id，可以自定义也可直接使用微信提供的 Id，用于拼接 Redis Hash Key。为多个小程序提供服务时必须设置，避免配置冲突。
   * @param redissonClient Redisson Client
   * @param redisKeyPrefix 用于在拼接 Redis Hash Key 时作为前缀，默认为 <code>maConfig</code>
   */
  public WxMaRedissonConfig(String appId, RedissonClient redissonClient, String redisKeyPrefix) {
    if (StringUtils.isBlank(redisKeyPrefix)) {
      redisKeyPrefix = DEFAULT_REDIS_KEY_PREFIX;
    }
    accessTokenLock = redissonClient.getLock(ConfigUtils.redisKey(redisKeyPrefix, appId, ACCESS_TOKEN_LOCK_KEY));
    jsapiTicketLock = redissonClient.getLock(ConfigUtils.redisKey(redisKeyPrefix, appId, JSAPI_TICKET_LOCK_KEY));
    cardApiTicketLock = redissonClient.getLock(ConfigUtils.redisKey(redisKeyPrefix, appId, CARD_API_TICKET_LOCK_KEY));
    accessTokenRMap = redissonClient.getMap(ConfigUtils.redisKey(redisKeyPrefix, appId, ACCESS_TOKEN_KEY));
    jsApiRMap = redissonClient.getMap(ConfigUtils.redisKey(redisKeyPrefix, appId, JSAPI_TICKET_KEY));
    cardApiRMap = redissonClient.getMap(ConfigUtils.redisKey(redisKeyPrefix, appId, CARD_API_TICKET_KEY));
  }


  private String getValue(RMap<String, String> rMap) {
    return rMap.get(HASH_VALUE_FIELD);
  }

  private Long getExpiresTime(RMap<String, String> rMap) {
    return ConfigUtils.expiresTime(rMap.get(HASH_EXPIRE_FIELD));
  }

  private void put(RMap<String, String> rMap, String value, long expireTime) {
    rMap.put(HASH_VALUE_FIELD, value);
    putExpire(rMap, expireTime);
  }

  private void putExpire(RMap<String, String> rMap, long expireTime) {
    rMap.put(HASH_EXPIRE_FIELD, String.valueOf(expireTime));
  }

  @Override
  public String getAccessToken() {
    return getValue(accessTokenRMap);
  }

  @Override
  public Lock getAccessTokenLock() {
    return accessTokenLock;
  }

  @Override
  public boolean isAccessTokenExpired() {
    return ConfigUtils.isExpired(getExpiresTime(accessTokenRMap));
  }

  @Override
  public synchronized void updateAccessToken(String accessToken, int expiresInSeconds) {
    put(accessTokenRMap, accessToken, ConfigUtils.expiresTimestamp(expiresInSeconds));
  }

  @Override
  public void expireAccessToken() {
    setExpiresTime(0);
  }

  @Override
  public long getExpiresTime() {
    return getExpiresTime(accessTokenRMap);
  }

  @Override
  public void setExpiresTime(long expiresTime) {
    putExpire(accessTokenRMap, expiresTime);
  }

  @Override
  public String getJsapiTicket() {
    return getValue(jsApiRMap);
  }

  @Override
  public Lock getJsapiTicketLock() {
    return jsapiTicketLock;
  }

  @Override
  public boolean isJsapiTicketExpired() {
    return ConfigUtils.isExpired(getExpiresTime(jsApiRMap));
  }

  @Override
  public void expireJsapiTicket() {
    putExpire(jsApiRMap, 0);
  }

  @Override
  public void updateJsapiTicket(String jsapiTicket, int expiresInSeconds) {
    put(jsApiRMap, jsapiTicket, ConfigUtils.expiresTimestamp(expiresInSeconds));
  }

  @Override
  public String getCardApiTicket() {
    return getValue(cardApiRMap);
  }

  @Override
  public Lock getCardApiTicketLock() {
    return cardApiTicketLock;
  }

  @Override
  public boolean isCardApiTicketExpired() {
    return ConfigUtils.isExpired(getExpiresTime(cardApiRMap));
  }

  @Override
  public void expireCardApiTicket() {
    putExpire(cardApiRMap, 0);
  }

  @Override
  public void updateCardApiTicket(String cardApiTicket, int expiresInSeconds) {
    put(cardApiRMap, cardApiTicket, ConfigUtils.expiresTimestamp(expiresInSeconds));
  }

}
