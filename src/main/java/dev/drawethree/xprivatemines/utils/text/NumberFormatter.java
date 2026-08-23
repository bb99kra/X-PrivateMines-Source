package dev.drawethree.xprivatemines.utils.text;

import java.text.DecimalFormat;

public class NumberFormatter {
   private static final String[] SUFFIXES = new String[]{"", "K", "M", "B", "T", "Q", "QT", "S", "SP", "O", "N", "D", "UD", "DD"};
   private static final DecimalFormat FORMAT = new DecimalFormat("#.##");

   public static String format(double number) {
      if (number < 1000.0) {
         return FORMAT.format(number);
      } else {
         int index;
         for (index = 0; number >= 1000.0 && index < SUFFIXES.length - 1; index++) {
            number /= 1000.0;
         }

         return FORMAT.format(number) + SUFFIXES[index];
      }
   }

   public static String format(long number) {
      return format((double)number);
   }
}
