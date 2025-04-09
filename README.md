```groovy
import org.apache.tools.ant.taskdefs.condition.Os

def isWindows = Os.isFamily(Os.FAMILY_WINDOWS)
def rpmFilePath = "$project.buildDir/tibrv.rpm"
def zipFilePath = "$project.buildDir/tibrv.zip"
def binariesPath = file("$project.buildDir/tibrv")

def resetDir(File dir) {
    delete dir
    mkdir dir
}

// Reusable file downloader using plain Groovy
def downloadFile(String url, String destPath) {
    new URL(url).withInputStream { input ->
        new File(destPath).withOutputStream { out -> out << input }
    }
}

task download_tibrv_win64 {
    description = "Downloads TIBCO zip for Windows"
    onlyIf { !file(zipFilePath).exists() }
    doLast {
        println("Downloading TIBCO zip for Windows...")
        downloadFile("https://example.com/tibrv.zip", zipFilePath)
    }
    outputs.file(zipFilePath).withPropertyName("tibrv-zip")
}

task extract_tibrv_win64(type: Copy, dependsOn: download_tibrv_win64) {
    description = "Extracts TIBCO zip for Windows"
    onlyIf { !binariesPath.exists() }
    doFirst {
        mkdir project.buildDir
        resetDir(binariesPath)
    }
    from(zipTree(zipFilePath))
    into binariesPath
    inputs.file(zipFilePath)
    outputs.dir(binariesPath)
}

task download_tibrv_linux {
    description = "Downloads TIBCO RPM for Linux"
    onlyIf { !file(rpmFilePath).exists() }
    doLast {
        println("Downloading TIBCO RPM for Linux...")
        downloadFile("https://example.com/tibrv.rpm", rpmFilePath)
    }
    outputs.file(rpmFilePath).withPropertyName("tibrv-rpm")
}

task extract_tibrv_linux(type: Exec, dependsOn: download_tibrv_linux) {
    description = "Extracts TIBCO RPM for Linux"
    onlyIf { !binariesPath.exists() }
    doFirst {
        mkdir project.buildDir
        resetDir(binariesPath)
    }
    workingDir binariesPath
    commandLine 'sh', '-c', "rpm2cpio ${rpmFilePath} | cpio -idm"
    inputs.file(rpmFilePath)
    outputs.dir(binariesPath)
}

task load_tibrv_binaries {
    description = "Loads TIBCO binaries for current OS"
    dependsOn isWindows ? extract_tibrv_win64 : extract_tibrv_linux
}
```
