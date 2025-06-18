```java
@Override
public boolean processEarmark(CreditCheckRequest request) {
    RequestType requestType = request.getRequestType();
    AtomicBoolean isValid = new AtomicBoolean(true);
    String clientId = request.getKlNumber() != null ? request.getKlNumber() : request.getBaseNumber();

    // Validate: for MODIFY or UNEARMARK, the original reference must exist
    if (EnumSet.of(RequestType.UNEARMARK, RequestType.MODIFY_CHECK_EARMARK, RequestType.MODIFY_EARMARK).contains(requestType)) {
        for (var deal : request.getDeals()) {
            String ref = deal.getOriginalReferenceNumber();
            if (ref == null || ref.isBlank() || !earmarkMap.containsKey(ref)) {
                log.warn("Invalid modify/unearmark request - missing/unknown original reference '{}'.", ref);
                isValid.set(false);
                break;
            }
        }
    }

    // Validate: for EARMARK or CHECK_EARMARK, ensure reference doesn't already exist
    if (EnumSet.of(RequestType.EARMARK, RequestType.CHECK_EARMARK).contains(requestType)) {
        for (var deal : request.getDeals()) {
            String ref = deal.getReferenceNumber();
            if (ref != null && !ref.isBlank() && earmarkMap.containsKey(ref)) {
                log.warn("Duplicate earmark submission detected for reference '{}'.", ref);
                isValid.set(false);
                break;
            }
        }
    }

    if (!isValid.get()) return false;

    boughtAmounts.putIfAbsent(clientId, new HashMap<>());
    soldAmounts.putIfAbsent(clientId, new HashMap<>());

    switch (requestType) {
        case EARMARK, CHECK_EARMARK -> {
            for (var deal : request.getDeals()) {
                String ref = deal.getReferenceNumber();
                earmarkMap.put(ref, new Earmark(deal.getBoughtAmount(), deal.getSoldAmount(), deal.getUsDollarAmt()));

                updateCurrencyAmount(boughtAmounts.get(clientId), deal.getBoughtCurrency(), deal.getBoughtAmount());
                updateCurrencyAmount(soldAmounts.get(clientId), deal.getSoldCurrency(), deal.getSoldAmount());
                calculatedCcyAmounts.merge(clientId, deal.getUsDollarAmt(), Double::sum);

                log.info("Added earmark: ref={}, client={}, bought={}, sold={}, usd={}",
                        ref, clientId, deal.getBoughtAmount(), deal.getSoldAmount(), deal.getUsDollarAmt());
            }
        }
        case UNEARMARK -> {
            for (var deal : request.getDeals()) {
                String origRef = deal.getOriginalReferenceNumber();
                Earmark old = earmarkMap.remove(origRef);

                if (old != null) {
                    updateCurrencyAmount(boughtAmounts.get(clientId), deal.getBoughtCurrency(), -deal.getBoughtAmount());
                    updateCurrencyAmount(soldAmounts.get(clientId), deal.getSoldCurrency(), -deal.getSoldAmount());
                    calculatedCcyAmounts.merge(clientId, -deal.getUsDollarAmt(), Double::sum);

                    log.info("Removed earmark: ref={}, client={}, bought={}, sold={}, usd={}",
                            origRef, clientId, deal.getBoughtAmount(), deal.getSoldAmount(), deal.getUsDollarAmt());
                } else {
                    log.warn("Tried to unearmark unknown reference '{}'.", origRef);
                }
            }
        }
        case MODIFY_EARMARK, MODIFY_CHECK_EARMARK -> {
            for (var deal : request.getDeals()) {
                String ref = deal.getReferenceNumber();
                String origRef = deal.getOriginalReferenceNumber();
                Earmark old = earmarkMap.get(origRef);

                if (old != null) {
                    updateCurrencyAmount(boughtAmounts.get(clientId), deal.getBoughtCurrency(), -old.boughtAmount);
                    updateCurrencyAmount(soldAmounts.get(clientId), deal.getSoldCurrency(), -old.soldAmount);
                    calculatedCcyAmounts.merge(clientId, -old.calculatedCcyAmount, Double::sum);

                    updateCurrencyAmount(boughtAmounts.get(clientId), deal.getBoughtCurrency(), deal.getBoughtAmount());
                    updateCurrencyAmount(soldAmounts.get(clientId), deal.getSoldCurrency(), deal.getSoldAmount());
                    calculatedCcyAmounts.merge(clientId, deal.getUsDollarAmt(), Double::sum);

                    earmarkMap.remove(origRef);
                    earmarkMap.put(ref, new Earmark(deal.getBoughtAmount(), deal.getSoldAmount(), deal.getUsDollarAmt()));

                    log.info("Modified earmark: oldRef={}, newRef={}, client={}, bought={}, sold={}, usd={}",
                            origRef, ref, clientId, deal.getBoughtAmount(), deal.getSoldAmount(), deal.getUsDollarAmt());
                } else {
                    log.warn("Missing original earmark for modify: '{}'.", origRef);
                    isValid.set(false);
                }
            }
        }
        case CHECK -> log.debug("CHECK request: no earmark action.");
    }
    return isValid.get();
}

private void updateCurrencyAmount(Map<String, Double> currencyMap, String currency, double delta) {
    currencyMap.merge(currency, delta, Double::sum);
}

```
