package io.github.tanyaofei.beancopier.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nullable;

/**
 * @author tanyaofei
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StringUtils {

  @Contract(value = "null -> false", pure = true)
  public static boolean hasLength(@Nullable String str) {
    return str != null && !str.isEmpty();
  }

  @Contract(value = "null -> true", pure = true)
  public static boolean hasNotLength(@Nullable String str) {
    return str == null || str.isEmpty();
  }

  @Contract(value = "null -> null", pure = true)
  public static String capitalize(@Nullable String str) {
    return changeFirstCharacterCase(str, true);
  }

  @Contract(value = "null -> null", pure = true)
  public static String uncapitalize(@Nullable String str) {
    return changeFirstCharacterCase(str, false);
  }

  @Contract(value = "null, _ -> null", pure = true)
  private static String changeFirstCharacterCase(@Nullable String str, boolean capitalize) {
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
