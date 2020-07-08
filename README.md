# mock-http-server
A minimal java mock server for soap/rest test
## Getting Started
### Build
```
ant (min 1.6)  => generate dist/mockserver.jar
```
### Usage
```
java -jar mockserver.jar <port> <body_response_xml_path>
```  
* port : listening port's server
* body_response_xml_path = path of xml (body) response
### Example
#### SOAP
##### Prepare
> Request.xml ( Body = testResources )
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                  xmlns:ns="http://my.request.namespace/">
 <soapenv:Body>
  <ns:testResources />
 </soapenv:Body>
</soapenv:Envelope>
```
> <body_response_xml_path>/testResources.xml
```xml
<ns:testResourcesResponse xmlns:ns="http://my.response.namespace/">
 <return>true</return>
</ns:testResourcesResponse>
```
##### Execute
> java -jar mockserver.jar 9080 <body_response_xml_path>

> curl --data "@Request.xml" http://localhost:9080/service/soap
```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
 <soap:Body>
  <ns:testResourcesResponse xmlns:ns="http://my.response.namespace/">
   <return>true</return>
  </ns:testResourcesResponse>
 </soap:Body>
</soap:Envelope>
```
#### REST
##### Prepare
> <body_response_xml_path>/testResources.json
```json
{
  "testResourcesResponse": {
    "return": "true"
  }
}
```
##### Execute
> java -jar mockserver.jar 9080 <body_response_xml_path>

> curl http://localhost:9080/service/rest?method=testResources
```json
{
  "testResourcesResponse": {
    "return": "true"
  }
}
```
