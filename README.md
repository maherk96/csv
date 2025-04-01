
```java
@Override
public void removeOrder(String orderRef) {
    var liveOrder = liveOrders.remove(orderRef);
    if (liveOrder == null) return; // Order was not found, nothing to do

    // Decrement count
    var symbol = liveOrder.getSymbol();
    var count = liveOrderCountBySymbol.getOrDefault(symbol, 1) - 1;
    if (count <= 0) {
        liveOrderCountBySymbol.remove(symbol);
        liveOrdersByPrice.remove(symbol);
    } else {
        liveOrderCountBySymbol.put(symbol, count);

        // Safely remove from nested structure
        var sideMap = liveOrdersByPrice.get(symbol);
        if (sideMap != null) {
            var priceMap = sideMap.get(liveOrder.getSide());
            if (priceMap != null) {
                priceMap.remove(liveOrder.getPrice());

                // Optional: clean up empty maps to prevent memory leaks
                if (priceMap.isEmpty()) {
                    sideMap.remove(liveOrder.getSide());
                }
                if (sideMap.isEmpty()) {
                    liveOrdersByPrice.remove(symbol);
                }
            }
        }
    }
}
```
