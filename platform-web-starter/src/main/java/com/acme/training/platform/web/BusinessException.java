package com.acme.training.platform.web;

import com.acme.training.platform.error.PlatformException;

/** @deprecated Business modules should throw PlatformException with a stable code. */
@Deprecated
public class BusinessException extends PlatformException {
 
    public BusinessException(String code, String message) {
        super(code, message);
    }
}
