```java
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public String readFeatureDesc(URI feature) {
    try {
        List<String> lines;

        if (feature.toString().startsWith("classpath:")) {
            // Extract the classpath resource path
            String resourcePath = feature.toString().replace("classpath:", "");
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);

            if (inputStream == null) {
                return "Feature file not found in classpath";
            }

            // Read lines from classpath resource
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                lines = reader.lines().collect(Collectors.toList());
            }
        } else {
            Path path = Paths.get(feature);

            // Read lines from a regular file
            try (Stream<String> stream = Files.lines(path, StandardCharsets.UTF_8)) {
                lines = stream.collect(Collectors.toList());
            }
        }

        // Extract feature description using Stream API
        String featureDescription = lines.stream()
                .takeWhile(line -> !line.trim().startsWith("Feature:")) // Stop at "Feature:"
                .filter(line -> line.trim().startsWith("#")) // Keep only comment lines
                .map(line -> line.replace("#", "").trim()) // Remove "#" and trim
                .collect(Collectors.joining(" ")); // Concatenate to form description

        return featureDescription.isEmpty() ? "No Description" : featureDescription;

    } catch (IOException | NullPointerException e) {
        log.error("Error reading feature description: " + e.getMessage());
        return "No Description";
    }
}
```
