/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

package com.ibm.ioc.parsers;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

/**
 * Instantiates ciphers based on Java Cryptographic Extension (JCE) framework specifications
 */
public class CipherParser {
  public static Cipher parse(final String transformation) throws NoSuchAlgorithmException,
      NoSuchPaddingException {
    return Cipher.getInstance(transformation);
  }
}
