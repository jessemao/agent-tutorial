package com.acme.training.platform.operator;

/** Platform seam for obtaining the authenticated operator. */
public interface CurrentOperator {

    String requiredOperatorId();
}
