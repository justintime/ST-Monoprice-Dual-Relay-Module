/**
 *  Dual Relay Adapter (i.e. Enerwave ZWN-RSM2 Adapter, Monoprice Dual Relay, Philio PAN04)
 *
 *  Copyright 2015 Justin Ellison
 * 
 *  Special thanks to Joel Tamkin and Eric Maycock for the bulk of this code
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
 */
definition(
    name: "Dual Relay Adapter",
    namespace: "justintime",
    author: "Justin Ellison",
    description: "Associates Dual Relay Switch Modules with one or two standard SmartThings 'virtual switch' devices for compatibility with standard control and automation techniques",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
  section("Configuration:") {
    input "rsm", "capability.switch", title: "Which Dual Relay Module?", multiple: false, required: true
    input "switch1", "capability.switch", title: "Virtual Switch to link to Switch 1?", multiple: false, required: true
    input "switch2", "capability.switch", title: "Virtual Switch to link to Switch 2?", multiple: false, required: false
  }
}

def installed() {
  log.debug "Installed!"
  subscribe(rsm, "switch1", rsmHandler)
  subscribe(rsm, "switch2", rsmHandler)
  subscribe(switch1, "switch", switchHandler)
  subscribe(switch2, "switch", switchHandler)

  initialize()
}

def updated() {
  log.debug "Updated!"
  unsubscribe()
  subscribe(rsm, "switch", rsmHandler)
  subscribe(rsm, "switch1", rsmHandler)
  subscribe(rsm, "switch2", rsmHandler)
  subscribe(switch1, "switch", switchHandler)
  subscribe(switch2, "switch", switchHandler)
  
  initialize()
}

def switchHandler(evt) {
  //log.debug "switchHandler: ${evt.value}, ${evt.deviceId}, ${evt.source}, ${switch2.id}"
  switch (evt.deviceId) {
  	case switch1.id:
		switch (evt.value) {
        	case 'on':
        		log.debug "switch 1 on"
                rsm.on1()
                break
        	case 'off':
        		log.debug "switch 1 off"
                rsm.off1()
                break
            }
        break
    case switch2.id:
    	switch (evt.value) {
        	case 'on':
        		log.debug "switch 2 on"
                rsm.on2()
                break
        	case 'off':
        		log.debug "switch 2 off"
                rsm.off2()
                break
            }
        break

    default:
    	pass
  }
}

def rsmHandler(evt) {
	log.debug "$evt.name $evt.value"
  if (evt.name == "switch1") {
  	switch (evt.value) {
      	case 'on':
          	switch1.on()
              break
          case 'off':
          	switch1.off()
              break
    }
  }
  else if (evt.name == "switch2") {
  	switch (evt.value) {
      	case 'on':
          	switch2.on()
              break
          case 'off':
          	switch2.off()
              break
    }
  }
}

def rsmRefresh() {
	rsm.refresh()
}
    
def initialize() {
  unschedule()
}
