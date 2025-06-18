```java
@Override
public boolean requestIsBelowLimits(
        CreditCheckRequest creditCheckRequest,
        CreditParams creditParams,
        EarmarkTracker<CreditCheckRequest> earmarkTracker) {

    final AtomicBoolean isBelowLimit = new AtomicBoolean(true);
    final String clientId = creditCheckRequest.getKlNumber() != null ?
            creditCheckRequest.getKlNumber() : creditCheckRequest.getBaseNumber();

    log.debug("[LimitCheck] Starting check for clientId: {}", clientId);

    if (creditParams == null) {
        log.warn("[LimitCheck] No credit params provided, skipping limit check.");
        return true;
    }

    if (!shouldCheckLimit(creditCheckRequest)) {
        log.debug("[LimitCheck] Request type does not require limit check. Skipping.");
        return true;
    }

    calculatedCurrencyAmount = 0;
    boughtAmounts.clear();
    soldAmounts.clear();

    creditCheckRequest.getDeals().forEach(deal -> {
        String boughtCurrency = deal.getBoughtCurrency();
        double boughtAmount = deal.getBoughtAmount();
        String soldCurrency = deal.getSoldCurrency();
        double soldAmount = deal.getSoldAmount();
        double usdAmount = deal.getUsdDollarAmt();

        boughtAmounts.merge(boughtCurrency, boughtAmount, Double::sum);
        soldAmounts.merge(soldCurrency, soldAmount, Double::sum);
        calculatedCurrencyAmount += usdAmount;
    });

    log.debug("[LimitCheck] Aggregated bought amounts: {}", boughtAmounts);
    log.debug("[LimitCheck] Aggregated sold amounts: {}", soldAmounts);
    log.debug("[LimitCheck] Total USD exposure: {}", calculatedCurrencyAmount);

    boughtAmounts.forEach((currency, amount) -> {
        double limit = creditParams.getLimit(currency);
        double earmarked = earmarkTracker.getBoughtAmount(clientId, currency);
        double remaining = limit - earmarked;

        log.debug("[LimitCheck] Bought: currency={}, limit={}, earmarked={}, remaining={}, requested={}",
                currency, limit, earmarked, remaining, amount);

        if (remaining < amount) {
            isBelowLimit.set(false);
            log.warn("[LimitCheck] Bought amount exceeds limit for currency: {}", currency);
        }
    });

    soldAmounts.forEach((currency, amount) -> {
        double limit = creditParams.getLimit(currency);
        double earmarked = earmarkTracker.getSoldAmount(clientId, currency);
        double remaining = limit - earmarked;

        log.debug("[LimitCheck] Sold: currency={}, limit={}, earmarked={}, remaining={}, requested={}",
                currency, limit, earmarked, remaining, amount);

        if (remaining < amount) {
            isBelowLimit.set(false);
            log.warn("[LimitCheck] Sold amount exceeds limit for currency: {}", currency);
        }
    });

    double totalLimit = creditParams.getLimit(calculatedCurrency);
    double earmarkedTotal = earmarkTracker.getCalculatedCcyAmount(clientId);
    double remainingTotal = totalLimit - earmarkedTotal;

    log.debug("[LimitCheck] Total exposure: limit={}, earmarked={}, remaining={}, requested={}",
            totalLimit, earmarkedTotal, remainingTotal, calculatedCurrencyAmount);

    if (remainingTotal < calculatedCurrencyAmount) {
        isBelowLimit.set(false);
        log.warn("[LimitCheck] Total exposure exceeds limit for calculated currency ({}).", calculatedCurrency);
    }

    return isBelowLimit.get();
}

```
