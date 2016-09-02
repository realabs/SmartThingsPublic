/* Z-Foscam IP Camera
	 *
	 *
	 * pmjoen@yahoo.com
	 * 20160810
	 *
	*/

def clientVersion() {
    return "3.2.9"
}

metadata {
	definition (name: "Z-Foscam IP Camera", namespace: "pmjoen", author: "Patrick Mjoen") {
		capability "Polling"
		capability "Image Capture"
        capability "Alarm"
        capability "Relay Switch"
        capability "Switch"
        capability "Refresh"
        capability "Motion Sensor"
        capability "Sensor"
        capability "Video Camera"
        capability "Video Capture"
        capability "Configuration"
        
        attribute "alarmStatus", "string"
        attribute "ledStatus",   "string"
        attribute "hubactionMode", "string"
        attribute "cruise1", "string"
        attribute "cruise2", "string"
        attribute "presetA", "string"
        attribute "presetB", "string"
        attribute "presetC", "string"
        attribute "mirrorStatus", "string"
        attribute "flipStatus", "string"
        attribute "cameraType", "string"
        attribute "alarmNotifyType", "string"
        attribute "imageDataJpeg", "string"
    
		command "alarmOn"
		command "alarmOff"
		command "toggleAlarm"

		command "toggleLED"
		command "ledOn"
		command "ledOff"
		command "ledAuto"
        
		command "left"
		command "right"
		command "up"
		command "down"
        
		command "cruisemap1"
		command "cruisemap2"
		command "stopCruise"
        
		command "preset1"
		command "preset2"
		command "preset3"
        
        command "reboot"
        command "checkMotionStatus"
        command "registerMotionCallback", ["string"]
        command "deRegisterMotionCallback"
        command "motionCallbackNotify"
        command "startVideo"
        command "setHiRes"
        command "setLowRes"
	}
    
    preferences {
        input title: "", description: "Foscam Device Handler v${clientVersion()}", displayDuringSetup: true, type: "paragraph", element: "paragraph"
        input title: "", description: "NOTE: For live streaming to work your phone needs to be able to reach your camera directly using the IP Address/URL below", displayDuringSetup: true, type: "paragraph", element: "paragraph"
        input("ip", "string", title:"Camera IP Address/Public Hostname", description: "Camera IP Address or DNS Hostname", required: true, displayDuringSetup: true)
        input("port", "number", title:"Camera Port", description: "Camera Port", defaultValue: "80" , required: true, displayDuringSetup: true)
        input("username", "string", title:"Camera Username (case sensitive)", description: "Camera Username (case sensitive)", required: true, displayDuringSetup: true)
        input("password", "password", title:"Camera Password (case sensitive)", description: "Camera Password (case sensitive)", required: false, displayDuringSetup: true)
        input title: "", description: "If your camera has a separate RTSP port configured then enter it here for live streaming (otherwise it defaults to the Camera Port above). Most cameras that have a separate RTSP port typically use port 554", displayDuringSetup: true, type: "paragraph", element: "paragraph"
        input("rtspport", "number", title:"RTSP Port", description: "RTSP Port", required: false, displayDuringSetup: true)
        input("hdcamera", "bool", title:"Enable this if the camera is a HD model (720p or higher)?", description: "Type of Camera", required: true, displayDuringSetup: true)
        input title: "", description: "SmartTiles MJPEG Streaming\nIf your HD camera supports MJPEG, Enable this option to view the live stream in SmartTiles using this URL: http://IPADDRESS:PORT/cgi-bin/CGIStream.cgi?cmd=GetMJStream&usr=USERNAME&pwd=PASSWORD", displayDuringSetup: true, type: "paragraph", element: "paragraph"
        input("mjpeg", "bool", title:"Enable HD Camera MJPEG Stream", description: "MJPEG Streaming", required: true, displayDuringSetup: true)
        input("mirror", "bool", title:"Mirror", description: "Mirror Image? (Horizontal)?")
        input("flip", "bool", title:"Flip", description: "Flip Image? (Vertical)?")
        input("motionLevel", "enum", title:"Motion Detect -> Sensitivity Level", multiple: false, defaultValue: "Medium", options: ["Lowest","Lower","Low","Medium","High"], description: "Alarm Motion Sensitivity Level", required: true, displayDuringSetup: true)
        input("motionEMail", "bool", title:"Motion Detect -> Send EMail", description: "Send e-Mail when motion is detected", defaultValue: true, required: true, displayDuringSetup: true)
        input("motionSnap", "bool", title:"Motion Detect -> Take Picture", description: "Take a picture when motion is detected", defaultValue: true, required: true, displayDuringSetup: true)
        input("motionRing", "bool", title:"Motion Detect -> Sound Camera Alarm (For HD cameras only)", description: "Sound local ring alarm when motion is detected", defaultValue: true, required: true, displayDuringSetup: true)
        input("motionRecord", "bool", title:"Motion Detect -> Record Video (For HD cameras only)", description: "Record a video when motion is detected", defaultValue: true, required: true, displayDuringSetup: true)
        //input("reArmInterval", "enum", title:"Motion Detect -> Re-Arm Interval (For HD cameras only)", multiple: false, defaultValue: "15s", options: ["5s","6s","7s","8s","9s","10s","11s","12s","13s","14s","15s"], description: "Alarm Motion Snap Interval in seconds", required: true, displayDuringSetup: true)
		input("preset1", "text", title: "Preset 1 (For HD cameras only)", description: "Name of your first preset position", defaultValue: "")
		input("preset2", "text", title: "Preset 2 (For HD cameras only)", description: "Name of your second preset position", defaultValue: "")
		input("preset3", "text", title: "Preset 3 (For HD cameras only)", description: "Name of your third preset position", defaultValue: "")
		input("cruisename1", "text", title: "Cruise Map 1 (For HD cameras only. Non-HD cameras will default to Horizontal.)", description: "Name of your first cruise map", defaultValue: "Horizontal")
		input("cruisename2", "text", title: "Cruise Map 2 (For HD cameras only. Non-HD cameras will default to Vertical.)", description: "Name of your second cruise map", defaultValue: "Vertical")
        input("lightCompensation", "bool", title:"Compensate for sudden light changes (For SD cameras only)", description: "Reduce Motion Alarms due to sudden changes in light", defaultValue: true, required: true, displayDuringSetup: true)
        input("detectionArea", "string", title:"(Advanced) Enter the motion detection area parameters (Optional, leave empty if unsure)", defaultValue: "area0=1023&area1=1023&area2=1023&area3=1023&area4=1023&area5=1023&area6=1023&area7=1023&area8=1023&area9=1023", description: "Enter the parameters to use for motion detection area without the leading or trailing &, leave blank for default full screen detection", required: false, displayDuringSetup: true)
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"summary", type: "generic", width: 6, height: 4){
			tileAttribute ("device.alarmStatus", key: "PRIMARY_CONTROL") {
              attributeState "off", label: "Off", action: "toggleAlarm", icon: "st.camera.dlink-hdpan", backgroundColor: "#FFFFFF", nextState:"..."
              attributeState "on", label: "On", action: "toggleAlarm", icon: "st.camera.dlink-hdpan",  backgroundColor: "#79b821", nextState:"..."
              attributeState "alarm", label: "Motion", action: "toggleAlarm", icon: "st.camera.dlink-hdpan",  backgroundColor: "#53A7C0", nextState:"..."
              attributeState "...", label: "...", action:"", nextState:"..."
            }
            tileAttribute ("device.ledStatus", key: "SECONDARY_CONTROL") {
                attributeState "autoOn", label: "Auto", action: "toggleLED", icon: "st.lights.multi-light-bulb-on", backgroundColor: "#79b821", nextState:"..."
                attributeState "autoOff", label: "Auto", action: "toggleLED", icon: "st.lights.multi-light-bulb-on", backgroundColor: "#9ceaf0", nextState:"..."
                attributeState "on", label: "On", action: "toggleLED", icon: "st.lights.multi-light-bulb-off", backgroundColor: "#79b821", nextState:"..."
                attributeState "off", label: "Off", action: "toggleLED", icon: "st.lights.multi-light-bulb-off", backgroundColor: "#FFFFFF", nextState:"..."
                attributeState "...", label: "...", action:"", nextState:"..."
            }
        }
        
        multiAttributeTile(name: "videoPlayer", type: "videoPlayer", width: 6, height: 4) {
            tileAttribute("device.camera", key: "CAMERA_STATUS") {
				attributeState("on", label: "Active", icon: "st.camera.dlink-hdpan", action: "", backgroundColor: "#79b821", defaultState: true)
				attributeState("off", label: "Inactive", icon: "st.camera.dlink-hdpan", action: "", backgroundColor: "#ffffff")
				attributeState("restarting", label: "Connecting", icon: "st.camera.dlink-hdpan", backgroundColor: "#53a7c0")
				attributeState("unavailable", label: "Click here to connect", icon: "st.camera.dlink-hdpan", action: "", backgroundColor: "#F22000")
			}

			tileAttribute("device.errorMessage", key: "CAMERA_ERROR_MESSAGE") {
				attributeState("errorMessage", label: "", value: "", defaultState: true)
			}

			tileAttribute("device.camera", key: "PRIMARY_CONTROL") {
				attributeState("on", label: "Active", icon: "st.camera.dlink-hdpan", backgroundColor: "#79b821")
				attributeState("off", label: "Inactive", icon: "st.camera.dlink-hdpan", backgroundColor: "#ffffff", defaultState: true)
				attributeState("restarting", label: "Connecting", icon: "st.camera.dlink-hdpan", backgroundColor: "#53a7c0")
				attributeState("unavailable", label: "Click here to connect", icon: "st.camera.dlink-hdpan", backgroundColor: "#F22000")
			}

            tileAttribute ("device.ledStatus", key: "SECONDARY_CONTROL") {
                attributeState "autoOn", label: "Auto", action: "toggleLED", icon: "st.lights.multi-light-bulb-on", backgroundColor: "#79b821", nextState:"..."
                attributeState "autoOff", label: "Auto", action: "toggleLED", icon: "st.lights.multi-light-bulb-on", backgroundColor: "#9ceaf0", nextState:"..."
                attributeState "on", label: "On", action: "toggleLED", icon: "st.lights.multi-light-bulb-off", backgroundColor: "#79b821", nextState:"..."
                attributeState "off", label: "Off", action: "toggleLED", icon: "st.lights.multi-light-bulb-off", backgroundColor: "#FFFFFF", nextState:"..."
                attributeState "...", label: "...", action:"", nextState:"..."
            }

            tileAttribute("device.startLive", key: "START_LIVE") {
				attributeState("live", action: "startVideo", defaultState: true)
			}

			tileAttribute("device.stream", key: "STREAM_URL") {
				attributeState("activeURL", defaultState: true)
			}

			/*tileAttribute("device.profile", key: "STREAM_QUALITY") {
				attributeState("hi", label: "Hi-Res", action: "setHiRes", defaultState: true)
				attributeState("low", label: "Low-Res", action: "setLowRes")
			}*/ // TODO: No profiles for now, lowRes MJPEG is not 100% stable so only use hiRes for now

			/*tileAttribute("device.betaLogo", key: "BETA_LOGO") {
				attributeState("betaLogo", label: "", value: "", defaultState: true)
			}*/
		}
		
        standardTile("alarmStatusA", "device.alarmStatus", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
            state "off", label: "Off", action: "toggleAlarm", icon: "st.security.alarm.clear", backgroundColor: "#FFFFFF", nextState:"..."
            state "on", label: "On", action: "toggleAlarm", icon: "st.security.alarm.clear",  backgroundColor: "#79b821", nextState:"..."
            state "alarm", label: "Motion", action: "toggleAlarm", icon: "st.alarm.alarm.alarm",  backgroundColor: "#53A7C0", nextState:"..."
            state "...", label: "...", action:"", nextState:"..."
        }
        
        carouselTile("cameraDetails", "device.image", width: 4, height: 2) { }

		standardTile("take", "device.image", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "take", label: "Take", action: "Image Capture.take", icon: "st.camera.camera", backgroundColor: "#FFFFFF", nextState:"taking"
			state "taking", label:'Taking', action: "", icon: "st.camera.take-photo", backgroundColor: "#53a7c0"
			state "image", label: "Take", action: "Image Capture.take", icon: "st.camera.camera", backgroundColor: "#FFFFFF", nextState:"taking"
		}

        standardTile("ledStatus", "device.ledStatus", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
          state "autoOn", label: "Auto", action: "toggleLED", icon: "st.lights.multi-light-bulb-on", backgroundColor: "#79b821", nextState:"..."
          state "autoOff", label: "Auto", action: "toggleLED", icon: "st.lights.multi-light-bulb-on", backgroundColor: "#9ceaf0", nextState:"..."
          state "on", label: "On", action: "toggleLED", icon: "st.lights.multi-light-bulb-off", backgroundColor: "#79b821", nextState:"..."
          state "off", label: "Off", action: "toggleLED", icon: "st.lights.multi-light-bulb-off", backgroundColor: "#FFFFFF", nextState:"..."
          state "...", label: "...", action:"", nextState:"..."
        }

//		standardTile("preset1", "device.presetA", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
//			state "preset1", label: '${currentValue}', action: "preset1", icon: ""
//		}

//		standardTile("preset2", "device.presetB", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
//			state "preset2", label: '${currentValue}', action: "preset2", icon: ""
//		}

//		standardTile("preset3", "device.presetC", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
//			state "preset3", label: '${currentValue}', action: "preset3", icon: ""
//		}
        
//		standardTile("cruisemap1", "device.cruise1", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
//			state "cruisemap1", label: '${currentValue}', action: "cruisemap1", icon: ""
//		}

//		standardTile("cruisemap2", "device.cruise2", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
//			state "cruisemap2", label: '${currentValue}', action: "cruisemap2", icon: ""
//		}
 
// 		standardTile("stopcruise", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
//			state "stopcruise", label: "Stop Cruise", action: "stopCruise", icon: ""
//		}

//		standardTile("left", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
//			state "left", label: "left", action: "left", icon: "st.thermostat.thermostat-left"
//		}

//		standardTile("right", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
//			state "right", label: "right", action: "right", icon: "st.thermostat.thermostat-right"
//		}

//		standardTile("up", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
//			state "up", label: "up", action: "up", icon: "st.thermostat.thermostat-up"
//		}

//		standardTile("down", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
//			state "down", label: "down", action: "down", icon: "st.thermostat.thermostat-down"
//		}

//		standardTile("stop", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
//			state "stop", label: "", action: "stopCruise", icon: "st.sonos.stop-btn"
//		}

//        standardTile("refresh", "device.status", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
//        	state "refresh", action:"refresh.refresh", icon:"st.secondary.refresh"
//        }
        
//        standardTile("blank", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
//        	state "blank", label: "", action: "", icon: "", backgroundColor: "#FFFFFF"
//        }
        
//        standardTile("blank2x", "device.image", width: 2, height: 2, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
//        	state "blank", label: "", action: "", icon: "", backgroundColor: "#FFFFFF"
//       }
        
//        standardTile("reboot", "device.image", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
//      		state "reboot", label: "reboot", action: "reboot", icon: "st.quirky.spotter.quirky-spotter-plugged"
//    	}
        
        standardTile("reboot", "device.image", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
      		state "reboot", label: "reboot", action: "reboot", icon: "st.quirky.spotter.quirky-spotter-plugged"
    	}
        
        standardTile("refresh", "device.status", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
        	state "refresh", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main "summary", "videoPlayer"
//        details(["cameraDetails", "take", "ledStatus", "alarmStatusA", "videoPlayer", "blank", "up", "blank", "preset1", "preset2", "preset3", "left", "stop", "right", "cruisemap1", "cruisemap2", "stopcruise", "blank", "down", "blank", "reboot", "blank", "refresh"])
        details(["videoPlayer","cameraDetails", "take", "ledStatus", "alarmStatusA", "refresh", "reboot"])
	}
}

