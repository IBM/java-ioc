/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.parsers;

import java.net.URI;
import java.net.URISyntaxException;

public class URIParser {

  public static URI parse(final String transformation) throws URISyntaxException {
    if (System.getProperty("os.name").startsWith("Win")) {
      return new URI(transformation.replace('\\', '/'));
    } else {
      return new URI(transformation);
    }
  }

}
