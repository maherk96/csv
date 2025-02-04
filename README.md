
```bash


#!/bin/bash

# Ensure a log file and order ID are provided
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <log_file> <order_id>"
    exit 1
fi

LOG_FILE="$1"
ORDER_ID="$2"

# Check if the log file exists
if [ ! -f "$LOG_FILE" ]; then
    echo "Error: Log file '$LOG_FILE' not found."
    exit 1
fi

# Grep the initial order ID to find related entries
echo "Searching for entries related to order ID: $ORDER_ID..."

# Find linked IDs from lines containing the initial ID
LINKED_IDS=$(grep "$ORDER_ID" "$LOG_FILE" | grep -Eo "(CQ[a-zA-Z0-9]+|DQ[a-zA-Z0-9]+)" | sort | uniq)

# Add the original order ID to the list of IDs to search for
ALL_IDS="$ORDER_ID"
for ID in $LINKED_IDS; do
    if [ "$ID" != "$ORDER_ID" ]; then
        ALL_IDS="$ALL_IDS|$ID"
    fi
done

# Search for all related log entries
echo "Found related IDs: $LINKED_IDS"
echo "Fetching all related log entries..."
grep -E "($ALL_IDS)" "$LOG_FILE"

exit 0
```
