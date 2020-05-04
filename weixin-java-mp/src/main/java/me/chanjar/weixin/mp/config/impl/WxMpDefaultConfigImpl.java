package me.chanjar.weixin.mp.config.impl;

import lombok.Data;
import me.chanjar.weixin.common.bean.WxAccessToken;
import me.chanjar.weixin.common.util.ConfigUtils;
import me.chanjar.weixin.common.util.http.apache.ApacheHttpClientBuilder;
import me.chanjar.weixin.mp.bean.WxMpHostConfig;
import me.chanjar.weixin.mp.config.WxMpConfigStorage;
import me.chanjar.weixin.mp.enums.TicketType;
import me.chanjar.weixin.mp.util.json.WxMpGsonBuilder;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于内存的微信配置provider，在实际生产环境中应该将这些配置持久化.
 *
 * @author chanjarster
 */
@Data
public class WxMpDefaultConfigImpl implements WxMpConfigStorage, Serializable {
  private static final long serialVersionUID = -6646519023303395185L;

  protected final String appId;
  protected final String secret;
  protected final String token;
  protected volatile String templateId;
  protected volatile String accessToken;
  protected final String aesKey;
  protected volatile long expiresTime;

  protected volatile String oauth2redirectUri;

  protected volatile String httpProxyHost;
  protected volatile int httpProxyPort;
  protected volatile String httpProxyUsername;
  protected volatile String httpProxyPassword;

  protected volatile String jsapiTicket;
  protected volatile long jsapiTicketExpiresTime;

  protected volatile String sdkTicket;
  protected volatile long sdkTicketExpiresTime;

  protected volatile String cardApiTicket;
  protected volatile long cardApiTicketExpiresTime;

  protected Lock accessTokenLock = new ReentrantLock();
  protected Lock jsapiTicketLock = new ReentrantLock();
  protected Lock sdkTicketLock = new ReentrantLock();
  protected Lock cardApiTicketLock = new ReentrantLock();

  protected volatile File tmpDirFile;

  protected volatile ApacheHttpClientBuilder apacheHttpClientBuilder;

  public WxMpDefaultConfigImpl(String appId, String secret, String token, String aesKey) {
    this.appId = appId;
    this.secret = secret;
    this.token = token;
    this.aesKey = aesKey;
  }

  @Override
  public boolean isAccessTokenExpired() {
    return System.currentTimeMillis() > this.expiresTime;
  }

  @Override
  public synchronized void updateAccessToken(WxAccessToken accessToken) {
    updateAccessToken(accessToken.getAccessToken(), accessToken.getExpiresIn());
  }

  @Override
  public synchronized void updateAccessToken(String accessToken, int expiresInSeconds) {
    this.accessToken = accessToken;
    this.expiresTime = System.currentTimeMillis() + (expiresInSeconds - 200) * 1000L;
  }

  @Override
  public void expireAccessToken() {
    this.expiresTime = 0;
  }

  @Override
  public String getTicket(TicketType type) {
    switch (type) {
      case SDK:
        return getSdkTicket();
      case JSAPI:
        return getJsapiTicket();
      case WX_CARD:
        return getCardApiTicket();
      default:
        return null;
    }
  }

  public void setTicket(TicketType type, String ticket) {
    switch (type) {
      case JSAPI:
        setJsapiTicket(ticket);
        break;
      case WX_CARD:
        setCardApiTicket(ticket);
        break;
      case SDK:
        setSdkTicket(ticket);
        break;
      default:
    }
  }

  @Override
  public Lock getTicketLock(TicketType type) {
    switch (type) {
      case SDK:
        return getSdkTicketLock();
      case JSAPI:
        return getJsapiTicketLock();
      case WX_CARD:
        return getCardApiTicketLock();
      default:
        return null;
    }
  }

  @Override
  public boolean isTicketExpired(TicketType type) {
    long expiresTime;
    switch (type) {
      case SDK:
        expiresTime = getSdkTicketExpiresTime();
        break;
      case JSAPI:
        expiresTime = getJsapiTicketExpiresTime();
        break;
      case WX_CARD:
        expiresTime = getCardApiTicketExpiresTime();
        break;
      default:
        return false;
    }
    return ConfigUtils.isExpired(expiresTime);
  }

  @Override
  public synchronized void updateTicket(TicketType type, String ticket, int expiresInSeconds) {
    setTicket(type, ticket);
    switch (type) {
      case JSAPI:
        setJsapiTicketExpiresTime(ConfigUtils.expiresTimestamp(expiresInSeconds));
        break;
      case WX_CARD:
        setCardApiTicketExpiresTime(ConfigUtils.expiresTimestamp(expiresInSeconds));
        break;
      case SDK:
        setSdkTicketExpiresTime(ConfigUtils.expiresTimestamp(expiresInSeconds));
        break;
      default:
    }
  }

  @Override
  public void expireTicket(TicketType type) {
    switch (type) {
      case JSAPI:
        setJsapiTicketExpiresTime(0);
        break;
      case WX_CARD:
        setCardApiTicketExpiresTime(0);
        break;
      case SDK:
        setSdkTicketExpiresTime(0);
        break;
      default:
    }
  }

  @Override
  public String toString() {
    return WxMpGsonBuilder.create().toJson(this);
  }

  @Override
  public boolean autoRefreshToken() {
    return true;
  }

  @Override
  public WxMpHostConfig getHostConfig() {
    return null;
  }

}
