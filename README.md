# rizzle-sdk
This project contains 3 modules named as feed-demo, feed-sdk and network-layer.

# feed-demo
This module demonstrates usage of feed-sdk module in activity, fragment and bottom navigation view.

# feed-sdk
All core logic of FaaS SDK is implemented in this module.

# network-layer
All apis related logic is implemented in this module.


# Generating POM and .aar
1. Generating .aar of feed-sdk module, if not already generated:
   i)   Check if .aar is already present: Open Project view -> feed-sdk -> build ->outputs -> aar -> feed-sdk-debug/release.aar
   ii)  If not present, generate it: Goto Gradle in top right of Android studio -> Android-Faas -> feed-sdk -> Tasks -> build -> build (double click)
   iii) Repeat step i
2. Generating POM: Goto studio terminal and execute
   i)   ./gradlew clean
   ii)  ./gradlew build   
   iii) ./gradlew --console=verbose publishToMavenLocal
3. Find the POM: C:\Users\user_name\.m2\repository\com\domain\name\0.0.1-SNAPSHOT

