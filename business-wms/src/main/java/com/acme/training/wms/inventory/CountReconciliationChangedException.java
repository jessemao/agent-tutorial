package com.acme.training.wms.inventory;

import java.util.List;

public final class CountReconciliationChangedException extends RuntimeException {
    private final String factsFingerprint;
    private final List<CountReconciliationResult> results;
    CountReconciliationChangedException(String factsFingerprint,List<CountReconciliationResult> results){super("inventory changed after count submission");this.factsFingerprint=factsFingerprint;this.results=results;}
    public String getFactsFingerprint(){return factsFingerprint;}
    public List<CountReconciliationResult> getResults(){return results;}
}
