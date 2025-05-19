```java
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.sync.ReadWriteSynchronizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Thread-safe manager for reading, updating, and saving properties
 * in the shared qap.properties file using Apache Commons Configuration 2.x.
 * 
 * <p>This class uses FileBasedConfigurationBuilder with a ReadWriteSynchronizer
 * to ensure thread-safe access when tests or modules run in parallel and 
 * modify the same properties file.</p>
 */
public class QAPPropertiesManager {

    private static final Logger log = LoggerFactory.getLogger(QAPPropertiesManager.class);

    private final FileBasedConfigurationBuilder<PropertiesConfiguration> builder;

    /**
     * Constructs a QAPPropertiesManager instance and initializes
     * a thread-safe configuration builder for the qap.properties file.
     */
    public QAPPropertiesManager() {
        Parameters params = new Parameters();
        File file = resolvePropertiesFile();

        this.builder = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
            .configure(params.properties()
                .setFile(file)
                .setSynchronizer(new ReadWriteSynchronizer())); // ensures thread-safety
    }

    /**
     * Resolves the location of the qap.properties file from the classpath.
     *
     * @return the File object pointing to the qap.properties file
     * @throws RuntimeException if the file is not found
     */
    private File resolvePropertiesFile() {
        return new File(getClass().getClassLoader().getResource("qap.properties").getFile());
    }

    /**
     * Updates a property in the qap.properties file.
     * This method is thread-safe and can be used concurrently from multiple threads.
     *
     * @param key   the property key wrapped in a QAPProperties enum or class
     * @param value the new value to assign
     * @return the current QAPPropertiesManager instance for method chaining
     * @throws RuntimeException if there is an error accessing the configuration
     */
    public QAPPropertiesManager updateProperties(QAPProperties key, String value) {
        try {
            PropertiesConfiguration config = builder.getConfiguration();
            log.info("Updating {} to {}", key.getKey(), value);
            config.setProperty(key.getKey(), value);
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * Saves the current state of the configuration back to the qap.properties file.
     * This method is thread-safe and should be called after any updates.
     *
     * @throws RuntimeException if saving the configuration fails
     */
    public void saveConfiguration() {
        try {
            log.info("Saving configuration");
            builder.save();
        } catch (ConfigurationException e) {
            log.error("Error saving configuration", e);
            throw new RuntimeException(e);
        }
    }
}

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Thread safety tests for QAPPropertiesManager using shared file.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class QAPPropertiesManagerThreadSafetyTest {

    private File tempPropertiesFile;

    @BeforeAll
    void setUp() throws Exception {
        Path original = Path.of(getClass().getClassLoader().getResource("qap.properties").toURI());
        Path copy = Files.createTempFile("qap-thread-test-", ".properties");
        Files.copy(original, copy, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        tempPropertiesFile = copy.toFile();
    }

    /**
     * Each thread updates a different key — should pass with no conflicts.
     */
    @Test
    void testConcurrentUpdatesToDifferentKeys() throws InterruptedException, ExecutionException {
        int threadCount = Math.min(5, QAPProperties.values().length);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        List<QAPProperties> selectedKeys = Arrays.asList(QAPProperties.values()).subList(0, threadCount);
        Map<QAPProperties, String> expectedValues = new ConcurrentHashMap<>();

        List<Callable<Void>> tasks = selectedKeys.stream().map(key -> (Callable<Void>) () -> {
            String value = "thread-" + key.name().toLowerCase();
            expectedValues.put(key, value);

            QAPPropertiesManager manager = new QAPPropertiesManager() {
                @Override
                protected File resolvePropertiesFile() {
                    return tempPropertiesFile;
                }
            };

            manager.updateProperties(key, value).saveConfiguration();
            return null;
        }).toList();

        executor.invokeAll(tasks).forEach(f -> {
            try {
                f.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        executor.shutdown();

        // Verify all updates
        PropertiesConfiguration reloaded = new Configurations().properties(tempPropertiesFile);
        expectedValues.forEach((key, expected) -> {
            String actual = reloaded.getString(key.getKey());
            assertEquals(expected, actual, "Mismatch for " + key);
        });
    }

    /**
     * All threads update the same property — demonstrates potential race conditions.
     * This test does not assert value equality because the final value depends on which thread writes last.
     */
    @Test
    void testConcurrentUpdatesToSameKey() throws InterruptedException, ExecutionException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        QAPProperties targetKey = QAPProperties.APPLICATION_NAME;

        List<String> writtenValues = new CopyOnWriteArrayList<>();

        List<Callable<Void>> tasks = IntStream.range(0, threadCount).mapToObj(i -> (Callable<Void>) () -> {
            String value = "conflict-thread-" + i;
            writtenValues.add(value);

            QAPPropertiesManager manager = new QAPPropertiesManager() {
                @Override
                protected File resolvePropertiesFile() {
                    return tempPropertiesFile;
                }
            };

            manager.updateProperties(targetKey, value).saveConfiguration();
            return null;
        }).toList();

        executor.invokeAll(tasks).forEach(f -> {
            try {
                f.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        executor.shutdown();

        // Reload and confirm value is one of the written values
        PropertiesConfiguration reloaded = new Configurations().properties(tempPropertiesFile);
        String finalValue = reloaded.getString(targetKey.getKey());

        assertTrue(writtenValues.contains(finalValue),
            "Final value [" + finalValue + "] should match one of the written values");
        System.out.println("⚠ Final value written to file: " + finalValue);
    }

    @AfterAll
    void cleanUp() {
        if (tempPropertiesFile != null && tempPropertiesFile.exists()) {
            tempPropertiesFile.delete();
        }
    }
}
```
