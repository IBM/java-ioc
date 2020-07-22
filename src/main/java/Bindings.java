//
// (C) Copyright IBM Corp. 2005 All Rights Reserved.
//
// Contact Information:
//
// IBM Corporation
// Legal Department
// 222 South Riverside Plaza
// Suite 1700
// Chicago, IL 60606, USA
//
// END-OF-HEADER
//
// -----------------------
// @author: mmotwani
//
// Date: Jan 15, 2014
// ---------------------

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
