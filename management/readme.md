<H1>Gateways REST API</H1>
Example of a REST API for storing gateways information.

<H2>Install</H2>

<H3>Requirements</H3>

1- The project is build using maven, so it is a requirement to have maven in the system class path. To check if maven is installed type mvn -v. If not response is given try installing it (in debian: sudo apt install maven).

2- The second requirement is having a working installation of java, preferably java 11.0.15

<H3>Package and Run</H3>
The project is packaged as jar and has an embbeded tomcat server, so it is self contained.
To package and run the service run the script in the root folder of the project:

    ./package-run.sh

The jar will be located under the target directory
<H3>Test</H3>
To run the automated test run the script in the project root folder:

    ./run-test.sh

<b>Designed tests include:</b>

Model Input Validation: Checks if input constrains are correctly validated by Hibernate Validator.

Bussines Logic Tests: Checks if all operations conserning Gateways are correctly performed and exceptions are well handled. 

Integration Tests: Checks entire flows since a request is made to an endpoint until a response is obtained.

<b>Note:<b>Some exceptions will appear in the terminal, that is because most tests validate error handling and captured exceptions are logged. At the end of the tests you should notice that none of them failed or where skipped.

<H3>Configuration</H3>
    
Service configuration is done via application.properties file, located in management/src/main/resources/application.properties.
The configuration contains three properties:

Access port:

    server.port=8089
Max amount of devices that can be associated to a gateway:

    musala.max.gateway.devices=10
Load test data:

    musala.enable.test.data=true

<H2>Usage</H2>
    
The api manages two entities: Gateways and Devices. Request examples are given below.

<H3>Gateway:</H3>
<H4>List gateways:</H4>
<b>Request:</b>

    curl --location --request GET 'http://localhost:8089/gateway/list'

<b>Response:</b>

Code: 200

    [
        {
            "id": 1,
            "serialNumber": "gw0",
            "name": "gateway0",
            "ipAddress": "10.8.6.50",
            "devices": [
                {
                    "id": 1,
                    "uid": 1,
                    "vendor": "Sony",
                    "createdAt": "2022-05-23T01:23:05.327+00:00",
                    "deviceStatus": "ONLINE"
                },

                .
                .
                .

                {
                    "id": 10,
                    "uid": 10,
                    "vendor": "Hawlett-Packard",
                    "createdAt": "2022-05-23T01:23:05.327+00:00",
                    "deviceStatus": "ONLINE"
                }
            ]
        },
        .
        .
        .
        {
            "id": 4,
            "serialNumber": "sn",
            "name": "gw",
            "ipAddress": "10.8.6.70",
            "devices": []
        }]

<H4>View Gateway:</H4>
<b>Request:</b>

    curl --location --request GET 'http://localhost:8089/gateway/view/1'

<b>Response:</b> 
    
Code: 200

    {
        "id": 2,
        "serialNumber": "gw1",
        "name": "gateway0",
        "ipAddress": "10.8.6.51",
        "devices": []    
    }

<H4>Create Gateway:</H4>
<b>Request:</b>

    curl --location --request POST 'http://localhost:8089/gateway/create' \
    --data-raw '{
        "name":"gw",
        "serialNumber":"sn",
        "ipAddress":"10.8.6.70"
    }'

<b>Response:</b>

Code: 201

    {
        "id": 4,
        "serialNumber": "sn",
        "name": "gw",
        "ipAddress": "10.8.6.70",
        "devices": []
    }

<H4>Update Gateway:</H4>
<b>Request:</b>

    curl --location --request PUT 'http://localhost:8089/gateway/update/4' \
    --header 'Content-Type: application/json' \
    --data-raw '{
        "name":"updated_gw",
        "serialNumber":"sn",
        "ipAddress":"10.8.6.70"
    }'

<b>Response:</b>

Code: 200

    {
        "id": 4,
        "serialNumber": "sn",
        "name": "updated_gw",
        "ipAddress": "10.8.6.70",
        "devices": []
    }

<H4>Attach Device to Gateway:</H4>
<b>Request:</b>

    curl --location --request PUT 'http://localhost:8089/gateway/2/attach/11'

<b>Response:</b>

Code: 200

    {
        "id": 2,
        "serialNumber": "gw1",
        "name": "gateway0",
        "ipAddress": "10.8.6.51",
        "devices": [
            {
                "id": 11,
                "uid": 11,
                "vendor": "IBM",
                "createdAt": "2022-05-23T01:23:05.327+00:00",
                "deviceStatus": "ONLINE"
            }
        ]
    }

<H4>Detach Device from Gateway:</H4>
<b>Request:</b>

    curl --location --request PUT 'http://localhost:8089/gateway/2/detach/11'

<b>Response:</b>

Code: 200

    {
        "id": 2,
        "serialNumber": "gw1",
        "name": "gateway0",
        "ipAddress": "10.8.6.51",
        "devices": []
    }

<H4>Delete Gateway:</H4>
<b>Request:</b>

    curl --location --request DELETE 'http://localhost:8089/gateway/delete/1'

<b>Response:</b>

Code: 200

<H3>Device:</H3>
<H4>List Devices:</H4>
<b>Request:</b>

    curl --location --request GET 'http://localhost:8089/device/list'

<b>Response:</b>

Code: 200

    [
        {
            "id": 1,
            "uid": 1,
            "vendor": "Sony",
            "createdAt": "2022-05-23T19:31:53.471+00:00",
            "deviceStatus": "ONLINE"
        },
        {
            "id": 2,
            "uid": 2,
            "vendor": "Apple",
            "createdAt": "2022-05-23T19:31:53.480+00:00",
            "deviceStatus": "ONLINE"
        },

        ...

        {
            "id": 13,
            "uid": 13,
            "vendor": "Intel",
            "createdAt": "2022-05-23T19:31:53.488+00:00",
            "deviceStatus": "ONLINE"
        }
    ]

<H4>View Device:</H4>
<b>Request:</b>

    curl --location --request GET 'http://localhost:8089/device/view/2'

<b>Response:</b>

Code: 200

    {
        "id": 2,
        "uid": 2,
        "vendor": "Apple",
        "createdAt": "2022-05-23T20:19:46.082+00:00",
        "deviceStatus": "ONLINE"
    }

<H4>Update Device:</H4>
<b>Request:</b>

    curl --location --request PUT 'http://localhost:8089/device/update/1' \
    --data-raw '{
        "uid": 1,
        "vendor": "updated_Sony",
        "createdAt": "2022-05-23T22:12:41.000+00:00",
        "deviceStatus": "ONLINE"
    }'

<b>Response:</b>
    
Code: 200

    {
        "id": 1,
        "uid": 1,
        "vendor": "updated_Sony",
        "createdAt": "2022-05-23T22:12:41.000+00:00",
        "deviceStatus": "ONLINE"
    }

<H4>Delete Device:</H4>
<b>Request:</b>

    curl --location --request DELETE 'http://localhost:8089/device/delete/1'

<b>Response:</b>
Code: 200
