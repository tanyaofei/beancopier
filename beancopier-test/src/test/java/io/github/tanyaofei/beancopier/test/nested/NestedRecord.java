package io.github.tanyaofei.beancopier.test.nested;

import java.util.List;

/**
 * @author tanyaofei
 */

public record NestedRecord(
    int seniority,
    NestedRecord child,
    List<NestedRecord> children
) {
}
