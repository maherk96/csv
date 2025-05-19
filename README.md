```java
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.sync.ReadWriteSynchronizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe manager for updating and saving properties
 * in the shared qap.properties file using Apache Commons Configuration 2.x.
 * <p>
 * Uses FileBasedConfigurationBuilder with a ReadWriteSynchronizer and an external
 * ReentrantReadWriteLock to ensure that update + save is atomic across threads.
 */
public class QAPPropertiesManager {

    private static final Logger log = LoggerFactory.getLogger(QAPPropertiesManager.class);

    private static final ReentrantReadWriteLock LOCK = new ReentrantReadWriteLock();
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
                .setSynchronizer(new ReadWriteSynchronizer())); // ensures thread-safe config access
    }

    /**
     * Resolves the location of the qap.properties file from the classpath.
     *
     * @return the File object pointing to the qap.properties file
     */
    protected File resolvePropertiesFile() {
        return new File(getClass().getClassLoader().getResource("qap.properties").getFile());
    }

    /**
     * Updates a property and immediately saves it to the qap.properties file.
     * This operation is thread-safe and atomic.
     *
     * @param key   the QAPProperties enum key
     * @param value the new value to set
     * @return the current QAPPropertiesManager instance (for chaining if needed)
     */
    public QAPPropertiesManager updateAndSave(QAPProperties key, String value) {
        LOCK.writeLock().lock();
        try {
            PropertiesConfiguration config = builder.getConfiguration();
            log.info("Updating {} to {}", key.getKey(), value);
            config.setProperty(key.getKey(), value);

            log.info("Saving configuration");
            builder.save();
        } catch (ConfigurationException e) {
            log.error("Failed to update or save property", e);
            throw new RuntimeException(e);
        } finally {
            LOCK.writeLock().unlock();
        }
        return this;
    }
}
```