import groovy.json.JsonSlurper

// Milli seconds delay between sending commands
private int delayInterval() {
 return 800
}

def initialize() {
    log.trace "Initialize called settings: $settings"
	try {
		if (!state.init) {
			state.init = true
		}
        response(refresh())
	} catch (e) {
		log.warn "initialize() threw $e"
	}
}

// TODO: For some users, BUG WITH SMARTTHINGS PLATFORM, EVERYTIME A SMARTAPP CALLS A DEVICE FUNCTION, THIS UPDATED FUNCTION IS ALSO CALLED!!! IT ALSO CAUSES RANDOM ISSUES LIKE EVENT STATE BEING LOST FOR SOME USERS
/*def updated() {
	log.trace "Update called settings: $settings"
	try {
		if (!state.init) {
			state.init = true
		}
        response(refresh()) // Get the updates and configure the video streams
	} catch (e) {
		log.warn "updated() threw $e"
	}
}*/

//START VIDEO
// Thank you for the tip @ahndee
mappings {
    path("/getInHomeURL") {
        action:
            [GET: "getInHomeURL"]
    }
    
    path("/getOutHomeURL") {
        action:
            [GET: "getOutHomeURL"]
    }
}

def getInHomeURL() {
    log.trace "Called getInHomeURL, returning $state.uri"
    state.uri ? [InHomeURL: state.uri]: null // return null if it's not initialized otherwise ST app crashes
}

