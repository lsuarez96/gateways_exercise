#Gateways REST API
Example of a REST API for storing gateways information.

##Project tree
management

|---- src

│   |---- main

│   │   |---- java

│   │   │   |----- com

│   │   │       |----- musala

│   │   │           |----- gateway

│   │   │               |----- management

│   │   │                   |---- annotation

│   │   │                   |---- config

│   │   │                   |---- controller

│   │   │                   |---- exception

│   │   │                   |---- model

│   │   │                   |---- repository

│   │   │                   |----- service

│   │   |----- resources

│   |----- test

│       |----- java

│           |----- com

│               |----- musala

│                   |----- gateway

│                       |----- management

|----- target

##Install
###Requirements
1- The project is build using maven, so it is a requirement to have maven in the system class path. To check if maven is installed type mvn -v. If not response is given try installing it (in debian: sudo apt install maven).

2- The second requirement is having a working installation of java, preferably java 11.0.15

###Package and Run
The project is packaged as jar and has an embbeded tomcat server, so it is self contained.
To package and run the service run the script in the root folder of the project:

./package-run.sh

The jar will be located under the target directory
###Test
To run the automated test run the script in the project root folder:

./run-test.sh

Designed tests include:
Model Input Validation: Checks if input constrains are correctly validated by Hibernate Validator.

Bussines Logic Tests: Checks if all operations conserning Gateways are correctly performed and exceptions are well handled. 

Integration Tests: Checks entire flows since a request is made to an endpoint until a response is obtained.

Note:Some exceptions will appear in the terminal, that is because most tests validate error handling and captured exceptions are logged. At the end of the tests you should notice that none of them failed or where skipped.

###Configuration
Service configuration is done via application.properties file, located in management/src/main/resources/application.properties.
The configuration contains three properties:

Access port:

    server.port=8089
Max amount of devices that can be associated to a gateway:

    musala.max.gateway.devices=10
Load test data:

    musala.enable.test.data=true

##Usage
The api manages two entities: Gateways and Devices. Request examples are given below.

###Gateway:
###List gateways:
####Request:

curl --location --request GET 'http://localhost:8089/gateway/list'

####Response:
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
    }
]

###View Gateway:
####Request:

curl --location --request GET 'http://localhost:8089/gateway/view/1'

####Response: 
Code: 200

{

    "id": 2,
    "serialNumber": "gw1",
    "name": "gateway0",
    "ipAddress": "10.8.6.51",
    "devices": []    
}

###Create Gateway:
####Request:

curl --location --request POST 'http://localhost:8089/gateway/create' \
--data-raw '{
    "name":"gw",
    "serialNumber":"sn",
    "ipAddress":"10.8.6.70"
}'

####Response:
Code: 201

{

    "id": 4,
    "serialNumber": "sn",
    "name": "gw",
    "ipAddress": "10.8.6.70",
    "devices": []
}

###Update Gateway:
####Request:

curl --location --request PUT 'http://localhost:8089/gateway/update/4' \
--header 'Content-Type: application/json' \
--data-raw '{
    "name":"updated_gw",
    "serialNumber":"sn",
    "ipAddress":"10.8.6.70"
}'

####Response:

Code: 200

{

    "id": 4,
    "serialNumber": "sn",
    "name": "updated_gw",
    "ipAddress": "10.8.6.70",
    "devices": []
}

###Attach Device to Gateway:
####Request:
curl --location --request PUT 'http://localhost:8089/gateway/2/attach/11'

####Response:
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

###Detach Device from Gateway:
####Request:
curl --location --request PUT 'http://localhost:8089/gateway/2/detach/11'

####Response:
Code: 200

{

    "id": 2,
    "serialNumber": "gw1",
    "name": "gateway0",
    "ipAddress": "10.8.6.51",
    "devices": []
}

###Delete Gateway:
####Request:
curl --location --request DELETE 'http://localhost:8089/gateway/delete/1'

####Response:
Code: 200

##Device:
###List Devices:
####Request:
curl --location --request GET 'http://localhost:8089/device/list'

####Response:
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

###View Device:
####Request:
curl --location --request GET 'http://localhost:8089/device/view/2'
####Response:
Code: 200

{

    "id": 2,
    "uid": 2,
    "vendor": "Apple",
    "createdAt": "2022-05-23T20:19:46.082+00:00",
    "deviceStatus": "ONLINE"
}

###Update Device:
####Request:
curl --location --request PUT 'http://localhost:8089/device/update/1' \
--data-raw '{
    "uid": 1,
    "vendor": "updated_Sony",
    "createdAt": "2022-05-23T22:12:41.000+00:00",
    "deviceStatus": "ONLINE"
}'

####Response:
Code: 200

{
    "id": 1,
    "uid": 1,
    "vendor": "updated_Sony",
    "createdAt": "2022-05-23T22:12:41.000+00:00",
    "deviceStatus": "ONLINE"
}

###Delete Device:
####Request:
curl --location --request DELETE 'http://localhost:8089/device/delete/1'
####Response:
Code: 200
