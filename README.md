```java
public int parseNext(DirectBuffer buffer) {
    final var len = buffer.capacity();
    log.debug("Received buffer: len={}, content='{}'", len, buffer.getStringWithoutLengthAscii(0, len));

    if (len < 5) {
        log.warn("Incomplete buffer: too short to contain valid message length.");
        return 0;
    }

    try {
        int index = 0;

        // Prepend
        final var prepend = ParsingUtil.getCharFromBuffer(buffer, index);
        index += NUM_BYTES_CHAR;
        log.debug("Parsed prepend: '{}'", prepend);

        // Total message length
        final var totalLen = ParsingUtil.getIntFromBuffer(buffer, index);
        index += NUM_BYTES_INT;
        log.debug("Parsed total length: {}", totalLen);

        if (len < totalLen) {
            log.warn("Incomplete buffer: expected length={}, actual={}", totalLen, len);
            return 0;
        }

        // Header length
        final var headerLen = ParsingUtil.getIntFromBuffer(buffer, index);
        index += NUM_BYTES_INT;
        log.debug("Parsed header length: {}", headerLen);

        // Header
        final var header = ParsingUtil.getStringFromBuffer(buffer, index, headerLen);
        index += header.getBytes().length;
        log.debug("Parsed header: {}", header);

        // Body length
        final var bodyLen = ParsingUtil.getIntFromBuffer(buffer, index);
        index += NUM_BYTES_INT;
        log.debug("Parsed body length: {}", bodyLen);

        // Body
        final var body = ParsingUtil.getStringFromBuffer(buffer, index, bodyLen);
        log.debug("Parsed body: {}", body);

        try {
            var creditCheckRequest = creditCheckRequestFactory.createCreditCheckRequest(body);
            creditCheckRequest.setHeader(header);
            creditCheckRequestListener.onCreditCheckRequest(creditCheckRequest);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse credit check body: '{}'", body, e);
        }

    } catch (IndexOutOfBoundsException e) {
        log.error("Failed to parse credit check buffer (len={}): '{}'", len, buffer.getStringWithoutLengthUtf8(0, len), e);
        return 0;
    }

    return 1;
}


```
