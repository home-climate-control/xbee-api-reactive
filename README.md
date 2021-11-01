xbee-api-reactive
==
[![Java CI with Gradle](https://github.com/home-climate-control/xbee-api-reactive/actions/workflows/gradle.yml/badge.svg)](https://github.com/home-climate-control/xbee-api-reactive/actions/workflows/gradle.yml)
[![CodeQL](https://github.com/home-climate-control/xbee-api-reactive/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/home-climate-control/xbee-api-reactive/actions/workflows/codeql-analysis.yml)
[![SonarCloud](https://github.com/home-climate-control/xbee-api-reactive/actions/workflows/sonarcloud.yml/badge.svg)](https://github.com/home-climate-control/xbee-api-reactive/actions/workflows/sonarcloud.yml)


## What?
This is a fork of Andrew Rapp's [xbee-api](https://github.com/andrewrapp/xbee-api) - Java API for communication with XBee/XBee-Pro series 1 (802.15.4) and series 2 (ZB/ZigBee Pro) OEM RF Modules.

Code bases diverged somewhere around 2015, and went their separate ways. Then, on November 1st 2021 this fork migrated from imperative to reactive, with no backward source compatibility.

## What for?

The primary driver behind this project is the [Home Climate Control](https://github.com/home-climate-control/dz) project.

## Future plans?

Not many. XBee occupies an interesting niche - as of the moment of writing, it is an overcomplicated overpriced solution to a pretty small subset of what [ESP8266](https://en.wikipedia.org/wiki/ESP8266) and [ESP32](https://en.wikipedia.org/wiki/ESP32) offer for a fraction of a price. However, it has an enormous advantage of not belonging to the WiFi monoculture susceptible to common problems - if your WiFi network goes down (and all your ESP* with it), it will stay up, hence its value for fault tolerant systems. And, therefore, this code base will be maintained until there is a viable alternative to XBee as an ecosystem.
