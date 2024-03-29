package io.github.tanyaofei.beancopier.test.util;

import io.github.tanyaofei.beancopier.BeanCopierConfiguration;
import io.github.tanyaofei.beancopier.NamingPolicy;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;

public class DumpConverterClasses implements BeforeAllCallback {


  public static final String dumpPath = "./target/generated-test-classes/" + NamingPolicy.getDefault().getPackage().replace(".", "/");


  @Override
  public void beforeAll(ExtensionContext extensionContext) throws Exception {
    File dumpPath = new File(DumpConverterClasses.dumpPath);
    if (!dumpPath.exists()) {
      if (!dumpPath.mkdirs()) {
        throw new IllegalStateException("Failed to make directories: " + dumpPath.getPath());
      }
    }
    System.setProperty(BeanCopierConfiguration.PropertyNames.CONVERTER_CLASS_DUMP_PATH, dumpPath.getPath());
  }


}
