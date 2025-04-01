addOrder:
Assumed that both liveOrdersByPrice.get(symbol) and .get(side) were already initialised. If this wasn’t the case, attempting to .put() an order into a null map caused a NullPointerException.
If a new order with side BUY came in and no previous BUY orders existed for that symbol, the call to .put(price, orderRef) would throw an exception because the internal map hadn’t been set up yet.

removeOrder:
The method attempted to remove orders from nested maps without checking if those maps existed. If the symbol or side didn’t exist, this led to a NullPointerException. Additionally, cleared empty maps that could affect later logic or memory use.

getToPrice:
Calling .firstKey() on an empty TreeMap led to a NoSuchElementException, which could occur during startup or if no orders had yet been added.
Saw this when price manager failed to start, calling getToPrice() on the GBP/USD order book would fail if no liquidity had been configured yet — since .firstKey() can’t operate on an empty map.

identifyMatchingOrders:
accessed liveOrdersByPrice.get(symbol).get(side), which could be null if no orders had been placed for that symbol and side. 
When matching an incoming SELL order for "EUR/GBP", it tried to stream from headMap(price), but if no SELL orders had been placed, the internal map was null.