def getOutHomeURL() {
    log.trace "Called getOutHomeURL, returning $state.uri"
    state.uri ? [OutHomeURL: state.uri] : null // return null if it's not initialized otherwise ST app crashes
}

def setHiRes() {
    log.debug "Setting hi resolution stream"
    if (hdcamera) {
        log.debug "Enabling HiRes Stream type 0 for HD Camera"
        hubGet("cmd=setMainVideoStreamType&streamType=0") // TODO: Should we always use stream 0?
        
        log.trace "Using h.264 main stream for high bitrate streaming"
        state.uri = "rtsp://${URLEncoder.encode(username)}:${URLEncoder.encode(password)}@${ip}:${rtspport ?: port}/videoMain" // http://foscam.us/forum/how-to-use-rtsp-and-https-for-hd-cameras-t4926.html, rstp requires URL encoding for special characters in the header
    } else {
        log.trace "Setting up high resolution stream for SD camera"
        state.uri = "http://${ip}:${port}" + "/videostream.cgi?" + "user=${username}&pwd=${password}" + "&resolution=32" // High resolution (640x480) is 32, low is 8 (320x240), don't URL encode the password since it doesnt work here
    }

    sendEvent(name: "profile", value: "hi", displayed: false)
}

private void setSubStreamingMode(mjpegEnable) {
    log.debug "Setting sub streaming mode for Camera (MJPEG mode enables SmartTiles compatibility) : ${mjpegEnable ? "MJPEG" : "h.264"}"
    hubGet("cmd=setSubStreamFormat&format=" + (mjpegEnable ? "1" : "0")) // 1 for MJPEG, 0 for H.264 stream http://foscam.us/forum/how-to-fetch-snapshots-and-mjpeg-stream-on-hd-cameras-t4328.html
}

def setLowRes() {
    log.debug "Setting low resolution stream"
    if (hdcamera) { // The newer HD cameras can operate as MPEG or h.264
        setSubStreamingMode(mjpeg) // Configure h.264 or MJPEG for sub streaming video. MJPEG required for compatibility with SmartTiles
        
        if (mjpeg) {
            log.trace "Using MJPEG for low bitrate streaming"
            state.uri = "http://${ip}:${port}" + "/cgi-bin/CGIStream.cgi?cmd=GetMJStream&" + "usr=${username}&pwd=${password}" // Don't URL encode the password here since it doesn't seem to work for special characters like ! with this special Video Tile
        } else {
            log.trace "Using h.264 sub stream for low bitrate streaming"
            state.uri = "rtsp://${URLEncoder.encode(username)}:${URLEncoder.encode(password)}@${ip}:${rtspport ?: port}/videoSub" // http://foscam.us/forum/how-to-use-rtsp-and-https-for-hd-cameras-t4926.html, rstp requires URL encoding for special characters in the header
        }
    } else { // Older SD cameras use MJPEG by default
        log.trace "Setting up low resolution stream for SD camera"
        state.uri = "http://${ip}:${port}" + "/videostream.cgi?" + "user=${username}&pwd=${password}" + "&resolution=8" // High resolution (640x480) is 32, low is 8 (320x240), don't URL encode the password since it doesnt work here
    }

    sendEvent(name: "profile", value: "low", displayed: false)
}

def startVideo() {
    def hiRes = (device.currentValue("profile") == "low" ? false : true) // default to hi res (since low res has issue with mjpeg)
	log.debug "Starting video streaming with ${hiRes ? "High" : "Low"} profile stream"

    log.trace "Fetching video from: ${state.uri}"
    
    if (!state.uri) {
        refresh() // Initialize the camera
    }
    
	def dataLiveVideo = [
		OutHomeURL  : state.uri, // This appears to be only one used
		InHomeURL   : state.uri, // TODO: What is this??
		ThumbnailURL: "http://cdn.device-icons.smartthings.com/camera/dlink-hdpan@2x.png",
		cookie      : [key: "key", value: "value"]
	]

    log.trace "Video settings: $dataLiveVideo"
    
	def event = [
		name           : "stream",
		value          : groovy.json.JsonOutput.toJson(dataLiveVideo).toString(),
		data		   : groovy.json.JsonOutput.toJson(dataLiveVideo),
		descriptionText: "Starting the live video stream",
		eventType      : "VIDEO",
		displayed      : false,
		isStateChange  : true
	]
    
	sendEvent(event)
}
//END START VIDEO

//TAKE PICTURE
def take() {
	log.debug("Taking Photo")
	sendEvent(name: "hubactionMode", value: "s3", displayed: false)
    if(hdcamera) {
		hubGet("cmd=snapPicture2")
    }
    else {
    	hubGet("/snapshot.cgi?")
    }
}
//END TAKE PICTURE

//SWITCH ACTIONS
def on() {
	log.debug "On requested, enabling monitoring"
    alarmOn()
}

