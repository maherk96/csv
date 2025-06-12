```java
sourceSets {
    qapgen {
        java.srcDir 'src/qapgen/java'
    }
}

configurations {
    qapgenImplementation.extendsFrom implementation
    qapgenRuntimeOnly.extendsFrom runtimeOnly
}

task generateQAPEnums(type: JavaExec) {
    group = "build setup"
    description = "Generates QAPTables enum from Oracle DB schema"

    classpath = sourceSets.qapgen.runtimeClasspath
    mainClass = 'com.citi.fx.qa.qap.QAPEnumGenerator'

    doFirst {
        println "Generating QAPTables.java from Oracle DB..."
    }
}

// Ensure QAPEnums are generated before compiling the main code
compileJava.dependsOn generateQAPEnums
```
