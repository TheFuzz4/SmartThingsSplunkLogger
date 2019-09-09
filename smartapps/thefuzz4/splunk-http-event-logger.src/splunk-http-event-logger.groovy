/**
* Event Logger For Splunk
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
*   used adrabkin code fix for the length with local logging
* 08-07-2019 Reformatting, removed HOST header from HTTP payload. See full changelog at https://github.com/halr9000/SmartThingsSplunkLogger
*/
definition(
    name: "Splunk HTTP Event Logger",
    namespace: "halr9000",
    author: "Brian Keifer, Jason Hamilton, Hal Rottenberg",
    description: "Log SmartThings events to a Splunk HTTP Event Collector server",
    category: "Convenience",
    iconUrl: "https://cdn.apps.splunk.com/media/public/icons/cdb1e1dc-5a0e-11e4-84b5-0af1e3fac1ba.png",
    iconX2Url: "https://cdn.apps.splunk.com/media/public/icons/cdb1e1dc-5a0e-11e4-84b5-0af1e3fac1ba.png",
    iconX3Url: "https://cdn.apps.splunk.com/media/public/icons/cdb1e1dc-5a0e-11e4-84b5-0af1e3fac1ba.png")

preferences {
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
    section("Scheduled Device Polling") {
        input "do_device_poll", "boolean", title: "Poll devices every 5 mins?", required: true
    }

    section ("Splunk Server") {
        input "use_local", "boolean", title: "Local Server?", required: true
        input "splunk_host", "text", title: "Splunk Hostname/IP", required: true
        input "use_ssl", "boolean", title: "Use SSL?", required: true
        input "splunk_port", "number", title: "Splunk Port", required: true
        input "splunk_token", "text", title: "Splunk Authentication Token", required: true
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
    doSubscriptions()
    if(do_device_poll) {
        runEvery5Minutes(reportStates)
    }
}

// enumerates all devices, converts to JSON output and adds to an array to send to splunk
def reportStates {
    def detailList = []
    alarms.each { detailList.add(deviceToJSON(it)) }
    codetectors.each { detailList.add(deviceToJSON(it)) }
    contacts.each { detailList.add(deviceToJSON(it)) }
    indicators.each { detailList.add(deviceToJSON(it)) }
    modes.each { detailList.add(deviceToJSON(it)) }
    motions.each { detailList.add(deviceToJSON(it)) }
    presences.each { detailList.add(deviceToJSON(it)) }
    relays.each { detailList.add(deviceToJSON(it)) }
    smokedetectors.each { detailList.add(deviceToJSON(it)) }
    switches.each { detailList.add(deviceToJSON(it)) }
    levels.each { detailList.add(deviceToJSON(it)) }
    temperatures.each { detailList.add(deviceToJSON(it)) }
    waterdetectors.each { detailList.add(deviceToJSON(it)) }
    locations.each { detailList.add(deviceToJSON(it)) }
    accelerations.each { detailList.add(deviceToJSON(it)) }
    energymeters.each { detailList.add(deviceToJSON(it)) }
    musicplayers.each { detailList.add(deviceToJSON(it)) }
    lightsensors.each { detailList.add(deviceToJSON(it)) }
    powermeters.each { detailList.add(deviceToJSON(it)) }
    batteries.each { detailList.add(deviceToJSON(it)) }
    buttons.each { detailList.add(deviceToJSON(it)) }
    voltagemeasurements.each { detailList.add(deviceToJSON(it)) }
    lockdevices.each { detailList.add(deviceToJSON(it)) }
    humidities.each { detailList.add(deviceToJSON(it)) }
    
    detailList.unique()
    detailList.each { logToSplunkHEC it }
    
}


// Subscribes to the various Events for a device or Location. The specified handlerMethod will be called when the Event is fired.
// subscribe specification: https://docs.smartthings.com/en/latest/ref-docs/smartapp-ref.html#subscribe
def doSubscriptions() {
    subscribe(alarms,"alarm",alarmHandler)
    subscribe(codetectors,"carbonMonoxideDetector",coHandler)
    subscribe(contacts,"contact", contactHandler)
    subscribe(indicators,"indicator", indicatorHandler)
    subscribe(modes,"locationMode", modeHandler)
    subscribe(motions,"motion", motionHandler)
    subscribe(presences,"presence", presenceHandler)
    subscribe(relays,"relaySwitch", relayHandler)
    subscribe(smokedetectors,"smokeDetector",smokeHandler)
    subscribe(switches,"switch", switchHandler)
    subscribe(levels,"level",levelHandler)
    subscribe(temperatures,"temperature", temperatureHandler)
    subscribe(waterdetectors,"water",waterHandler)
    subscribe(locations,"location",locationHandler)
    subscribe(accelerations, "acceleration", accelerationHandler)
    subscribe(energymeters, "energy", energyHandler)
    subscribe(musicplayers, "music", musicHandler)
    subscribe(lightsensors,"illuminance",illuminanceHandler)
    subscribe(powermeters,"power",powerHandler)
    subscribe(batteries,"battery", batteryHandler)
    subscribe(buttons, "button", buttonHandler)
    subscribe(voltagemeasurements, "voltage", voltageHandler)
    subscribe(lockdevices, "lock", lockHandler)
    subscribe(humidities, "humidity", humidityHandler)
}



// convert any device into a json output
def deviceToJSON(device) {
    def theDevice = device
    def deviceCapabilities = theDevice.capabilities
    def eventString = ""
    eventString += "{\"event\":{\"eventType\":\"scheduledPoll\",\"sourcetype\":\"smartthings\", \"deviceName\":\"${theDevice.label}\", \"deviceId\":\"${theDevice.id}\",\"capabilities\":{"
    deviceCapabilities.each { cap ->
        eventString = eventString + "\"${cap.name}\":{"
        cap.attributes.each { attr ->
            def currentValue = theDevice.currentValue(attr.name)
            eventString = eventString + "\"${attr.name}\":\"${currentValue}\","
        }
        if(eventString[-1] == ",") {
            eventString = eventString.substring(0,eventString.length()-1) + "},"
        } else {
            eventString = eventString + "},"
        }
    }
    if(eventString[-1] == ",") {
        eventString = eventString.substring(0,eventString.length()-1) + "}"
    } else {
        eventString = eventString + "}"
    }

    eventString = eventString + "}}"
    return eventString

}


// converts smartthings events to json
def eventToJSON(evt) {
    def json = ""
    json += "{\"event\":"
    json += "{\"date\":\"${evt.date}\","
    json += "\"name\":\"${evt.name}\","
    json += "\"displayName\":\"${evt.displayName}\","
    json += "\"device\":\"${evt.device}\","
    json += "\"deviceId\":\"${evt.deviceId}\","
    json += "\"value\":\"${evt.value}\","
    json += "\"isStateChange\":\"${evt.isStateChange()}\","
    json += "\"id\":\"${evt.id}\","
    json += "\"description\":\"${evt.description}\","
    json += "\"descriptionText\":\"${evt.descriptionText}\","
    json += "\"installedSmartAppId\":\"${evt.installedSmartAppId}\","
    json += "\"isoDate\":\"${evt.isoDate}\","
    json += "\"isDigital\":\"${evt.isDigital()}\","
    json += "\"isPhysical\":\"${evt.isPhysical()}\","
    json += "\"location\":\"${evt.location}\","
    json += "\"locationId\":\"${evt.locationId}\","
    json += "\"unit\":\"${evt.unit}\","
    json += "\"stSource\":\"${evt.source}\","
    json += "\"sourcetype\":\"smartthings\"}"
    json += "}"

    return json
}


// assumes well-formed HEC json, sends to HEC endpoint item by item
def logToSplunkHEC(json) {
    //log.debug("JSON: ${json}")
    def ssl = use_ssl.toBoolean()
    def local = use_local.toBoolean()
    def http_protocol
    def splunk_server = "${splunk_host}:${splunk_port}"
    def length = json.getBytes().size().toString()
    def msg = parseLanMessage(description)
    def body = msg.body
    def status = msg.status

    // Write data locally using Hub Action (internal LAN IP ok)
    if (local == true) {
        def result = (new physicalgraph.device.HubAction([
            method: "POST",
            path: "/services/collector/event",
            headers: [
                'Authorization': "Splunk ${splunk_token}",
                "Content-Length":"${length}",
                HOST: "${splunk_server}",
                "Content-Type":"application/json",
                "Accept-Encoding":"gzip,deflate"
            ],
            body:json
        ]))
        log.debug result
        sendHubCommand(result); // do it!
        return result
    }
    // Write data via ST cloud (must ensure Splunk HEC IP and port are publicly accessible)
    else {
        //log.debug "Use Remote"
        //log.debug "Current SSL Value ${use_ssl}"
        if (ssl == true) {
            //log.debug "Using SSL"
            http_protocol = "https"
        }
        else {
            //log.debug "Not Using SSL"
            http_protocol = "http"
        }

        def params = [
            uri: "${http_protocol}://${splunk_host}:${splunk_port}/services/collector/event",
            headers: [ 
                'Authorization': "Splunk ${splunk_token}" 
            ],
            body: json
        ]
        log.debug params
        try {
            httpPostJson(params) // do it!
        } catch ( groovyx.net.http.HttpResponseException ex ) {
            log.debug "Unexpected response error: ${ex.statusCode}"
        }
    }

}

// Build JSON object and write it to Splunk HEC
// event specification: https://docs.smartthings.com/en/latest/ref-docs/event-ref.html
// logToSplunkHEC is used for both events and scheduled device polling
def genericHandler(evt) {
    logToSplunkHEC(eventToJSON(evt))
}

// Today all of the subscriptions use the generic handler, but could be customized if needed
def alarmHandler(evt) {
    genericHandler(evt)
}

def coHandler(evt) {
    genericHandler(evt)
}

def indicatorHandler(evt) {
    genericHandler(evt)
}

def presenceHandler(evt) {
    genericHandler(evt)
}

def switchHandler(evt) {
    genericHandler(evt)
}

def smokeHandler(evt) {
    genericHandler(evt)
}

def levelHandler(evt) {
    genericHandler(evt)
}

def contactHandler(evt) {
    genericHandler(evt)
}

def temperatureHandler(evt) {
    genericHandler(evt)
}

def motionHandler(evt) {
    genericHandler(evt)
}

def modeHandler(evt) {
    genericHandler(evt)
}

def relayHandler(evt) {
    genericHandler(evt)
}

def waterHandler(evt) {
    genericHandler(evt)
}

def locationHandler(evt) {
    genericHandler(evt)
}

def accelerationHandler(evt) {
    genericHandler(evt)
}

def energyHandler(evt) {
    genericHandler(evt)
}

def musicHandler(evt) {
    genericHandler(evt)
}
def illuminanceHandler(evt) {
    genericHandler(evt)
}

def powerHandler(evt) {
    genericHandler(evt)
}

def humidityHandler(evt) {
    genericHandler(evt)
}

def batteryHandler(evt) {
    genericHandler(evt)
}

def buttonHandler(evt) {
    genericHandler(evt)
}

def voltageHandler(evt) {
    genericHandler(evt)
}

def lockHandler(evt) {
    genericHandler(evt)
}
