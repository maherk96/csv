
```java
@Override
public void removeOrder(String orderRef) {
    var liveOrder = liveOrders.remove(orderRef);
    if (liveOrder == null) return;

    var symbol = liveOrder.getSymbol();
    var side = liveOrder.getSide();
    var price = liveOrder.getPrice();

    // Decrement count
    int updatedCount = liveOrderCountBySymbol.merge(symbol, -1, Integer::sum);
    if (updatedCount <= 0) {
        liveOrderCountBySymbol.remove(symbol);
    }

    // Remove from price map
    var sideMap = liveOrdersByPrice.get(symbol);
    if (sideMap == null) return;

    var priceMap = sideMap.get(side);
    if (priceMap == null) return;

    priceMap.remove(price);

    // Optional: Clean up empty maps
    if (priceMap.isEmpty()) {
        sideMap.remove(side);
    }
    if (sideMap.isEmpty()) {
        liveOrdersByPrice.remove(symbol);
    }
}
```
