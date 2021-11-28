# Spring Application Development at EROAD

# Introduction
The purpose of this workshop is to introduce you to the Spring Framework and some commonly used components
of the EROAD technical *stack*, showcased in the context of a basic **REST** microservice. 

## Technologies covered
1. Spring Web
2. OpenAPI 3.0 & OpenAPI Generator
3. Spring Data JPA
4. Hibernate & Hibernate Validator
5. Spring Aspect (AOP: Aspect-Oriented-Programming)
6. MapStruct
7. Project Lombok
8. H2 Database Engine

# Project structure
This project is serves as a starting point for a backend Spring microservice, there are a number
of tagged commits which can be used to skip ahead to different parts of this workshop.

# Problem
We want to create a RESTful backend service for managing information about devices.

## User Story
As a fleet manager, I want to be able to view relevant information about devices registered to my fleet,
including their installation status.

## Acceptance Criteria
1. Fleet managers can create a device with attributes
2. Fleet managers query a device by Id, to get its attributes
3. The required attributes are:
   1. Id (UUID) **Required**, **Unique**
   2. SerialNumber (String) **Required**, **Unique**
   3. LifeCycleState (Enum: PENDING_INSTALL) **Required**, *default: PENDING_INSTALL*

## REST operations
* `POST /devices`: creates a device - returns `201`
* `GET /devices/{id}`: retrieves a device by id - returns `200`

## Part 1 - Designing our OpenAPI Specification
### What is an OpenAPI Specification
> The OpenAPI Specification (OAS) defines a standard, language-agnostic interface to RESTful APIs
> which allows both humans and computers to discover and understand the capabilities of the service
> without access to source code, documentation, or through network traffic inspection.

### Benefits of Specification first development
* Generated client/server code
* Easy to mock
* Promotes a collaborative design process
* Acts as living documentation

### Useful Links
* https://swagger.io/specification/

## Getting started
Create the following file in your application resources folder:

#### **`src/main/resources/api.yaml`**
```yaml
openapi: 3.0.3
info:
  title: Device Management API
  version: 1.0.0
```
* The `openapi` field provides the semantic version number of the Specification version that the document uses
* The `info` field provides additional metadata about the API, that may be used by the tooling

### The `components` field
`components` define the various schemas for the specification. Let's start by defining a schema
for a `Device`:
#### **`src/main/resources/api.yaml`**
```yaml
components:
  schemas:
    Device:
      type: object
      required:
        - serialNumber
      properties:
        id:
          type: string
          format: uuid
          description: The device ID.
        serialNumber:
          type: string
          description: The device serial number.
        lifeCycleState:
          type: string
          enum:
            - PENDING_INSTALL
            - INSTALLED
          description: The device life-cycle state.
  responses:
    DeviceResponse:
      description: A device response
      content:
        application/json:
          schema:
            $ref: "#/components/schemas/Device"
```
* If `type` is `object` then a number of `properties` must be defined.
* Each of these `properties` is named and has a type of its own (could also be type `object`)

### The `paths` field
`paths` defines the available paths and operations for the API. Go ahead and define the
`GET /devices` operation: 

#### **`src/main/resources/api.yaml`**
```yaml
paths:
  /devices:
    post:
      summary: Creates a device
      operationId: createDevice
      requestBody:
        description: A device creation request.
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/Device"
      responses:
        '201':
          $ref: "#/components/responses/DeviceResponse"
```

## Part 1 - Task 1
Define the `GET /devices/{deviceId}` path:
#### **`src/main/resources/api.yaml`**
```yaml
paths:
  /devices:
    post:
      ...
    # GET definition here:
```

### Tips
You will need to define a `deviceId` path parameter, refer to the following documentation:
* https://swagger.io/specification/#path-item-object
* https://swagger.io/specification/#operation-object

You can build the project with `mvn clean install`, this will let you know if you have any
errors in your `api.yaml` file.

### Solution
<details>
  <summary>Click to expand!</summary>

  #### **`src/main/resources/api.yaml`**
  ```yaml
  paths:
    /devices:
      post:
        # defined already
    /devices/{deviceId}:
      get:
        summary: Returns a device by id
        operationId: getDevice
        parameters:
          - in: path
            name: deviceId
            schema:
              type: string
            required: true
        responses:
          '200':
            $ref: "#/components/responses/DeviceResponse"
  ```

### Examining the generated code
#### **`target/generated-sources/api.yaml`**
```yaml

```

## Part 2 - Designing our Hibernate Entities

## Part 3 - Configuring Spring Data JPA

## Part 4 - Implementing the Controller

## Part 5 - Configuring MapStruct

## Part 6 - Implementing the Service
