package me.chanjar.weixin.mp.config.impl;

import me.chanjar.weixin.common.util.ConfigUtils;
import me.chanjar.weixin.mp.enums.TicketType;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class WxMpRedissonConfigImpl extends WxMpDefaultConfigImpl {

  public static final String DEFAULT_REDIS_KEY_PREFIX = "wx";

  private static final String ACCESS_TOKEN_KEY = "access_token";
  private static final String ACCESS_TOKEN_LOCK_KEY = "accessTokenLock";
  private static final String TICKET_KEY = "ticket:key";
  private static final String TICKET_LOCK_KEY = "ticketLock";

  private final RedissonClient redissonClient;
  private final RBucket<String> accessTokenBucket;
  private final String ticketPrefix;
  private final String ticketLockPrefix;

  private final Lock accessTokenLock;

  public WxMpRedissonConfigImpl(String appId, String secret, String token, String aesKey, RedissonClient redissonClient) {
    this(appId, secret, token, aesKey, redissonClient, null);
  }

  public WxMpRedissonConfigImpl(String appId, String secret, String token, String aesKey, RedissonClient redissonClient, String redisKeyPrefix) {
    super(appId, secret, token, aesKey);
    if (StringUtils.isBlank(redisKeyPrefix)) {
      redisKeyPrefix = DEFAULT_REDIS_KEY_PREFIX;
    }
    this.redissonClient = redissonClient;

    accessTokenLock = redissonClient.getLock(ConfigUtils.redisKey(redisKeyPrefix, ACCESS_TOKEN_LOCK_KEY, appId));
    accessTokenBucket = redissonClient.getBucket(ConfigUtils.redisKey(redisKeyPrefix, ACCESS_TOKEN_KEY, appId));

    ticketPrefix = ConfigUtils.redisKey(redisKeyPrefix, TICKET_KEY, appId);
    ticketLockPrefix = ConfigUtils.redisKey(redisKeyPrefix, TICKET_LOCK_KEY, appId);
  }

  @Override
  public String getAccessToken() {
    return accessTokenBucket.get();
  }

  @Override
  public Lock getAccessTokenLock() {
    return accessTokenLock;
  }

  @Override
  public boolean isAccessTokenExpired() {
    return ConfigUtils.isTTLExpired(accessTokenBucket.remainTimeToLive());
  }

  @Override
  public void expireAccessToken() {
    accessTokenBucket.delete();
  }

  @Override
  public synchronized void updateAccessToken(String accessToken, int expiresInSeconds) {
    accessTokenBucket.set(accessToken, expiresInSeconds, TimeUnit.SECONDS);
  }

  @Override
  public String getTicket(TicketType type) {
    return getTicketBucket(type).get();
  }

  @Override
  public Lock getTicketLock(TicketType type) {
    return redissonClient.getLock(ConfigUtils.redisKey(ticketLockPrefix, type.getCode()));
  }

  @Override
  public boolean isTicketExpired(TicketType type) {
    return ConfigUtils.isTTLExpired(getTicketBucket(type).remainTimeToLive());
  }

  @Override
  public void expireTicket(TicketType type) {
    getTicketBucket(type).delete();
  }

  @Override
  public synchronized void updateTicket(TicketType type, String jsapiTicket, int expiresInSeconds) {
    getTicketBucket(type).set(jsapiTicket, expiresInSeconds, TimeUnit.SECONDS);
  }

  private RBucket<String> getTicketBucket(TicketType type) {
    return redissonClient.getBucket(ConfigUtils.redisKey(ticketPrefix, type.getCode()));
  }

}
