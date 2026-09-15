package com.acme.training.wms.count;

enum CountAction {
    CREATE,
    SAVE,
    SUBMIT,
    REJECT,
    REOPEN,
    CANCEL,
    APPROVE;

    static CountAction parseTransition(String value) {
        try {
            CountAction action = CountAction.valueOf(value);
            if (action == REJECT || action == REOPEN || action == CANCEL) {
                return action;
            }
            throw new InvalidCountRequestException("不支持的盘点动作");
        } catch (RuntimeException failure) {
            if (failure instanceof InvalidCountRequestException) {
                throw failure;
            }
            throw new InvalidCountRequestException("不支持的盘点动作");
        }
    }
}
