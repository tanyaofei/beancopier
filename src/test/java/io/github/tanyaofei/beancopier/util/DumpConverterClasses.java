package io.github.tanyaofei.beancopier.util;

import io.github.tanyaofei.beancopier.BeanCopierConfiguration;
import io.github.tanyaofei.beancopier.NamingPolicy;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;

public class DumpConverterClasses implements BeforeAllCallback {


  @Override
  public void beforeAll(ExtensionContext extensionContext) throws Exception {
    File dumpPath = new File("./target/generated-test-classes/" + NamingPolicy.getDefault().getPackage().replace(".", "/"));
    if (!dumpPath.exists()) {
      if (!dumpPath.mkdirs()) {
        throw new IllegalStateException("Failed to make directories: " + dumpPath.getPath());
      }
    }
    System.setProperty(BeanCopierConfiguration.PropertyNames.CONVERTER_CLASS_DUMP_PATH, dumpPath.getPath());
  }


}