def off() {
	log.debug "Off requested, disabling monitoring"
    alarmOff()
}
//END SWITCH ACTIONS

//ALARM ACTIONS
def both() {
	log.debug "Alarm both requested, enabling monitoring and taking picture"
    take()
    alarmOn()
}

def siren() {
	log.debug "Alarm siren requested, enabling monitoring and taking picture"
    take()
    alarmOn()
}

def strobe() {
	log.debug "Alarm strobe requested, enabling monitoring and taking picture"
    take()
    alarmOn()
}

def toggleAlarm() {
	log.debug "Toggling Alarm"
	if(device.currentValue("alarmStatus") == "off") {
    	alarmOn()
  	}
	else {
    	alarmOff()
	}
}

def alarmOn() {
	log.debug "Enabling Alarm"

	if(hdcamera) {
		delayBetween([hubGet("cmd=setMotionDetectConfig&isEnable=1&snapInterval=1&sensitivity=${getMotionLevel(motionLevel)}&linkage=${getMotionAlarmEvents()}&triggerInterval=${getReArmInterval("15s")}&schedule0=281474976710655&schedule1=281474976710655&schedule2=281474976710655&schedule3=281474976710655&schedule4=281474976710655&schedule5=281474976710655&schedule6=281474976710655&${getDetectionArea(detectionArea)}&1421696056773"), poll()], delayInterval())
    }
    else {
    	delayBetween([hubGet("/set_alarm.cgi?motion_armed=1&motion_sensitivity=${getMotionLevel(motionLevel)}&motion_compensation=${lightCompensation ? "1" : "0"}&mail=${motionEMail ? "1" : "0"}&upload_interval=${motionSnap ? "1" : "0"}&"), poll()], delayInterval())
    }
}

def alarmOff() {
	log.debug "Disabling Alarm"

    if(hdcamera) {
		delayBetween([hubGet("cmd=setMotionDetectConfig&isEnable=0"), poll()], delayInterval())
    }
    else {
    	delayBetween([hubGet("/set_alarm.cgi?motion_armed=0&"), poll()], delayInterval())
    }
    
}
//END ALARM ACTIONS

//LED ACTIONS
//Toggle LED's
def toggleLED() {
	log.debug("Toggle LED")

    if(hdcamera) {
        if((device.currentValue("ledStatus") == "autoOn") || (device.currentValue("ledStatus") == "autoOff")) {
            ledOn()
        } else if(device.currentValue("ledStatus") == "on") {
            ledOff()
        } else {
            ledAuto()
        }
    } else {
        ledAuto() // There is no way to get current status of LED for SD Cameras to keep it in Auto all the time
    }
}

def ledOn() {
    log.debug("LED changed to: on")
    if(hdcamera) {
	    delayBetween([hubGet("cmd=setInfraLedConfig&mode=1"), hubGet("cmd=openInfraLed"), poll()], delayInterval())
    }
    else {
    	delayBetween([hubGet("/decoder_control.cgi?command=95&"), poll()], delayInterval())
    }
}

def ledOff() {
    log.debug("LED changed to: off")
    if(hdcamera) {
    	delayBetween([hubGet("cmd=setInfraLedConfig&mode=1"), hubGet("cmd=closeInfraLed"), poll()], delayInterval())
    }
    else {
    	delayBetween([hubGet("/decoder_control.cgi?command=94&"), poll()], delayInterval())
    }
}

def ledAuto() {
    log.debug("LED changed to: auto")
	if(hdcamera) {
		delayBetween([hubGet("cmd=setInfraLedConfig&mode=0"), poll()], delayInterval())
    }
    else {
    	delayBetween([hubGet("/decoder_control.cgi?command=95&"), poll()], delayInterval())
    }
}
//END LED ACTIONS

//PRESET ACTIONS
def preset1() {
	log.debug("Preset 1 Selected - ${preset1}")
	if(hdcamera) {
		delayBetween([hubGet("cmd=ptzGotoPresetPoint&name=${URLEncoder.encode(preset1)}"), poll()], delayInterval())
    }
    else {
    	delayBetween([hubGet("/decoder_control.cgi?command=31&"), poll()], delayInterval())
    }
}

def preset2() {
	log.debug("Preset 2 Selected - ${preset2}")
	if(hdcamera) {
		delayBetween([hubGet("cmd=ptzGotoPresetPoint&name=${URLEncoder.encode(preset2)}"), poll()], delayInterval())
    }
    else {
    	delayBetween([hubGet("/decoder_control.cgi?command=33&"), poll()], delayInterval())
    }
}

def preset3() {
	log.debug("Preset 3 Selected - ${preset3}")
	if(hdcamera) {
		delayBetween([hubGet("cmd=ptzGotoPresetPoint&name=${URLEncoder.encode(preset3)}"), poll()], delayInterval())
    }
    else {
    	delayBetween([hubGet("/decoder_control.cgi?command=35&"), poll()], delayInterval())
    }
}
//END PRESET ACTIONS

//CRUISE ACTIONS
def cruisemap1() {
	log.debug("Cruise Map 1 Selected - ${cruisename1}")
	if(hdcamera) {
		delayBetween([hubGet("cmd=ptzStartCruise&mapName=${cruisename1}"), poll()], delayInterval())
    }
    else {
    	delayBetween([hubGet("/decoder_control.cgi?command=28&"), poll()], delayInterval())
    }
}

def cruisemap2() {
	log.debug("Cruise Map 2 Selected - ${cruisename2}")
	if(hdcamera) {
		delayBetween([hubGet("cmd=ptzStartCruise&mapName=${cruisename2}"), poll()], delayInterval())
    }
    else {
    	delayBetween([hubGet("/decoder_control.cgi?command=26&"), poll()], delayInterval())
    }
}

def stopCruise() {
	log.debug("Stop Cruise")
	if(hdcamera) {
		hubGet("cmd=ptzStopRun")
    }
    else {
    	delayBetween([hubGet("/decoder_control.cgi?command=29&"), hubGet("/decoder_control.cgi?command=27&")], 200)
    }
}
//END CRUISE ACTIONS

//PTZ CONTROLS
def left() {
	if(hdcamera) { // HD Camera compensates for mirror/flip automatically
        delayBetween([hubGet("cmd=ptzMoveLeft"), hubGet("cmd=ptzStopRun")], 1000)
    }
    else {
    	if(mirror) {
	    	hubGet("/decoder_control.cgi?command=4&onestep=1&")
        }
        else {
        	hubGet("/decoder_control.cgi?command=6&onestep=1&")
        }
    }
}

def right() {
	if(hdcamera) { // HD Camera compensates for mirror/flip automatically
        delayBetween([hubGet("cmd=ptzMoveRight"), hubGet("cmd=ptzStopRun")], 1000)
    }
    else {
    	if(mirror) {
	    	hubGet("/decoder_control.cgi?command=6&onestep=1&")
        }
        else {
        	hubGet("/decoder_control.cgi?command=4&onestep=1&")
        }
    }
}

def up() {
	if(hdcamera) { // HD Camera compensates for mirror/flip automatically
        delayBetween([hubGet("cmd=ptzMoveUp"), hubGet("cmd=ptzStopRun")], 1000)
    }
    else {
    	if(flip) {
	    	hubGet("/decoder_control.cgi?command=2&onestep=1&")
        }
        else {
        	hubGet("/decoder_control.cgi?command=0&onestep=1&")
        }
    }
}

