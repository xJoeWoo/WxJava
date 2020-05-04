package me.chanjar.weixin.cp.config.impl;

import me.chanjar.weixin.common.util.ConfigUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.util.concurrent.locks.Lock;

/**
 * <pre>
 *    使用说明：本实现仅供参考，并不完整.
 *    比如为减少项目依赖，未加入redis分布式锁的实现，如有需要请自行实现。
 * </pre>
 *
 * @author gaigeshen
 */
public class WxCpRedissionConfigImpl extends WxCpDefaultConfigImpl {
  public static final String DEFAULT_REDIS_KEY_PREFIX = "WX_CP_";
  public static final String DEFAULT_LOCK_REDIS_KEY_PREFIX = "wxCp";
  private static final String ACCESS_TOKEN_KEY = "ACCESS_TOKEN";
  private static final String ACCESS_TOKEN_EXPIRES_TIME_KEY = "ACCESS_TOKEN_EXPIRES_TIME";
  private static final String ACCESS_TOKEN_LOCK_KEY = "accessTokenLock";
  private static final String JS_API_TICKET_KEY = "JS_API_TICKET";
  private static final String JS_API_TICKET_EXPIRES_TIME_KEY = "JS_API_TICKET_EXPIRES_TIME";
  private static final String JS_API_TICKET_LOCK_KEY = "jsApiLock";
  private static final String AGENT_JSAPI_TICKET_KEY = "AGENT_%s_JSAPI_TICKET";
  private static final String AGENT_JSAPI_TICKET_EXPIRES_TIME_KEY = "AGENT_%s_JSAPI_TICKET_EXPIRES_TIME";
  private static final String AGENT_JSAPI_TICKET_LOCK_KEY = "agentJsApiTicketLock";

  private final RBucket<String> accessTokenBucket;
  private final RBucket<String> accessTokenExpiresTimeBucket;
  private final RBucket<String> jsApiTicketBucket;
  private final RBucket<String> jsApiTicketExpiresTimeBucket;
  private final RBucket<String> agentJsApiTicketBucket;
  private final RBucket<String> agentJsApiTicketExpiresTimeBucket;

  private final Lock accessTokenLock;
  private final Lock jsApiTicketLock;
  private final Lock agentJsApiTicketLock;

  public WxCpRedissionConfigImpl(String corpId, String corpSecret, Integer agentId, String token, String aesKey, RedissonClient redissonClient) {
    this(corpId, corpSecret, agentId, token, aesKey, redissonClient, null);
  }

  public WxCpRedissionConfigImpl(String corpId, String corpSecret, Integer agentId, String token, String aesKey, RedissonClient redissonClient, String redisKeyPrefix) {
    super(corpId, corpSecret, token, aesKey, agentId);

    if (StringUtils.isBlank(redisKeyPrefix)) {
      redisKeyPrefix = DEFAULT_REDIS_KEY_PREFIX;
    }

    // Use standard ':' split redis key if redisKeyPrefix is set
    if (redisKeyPrefix.equals(DEFAULT_REDIS_KEY_PREFIX)) {
      accessTokenBucket = redissonClient.getBucket(redisKeyPrefix + ACCESS_TOKEN_KEY);
      accessTokenExpiresTimeBucket = redissonClient.getBucket(redisKeyPrefix + ACCESS_TOKEN_EXPIRES_TIME_KEY);
      jsApiTicketBucket = redissonClient.getBucket(redisKeyPrefix + JS_API_TICKET_KEY);
      jsApiTicketExpiresTimeBucket = redissonClient.getBucket(redisKeyPrefix + JS_API_TICKET_EXPIRES_TIME_KEY);
      agentJsApiTicketBucket = redissonClient.getBucket(redisKeyPrefix + String.format(AGENT_JSAPI_TICKET_KEY, agentId));
      agentJsApiTicketExpiresTimeBucket = redissonClient.getBucket(redisKeyPrefix + String.format(AGENT_JSAPI_TICKET_EXPIRES_TIME_KEY, agentId));
    } else {
      accessTokenBucket = redissonClient.getBucket(ConfigUtils.redisKey(redisKeyPrefix, ACCESS_TOKEN_KEY));
      accessTokenExpiresTimeBucket = redissonClient.getBucket(ConfigUtils.redisKey(redisKeyPrefix, ACCESS_TOKEN_EXPIRES_TIME_KEY));
      jsApiTicketBucket = redissonClient.getBucket(ConfigUtils.redisKey(redisKeyPrefix, JS_API_TICKET_KEY));
      jsApiTicketExpiresTimeBucket = redissonClient.getBucket(ConfigUtils.redisKey(redisKeyPrefix, JS_API_TICKET_EXPIRES_TIME_KEY));
      agentJsApiTicketBucket = redissonClient.getBucket(ConfigUtils.redisKey(redisKeyPrefix, String.format(AGENT_JSAPI_TICKET_KEY, agentId)));
      agentJsApiTicketExpiresTimeBucket = redissonClient.getBucket(ConfigUtils.redisKey(redisKeyPrefix, String.format(AGENT_JSAPI_TICKET_EXPIRES_TIME_KEY, agentId)));
    }

    // Remove if statement if DEFAULT_REDIS_KEY_PREFIX follows standard ':' redis key format
    if (redisKeyPrefix.equals(DEFAULT_REDIS_KEY_PREFIX)) {
      redisKeyPrefix = DEFAULT_LOCK_REDIS_KEY_PREFIX;
    }
    accessTokenLock = redissonClient.getLock(ConfigUtils.redisKey(redisKeyPrefix, ACCESS_TOKEN_LOCK_KEY));
    jsApiTicketLock = redissonClient.getLock(ConfigUtils.redisKey(redisKeyPrefix, JS_API_TICKET_LOCK_KEY));
    agentJsApiTicketLock = redissonClient.getLock(ConfigUtils.redisKey(redisKeyPrefix, AGENT_JSAPI_TICKET_LOCK_KEY, agentId.toString()));
  }

