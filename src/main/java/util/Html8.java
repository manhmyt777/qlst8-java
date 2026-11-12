package util;

import java.text.NumberFormat;
import java.util.Locale;

public final class Html8 {
  public static String e(Object value) {
    return value == null
        ? ""
        : value
            .toString()
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
  }

  public static String money(Object value) {
    return value instanceof Number
        ? NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(value) + " ₫"
        : "0 ₫";
  }
}