def down() {
	if(hdcamera) { // HD Camera compensates for mirror/flip automatically
        delayBetween([hubGet("cmd=ptzMoveDown"), hubGet("cmd=ptzStopRun")], 1000)
    }
    else {
    	if(flip) {
    		hubGet("/decoder_control.cgi?command=0&onestep=1&")
        }
        else {
        	hubGet("/decoder_control.cgi?command=2&onestep=1&")
        }
    }
}
//END PTZ CONTROLS

//REBOOT
def reboot() {
	log.debug "Rebooting camera"
	if(hdcamera) {
		hubGet("cmd=rebootSystem")
    }
    else {
    	hubGet("/reboot.cgi?&" + getLogin())
    }
}
//END REBOOT

def configure() {
    log.trace "Configuration called"
    
    def hiRes = (device.currentValue("profile") == "low" ? false : true) // default fall back hi resolution since low resolution has issues with mjpeg and ST app compatibility
	log.debug "Configuring video streaming for ${hiRes ? "High" : "Low"} Resolution stream"

    if (mjpeg && hdcamera) {
        log.debug "Enabling Camera MJPEG sub streaming mode for SmartTile compatibility"
        setSubStreamingMode(mjpeg)
    }
    
    if (hiRes) {
        setHiRes()
    } else {
        setLowRes()
    }
}

def refresh() {
	log.trace "Refresh called. Settings -> $settings"
    state.cameraHost = null // Reset it to force a lookup
    configure() // Configure the camera
	poll()
}

def poll() {
	log.trace "Poll called"
    
	//Update the tiles names
    sendEvent(name: "cruise1", value: "${(cruisename1 == null ) ? "" : cruisename1} Cruise", isStateChange: true, displayed: false)
    sendEvent(name: "cruise2", value: "${(cruisename2 == null ) ? "" : cruisename2} Cruise", isStateChange: true, displayed: false)
    sendEvent(name: "presetA", value: "Preset ${(preset1 == null ) ? "" : preset1}", isStateChange: true, displayed: false)
    sendEvent(name: "presetB", value: "Preset ${(preset2 == null ) ? "" : preset2}", isStateChange: true, displayed: false)
    sendEvent(name: "presetC", value: "Preset ${(preset3 == null ) ? "" : preset3}", isStateChange: true, displayed: false)
    sendEvent(name: "cameraType", value: "${hdcamera ? "HD" : "SD"}", displayed: false)
    sendEvent(name: "alarmNotifyType", value: "${hdcamera ? "Pull" : "Push"}", displayed: false) // Only SD camera support Push notifications

    def cmds = [] // we can only have one delayBetween, put them all together in the right order

	//Poll Motion Alarm Status and IR LED Mode and device status
    if (hdcamera) {
        // Get the device, LED and Mirror status first
        cmds << hubGet("cmd=getDevState") // Motion detection/alarm status
        cmds << hubGet("cmd=getInfraLedConfig")
        cmds << hubGet("cmd=getMirrorAndFlipSetting")
        // cmds << hubGet("cmd=getMotionDetectConfig") // not required for now, we have everything we need

		// Enable/Disable Mirror
        if (mirror && (device.currentValue("mirrorStatus") != "1")) {
            log.debug "Enabling video mirroring"
            cmds << hubGet("cmd=mirrorVideo&isMirror=1")
            cmds << hubGet("cmd=getMirrorAndFlipSetting")
        }
        else if (!mirror && (device.currentValue("mirrorStatus") != "0")) {
            log.debug "Disabling video mirroring"
            cmds << hubGet("cmd=mirrorVideo&isMirror=0")
            cmds << hubGet("cmd=getMirrorAndFlipSetting")
        }

		// Enable/Disable Flip
        if (flip && (device.currentValue("flipStatus") != "1")) {
            log.debug "Enabling video flipping"
            cmds << hubGet("cmd=flipVideo&isFlip=1")
            cmds << hubGet("cmd=getMirrorAndFlipSetting")
        }
        else if (!flip && (device.currentValue("flipStatus") != "0")) {
            log.debug "Disabling video flipping"
            cmds << hubGet("cmd=flipVideo&isFlip=0")
            cmds << hubGet("cmd=getMirrorAndFlipSetting")
        }
	}
    else {
        // Get all the settings first
        cmds << hubGet("/get_status.cgi?") // Motion Detection Status
        cmds << hubGet("/get_params.cgi?") // Alarm Status
    	cmds << hubGet("/get_camera_params.cgi?") // Mirror and flip status

		// Enable/Disable Mirror
        def enableMirror = false
        def enableFlip = false
        if (mirror && (device.currentValue("mirrorStatus") != "1")) {
            log.debug "Enabling video mirroring"
            enableMirror = true
        }
        else if (!mirror && (device.currentValue("mirrorStatus") != "0")) {
            log.debug "Disabling video mirroring"
            enableMirror = false
        }

		// Enable/Disable Flip
        if (flip && (device.currentValue("flipStatus") != "1")) {
            log.debug "Enabling video flipping"
            enableFlip = true
        }
        else if (!flip && (device.currentValue("flipStatus") != "0")) {
            log.debug "Disabling video flipping"
            enableFlip = false
        }

		if (enableMirror && enableFlip) {
            cmds << hubGet("/camera_control.cgi?param=5&value=3&")
			cmds << hubGet("/get_camera_params.cgi?")
        } else if (!enableMirror && enableFlip) {
            cmds << hubGet("/camera_control.cgi?param=5&value=1&")
			cmds << hubGet("/get_camera_params.cgi?")
        } else if (enableMirror && !enableFlip) {
            cmds << hubGet("/camera_control.cgi?param=5&value=2&")
			cmds << hubGet("/get_camera_params.cgi?")
        } else if (!enableMirror && !enableFlip) {
            cmds << hubGet("/camera_control.cgi?param=5&value=0&")
			cmds << hubGet("/get_camera_params.cgi?")
        }
    }

    //log.trace "Executing -> ${cmds.inspect()}"
    delayBetween(cmds, delayInterval())
}

private getLogin() {
	if(hdcamera) {
    	return "usr=${URLEncoder.encode(username)}&pwd=${URLEncoder.encode(password)}&"
    }
    else {
    	return "user=${URLEncoder.encode(username)}&pwd=${URLEncoder.encode(password)}"
    }
}

