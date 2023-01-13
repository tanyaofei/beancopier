package io.github.tanyaofei.beancopier.utils.breakout;

import java.security.AllPermission;
import java.security.ProtectionDomain;

public class HackerClassLoader extends ClassLoader {

  public HackerClassLoader() {
    super(HackerClassLoader.class.getClassLoader());
  }

  public Class<?> define(byte[] code) {
    var domain = new ProtectionDomain(null,
        new AllPermission().newPermissionCollection()
    );
    return defineClass(null, code, 0, code.length, domain);
  }

}
