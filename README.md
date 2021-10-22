xbee-api
==
[![Java CI with Gradle](https://github.com/home-climate-control/xbee-api/actions/workflows/gradle.yml/badge.svg)](https://github.com/home-climate-control/xbee-api/actions/workflows/gradle.yml)
[![CodeQL](https://github.com/home-climate-control/xbee-api/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/home-climate-control/xbee-api/actions/workflows/codeql-analysis.yml)

## What?
This is a fork of Andrew Rapp's [xbee-api](https://github.com/andrewrapp/xbee-api) - Java API for communication with XBee/XBee-Pro series 1 (802.15.4) and series 2 (ZB/ZigBee Pro) OEM RF Modules. Code bases diverged somewhere around 2015, and went their separate ways.

## What for?

The primary driver behind this project is the [Home Climate Control](https://github.com/home-climate-control/dz) project.

## Future plans?

Not many, except to port it to Reactive Streams to stay in sync with Home Climate Control's [Reactive Streams overhaul](https://github.com/home-climate-control/dz/milestone/12) - the work is starting right now, see [the /reactive branch](https://github.com/home-climate-control/xbee-api/tree/reactive).

To elaborate a bit, XBee occupies an interesting niche - as of the moment of writing, it is an overcomplicated overpriced solution to a pretty small subset of what [ESP8266](https://en.wikipedia.org/wiki/ESP8266) and [ESP32](https://en.wikipedia.org/wiki/ESP32) offer for a fraction of a price. However, it has an enormous advantage of not belonging to the WiFi monoculture susceptible to common problems - if your WiFi network goes down (and all your ESP* with it), it will stay up, hence its value for fault tolerant systems. And, therefore, this code base will be maintained until there is a viable alternative to XBee as an ecosystem.
