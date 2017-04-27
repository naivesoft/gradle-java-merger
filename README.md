# Gradle Plugin java-merger

## What is it?

A [Gradle](https://gradle.org/) Plugin to merge all your used java fils into one (using inner classes).

This plugin is designed to be used for online contests like [CodinGame](https://www.codingame.com/).

### How to use it

Add the following part in your `build.gradle`

```
merge {
    mainClassName = "your.main.ClassName"
}
```
