/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc;

/**
 * Utility class to convert sizes to string and back.
 */
public final class Size {
  private static final int ONE_KB = 1024;

  public static final int KiB = ONE_KB; // NOSONAR
  public static final int MiB = ONE_KB * KiB; // NOSONAR
  public static final long GiB = ONE_KB * MiB; // NOSONAR
  public static final long TiB = ONE_KB * GiB; // NOSONAR
  public static final long PiB = ONE_KB * TiB; // NOSONAR

  private long size; // NOSONAR

  public Size() {}

  public Size(final long size) {
    this.size = size;
  }

  public Size(final String sizeStr) {
    setValue(sizeStr);
  }

  public void setValue(final long size) {
    this.size = size;
  }

  public void setValue(final String sizeStr) {
    this.size = getSizeInBytes(sizeStr);
  }

  public long getValue() {
    return this.size;
  }

  public static String getSizeInString(final long sizeBytes, final int precision) {
    final double size = sizeBytes;

    double finalSize = 0;
    String sizeSuffix = "";
    if (size < KiB) {
      finalSize = size;
      sizeSuffix = "  B";
    } else if (size < MiB) {
      finalSize = size / KiB;
      sizeSuffix = "KiB";
    } else if (size < GiB) {
      finalSize = size / MiB;
      sizeSuffix = "MiB";
    } else if (size < TiB) {
      finalSize = size / GiB;
      sizeSuffix = "GiB";
    } else if (size < PiB) {
      finalSize = size / TiB;
      sizeSuffix = "TiB";
    } else {
      finalSize = size / PiB;
      sizeSuffix = "PiB";
    }

    return String.format("%." + precision + "f %s", finalSize, sizeSuffix); // NOSONAR - precision string used for
                                                                            // formatting

  }

  public static long getSizeInBytes(final String rawCapacityStr) {
    // I hope that something more standard could be used
    final String capacityStr = rawCapacityStr.trim();
    double baseValue = -1;
    long multiplier = 1;

    int index = 0;

    // while the character is a numerical value (0-9) or is a decimal point (.)
    while (index != capacityStr.length()
        && ((capacityStr.charAt(index) >= '0' && capacityStr.charAt(index) <= '9')
            || capacityStr.charAt(index) == '.')) {
      index++;
    }
    if (index != 0) {
      baseValue = Double.parseDouble(capacityStr.substring(0, index));
      while (index != capacityStr.length() && capacityStr.charAt(index) == ' ') {
        index++;
      }
      final String multiplierStr =
          (index == capacityStr.length()) ? "" : capacityStr.substring(index);

      if (!"".equals(multiplierStr)) {
        if ("K".equalsIgnoreCase(multiplierStr) || "KB".equalsIgnoreCase(multiplierStr)
            || "KiB".equalsIgnoreCase(multiplierStr)) {
          multiplier = KiB;
        } else if ("M".equalsIgnoreCase(multiplierStr) || "MB".equalsIgnoreCase(multiplierStr)
            || "MiB".equalsIgnoreCase(multiplierStr)) {
          multiplier = MiB;
        } else if ("G".equalsIgnoreCase(multiplierStr) || "GB".equalsIgnoreCase(multiplierStr)
            || "GiB".equalsIgnoreCase(multiplierStr)) {
          multiplier = GiB;
        } else if ("T".equalsIgnoreCase(multiplierStr) || "TB".equalsIgnoreCase(multiplierStr)
            || "TiB".equalsIgnoreCase(multiplierStr)) {
          multiplier = TiB;
        } else if ("P".equalsIgnoreCase(multiplierStr) || "PB".equalsIgnoreCase(multiplierStr)
            || "PiB".equalsIgnoreCase(multiplierStr)) {
          multiplier = PiB;
        } else {
          throw new IllegalArgumentException("Bad multiplier " + multiplierStr);
        }
      }
      return (long) (multiplier * baseValue);
    }
    throw new IllegalArgumentException("Can't parse " + capacityStr);
  }
}
