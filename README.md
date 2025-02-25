```groovy
ext.addJavaGenerator = { map ->
    map.putIfAbsent("sourceSetName", "generator")
    map.putIfAbsent("taskName", "runGenerator")
    def generatorTaskName = map.taskName.toString()

    map.putIfAbsent("generatedSourcesSubDir", map.sourceSetName)
    def generatorOutputDir = "$projectDir/build/generated/sources/" + map.generatedSourcesSubDir
    map.putIfAbsent("generatorOutputDir", generatorOutputDir)
    map.putIfAbsent("clearGeneratorOutputDirOnBuild", generatorOutputDir.startsWith("$projectDir/build/"))
    map.putIfAbsent("addGeneratorOutputDirAsGeneratedSourceSetInput", true)
    map.putIfAbsent("generatedSourceSetName", "main")
    map.putIfAbsent("generatedJarTaskName", null)
    map.putIfAbsent("generatedSourcesJarTaskName",
        map.generatedSourceSetName == "main" ? "sourcesJar" : null
    )

    def logHeader = "addJavaGenerator[$generatorTaskName]"
    logger.info "$logHeader: map=$map"

    // 1) Add source sets
    for (sourceSetName in [map.sourceSetName, map.generatedSourceSetName]) {
        if (sourceSets.findByName(sourceSetName) == null) {
            logger.info "$logHeader: adding source set: '$sourceSetName'"
            sourceSets {
                "$sourceSetName" {
                    // You can do more config here if needed
                }
            }
        }
    }

    def generatorSourceSet = sourceSets.findByName(map.sourceSetName)
    def generatedSourceSet = sourceSets.findByName(map.generatedSourceSetName)

    // 2) Add the generator output dir to the generated source set inputs
    if (map.addGeneratorOutputDirAsGeneratedSourceSetInput) {
        logger.info "$logHeader: adding generator output dir to '${generatedSourceSet.name}'"
        generatedSourceSet.java {
            srcDirs += ["$generatorOutputDir/java"]
        }
        generatedSourceSet.resources {
            srcDirs += ["$generatorOutputDir/resources"]
        }
    }

    // 3) Register the runGenerator task
    logger.info("$logHeader: adding task: '$generatorTaskName'")
    tasks.register(generatorTaskName, JavaExec) {

        dependsOn generatorSourceSet.classesTaskName

        classpath = generatorSourceSet.runtimeClasspath

        // For example, if you want to allow specifying these in map:
        if (map.containsKey("mainClass")) {
            mainClass = map.mainClass
        }
        if (map.containsKey("args")) {
            args = map.args
        }
        if (map.containsKey("systemProperties")) {
            systemProperties.putAll(map.systemProperties)
        }

        outputs.dir generatorOutputDir
        outputs.cacheIf { true }

        doFirst {
            if (map.clearGeneratorOutputDirOnBuild) {
                logger.info "$logHeader: deleting output directory: $generatorOutputDir"
                delete generatorOutputDir
            }
        }
    }

    // 4) Wire runGenerator into the generated source set's compile and resources tasks
    tasks.named(generatedSourceSet.compileJavaTaskName) {
        dependsOn generatorTaskName
    }
    tasks.named(generatedSourceSet.processResourcesTaskName) {
        dependsOn generatorTaskName
    }

    // 5) Add generated output to jar (if configured)
    if (map.generatedJarTaskName != null) {
        logger.info "$logHeader: adding generated classes to '${map.generatedJarTaskName}'"
        tasks.named(map.generatedJarTaskName) {
            from generatedSourceSet.output
        }
    }

    // 6) Add generated sources to sourcesJar (if configured)
    if (map.generatedSourcesJarTaskName != null) {
        tasks.named(map.generatedSourcesJarTaskName) {
            dependsOn generatorTaskName
            if (map.generatedSourceSetName != "main") {
                logger.info "$logHeader: adding generated sources to '${map.generatedSourcesJarTaskName}'"
                from "$generatorOutputDir/java"
                from "$generatorOutputDir/resources"
            }
        }
    }
}
```

```groovy
apply from: "$rootDir/gradle/java-generator.gradle"

addJavaGenerator(
    taskName: "generateMessages",
    mainClass: "com.citi.icg.ambrosia.annotation.processing.SbeToolGenerator",
    args: [
        "$projectDir/src/main/java/com/citi/fx/qa/mock/positionmanager/sbe",
        "src/main/resources/position_manager_messages.xml"
    ],
    systemProperties: [
        'sbe.output.dir': "$projectDir/src/main/java/com/citi/fx/qa/mock/positionmanager/sbe",
        'sbe.target.language': 'Java',
        'sbe.validation.stop.on.error': 'true',
        'sbe.validation.xsd': 'src/main/resources/sbe.xsd'
    ]
)
```
