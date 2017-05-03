# Gradle Plugin java-merger

## What is it?

A [Gradle](https://gradle.org/) Plugin to merge all your used java fils into one (using inner classes).

This plugin is designed to be used for online contests like [CodinGame](https://www.codingame.com/).

This plugin is based on the work of Manwe56 : https://github.com/Manwe56/competitive-programming.

## How to use it

### Install plugin

This plugin is heberged on my personnal [Bintray Maven repo](https://bintray.com/gautierlevert/maven).

You can use it in your gradle build script by adding :

```
buildscript {
    repositories {
        jcenter()
        mavenCentral()
        maven { url "http://dl.bintray.com/gautierlevert/maven" }
    }

    dependencies {
        classpath 'org.mybop.gradle:java-merger:0.3'
    }
}
```

Then applying plugin `java-merger`.

```
apply plugin: 'java-merger'
```

Finally to specify the main class name use the config block named `merge` and specify the property `mainClassName`.

```
merge {
    mainClassName = "your.main.ClassName"
}
```

Complete build.gradle result : 

```
buildscript {
    repositories {
        jcenter()
        mavenCentral()
        maven { url "http://dl.bintray.com/gautierlevert/maven" }
    }

    dependencies {
        classpath 'org.mybop.gradle:java-merger:0.3'
    }
}

apply plugin: 'java'
apply plugin: 'java-merger'

sourceCompatibility = 1.8

merge {
    mainClassName = 'org.mybop.codingame.Player'
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    testCompile 'junit:junit:4.12'
    testCompile 'org.mockito:mockito-core:2.7.22'
}
```

### Launch task

To merge your java files, simply launch the gradle task `merge`.

This will produce a file with the same name as your main class name (except the package) in your build directory in folder `merge`.

## More ?

Feel free to test it, share it, tweak it...
You can push issues or submit merge request if you want to participate.

