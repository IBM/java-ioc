/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import org.ini4j.Config;
import org.ini4j.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Windows/Unix version of Ini
 *
 * Same as {@link Ini} but with os-specific configuration set based on the system {@code os.name} property.
 */
public final class WUIni extends Ini {
  private static final long serialVersionUID = -1465353791960385242L;
  private static final Logger _logger = LoggerFactory.getLogger(WUIni.class);

  private WUIni() {
    super();
  }

  /**
   * Factory method to construct a WUIni
   * 
   * @return new WUIni
   */
  public static WUIni makeWUIni() {
    final WUIni wuIni = new WUIni();
    final Config cfg = Config.getGlobal().clone();
    if (System.getProperty("os.name").startsWith("Win")) {
      cfg.setEscape(false);
      cfg.setPathSeparator(PATH_SEPARATOR);
    }
    wuIni.setConfig(cfg);
    return wuIni;
  }

  /**
   * Factory method to construct a WUIni
   * 
   * @param input file this WUIni should wrap
   * @return new WUIni with the provided file set and loaded
   * @throws IOException if the file cannot be read
   */
  public static WUIni makeWUIni(final File... inputs) throws IOException {
    final WUIni wuIni = makeWUIni();
    for (final File input : inputs) {
      try {
        wuIni.load(input);
      } catch (final FileNotFoundException e) {
        _logger.debug("No ini file found at {}.  Will treat as empty file.", input);
      }
    }
    return wuIni;
  }

  /**
   * Factory method to construct a WUIni
   * 
   * @param input URL this WUIni should wrap
   * @return new WUIni with the provided URL set and loaded
   * @throws IOException if the URL cannot be read
   */
  public static WUIni makeWUIni(final URI... inputs) throws IOException {
    final WUIni wuIni = makeWUIni();
    for (final URI input : inputs) {
      try {
        wuIni.load(input.toURL());
      } catch (final FileNotFoundException e) {
        _logger.debug("No ini file found at {}.  Will treat as empty file.", input);
      }
    }
    return wuIni;
  }

}
