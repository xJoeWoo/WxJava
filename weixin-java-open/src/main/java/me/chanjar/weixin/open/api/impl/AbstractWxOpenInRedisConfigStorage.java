package me.chanjar.weixin.open.api.impl;


import me.chanjar.weixin.common.util.ConfigUtils;

/**
 * @author yangyidian
 * @date 2020/01/09
 **/
public abstract class AbstractWxOpenInRedisConfigStorage extends WxOpenInMemoryConfigStorage {
  protected final static String COMPONENT_VERIFY_TICKET_KEY = "wechat_component_verify_ticket";
  protected final static String COMPONENT_ACCESS_TOKEN_KEY = "wechat_component_access_token";
  protected final static String COMPONENT_ACCESS_TOKEN_LOCK_KEY = "wechat_component_access_token_lock";

  protected final static String AUTHORIZER_REFRESH_TOKEN_KEY = "wechat_authorizer_refresh_token";
  protected final static String AUTHORIZER_ACCESS_TOKEN_KEY = "wechat_authorizer_access_token";

  protected final static String JSAPI_TICKET_KEY = "wechat_jsapi_ticket";

  protected final static String CARD_API_TICKET_KEY = "wechat_card_api_ticket";

  protected final String redisKeyPrefix;
  /**
   * redis 存储的 key 的前缀，可为空
   */
  protected final String componentVerifyTicketKey;
  protected final String componentAccessTokenKey;
  protected final String componentAccessTokenLockKey;

  protected final String authorizerRefreshTokenKeyPrefix;
  protected final String authorizerAccessTokenKeyPrefix;

  protected final String jsapiTicketKeyPrefix;

  protected final String cardApiTicketKeyPrefix;

  public AbstractWxOpenInRedisConfigStorage(String componentAppId, String componentAppSecret, String componentToken, String componentAesKey) {
    this(componentAppId, componentAppSecret, componentToken, componentAesKey, null);
  }

  public AbstractWxOpenInRedisConfigStorage(String componentAppId, String componentAppSecret, String componentToken, String componentAesKey, String redisKeyPrefix) {
    super(componentAppId, componentAppSecret, componentToken, componentAesKey);
    this.redisKeyPrefix = redisKeyPrefix;

    componentVerifyTicketKey = ConfigUtils.redisKey(redisKeyPrefix, COMPONENT_VERIFY_TICKET_KEY, componentAppId);
    componentAccessTokenKey = ConfigUtils.redisKey(redisKeyPrefix, COMPONENT_ACCESS_TOKEN_KEY, componentAppId);
    componentAccessTokenLockKey  = ConfigUtils.redisKey(redisKeyPrefix,COMPONENT_ACCESS_TOKEN_LOCK_KEY, componentAppId);

    authorizerRefreshTokenKeyPrefix = ConfigUtils.redisKey(redisKeyPrefix, AUTHORIZER_REFRESH_TOKEN_KEY, componentAppId);
    authorizerAccessTokenKeyPrefix = ConfigUtils.redisKey(redisKeyPrefix, AUTHORIZER_ACCESS_TOKEN_KEY, componentAppId);

    // 下面两个不用 keyPrefix，why?
    jsapiTicketKeyPrefix = ConfigUtils.redisKey(JSAPI_TICKET_KEY, componentAppId);
    cardApiTicketKeyPrefix = ConfigUtils.redisKey(CARD_API_TICKET_KEY, componentAppId);
  }

}
