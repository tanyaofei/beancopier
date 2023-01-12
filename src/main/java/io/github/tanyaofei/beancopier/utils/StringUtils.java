package io.github.tanyaofei.beancopier.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author tanyaofei
 * @since 0.0.1
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StringUtils {

  public static boolean hasLength(String str) {
    return str != null && !str.isEmpty();
  }

  public static boolean hasNotLength(String str) {
    return str == null || str.isEmpty();
  }

  public static String capitalize(String str) {
    return changeFirstCharacterCase(str, true);
  }

  public static String uncapitalize(String str) {
    return changeFirstCharacterCase(str, false);
  }

  private static String changeFirstCharacterCase(String str, boolean capitalize) {
    if (hasNotLength(str)) {
      return str;
    } else {
      char baseChar = str.charAt(0);
      char updatedChar;
      if (capitalize) {
        updatedChar = Character.toUpperCase(baseChar);
      } else {
        updatedChar = Character.toLowerCase(baseChar);
      }

      if (baseChar == updatedChar) {
        return str;
      } else {
        char[] chars = str.toCharArray();
        chars[0] = updatedChar;
        return new String(chars, 0, chars.length);
      }
    }
  }

}
