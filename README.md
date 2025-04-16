```java
import java.util.Arrays;

public record QAPJunitLifeCycleEvent(
    LifeCycleEvent event,
    byte[] exception,
    byte[] logs
) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QAPJunitLifeCycleEvent that)) return false;
        return event == that.event &&
               Arrays.equals(exception, that.exception) &&
               Arrays.equals(logs, that.logs);
    }

    @Override
    public int hashCode() {
        int result = event != null ? event.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(exception);
        result = 31 * result + Arrays.hashCode(logs);
        return result;
    }

    @Override
    public String toString() {
        return "QAPJunitLifeCycleEvent[" +
               "event=" + event + 
               ", exception=" + Arrays.toString(exception) +
               ", logs=" + Arrays.toString(logs) +
               ']';
    }
}
```
