 preferences {
    input("deviceIP", "string", title:"IP Address", description: "IP Address", required: true, displayDuringSetup: true)
}

metadata {
	// Automatically generated. Make future change here.
	definition (name: "TED5000", author: "swap-file") {
		capability "Energy Meter"
		capability "Polling"
		capability "Refresh"
        capability "Sensor"
		capability "Power Meter"
	}

	// UI tile definitions
	tiles {
	    
		valueTile(	"power", 
        			"device.power", 
        			width: 2, 
                    height: 2,
                    decoration: "flat"
                 ) 
        {
            state(	"power",
                    label:'${currentValue} W', 
                  	backgroundColors:[
					[value: 200, color: "#153591"],
					[value: 400, color: "#1e9cbb"],
					[value: 600, color: "#90d2a7"],
					[value: 700, color: "#44b621"],
					[value: 1000, color: "#f1d801"],
					[value: 1200, color: "#d04e00"],
					[value: 1400, color: "#bc2323"]
				]
                 )
		}
        
        
        standardTile("refresh", "device.power") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main(["power", "energy"])
		details(["power", "refresh"])
	}
}

def poll() {
	log.trace 'Poll Called'
	runCmd()
}

def refresh() {
	log.trace 'Refresh Called'
	runCmd()
}

def runCmd() {
	def host = deviceIP
	def hosthex = convertIPtoHex(host).toUpperCase()
	def LocalDevicePort = "80"
	def porthex = convertPortToHex(LocalDevicePort).toUpperCase()
	device.deviceNetworkId = "$hosthex:$porthex"

	//log.debug "The device id configured is: $device.deviceNetworkId"

	def headers = [:] 
	headers.put("HOST", "$host:$LocalDevicePort")
	headers.put("Content-Type", "application/x-www-form-urlencoded")
    
	//log.debug "The Header is $headers"
    
	def path = '/api/LiveData.xml'
	def body = ''
	//log.debug "Uses which method: $DevicePostGet"
	def method = "GET"

	try {
    	log.debug "Making TED5000 request to $device.deviceNetworkId"
		def hubAction = new physicalgraph.device.HubAction(
			method: method,
			path: path,
			body: body,
			headers: headers
			)
		hubAction.options = [outputMsgToS3:false]
		//log.debug hubAction
		hubAction
	}
	catch (Exception e) {
		log.debug "Hit Exception $e on $hubAction"
	}
}

private String convertIPtoHex(ipAddress) { 
	String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
	//log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
	return hex
}
private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
	//log.debug hexport
	return hexport
}

def parse(String description) {
//this is automatically called when the hub action returns
	try {
 	def msg = parseLanMessage(description)
    def xml = msg.xml
 	def powerNow = xml.Power.Total.PowerNow
    log.debug "Got Reply - power: $powerNow W"
    sendEvent (name: "power", value: powerNow, unit:"W")
    	}
	catch (Exception e) {
		log.debug "Hit Exception $e"
	}
}

