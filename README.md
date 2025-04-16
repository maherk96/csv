```java
import java.util.Arrays;

public record TestLaunchLogSearchData(
    int testRunID,
    String displayName,
    String methodName,
    String testClassName,
    String testLaunchID,
    byte[] testLogs
) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestLaunchLogSearchData that)) return false;
        return testRunID == that.testRunID &&
                Objects.equals(displayName, that.displayName) &&
                Objects.equals(methodName, that.methodName) &&
                Objects.equals(testClassName, that.testClassName) &&
                Objects.equals(testLaunchID, that.testLaunchID) &&
                Arrays.equals(testLogs, that.testLogs);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(testRunID, displayName, methodName, testClassName, testLaunchID);
        result = 31 * result + Arrays.hashCode(testLogs);
        return result;
    }

    @Override
    public String toString() {
        return "TestLaunchLogSearchData[" +
                "testRunID=" + testRunID +
                ", displayName=" + displayName +
                ", methodName=" + methodName +
                ", testClassName=" + testClassName +
                ", testLaunchID=" + testLaunchID +
                ", testLogs=" + Arrays.toString(testLogs) +
                ']';
    }
}
```
