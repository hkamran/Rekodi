#Service Recorder

	The main function of this program is to mock and playback service calls such as 
	SOAP/REST on HTTP.The reason this program was made was due to the issue of having 
	unreliable services, which would then hinder development and/or testing efforts.
	So, this program hopes to remove the effort required to mock calls and return 
	back to development/testing efforts.
	
<div align="center">
	<img src="http://104.236.89.192/ServiceRecorder/screenshot_1.png"></img>
</div>

#How does it work:
	This program acts as a proxy between the system and the service allowing it to 
	capture traffic or playback mocked traffic.

#How to use:	
	Execute the following command java -jar ServiceRecorder-X.X.X.jar.
	Set your system to point to localhost:9090 and set the redirection rule. Your system will
	send its traffic to this proxy and then get redirected to the actual service.

#Requirements:
    Java 1.7 
    Maven 3.1.0
	
#Build Steps:
	Execute the following command - mvn clean compile assembly:single
	
#Builds:
	http://104.236.89.192/ServiceRecorder/builds/