private hubGet(def apiCommand) {
	// Check if we have a hostname and if so convert to IP Address
    if (state.cameraHost != ip) { // check if the IP/Host has changed
        if (!isIPAddress(ip)) {
            //log.trace "Converting hostname $ip to IP Address before continuing"
            state.ipAddress = convertHostnameToIPAddress(ip)
            log.trace "Got IPAddress=${state.ipAddress} for hostname=$ip"
        }
        else {
            state.ipAddress = ip
            log.trace "Using IPAddress=${state.ipAddress}"
        }
        state.cameraHost = ip // We've completed the above successfully, cache it to improve performance
    } else {
        log.trace "Using cached IPAddress=${state.ipAddress}"
    }
    
	//Setting Network Device Id
    def iphex = convertIPtoHex(state.ipAddress)
    def porthex = convertPortToHex(port)
    device.deviceNetworkId = "$iphex:$porthex"
    //log.trace "Device Network Id set to ${iphex}:${porthex}"

	//log.trace("Executing hubaction on " + getHostAddress(state.ipAddress))

    def uri = ""
    if (hdcamera) {
    	uri = "/cgi-bin/CGIProxy.fcgi?" + getLogin() + apiCommand
	}
    else {
    	uri = apiCommand + getLogin()
    }
    
	boolean doHubAction = false
    if (isPublicIPAddress(state.ipAddress)) { // If we are working with a public IP address then use httpGet from ST cloud to public IP, it's faster and more reliable (doesn't depend on loopback)
        log.trace "Sending httpGet command -> http://${getHostAddress(ip)}$uri"
        try {
            httpGet("http://${getHostAddress(ip)}$uri") { response -> parseHttpGetResponse(response) }
        } catch (Exception e) {
            log.warn "Unable to connect to host ${getHostAddress(ip)}, Error: $e"
            log.debug "Falling back to hubAction and retrying command"
            doHubAction = true // lets fall back and retry, hubAction works for both private and public ip addresses
        }
    } else { // If we are using local IP Address, use hubAction, the only way to communicate from hub to Camera.
    	doHubAction = true
    }
    
    if (doHubAction) {
        log.trace "Sending hubAction command -> http://${getHostAddress(state.ipAddress)}$uri"
        def hubAction = new physicalgraph.device.HubAction(
            method: "GET",
            path: uri,
            headers: [HOST:getHostAddress(state.ipAddress)]
        )

        // If we have a picture then extract it and store it and set the hubAction mode accordingly
        if (device.currentValue("hubactionMode") == "s3") {
            hubAction.options = [outputMsgToS3:true]
            sendEvent(name: "hubactionMode", value: "local", displayed: false)
        }
        sendHubCommand(hubAction)
    }
}

def parseHttpGetResponse(response) {
    log.trace "Received response from camera to httpGet, headers=${response.headers.'Content-Type'}, status=$response.status"
    if (response.status == 200) {
        if(response.headers.'Content-Type'.contains("image/jpeg")) { // If we have a picture store it directly
            if(response.data) {
                def image = response.data

                // Send the image to an App who wants to consume it via an event as a Base64 String - NOTE: Disabled for now since ST has put a limitation on this which is causing trouble with the carousel
                //def bytes = image.buf
                //log.debug "JPEG Data Size: ${bytes.size()}"
                //String str = bytes.encodeBase64()
                //sendEvent(name: "imageDataJpeg", value: str, displayed: false, isStateChange: true)
                sendEvent(name: "imageDataJpeg", value: "", displayed: false, isStateChange: false) // Wipe it clean so it empties any old data

                // Now save it to the S3 cloud, so this in the end since it removes the data from the object leaving it empty
                log.info "Saving picture to SmartThings"
                storeImage(getPictureName(), image)
            } else {
                log.warn "Received an empty response from camera, expecting a JPEG image"
            }
        } else { // Otherwise process the camera response codes
            def body = response.data.getText()
            //log.trace "httpGet -> ${body}"
            processResponse(body)
        }
    } else {
        log.error "Error response from host ${getHostAddress(state.ipAddress)}, HTTP Response code: $response.status"
    }
}

//Parse events into attributes (this function is called when we are using hubAction. i.e. for a LAN IP address)
def parse(String description) {
	log.trace "Received response from Camera to hubAction"
    
    def descMap = parseDescriptionAsMap(description)
    //log.trace "${descMap.inspect()}"
    
    // Check if its a picture and process it
	if (descMap["bucket"] && descMap["key"]) {
    	log.info "Saving picture to SmartThings"
		putImageInS3(descMap)
	} else if (descMap["headers"] && descMap["body"]) { // Otherwise check camera response
        def body = new String(descMap["body"].decodeBase64())
	    //log.trace "Body -> ${body}"
		processResponse(body)
	}
}


