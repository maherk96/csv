```java
public static String readFeatureDesc(URI feature) {
    try {
        Path path;
        if ("classpath".equals(feature.getScheme())) {
            // Convert classpath URI to an InputStream
            InputStream inputStream = YourClass.class.getClassLoader().getResourceAsStream(feature.getPath());
            if (inputStream == null) {
                return "No Description";
            }
            // Read the input stream as a BufferedReader
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                return reader.lines()
                        .filter(line -> line.trim().startsWith("Feature:"))
                        .map(line -> line.substring("Feature:".length()).trim())
                        .findFirst()
                        .orElse("No Description");
            }
        } else {
            path = Paths.get(feature);
        }

        // If it's a regular file, continue using NIO
        Optional<String> featureLines = Files.lines(path)
                .filter(line -> line.trim().startsWith("Feature:"))
                .findFirst();
        return featureLines
                .map(line -> line.substring("Feature:".length()).trim())
                .orElse("No Description");

    } catch (IOException | NullPointerException e) {
        log.error(e.getMessage());
        return "No Description";
    }
}
```
