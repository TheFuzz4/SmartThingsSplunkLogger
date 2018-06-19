/**
* Event Logger For Splunk
* This was originally created by Brian Keifer I modified it to work with the Splunk HTTP Event Collector
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
*/
definition(
name: "Splunk HTTP Event Logger",
namespace: "thefuzz4",
author: "Brian Keifer and Jason Hamilton",
description: "Log SmartThings events to a Splunk HTTP Event Collector server",
category: "Convenience",
iconUrl: "http://apmblog.dynatrace.com/wp-content/uploads/2014/07/Splunk_thumbnail.png",
iconX2Url: "http://apmblog.dynatrace.com/wp-content/uploads/2014/07/Splunk_thumbnail.png",
iconX3Url: "http://apmblog.dynatrace.com/wp-content/uploads/2014/07/Splunk_thumbnail.png")

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
// TODO: subscribe to attributes, devices, locations, etc.
doSubscriptions()
}

def doSubscriptions() {
subscribe(alarms,	"alarm",	alarmHandler)
subscribe(codetectors,	"carbonMonoxideDetector",	coHandler)
subscribe(contacts,	"contact", contactHandler)
subscribe(indicators,	"indicator", indicatorHandler)
subscribe(modes,	"locationMode", modeHandler)
subscribe(motions,	"motion", motionHandler)
subscribe(presences,	"presence", presenceHandler)
subscribe(relays,	"relaySwitch", relayHandler)
subscribe(smokedetectors,	"smokeDetector",	smokeHandler)
subscribe(switches,	"switch", switchHandler)
subscribe(levels,	"level",	levelHandler)
subscribe(temperatures,	"temperature", temperatureHandler)
subscribe(waterdetectors,	"water",	waterHandler)
subscribe(location,	"location",	locationHandler)
subscribe(accelerations, "acceleration", accelerationHandler)
subscribe(energymeters, "energy", energyHandler)
subscribe(musicplayers, "music", musicHandler)
subscribe(lightSensor,	"illuminance",	illuminanceHandler)
subscribe(powermeters,	"power",	powerHandler)
subscribe(batteries,	"battery", batteryHandler)
subscribe(button, "button", buttonHandler)
subscribe(voltageMeasurement, "voltage", voltageHandler)
subscribe(lockDevice, "lock", lockHandler)
subscribe(humidities, "humidity", humidityHandler)
}

def genericHandler(evt) {
/*
log.debug("------------------------------")
log.debug("date: ${evt.date}")
log.debug("name: ${evt.name}")
log.debug("displayName: ${evt.displayName}")
log.debug("device: ${evt.device}")
log.debug("deviceId: ${evt.deviceId}")
log.debug("value: ${evt.value}")
log.debug("isStateChange: ${evt.isStateChange()}")
log.debug("id: ${evt.id}")
log.debug("description: ${evt.description}")
log.debug("descriptionText: ${evt.descriptionText}")
log.debug("installedSmartAppId: ${evt.installedSmartAppId}")
log.debug("isoDate: ${evt.isoDate}")
log.debug("isDigital: ${evt.isDigital()}")
log.debug("isPhysical: ${evt.isPhysical()}")
log.debug("location: ${evt.location}")
log.debug("locationId: ${evt.locationId}")
log.debug("source: ${evt.source}")
log.debug("unit: ${evt.unit}")
*/

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
json += "\"source\":\"${evt.source}\",}"
json += "}"
//log.debug("JSON: ${json}")
def ssl = use_ssl.toBoolean()
def local = use_local.toBoolean()
def http_protocol
def splunk_server = "${splunk_host}:${splunk_port}"
def length = json.getBytes().size().toString()
def msg = parseLanMessage(description)
def body = msg.body
def status = msg.status

if (local == true) {
//sendHubCommand(new physicalgraph.device.HubAction([
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
sendHubCommand(result);
return result
}
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
httpPostJson(params)
} catch ( groovyx.net.http.HttpResponseException ex ) {
log.debug "Unexpected response error: ${ex.statusCode}"
}
}
}

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
