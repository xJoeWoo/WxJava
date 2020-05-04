package me.chanjar.weixin.common.util;

import org.testng.annotations.Test;

import static me.chanjar.weixin.common.util.ConfigUtils.*;
import static org.testng.Assert.*;

public class ConfigUtilsTest {

  @Test
  public void testExpiresTimestamp() {
    assertEquals(expiresTimestamp(3600), System.currentTimeMillis() + (3600 - ConfigUtils.EXPIRES_AHEAD_SECONDS) * 1000);
  }

  @Test
  public void testRedisKey() {
    assertEquals(redisKey(""), "");
    assertEquals(redisKey(":"), "");
    assertEquals(redisKey(":x:"), "x");
    assertEquals(redisKey(":x"), "x");
    assertEquals(redisKey("x:"), "x");
    assertEquals(redisKey("x", ":", ":y"), "x:y");
    assertEquals(redisKey("x", ":", ":::"), "x::");
  }

  @Test
  public void testExpiresTime() {
    assertEquals(expiresTime(""), 0);
    assertEquals(expiresTime("asdf"), 0);
    assertEquals(expiresTime("3"), 3);
    assertEquals(expiresTime("3333333333333333333333333333333333333333"), 0);
    assertEquals(expiresTime(null), 0);
  }

  @Test
  public void testIsExpired() {
    assertTrue(isExpired(System.currentTimeMillis() - EXPIRES_IN_MINIMUM_SECONDS * 1000));
    assertTrue(isExpired(System.currentTimeMillis() - 300 - EXPIRES_IN_MINIMUM_SECONDS * 1000));
    assertFalse(isExpired(System.currentTimeMillis() + 300 - EXPIRES_IN_MINIMUM_SECONDS * 1000));
  }

  @Test
  public void testIsTTLExpired() {
    assertTrue(isTTLExpired(ConfigUtils.EXPIRES_IN_MINIMUM_SECONDS));
    assertFalse(isTTLExpired(ConfigUtils.EXPIRES_IN_MINIMUM_SECONDS + 1));
  }
}
