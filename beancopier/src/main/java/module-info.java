module io.github.tanyaofei.beancopier {
  requires static lombok;
  requires guava;
  requires org.objectweb.asm;
  requires org.objectweb.asm.commons;
  requires org.jetbrains.annotations;
  requires org.slf4j;
  requires jsr305;

  exports io.github.tanyaofei.beancopier;
  exports io.github.tanyaofei.beancopier.annotation;
  exports io.github.tanyaofei.beancopier.exception;
}