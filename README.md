# Deploy Eye
![Deploy Eye Logo](https://i.imgur.com/4C2dZEa.png)

Deploy Eye will monitor your deployed services / components by polling a HTTP(s) endpoint.
It will keep track of:
- The current status of your services
- The availability history (down & uptimes, reasons)
- Version history

![Dashboard](https://i.imgur.com/HbC0T1T.png)

![History](https://i.imgur.com/lHvbHiu.png)



## Configuration
All configuration is saved under /opt/deployeye/ - you will have to create this directory manually.

### app.json
app.json contains all relevant configuration items.
Use this default template and adjust where relevant
```json
{
  "baseURL": "https://my_url/deployeye/",
  "dbDriver": "com.mysql.cj.jdbc.Driver",
  "dbURL": "jdbc:mysql://localhost:3306/deployeye?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Europe/Zurich&autoReconnect=true&autoReconnectForPools=true&reconnectAtTxEnd=true&sessionVariables=sql_mode=''",
  "dbUser": "username",
  "dbName": "deployeye",
  "dbPW": "*****************",
  "dbPoolSize": 10,
  "dbBackupIntervalInMinutes": 720,
  "dbBackupDumpBinary": "mysqldump",
  "dbBackupKeepForXDays": 20,
  "adminSecret": "***********"
}
```

### mail.json
mail.json configures the mail account which will be used to send mail notifications.
```json
{
    "address": "deployeye@something.com",
    "login": "deployeye",
    "pw": "f00b4r",
    "inbox_folder_name": "INBOX",
    "host_smtp": "192.168.200.1",
    "port_smtp": "25",
    "secure_smtp": false,
    "host_imap": "192.168.200.1",
    "port_imap": "143",
    "secure_imap": false,
    "trust_all_certs": true
}
```

### eyes.json
Eyes are services that Deploy Eye shall monitor.
Example configuration:
``` json
[
  {
    "name" : "deathStarLaserControlUnit",
    "url" : "http://192.168.200.111:8181/check-health/",
    "keyword_version" : "version",
    "keyword_branch" : "branch",
    "keyword_health" : "health",
    "keyword_changelog" : "changelog",
    "header_name" : "X-API-KEY",
    "header_value" : "W7W-a3Lp~z82[`U.d"
  },{
    "name" : "deathStarPowerSupply",
    "url" : "https://192.168.200.23:443/check-health/",
    "keyword_version" : "psu_version",
    "keyword_branch" : "psu_branch",
    "keyword_health" : "health",
    "keyword_changelog" : "changelog",
    "cookie_name" : "authToken",
    "cookie_value": "imperialPassword123",
	"notification_recipients":["maintenance@imperial.gov","bevel.lemelisk@imperial.gov"]
  },{
    "name" : "laserCanons",
    "url" : "https://192.168.150.150:443/check-health/",
    "keyword_version" : "version",
    "keyword_branch" : "branch",
    "keyword_health" : "health",
    "keyword_changelog" : "changelog",
    "cookie_name" : "authToken",
    "cookie_value": "@/opt/deployeye/creds/laserCookie"
  }
]
```
When setting notification_recipients, the recipients will receive e-mail notifications when services go down, recover or change its version.


Note: if cookie_value starts with a @, then the following string is interpreted as a file path to read as the value

The expected response for "deathStarLaserControlUnit" would look like the following:
```json
{
  "version":"1.0.1",
  "branch":"master",
  "health":"ok",
  "changelog":"https://deathstar.intranet.space/x/s8XIBQ"
}
```
If the health string isn't "OK" it will be saved as the fail reason accordingly.

Note: health may be a JSON array, in which case "OK" means the length of the array being zero.
If the length isn't zero, than the json array will be "flattened" by toString and this string used as the fail reason. 
Example: "health":[[1619624880705,"some error"],[1619625601244,"another error!"]]

### DB Setup
You can either use MySQL Workbench or use https://github.com/tomoemon/mwb2sql to convert the current mwb file into the required INSERT statements to get your DB up and running:
https://github.com/ozzi-/deployeye/blob/main/res/deployeye.mwb


### Reload
In order to reload all config during runtime, you may send a POST to /reload. In order to authenticate the request, send along a "X-SECRET" header with the value defined in app.json.
