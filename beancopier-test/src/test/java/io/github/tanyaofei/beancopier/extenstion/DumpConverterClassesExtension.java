package io.github.tanyaofei.beancopier.extenstion;

import io.github.tanyaofei.beancopier.BeanCopierConfiguration;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;

/**
 * @author tanyaofei
 */
public class DumpConverterClassesExtension implements BeforeTestExecutionCallback {

  protected String getPackageName() {
    return "./target/generated-test-classes/";
  }

  @Override
  public void beforeTestExecution(ExtensionContext extensionContext) {
    File dumpPath = new File(getPackageName());
    if (!dumpPath.exists()) {
      if (!dumpPath.mkdirs()) {
        throw new IllegalStateException("Failed to make directories: " + dumpPath.getPath());
      }
    }
    System.setProperty(BeanCopierConfiguration.PropertyNames.CONVERTER_CLASS_DUMP_PATH, dumpPath.getPath());
  }
}