def parseDescriptionAsMap(description) {
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

def putImageInS3(map) {
    //log.trace "${map.inspect()}"

	try {
		def imageBytes = getS3Object(map.bucket, map.key + ".jpg")

		if(imageBytes)
		{
			def s3ObjectContent = imageBytes.getObjectContent()
			def image = new ByteArrayInputStream(s3ObjectContent.bytes)
            if (image) {
                // Send the image to an App who wants to consume it via an event as a Base64 String
                def bytes = image.buf
                //log.debug "JPEG Data Size: ${bytes.size()}"
                String str = bytes.encodeBase64()
                sendEvent(name: "imageDataJpeg", value: str, displayed: false, isStateChange: true)

                // Now save it to the S3 cloud, so this in the end since it removes the data from the object leaving it empty
                log.info "Saving picture to SmartThings"
                storeImage(getPictureName(), image)
            } else {
                log.warn "No picture returned, nothing to save"
            }
        } else {
            log.warn "No picture content, nothing to save"
        }
	}
	catch(Exception e) {
		log.error "Error processing image, Error: $e"
	}
	finally {
		//Explicitly close the stream
		if (s3ObjectContent) { s3ObjectContent.close() }
	}
}

// Process the response from the camera
def processResponse(String body) {
    if(hdcamera) {
        def statusVars = new XmlSlurper(false,false,true).parseText(body?.trim()?.replaceFirst("^([\\W]+)<","<")) // Some cameras create a malformed XML so ignore BOM's and extra characters before the start of the XML
        //log.trace "Vars -> ${statusVars}"

        // Check the result value for the command sent
        switch (statusVars.result) {
            case "0":
                //log.warn "Camera responded with result ${statusVars.result} -> ALL's GOOD, THIS IS JUST A DEBUG" // Do nothing, this is good!
                break;

            case "-1":
                log.error "Camera responded with result ${statusVars.result} -> CGI request string format error, your Username or Password may contain invalid character. The only allowed special characters are ~!@^*()_"
                break;

            case "-2":
                log.error "Camera responded with result ${statusVars.result} -> Invalid username or password. Check your Username and Password (BOTH are case sensitive)"
                break;

            case "-3":
                log.error "Camera responded with result ${statusVars.result} -> Access denied"
                break;

            case "-4":
                log.error "Camera responded with result ${statusVars.result} -> CGI execution failed"
                break;

            case "-5":
                log.error "Camera responded with result ${statusVars.result} -> Timeout"
                break;

            case "-6":
                log.error "Camera responded with result ${statusVars.result} -> Reserved error"
                break;

            case "-7":
                log.error "Camera responded with result ${statusVars.result} -> Unknown error"
                break;

            case "-8":
                log.error "Camera responded with result ${statusVars.result} -> Reserved error"
                break;

            default:
                log.error "Camera responded with result ${statusVars.result} -> Unknown error"
                break;
        }

        def motionAlarm = "$statusVars.motionDetectAlarm" // $statusVars.isEnable is used with getMotionDetectConfig
        def ledM = "$statusVars.mode"
        def ledS = "$statusVars.infraLedState"
        def flipS = "$statusVars.isFlip"
        def mirrorS = "$statusVars.isMirror"

        // Get mirror and flip settings
        if (mirrorS?.trim() != "") {
            log.info "Polled: Mirror status $mirrorS"
            sendEvent(name: "mirrorStatus", value: mirrorS, displayed: false)
        }
        if (flipS?.trim() != "") {
            log.info "Polled: Flip status $flipS"
            sendEvent(name: "flipStatus", value: flipS, displayed: false)
        }

        //Get Motion Alarm Status
        if(motionAlarm == "0") {
            log.info("Polled: Motion Alarm Off")
            sendEvent(name: "alarmStatus", value: "off")
            sendEvent(name: "alarm", value: "off")
            sendEvent(name: "switch", value: "off")
            sendEvent(name: "motion", value: "inactive", descriptionText: "$device.displayName motion stopped")
        }
        else if(motionAlarm == "1") {
            log.info("Polled: Motion Alarm On")
            sendEvent(name: "alarmStatus", value: "on")
            sendEvent(name: "alarm", value: "both")
            sendEvent(name: "switch", value: "on")
            sendEvent(name: "motion", value: "inactive", descriptionText: "$device.displayName motion stopped")
        }
        else if(motionAlarm == "2") {
            log.info("Polled: Motion Alarm Alert!!")
            sendEvent(name: "alarmStatus", value: "alarm")
            sendEvent(name: "alarm", value: "both")
            sendEvent(name: "switch", value: "on")
            sendEvent(name: "motion", value: "active", descriptionText: "$device.displayName detected motion")
        }

        //Get IR LED Mode
        if(ledM == "0") {
            log.info("Polled: LED Mode Auto")
            state.ledMode = "auto" // this is an internal variable to track the LED mode
        }
        else if(ledM == "1") {
            log.info("Polled: LED Mode Manual")
            state.ledMode = "manual" // this is an internal variable to track the LED mode
        }

        //Get IR LED Status
        //log.trace "Mode " + state.ledMode + " State " + state.ledState
        if(ledS == "1") {
            log.info("Polled: LED On")
            state.ledState = "on" // this is an internal variable to track the LED state
        }
        else if(ledS == "0") {
            log.info("Polled: LED Off")
            state.ledState = "off" // this is an internal variable to track the LED state
        }

        //Update LED Status
        if (state.ledState == "on") {
            if (state.ledMode == "auto") {
                state.ledState = "reset"
                state.ledMode = "reset"
                sendEvent(name: "ledStatus", value: "autoOn", displayed: false)
                log.trace "ledStatus " + device.currentValue("ledStatus")
            }
            else if (state.ledMode == "manual") {
                state.ledState = "reset"
                state.ledMode = "reset"
                sendEvent(name: "ledStatus", value: "on", displayed: false)
                log.trace "ledStatus " + device.currentValue("ledStatus")
            }
        }
        else if (state.ledState == "off") {
            if (state.ledMode == "auto") {
                state.ledState = "reset"
                state.ledMode = "reset"
                sendEvent(name: "ledStatus", value: "autoOff", displayed: false)
                log.trace "ledStatus " + device.currentValue("ledStatus")
            }
            else if (state.ledMode == "manual") {
                state.ledState = "reset"
                state.ledMode = "reset"
                sendEvent(name: "ledStatus", value: "off", displayed: false)
                log.trace "ledStatus " + device.currentValue("ledStatus")
            }
        }
    } else {
        // Check for an error in the result value of the command sent
        if (body.find("401 Unauthorized")) {
            log.error "Camera responded with an 401 Unauthorized error. Check you Username and Password (BOTH are case sensitive). Error -> ${body}"
            return
        } else if (body.find("illegal params")) {
            log.error "Camera responded with an error. Likely caused by an invalid Username or Password, check you Username and Password (BOTH are case sensitive). Error -> ${body}"
            return
        }

        // Don't update events here since often the order of commands get mixed up, capture the states first
        if(body.find("alarm_motion_armed=0")) {
            log.info("Polled: Motion Alarm Off")
            state.sdAlarmArmed = "Off"
        }
        else if(body.find("alarm_motion_armed=1")) { // only check for "on" status when alarm is not active
            log.info("Polled: Motion Alarm On")
            state.sdAlarmArmed = "On"
        }

        if(body.find("alarm_status=1")) { // Check for active alarm
            log.info("Polled: Motion Alarm Alert!!")
            state.sdAlarmStatus = "Alarm"
        } else if(body.find("alarm_status=0")) {
            state.sdAlarmStatus = "None"
        }

        // Check our state now and update the events
        log.trace "Motion: ${state.sdAlarmStatus}, Armed: ${state.sdAlarmArmed}"
        if (state.sdAlarmArmed == "Off") { // First priority, if we're in off state then turn it off
            sendEvent(name: "alarmStatus", value: "off")
            sendEvent(name: "alarm", value: "off")
            sendEvent(name: "switch", value: "off")
            sendEvent(name: "motion", value: "inactive", descriptionText: "$device.displayName motion stopped")
            state.sdAlarmStatus = "None" // Since they are 2 different registers we can have have Alarm on but monitoring off, force Alarm off if monitoring is off
        } else if ((state.sdAlarmArmed == "On") && (state.sdAlarmStatus == "Alarm")) { // Next priority Alarm 
            sendEvent(name: "alarmStatus", value: "alarm")
            sendEvent(name: "alarm", value: "both")
            sendEvent(name: "switch", value: "on")
            sendEvent(name: "motion", value: "active", descriptionText: "$device.displayName detected motion")
        } else if ((state.sdAlarmArmed == "On") && (state.sdAlarmStatus == "None")) { // If not in alarm check for On
            sendEvent(name: "alarmStatus", value: "on")
            sendEvent(name: "alarm", value: "both")
            sendEvent(name: "switch", value: "on")
            sendEvent(name: "motion", value: "inactive", descriptionText: "$device.displayName motion stopped")
        }

        if(body.find("alarm_http=0")) {
            log.info("Polled: Motion Alarm Callback Notification Disabled")
        } else if(body.find("alarm_http=1")) {
            def callbackURL = (body =~ ".*?alarm_http_url='(.*?)'")[0][1]
            log.info("Polled: Motion Alarm Callback Notification Enabled with URL $callbackURL")
        }

        // Get mirror and flip settings
        if (body.find("flip=0")) {
            log.info "Polled: Flip Status 0, Mirror Status 0"
            sendEvent(name: "mirrorStatus", value: "0", displayed: false)
            sendEvent(name: "flipStatus", value: "0", displayed: false)
        } else if (body.find("flip=1")) {
            log.info "Polled: Flip status 1, Mirror Status 0"
            sendEvent(name: "mirrorStatus", value: "0", displayed: false)
            sendEvent(name: "flipStatus", value: "1", displayed: false)
        } else if (body.find("flip=2")) {
            log.info "Polled: Flip status 0, Mirror status 1"
            sendEvent(name: "mirrorStatus", value: "1", displayed: false)
            sendEvent(name: "flipStatus", value: "0", displayed: false)
        } else if (body.find("flip=3")) {
            log.info "Polled: Flip status 1, Mirror status 1"
            sendEvent(name: "mirrorStatus", value: "1", displayed: false)
            sendEvent(name: "flipStatus", value: "1", displayed: false)
        }

        //The API does not provide a way to poll for LED status on 8xxx series at the moment, just keep it in autoOff mode
        sendEvent(name: "ledStatus", value: "autoOff", isStateChange: true, displayed: false)
    }
}

def checkMotionStatus() {
	log.debug "Checking motion alarm status"

	//Poll Motion Alarm Status
    if (hdcamera) {
        hubGet("cmd=getDevState") // Motion/Alarm status
    }
    else {
    	delayBetween([hubGet("/get_status.cgi?"), hubGet("/get_params.cgi?")], delayInterval()) // Motion Detection Status, Alarm Status
    }
}

def registerMotionCallback(callbackURL)
{
	if (device.currentValue("alarmNotifyType") == "Pull") {
    	log.error "This cameras doesn't support callback URL, this should not be called"
    } else {
		log.debug "Registering motion detection callback URL -> $callbackURL"
        delayBetween([hubGet("/set_alarm.cgi?http=1&http_url=$callbackURL&"), poll()], delayInterval())
    }
}

def deRegisterMotionCallback()
{
	if (device.currentValue("alarmNotifyType") == "Pull") {
    	log.error "This camera doesn't support deregistering callback URL, this should not be called"
    } else {
		log.debug "DeRegistering motion detection callback"
        delayBetween([hubGet("/set_alarm.cgi?http=0&"), poll()], delayInterval())
    }
}

def motionCallbackNotify()
{
    log.info("Callback Notify: Motion Alarm Alert!!")
    sendEvent(name: "alarmStatus", value: "alarm")
    sendEvent(name: "alarm", value: "both")
    sendEvent(name: "switch", value: "on")
    sendEvent(name: "motion", value: "active", descriptionText: "$device.displayName detected motion")
    delayBetween(["delay 1000", poll()], 10000) // Polling will set the mode back to on or off (current mode), wait 10 seconds before turning off the alarm
}

def testTrace() {
	log.trace "*** Calling test trace ***"
}

private getPictureName() {
  def pictureUuid = java.util.UUID.randomUUID().toString().replaceAll('-', '')
  "image" + "_$pictureUuid" + ".jpg"
}

private getHostAddress(host) {
	return "${host}:${port}"
}

private String convertIPtoHex(ipAddress) {
	// Check for valid IPv4 address, ST doesn't support IPv6 yet
    if (!isIPAddress(ipAddress)) {
        log.error "Invalid IP Address $ipAddress, check your settings!"
    }
    
	try {
        String hex = ipAddress.tokenize( '.' ).collect {  String.format('%02x', it.toInteger() ) }.join()  // thanks to @pstuart
        return hex
    } catch (Exception e) {
        log.error "Invalid IP Address $ipAddress, check your settings! Error: $e"
    }
}

private String convertPortToHex(port) {
	if (!port || (port == 0)) {
    	log.error "Invalid port $port, check your settings!"
    }
    
    try {
        String hexport = port.toString().format('%04x', port.toInteger() )   // thanks to @pstuart
        return hexport
    } catch (Exception e) {
        log.error "Invalid port $port, check your settings! Error: $e"
    }
}

private String getDetectionArea(area) {
	log.trace "User entered detection:$area"
    
    if (area?.trim()) {
    	// Remove any trailing or leading & if entered accidentally
        area = area.replaceAll("^&+", "") // Leading &
    	area = area.replaceAll("&+\$", "") // Trailing &
        return area
    } else {
    	return "area0=1023&area1=1023&area2=1023&area3=1023&area4=1023&area5=1023&area6=1023&area7=1023&area8=1023&area9=1023" // Default is full frame detection
    }
}

private String getMotionLevel(motion) {
	log.trace "Motion Level is $motion"

	String retVal = ""
    
    switch (motion) {
    	case "Lowest":
        	if (hdcamera)
        		retVal = "4"
        	else
                retVal = "9"
            break
            
    	case "Lower":
        	if (hdcamera)
        		retVal = "3"
        	else
                retVal = "7"
            break
            
    	case "Low":
        	if (hdcamera)
        		retVal = "0"
        	else
                retVal = "5"
            break
            
    	case "Medium":
        	if (hdcamera)
        		retVal = "1"
        	else
                retVal = "3"
            break
            
    	case "High":
        	if (hdcamera)
        		retVal = "2"
        	else
                retVal = "0"
            break
            
		default:
        	log.warn "Invalid motion level $motion, check your settings, reverting to default"
            
        	if (hdcamera)
        		retVal = "1"
        	else
                retVal = "3"
            break
    }
    
    log.trace("Motion value is $retVal")
    
    return retVal
}

private String getReArmInterval(interval) {
	log.trace("Trigger/ReArm internal is $interval")

	String retVal = ""
    
    switch (interval) {
    	case "5s":
        	retVal = "0"
            break
            
    	case "6s":
        	retVal = "1"
            break
            
    	case "7s":
        	retVal = "2"
            break
            
    	case "8s":
        	retVal = "3"
            break
            
    	case "9":
        	retVal = "4"
            break
            
    	case "10s":
        	retVal = "5"
            break
            
    	case "11s":
        	retVal = "6"
            break
            
    	case "12s":
        	retVal = "7"
            break
            
    	case "13s":
        	retVal = "8"
            break
            
    	case "14s":
        	retVal = "9"
            break
            
    	case "15s":
        	retVal = "10"
            break
            
		default:
        	log.warn "Invalid trigger interval $interval, check your settings, reverting to default 10"
            
        	retVal = "10"
            break
    }

	log.trace("Snap interval value is $retVal")
    
    return retVal
}

private String getMotionAlarmEvents() {
	int ret = 0 // Default nothing to enable
    
    if (motionRing) {
    	ret |= 0x1 // Enable local ringer
	    log.trace "Enabled motion ringer, $ret"
    }
    
    if (motionEMail) {
    	ret |= 0x2 // Enable sending eMails
	    log.trace "Enabled motion eMail, $ret"
    }
    
    if (motionSnap) {
    	ret |= 0x4 // Enable taking pictures
	    log.trace "Enabled motion snap pictures, $ret"
    }
    
    if (motionRecord) {
    	ret |= 0x8 // Enabling taking a video recording
	    log.trace "Enabled motion video recording, $ret"
    }
    
    log.trace "Motion alarm config value $ret"

    return ret.toString()
}

private boolean isIPAddress(String ipAddress)
{
    try
    {
         String[] parts = ipAddress.split("\\.")
         if (parts.length != 4) {
         	return false
         }
         for (int i = 0; i < 4; ++i)
         {
             int p = Integer.parseInt(parts[i])
             if (p > 255 || p < 0) {
             	return false
             }
         }
         return true
    } catch (Exception e)
    {
        return false
    }
}

private String convertHostnameToIPAddress(hostname) {
	def params = [
      uri: "http://api.myiponline.net/dig?url=" + hostname // thanks @cosmicpuppy
    ]

	def retVal = null
    
	try {
    	retVal = httpGet(params) { response ->
			log.trace "Request was successful, data=$response.data, status=$response.status"
            for(result in response.data) {
	            for(subresult in result) {
                    if (subresult.type == "A") {
                        //log.trace("Hostname $subresult.host has IP Address $subresult.ip")
                        return subresult.ip
                    }
                }
            }
        }
    } catch (Exception e) {
    	log.warn("Unable to convert hostname to IP Address, Error: $e")
    }
    
    return retVal
}

// Check if an ipAddress is a public ip address
private boolean isPublicIPAddress(String ipAddress) {
    try {
    	// Check for private IP Addresses
        // 0.255.255.255  (0/8 prefix) (all local)
        // 127.255.255.255  (127/8 prefix) (loopback)
        // 10.255.255.255  (10/8 prefix)
        // 172.31.255.255  (172.16/12 prefix)
        // 192.168.255.255 (192.168/16 prefix)
        // 169.254.255.255 (169.254/16 prefix) (link local)
        // Between 224.0.0.1 and 239.255.255.255 (multicast addresses)
    	if (ipAddress.find(/(^0\.)|(^127\.)|(^10\.)|(^172\.1[6-9]\.)|(^172\.2[0-9]\.)|(^172\.3[0-1]\.)|(^192\.168\.)|(^169\.254\.)|(^22[4-9]\.)|(^23[0-9]\.)/)) {
            log.trace "IPAddress $ipAddress is a private IP Address"
            return false
        } else {
            log.trace "IPAddress $ipAddress is a public IP Address"
            return true
        }
    } catch (Exception e) {
    	log.error "Invalid IPAddress $ipAddress"
        return false
    }
}