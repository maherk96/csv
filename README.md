The issue was caused by a conflict introduced by Spring Boot, which pulls in Jetty dependencies that are incompatible with the version required by Ambrosia. Ambrosia expects Jetty 11.0.21, but due to a transitive dependency from Spring, Jetty 12.0.14 was being resolved instead.

I initially tried excluding Jetty from Spring Boot using the following approach, but it did not work:

implementation("org.springframework.boot:spring-boot-starter-web") {
    exclude group: "org.eclipse.jetty"
}

I then enforced Jetty 11.0.21 using Gradle’s resolution strategy:

configurations.all {
    resolutionStrategy.eachDependency { details ->
        if (details.requested.group == "org.eclipse.jetty") {
            details.useVersion "11.0.21"
        }
    }
}

This resolves the issue, but it feels like an inelegant solution and may become difficult to maintain in the future, especially if Ambrosia updates its core dependencies without notifying us. I’m happy to keep this approach for now, but let me know if you have a better alternative.
