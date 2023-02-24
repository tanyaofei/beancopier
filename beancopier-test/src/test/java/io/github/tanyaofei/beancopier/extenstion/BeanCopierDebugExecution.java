package io.github.tanyaofei.beancopier.extenstion;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;

/**
 * @author tanyaofei
 */
public class BeanCopierDebugExecution implements BeforeEachCallback {

  protected String debugLocation() {
    return "./target/generated-test-classes/";
  }

  @Override
  public void beforeEach(ExtensionContext extensionContext) {
    var debugLocation = new File(debugLocation());
    if (!debugLocation.exists()) {
      if (!debugLocation.mkdirs()) {
        throw new IllegalStateException("Failed to make directories: " + debugLocation.getPath());
      }
    }
    System.setProperty("beancopier.debugLocation", debugLocation.getPath());
  }
}
