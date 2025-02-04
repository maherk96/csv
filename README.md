
```bash
#!/bin/bash

# Ensure an order ID is provided
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <order_id>"
    exit 1
fi

ORDER_ID="$1"

# Find the latest log file in the current directory
LOG_FILE=$(ls -t *.log 2>/dev/null | head -n 1)

# Check if a log file was found
if [ -z "$LOG_FILE" ]; then
    echo "Error: No log files found in the current directory."
    exit 1
fi

echo "Using latest log file: $LOG_FILE"

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
