/**
 *  
 *  Monoprice 11990 Dual Relay Module
 *
 *  Copyright 2015 Justin Ellison
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *	Device Type supporting all the feautres of the Monoprice device including both switches, with real-time status
 *  of both switch 1 and 2.
 *
 *  Special thanks to Eric Maycock for doing the bulk of the work with the Philio PAN04 module, without his work
 *  this device type wouldn't exist.
 *	
 */
 

preferences {
  input ("delayMillis", "number", title: "Command delay in ms", 
    description: "Time in milliseconds to delay sending multiple commands.", defaultValue: 0,
    required: false, range: "0..5000")
}

metadata {
    definition (name: "Monoprice 11990 Dual Relay Module", namespace: "justintime", author: "Justin Ellison") {
        capability "Polling"
        capability "Refresh"
        capability "Switch"

        attribute "switch1", "string"
        attribute "switch2", "string"

        command "on1"
        command "off1"
        command "on2"
        command "off2"

        fingerprint deviceId: "0x1001", inClusters: "0x5E, 0x86, 0x72, 0x5A, 0x85, 0x59, 0x73, 0x25, 0x20, 0x27, 0x71, 0x2B, 0x2C, 0x75, 0x7A, 0x60, 0x32, 0x70"
    }

    simulator {
        status "on": "command: 2003, payload: FF"
        status "off": "command: 2003, payload: 00"

        reply "2001FF,delay 100,2502": "command: 2503, payload: FF"
        reply "200100,delay 100,2502": "command: 2503, payload: 00"
    }

    tiles {
        standardTile("switch1", "device.switch1",canChangeIcon: true) {
            state "on", label: "switch1", action: "off1", icon: "st.switches.switch.on", backgroundColor: "#79b821"
            state "off", label: "switch1", action: "on1", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
        }
        standardTile("switch2", "device.switch2",canChangeIcon: true) {
            state "on", label: "switch2", action: "off2", icon: "st.switches.switch.on", backgroundColor: "#79b821"
            state "off", label: "switch2", action: "on2", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
        }
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main(["switch1", "switch2"])
        details(["switch1","switch2","refresh"])
    }
}

def parse(String description) {
    log.debug "Parsing '${description}'"
    def result = []
    def cmd = zwave.parse(description)
    if (cmd) {
        result += zwaveEvent(cmd)
        log.debug "Parsed ${cmd} to ${result.inspect()}"
    } else {
        log.debug "Non-parsed event: ${description}"
    }
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    def result
    if (cmd.value == 0) {
        result = createEvent(name: "switch", value: "off")
    } else {
        result = createEvent(name: "switch", value: "on")
    }
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    sendEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
    def result = []
    result << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format()
    result << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:2).format()
    response(delayBetween(result, settings.delayMillis)) // returns the result of reponse()
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCapabilityReport cmd) {
    log.debug "multichannelv3.MultiChannelCapabilityReport $cmd"
    if (cmd.endPoint == 2 ) {
        def currstate = device.currentState("switch2").getValue()
        if (currstate == "on")
        	sendEvent(name: "switch2", value: "off", isStateChange: true, display: false)
        else if (currstate == "off")
        	sendEvent(name: "switch2", value: "on", isStateChange: true, display: false)
    }
    else if (cmd.endPoint == 1 ) {
        def currstate = device.currentState("switch1").getValue()
        if (currstate == "on")
        sendEvent(name: "switch1", value: "off", isStateChange: true, display: false)
        else if (currstate == "off")
        sendEvent(name: "switch1", value: "on", isStateChange: true, display: false)
    }
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def map = [ name: "switch$cmd.sourceEndPoint" ]

	if (cmd.commandClass == 37){
        if (cmd.parameter == [0]) {
            map.value = "off"
        }
        if (cmd.parameter == [255]) {
            map.value = "on"
        }
        createEvent(map)
    }
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    // This will capture any commands not handled by other instances of zwaveEvent
    // and is recommended for development so you can see every command the device sends
    return createEvent(descriptionText: "${device.displayName}: ${cmd}")
}

def refresh() {
    log.debug "Executing 'refresh'"
	def cmds = []
	cmds << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format()
    cmds << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:2).format()
	delayBetween(cmds, settings.delayMillis)
}

def poll() {
    log.debug "Executing 'poll'"
	delayBetween([
    	zwave.switchBinaryV1.switchBinaryGet().format(),
    	zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
	], settings.delayMillis)
}

def on1() {
    log.debug "Executing 'on1' with delay of ${settings.delayMillis}"
    delayBetween([
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:1, parameter:[255]).format(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format()
    ], settings.delayMillis)
}

def off1() {
    log.debug "Executing 'off1'"
    delayBetween([
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:1, parameter:[0]).format(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format()
    ], settings.delayMillis)
}

def on2() {
    log.debug "Executing 'on2'"
    delayBetween([
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:1, parameter:[255]).format(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:2).format()
    ], settings.delayMillis)
}

def off2() {
    log.debug "Executing 'off2'"
    delayBetween([
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:1, parameter:[0]).format(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:2).format()
    ], settings.delayMillis)
}
