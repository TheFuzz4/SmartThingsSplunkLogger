/**
 * Event Logger For Segment
 * This was originally created by Brian Keifer.
 * Modified by TheFuzz4 (Github)  to work with the Segment HTTP Event Collector
 * Modified by tya (Githb) to work with the Segment HTTP Event Collector
 *
 * Copyright 2015 Brian Keifer
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 *
 * 01-17-2016 Merged code from rlyons20 to splunk lock devices
 * 12-04-2016 Fixed the results so that they not only spit out the results but they still also send it off to splunk like it should
 * 02-16-2016 Added the ability for non-ssl/SSL
 * 05-18-2016 Added the ability to log to splunk over the lan and
 * 10-24-2017 Added the code from Uto to log humidity readings and spelling fixes
 * used adrabkin code fix for the length with local logging
 * 02-17-2022 Forked and adapted to send messages to Segment.com tracking API
 */
definition(
    name: "Segment HTTP Event Logger",
    namespace: "tya",
    author: "Brian Keifer and Jason Hamilton and Ty Alexander",
    description: "Log SmartThings events to a Segment HTTP Event Tracking API",
    category: "Logging",
    iconUrl: "http://fixme.soon/Segment_thumbnail.png",
)

preferences {
    page() {
        section ("Segment Write Key") {
            input "segmentWriteKey", "text", title: "Segment Write Key", required: true
        }
    }
    page() {
        section("Log these presence sensors:") {
            input "presences", "capability.presenceSensor", multiple: true, required: false
        }
        section("Log these switches:") {
            input "switches", "capability.switch", multiple: true, required: false
        }
        section("Log these switch levels:") {
            input "levels", "capability.switchLevel", multiple: true, required: false
        }
        section("Log these motion sensors:") {
            input "motions", "capability.motionSensor", multiple: true, required: false
        }
        section("Log these temperature sensors:") {
            input "temperatures", "capability.temperatureMeasurement", multiple: true, required: false
        }
        section("Log these humidity sensors:") {
            input "humidities", "capability.relativeHumidityMeasurement", multiple: true, required: false
        }
        section("Log these contact sensors:") {
            input "contacts", "capability.contactSensor", multiple: true, required: false
        }
        section("Log these alarms:") {
            input "alarms", "capability.alarm", multiple: true, required: false
        }
        section("Log these indicators:") {
            input "indicators", "capability.indicator", multiple: true, required: false
        }
        section("Log these CO detectors:") {
            input "codetectors", "capability.carbonMonoxideDetector", multiple: true, required: false
        }
        section("Log these smoke detectors:") {
            input "smokedetectors", "capability.smokeDetector", multiple: true, required: false
        }
        section("Log these water detectors:") {
            input "waterdetectors", "capability.waterSensor", multiple: true, required: false
        }
        section("Log these acceleration sensors:") {
            input "accelerations", "capability.accelerationSensor", multiple: true, required: false
        }
        section("Log these energy meters:") {
            input "energymeters", "capability.energyMeter", multiple: true, required: false
        }
        section("Log these music players:") {
            input "musicplayer", "capability.musicPlayer", multiple: true, required: false
        }
        section("Log these power meters:") {
            input "powermeters", "capability.powerMeter", multiple: true, required: false
        }
        section("Log these illuminance sensors:") {
            input "illuminances", "capability.illuminanceMeasurement", multiple: true, required: false
        }
        section("Log these batteries:") {
            input "batteries", "capability.battery", multiple: true, required: false
        }
        section("Log these buttons:") {
            input "button", "capability.button", multiple: true, required: false
        }
        section("Log these voltages:") {
            input "voltage", "capability.voltageMeasurement", multiple: true, required: false
        }
        section("Log these locks:") {
            input "lockDevice", "capability.lock", multiple: true, required: false
        }
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize() {
    subscribe(alarms,	"alarm",	segmentHttpEventLogger)
    subscribe(codetectors,	"carbonMonoxideDetector",	segmentHttpEventLogger)
    subscribe(contacts,	"contact", segmentHttpEventLogger)
    subscribe(indicators,	"indicator", segmentHttpEventLogger)
    subscribe(modes,	"locationMode", segmentHttpEventLogger)
    subscribe(motions,	"motion", segmentHttpEventLogger)
    subscribe(presences,	"presence", segmentHttpEventLogger)
    subscribe(relays,	"relaySwitch", segmentHttpEventLogger)
    subscribe(smokedetectors,	"smokeDetector",	segmentHttpEventLogger)
    subscribe(switches,	"switch", segmentHttpEventLogger)
    subscribe(levels,	"level",	segmentHttpEventLogger)
    subscribe(temperatures,	"temperature", segmentHttpEventLogger)
    subscribe(waterdetectors,	"water",	segmentHttpEventLogger)
    subscribe(location,	"location",	segmentHttpEventLogger)
    subscribe(accelerations, "acceleration", segmentHttpEventLogger)
    subscribe(energymeters, "energy", segmentHttpEventLogger)
    subscribe(musicplayers, "music", segmentHttpEventLogger)
    subscribe(lightSensor,	"illuminance",	segmentHttpEventLogger)
    subscribe(powermeters,	"power",	segmentHttpEventLogger)
    subscribe(batteries,	"battery", segmentHttpEventLogger)
    subscribe(button, "button", segmentHttpEventLogger)
    subscribe(voltageMeasurement, "voltage", segmentHttpEventLogger)
    subscribe(lockDevice, "lock", segmentHttpEventLogger)
    subscribe(humidities, "humidity", segmentHttpEventLogger)
}

def segmentHttpEventLogger(evt) {
    def token = segmentWriteKey.bytes.endoceBase64().toString()
    def params = [
        uri: "https://api.segment.io",
        path: "/v1/track",
        headers: ["Authorization": "basic ${token}"],
        body: ["event": evt],
    ]

    log.debug("Sending event to Segment")
    log.debug(params)
    try {
        httpPostJson(params) { resp ->
            log.debug "response message ${resp}"
        }
    } catch (e) {
        // successful creates come back as 200, so filter for 'Created' and throw anything else
        if (e.toString() != 'groovyx.net.http.ResponseParseException: Created') {
            log.error "Error sending event: $e"
            throw e
        }
    }
}
