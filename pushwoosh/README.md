**Mobile Push Notification Service**

## Supported integrations

- [PushWoosh](https://www.pushwoosh.com/)

## System properties

|Name|Default value|Description|
|---|---|---|
|openexchange.pushwoosh.api.accesstoken| |PushWoosh Remote API [access token](https://go.pushwoosh.com/v2/api_access)| 
|openexchange.pushwoosh.api.endpoint|`https://cp.pushwoosh.com/json/1.3`|PushWoosh Remote API endpoint|

Note:

In case it's being run behind HTTP/HTTPS proxy make sure to include following system properties to command line:

`-Dhttps.proxyHost=... -Dhttps.proxyPort=... -Djavax.net.ssl.trustStore=... -Djavax.net.ssl.trustStorePassword=...`


