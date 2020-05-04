package me.chanjar.weixin.common.util;

import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

public final class ConfigUtils {

  public static final int EXPIRES_AHEAD_SECONDS = 200;
  public static final int EXPIRES_IN_MINIMUM_SECONDS = 2;

  private ConfigUtils() {
  }

  /**
   * 会过期的数据提前过期时间，预留200秒的时间
   */
  public static long expiresTimestamp(int expiresInSeconds) {
    return System.currentTimeMillis() + (expiresInSeconds - EXPIRES_AHEAD_SECONDS) * 1000L;
  }

  /**
   * 拼接冒号分割的 Redis Key
   */
  public static String redisKey(@NonNull String... parts) {
    if (parts.length < 1) {
      throw new IllegalArgumentException("parts 不能为空");
    }
    StringBuilder redisKey = new StringBuilder();
    for (String part : parts) {
      String toBeAppend = StringUtils.removeEnd(StringUtils.removeStart(part, ":"), ":");
      if (StringUtils.isBlank(toBeAppend)) {
        continue;
      }
      if (redisKey.length() > 0) {
        redisKey.append(":");
      }
      redisKey.append(toBeAppend);
    }
    return redisKey.toString();
  }

  /**
   * 字符串形式的时间戳转换成 long
   *
   * @param expiresTime 时间戳，单位为毫秒
   */
  public static long expiresTime(String expiresTime) {
    if (StringUtils.isBlank(expiresTime)) {
      return 0;
    }
    try {
      return Long.parseLong(expiresTime);
    } catch (Exception e) {
      return 0;
    }
  }

  /**
   * 判断 expiresTime 是否已经过期
   *
   * @param expiresTime 时间戳，单位为毫秒
   */
  public static boolean isExpired(String expiresTime) {
    return isExpired(expiresTime(expiresTime));
  }

  /**
   * 判断 expiresTime 是否已经过期
   *
   * @param expiresTime 时间戳，单位为毫秒
   */
  public static boolean isExpired(long expiresTime) {
    return System.currentTimeMillis() >= (expiresTime + EXPIRES_IN_MINIMUM_SECONDS * 1000);
  }

  /**
   * 判断 expiresTime 是否已经过期
   *
   * @param ttl Redis 返回的存活时间，单位为秒
   */
  public static boolean isTTLExpired(long ttl) {
    return ttl <= EXPIRES_IN_MINIMUM_SECONDS;
  }
}