  @Override
  public String getAccessToken() {
    return accessTokenBucket.get();
  }

  @Override
  public boolean isAccessTokenExpired() {
    return ConfigUtils.isExpired(accessTokenExpiresTimeBucket.get());
  }

  @Override
  public void expireAccessToken() {
    accessTokenExpiresTimeBucket.set("0");
  }

  @Override
  public synchronized void updateAccessToken(String accessToken, int expiresInSeconds) {
    accessTokenBucket.set(accessToken);
    accessTokenExpiresTimeBucket.set(String.valueOf(ConfigUtils.expiresTimestamp(expiresInSeconds)));
  }

  @Override
  public long getExpiresTime() {
    return ConfigUtils.expiresTime(accessTokenExpiresTimeBucket.get());
  }

  @Override
  public Lock getAccessTokenLock() {
    return accessTokenLock;
  }

  @Override
  public String getJsapiTicket() {
    return jsApiTicketBucket.get();
  }

  @Override
  public boolean isJsapiTicketExpired() {
    return ConfigUtils.isExpired(jsApiTicketExpiresTimeBucket.get());
  }

  @Override
  public void expireJsapiTicket() {
    jsApiTicketExpiresTimeBucket.set("0");
  }

  @Override
  public synchronized void updateJsapiTicket(String jsapiTicket, int expiresInSeconds) {
    jsApiTicketBucket.set(jsapiTicket);
    jsApiTicketExpiresTimeBucket.set(String.valueOf(ConfigUtils.expiresTimestamp(expiresInSeconds)));
  }

  @Override
  public Lock getJsApiTicketLock() {
    return jsApiTicketLock;
  }

  @Override
  public String getAgentJsapiTicket() {
    return agentJsApiTicketBucket.get();
  }

  @Override
  public boolean isAgentJsapiTicketExpired() {
    return ConfigUtils.isExpired(agentJsApiTicketExpiresTimeBucket.get());
  }

  @Override
  public void expireAgentJsapiTicket() {
    agentJsApiTicketExpiresTimeBucket.set("0");
  }

  @Override
  public void updateAgentJsapiTicket(String jsapiTicket, int expiresInSeconds) {
    agentJsApiTicketBucket.set(jsapiTicket);
    agentJsApiTicketExpiresTimeBucket.set(String.valueOf(ConfigUtils.expiresTimestamp(expiresInSeconds)));
  }

  @Override
  public Lock getAgentJsApiTicketLock() {
    return agentJsApiTicketLock;
  }

  @Override
  public boolean autoRefreshToken() {
    return true;
  }

}
