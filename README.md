# Rekodi: Record and Playback Service Calls
A proxy with the ability to record and playback HTTP Responses. Ideally made 
to record service calls (SOAP/REST)

### Releases:
Visit [releases](http://hkamran.info/projects/rekodi/releases) page

### Screenshot:

<div align="left">
	<img src="http://hkamran.info/projects/rekodi/screenshots/screenshot_1.png"></img>
</div>

### Quick Start:

java -jar Rekodi-X-X-X.jar `-webPort [PORT]` `-proxyPort [PORT]` 

- **webPort:** Specify the port for the web interface `(Optional)` `[Default: 8090]` 
- **proxyPort:** Specify the port for the proxy 	  `(Optional)` `[Default: 9090]` 		
	
### Requirements:
Java 1.7

### Build Steps: 

- mvn clean
- mvn install package

### Links
https://github.com/hkamran/Rekodi-UI 


