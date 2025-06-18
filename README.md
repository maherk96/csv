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

    Map<String, BigDecimal> boughtAmounts = new HashMap<>();
    Map<String, BigDecimal> soldAmounts = new HashMap<>();
    double[] calculatedCurrencyAmount = {0};
    String calculatedCurrency = "USD";

    creditCheckRequest.getDeals().forEach(deal -> {
        String boughtCurrency = deal.getBoughtCurrency();
        BigDecimal boughtAmount = deal.getBoughtAmount();
        String soldCurrency = deal.getSoldCurrency();
        BigDecimal soldAmount = deal.getSoldAmount();
        double usdAmount = deal.getUsdDollarAmt();

        boughtAmounts.merge(boughtCurrency, boughtAmount, BigDecimal::add);
        soldAmounts.merge(soldCurrency, soldAmount, BigDecimal::add);
        calculatedCurrencyAmount[0] += usdAmount;
    });

    log.debug("[LimitCheck] Aggregated bought amounts: {}", boughtAmounts);
    log.debug("[LimitCheck] Aggregated sold amounts: {}", soldAmounts);
    log.debug("[LimitCheck] Total USD exposure: {}", calculatedCurrencyAmount[0]);

    boughtAmounts.forEach((currency, amount) -> {
        BigDecimal limit = creditParams.getLimit(currency);
        BigDecimal earmarked = earmarkTracker.getBoughtAmount(clientId, currency);
        BigDecimal remaining = limit.subtract(earmarked);

        log.debug("[LimitCheck] Bought: currency={}, limit={}, earmarked={}, remaining={}, requested={}",
                currency, limit, earmarked, remaining, amount);

        if (remaining.compareTo(amount) < 0) {
            isBelowLimit.set(false);
            log.warn("[LimitCheck] Bought amount exceeds limit for currency: {}", currency);
        }
    });

    soldAmounts.forEach((currency, amount) -> {
        BigDecimal limit = creditParams.getLimit(currency);
        BigDecimal earmarked = earmarkTracker.getSoldAmount(clientId, currency);
        BigDecimal remaining = limit.subtract(earmarked);

        log.debug("[LimitCheck] Sold: currency={}, limit={}, earmarked={}, remaining={}, requested={}",
                currency, limit, earmarked, remaining, amount);

        if (remaining.compareTo(amount) < 0) {
            isBelowLimit.set(false);
            log.warn("[LimitCheck] Sold amount exceeds limit for currency: {}", currency);
        }
    });

    BigDecimal totalLimit = creditParams.getLimit(calculatedCurrency);
    BigDecimal earmarkedTotal = earmarkTracker.getCalculatedCcyAmount(clientId);
    BigDecimal remainingTotal = totalLimit.subtract(earmarkedTotal);

    log.debug("[LimitCheck] Total exposure: limit={}, earmarked={}, remaining={}, requested={}",
            totalLimit, earmarkedTotal, remainingTotal, calculatedCurrencyAmount[0]);

    if (remainingTotal.doubleValue() < calculatedCurrencyAmount[0]) {
        isBelowLimit.set(false);
        log.warn("[LimitCheck] Total exposure exceeds limit for calculated currency ({}).", calculatedCurrency);
    }

    return isBelowLimit.get();
}
```
