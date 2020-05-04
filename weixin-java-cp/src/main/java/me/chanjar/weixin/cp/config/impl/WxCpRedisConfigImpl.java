package me.chanjar.weixin.cp.config.impl;

import me.chanjar.weixin.common.bean.WxAccessToken;
import me.chanjar.weixin.common.util.http.apache.ApacheHttpClientBuilder;
import me.chanjar.weixin.cp.config.WxCpConfigStorage;
import me.chanjar.weixin.cp.constant.WxCpApiPathConsts;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.File;

/**
 * <pre>
 *    使用说明：本实现仅供参考，并不完整.
 *    比如为减少项目依赖，未加入redis分布式锁的实现，如有需要请自行实现。
 * </pre>
 *
 * @deprecated 未实现分布式锁，建议使用 <code>WxCpRedissonConfigImpl</code>
 * @author gaigeshen
 */
@Deprecated
public class WxCpRedisConfigImpl extends WxCpDefaultConfigImpl {
  private static final String ACCESS_TOKEN_KEY = "WX_CP_ACCESS_TOKEN";
  private static final String ACCESS_TOKEN_EXPIRES_TIME_KEY = "WX_CP_ACCESS_TOKEN_EXPIRES_TIME";
  private static final String JS_API_TICKET_KEY = "WX_CP_JS_API_TICKET";
  private static final String JS_API_TICKET_EXPIRES_TIME_KEY = "WX_CP_JS_API_TICKET_EXPIRES_TIME";
  private static final String AGENT_JSAPI_TICKET_KEY = "WX_CP_AGENT_%s_JSAPI_TICKET";
  private static final String AGENT_JSAPI_TICKET_EXPIRES_TIME_KEY = "WX_CP_AGENT_%s_JSAPI_TICKET_EXPIRES_TIME";

  private final JedisPool jedisPool;

  public WxCpRedisConfigImpl(String corpId, String corpSecret, String token, String aesKey, Integer agentId, JedisPool jedisPool) {
    super(corpId, corpSecret, token, aesKey, agentId);
    this.jedisPool = jedisPool;
  }

  /**
   * This method will be destroy jedis pool
   */
  public void destroy() {
    this.jedisPool.destroy();
  }

  @Override
  public String getAccessToken() {
    try (Jedis jedis = this.jedisPool.getResource()) {
      return jedis.get(ACCESS_TOKEN_KEY);
    }
  }

  @Override
  public boolean isAccessTokenExpired() {
    try (Jedis jedis = this.jedisPool.getResource()) {
      String expiresTimeStr = jedis.get(ACCESS_TOKEN_EXPIRES_TIME_KEY);

      if (expiresTimeStr != null) {
        return System.currentTimeMillis() > Long.parseLong(expiresTimeStr);
      }

      return true;

    }
  }

  @Override
  public void expireAccessToken() {
    try (Jedis jedis = this.jedisPool.getResource()) {
      jedis.set(ACCESS_TOKEN_EXPIRES_TIME_KEY, "0");
    }
  }

  @Override
  public synchronized void updateAccessToken(WxAccessToken accessToken) {
    this.updateAccessToken(accessToken.getAccessToken(), accessToken.getExpiresIn());
  }

  @Override
  public synchronized void updateAccessToken(String accessToken, int expiresInSeconds) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      jedis.set(ACCESS_TOKEN_KEY, accessToken);

      jedis.set(ACCESS_TOKEN_EXPIRES_TIME_KEY,
        (System.currentTimeMillis() + (expiresInSeconds - 200) * 1000L) + "");
    }
  }

  @Override
  public String getJsapiTicket() {
    try (Jedis jedis = this.jedisPool.getResource()) {
      return jedis.get(JS_API_TICKET_KEY);
    }
  }

  @Override
  public boolean isJsapiTicketExpired() {
    try (Jedis jedis = this.jedisPool.getResource()) {
      String expiresTimeStr = jedis.get(JS_API_TICKET_EXPIRES_TIME_KEY);

      if (expiresTimeStr != null) {
        long expiresTime = Long.parseLong(expiresTimeStr);
        return System.currentTimeMillis() > expiresTime;
      }

      return true;
    }
  }

  @Override
  public void expireJsapiTicket() {
    try (Jedis jedis = this.jedisPool.getResource()) {
      jedis.set(JS_API_TICKET_EXPIRES_TIME_KEY, "0");
    }
  }

  @Override
  public synchronized void updateJsapiTicket(String jsapiTicket, int expiresInSeconds) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      jedis.set(JS_API_TICKET_KEY, jsapiTicket);
      jedis.set(JS_API_TICKET_EXPIRES_TIME_KEY,
        (System.currentTimeMillis() + (expiresInSeconds - 200) * 1000L + ""));
    }

  }

  @Override
  public String getAgentJsapiTicket() {
    try (Jedis jedis = this.jedisPool.getResource()) {
      return jedis.get(String.format(AGENT_JSAPI_TICKET_KEY, agentId));
    }
  }

  @Override
  public boolean isAgentJsapiTicketExpired() {
    try (Jedis jedis = this.jedisPool.getResource()) {
      String expiresTimeStr = jedis.get(String.format(AGENT_JSAPI_TICKET_EXPIRES_TIME_KEY, agentId));

      if (expiresTimeStr != null) {
        return System.currentTimeMillis() > Long.parseLong(expiresTimeStr);
      }

      return true;
    }
  }

  @Override
  public void expireAgentJsapiTicket() {
    try (Jedis jedis = this.jedisPool.getResource()) {
      jedis.set(String.format(AGENT_JSAPI_TICKET_EXPIRES_TIME_KEY, agentId), "0");
    }
  }

  @Override
  public void updateAgentJsapiTicket(String jsapiTicket, int expiresInSeconds) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      jedis.set(String.format(AGENT_JSAPI_TICKET_KEY, agentId), jsapiTicket);
      jedis.set(String.format(AGENT_JSAPI_TICKET_EXPIRES_TIME_KEY, agentId),
        (System.currentTimeMillis() + (expiresInSeconds - 200) * 1000L + ""));
    }

  }

  @Override
  public long getExpiresTime() {
    try (Jedis jedis = this.jedisPool.getResource()) {
      String expiresTimeStr = jedis.get(ACCESS_TOKEN_EXPIRES_TIME_KEY);

      if (expiresTimeStr != null) {
        return Long.parseLong(expiresTimeStr);
      }

      return 0L;

    }
  }

  @Override
  public boolean autoRefreshToken() {
    return true;
  }

}
