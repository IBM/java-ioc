/*
* Copyright (c) IBM Corporation 2020. All Rights Reserved.
* Project name: java-ioc
* This project is licensed under the Apache License 2.0, see LICENSE.
*/

import com.ibm.ioc.JavaBindings;
import com.ibm.ioc.JavaBindingsProvider.BindingsFactory;
import com.ibm.ioc.RemotePropertiesLookup;
import com.ibm.ioc.RemotePropertiesProvider;
import com.ibm.ioc.impl.NullRemotePropertiesLookup;

public class Bindings implements JavaBindings {
  @Override
  public void register(final BindingsFactory def) {
    def.def(NullRemotePropertiesLookup.class)
        .iface(RemotePropertiesLookup.class, "null");

    def.builder(RemotePropertiesProvider.Builder.class, RemotePropertiesProvider.class)
        .ref("remote-properties-lookup", RemotePropertiesLookup.class);
  }
}
