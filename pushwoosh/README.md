**Mobile Push Notification Web Service**

## Supported integrations

- [PushWoosh](https://www.pushwoosh.com/)


## Maven build profiles

|Name|Activated by default (y/n)|Description|
|---|---|---|
|it-test|n|Run integration tests|
|docker|n|Produces docker image for the local component testing|

## System properties

### Service bootstrap

|Name|Default value|Description|
|---|---|---|
|openexchange.pushwoosh.api.accesstoken| |PushWoosh Remote API [access token](https://go.pushwoosh.com/v2/api_access)| 
|openexchange.pushwoosh.api.endpoint|`https://cp.pushwoosh.com/json/1.3`|PushWoosh Remote API endpoint|

### Integration test

To run integration test use this cli
 
`$mvn clean install -P it-test`

Also **in addition** to required bootstrap parameters add following:

|Name|Default value|Description|
|---|---|---|
|openexchange.pushwoosh.test.userid| |Any unique user identifier as email, facebookId etc|
|openexchange.pushwoosh.test.applicationcode| |PushWoosh application code|
|openexchange.pushwoosh.test.devicehwid| |Mobile device id|
|openexchange.pushwoosh.test.devicepushtoken| |Push token issued per device|
|Dopenexchange.pushwoosh.test.devicetype|1|A device type. Only `1` = iOS is supported so far|

### Proxy settings

Please note if you run service or integration tests behind HTTP/HTTPS proxy make sure to include following system properties to command line:

|Name|Default value|Description|
|---|---|---|
|https.proxyHost| |HTTP/HTTPS proxy host|
|https.proxyPort| |HTTP/HTTPS proxy port|
|javax.net.ssl.trustStore| |A path to local keystore with required proxy certificates|
|javax.net.ssl.trustStorePassword| |A local keystore password|


