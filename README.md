# Spring Application Development at EROAD

# Introduction
The purpose of this workshop is to introduce you to the Spring Framework and some commonly used components
of the EROAD technical *stack*, showcased in the context of a basic **REST** microservice. 

## Technologies covered
1. Spring Web
2. OpenAPI 3.0
3. Spring Data JPA
4. Hibernate & Hibernate Validator
5. MapStruct
6. Project Lombok
7. H2 Database Engine

## System Dependencies
* JDK-11
* Maven

## How to build
You will need to be connected to the VPN, so that you can access Nexus to pull the dependencies:
```bash
mvn clean install
```

# Project structure
This project is serves as a starting point for a backend Spring microservice, each submodule serves
as a starting point for each section of this workshop. You can work on device-management-pt1 all the
way through to the end if you want, I have included the other modules in case you get stuck though.

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

## Task 1
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
</details>

### Examining the generated code
#### **`target/generated-sources/openapi/src/gen/java/main/nz/co/eroad/device/management/model/DeviceDTO.java`**
This file contains a class generated from the schema that we defined earlier, it is annotated with Jackson
annotations that will tell our application how to serialise and deserialize the `DeviceDTO`.

#### **`target/generated-sources/openapi/src/gen/java/main/nz/co/eroad/device/management/api/DevicesApi.java`**
This file contains an interface, generated from our OpenAPI paths definition. It is annotated with
metadata that will help the Spring framework understand the API definition. We will implement this
interface in our Controller layer.

## Part 2 - Designing our Hibernate Entities
> Hibernate ORM (or simply Hibernate) is an object–relational mapping tool for the Java programming
> language. It provides a framework for mapping an object-oriented domain model to a relational database.

### Instructions
Start by creating a new package: `nz.co.eroad.device.management.entity`.

Within this new package, let's create a domain entity for a device:
#### **`entity/Device.java`**
```java
@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

}
```
The `@Entity` annotation tells hibernate that this is a managed entity.

## Task 2
Define the attributes `serialNumber` of type `String` and `lifeCycleState` a custom enum type
#### **`entity/Device.java`**
```java
@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    
    // serialNumber
   
    // lifeCycleState 

}
```

### Tips
* You can tell hibernate that a property is unique with the `@NaturalId` annotation
* Enum properties must be marked with `@Enumerated`

### Solution
<details>
  <summary>Click to expand!</summary>

#### **`entity/LifeCycleState.java`**
  ```java
    public enum LifeCycleState {
        PENDING_INSTALL
    }
  ```
#### **`entity/Device.java`**
```java
@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "serialNumber", nullable = false)
    @NaturalId
    private String serialNumber;

    @Column(name = "lifeCycleState", nullable = false)
    @Enumerated(EnumType.STRING)
    private LifeCycleState lifeCycleState;

} 
 ```
</details>

### Getters & Setters
Hibernate requires that we define, getters and setters for our properties, this can be accomplished
quite easily with Lombok annotations:
#### **`entity/Device.java`**
```java
@Entity
@Table(name = "devices")
@Getter
@Setter
public class Device {
}
```

### HashCode and Equals
We are also required to provide a sensible implementation of `hashCode` and `equals`, these methods
are important because hibernate uses them for determining equality between managed entities:
#### **`entity/Device.java`**
```java
@Entity
@Table(name = "devices")
@Getter
@Setter
public class Device {
   ...

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
      Device device = (Device) o;
      return id != null && Objects.equals(id, device.id);
   }

   @Override
   public int hashCode() {
      return getClass().hashCode();
   }
   
}
```

### ToString
It's never a bad idea to override `toString()`, to provide a string representation of our entity for logging
purposes. This is easily accomplished with the Lombok annotation `@ToString`:
#### **`entity/Device.java`**
```java
@Entity
@Table(name = "devices")
@Getter
@Setter
@ToString
public class Device {
   ...
}
```

### Final result
Here is what we're left with:
#### **`entity/Device.java`**
```java
@Entity
@Table(name = "devices")
@Getter
@Setter
@ToString
public class Device {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "serialNumber", nullable = false)
    @NaturalId
    private String serialNumber;

    @Column(name = "lifeCycleState", nullable = false)
    @Enumerated(EnumType.STRING)
    private LifeCycleState lifeCycleState;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Device device = (Device) o;
        return id != null && Objects.equals(id, device.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
```

If you try to compile the project now, it won't work. That's because we haven't defined a datasource
for hibernate to use. We will do this in the next step!

## Part 3 - Configuring Spring Data JPA
Now that we've defined our `Device` entity, we're going to need some way to save and retrieve instances from the database.
This is where Spring Data JPA comes in.

Let's add the following configuration to our Spring configuration - `application.yaml`:
#### **`src/main/resources/application.yaml`**
```yaml
spring:
  h2:
    console:
      enabled: true
  datasource:
    url: jdbc:h2:mem:device-management
    username: sa
    password: password
    driver-class-name: org.h2.Driver
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
  ...
```
For this workshop we're going to be using H2 as our datastore. H2 is a fully in-memory relational database,
but it supports the JDBC specification, so we can use it with Hibernate & Spring Data JPA transparently.

### Spring Data JPA Repositories
Spring Data JPA offers powerful abstractions for accessing our data. One of the most powerful of these abstractions
is the repository pattern. Create a new package named `repository`, then create a new class called `DeviceRepository`:

#### **`repository/DeviceRepository.java`**
```java
@Repository
public interface DeviceRepository extends CrudRepository<Device, UUID> {
    
}
```
* The `@Repository` annotation tells Spring to catch persistence-specific exceptions and re-throw them as one of Spring's unified unchecked exceptions.
* `CrudRepository<T, ID>` is an interface offered by Spring Data JPA for CRUD operations, the first type parameter `T` is the Entity managed by the repository and the second type parameter `ID` is the Id type for the entity.

We can program our persistence operations against this simple interface, and Spring will handle injecting
an implementation at Runtime based on our configured datasource.

## Part 4 - Implementing the Controller layer
Now it's time to implement the API that we defined earlier. The `openapi-generator-maven-plugin` has
generated an interface `DevicesApi.class` for this purpose, it contains all the annotations necessary for
Spring to map our code to the correct request parameters.

Let's create a new package `controller` and within this package, let's define our `DeviceController`:
#### **`repository/DeviceRepository.java`**
```java
@Controller
public class DeviceController implements DevicesApi {

   @Override
   public ResponseEntity<DeviceDTO> createDevice(DeviceDTO deviceDTO) {
      return ResponseEntity.noContent().build();
   }

   @Override
   public ResponseEntity<DeviceDTO> getDevice(String deviceId) {
      return ResponseEntity.noContent().build();
   }

}
```
* The `@Controller` annotation indicates that a particular class serves the role of a `controller`.
* The controller layer has lots of Framework & transport specific code, we should try to isolate the inner business logic from these implementation details

The controller layer should be as minimal as possible and light on logic, any business logic should be
delegated to a service class, ideally service classes are as reusable as possible as they are not polluted
by framework concerns, or a transport method (HTTP).

## Task 3
Delegate the device creation and lookup capabilities to the `DeviceService` class in the `service` package.

### Tips
* You will need to add a private property that is of type `DeviceService`.
* You can tell Spring to inject this dependency by applying the `@Autowire` annotation to the property.
* A response entity with status `201` or `CREATED` can be built as follows: `ResponseEntity.created(location).body(deviceDTO)`
* The location (path to newly created resource) can be generated as follows:
```java
URI location = ServletUriComponentsBuilder
       .fromCurrentRequest()
       .path("/{id}")
       .buildAndExpand(result.getId())
       .toUri();
```

### Solution
<details>
  <summary>Click to expand!</summary>

#### **`controller/DeviceController.java`**
```java
@Controller
public class DeviceController implements DevicesApi {
    
    @Autowired
    private DeviceService deviceService;
    
    @Override
    public ResponseEntity<DeviceDTO> createDevice(DeviceDTO deviceDTO) {
       DeviceDTO result = deviceService.createDevice(deviceDTO);

       URI location = ServletUriComponentsBuilder
               .fromCurrentRequest()
               .path("/{id}")
               .buildAndExpand(result.getId())
               .toUri();

       return ResponseEntity.created(location).body(result);
    }
    
    @Override
    public ResponseEntity<DeviceDTO> getDevice(String deviceId) {
        return ResponseEntity.ok(deviceService.getDevice(deviceId));
    }

}
 ```
</details>

### More lombok magic
The way we have autowired the above `DeviceService` is not the recommended best-practice method. We
should generally try to inject a classes dependencies via it's constructor, one benefit of this approach
is that our code is less tied to the dependency injection framework. Constructor injection could look something
like this:

#### **`controller/DeviceController.java`**
```java
@Controller
public class DeviceController implements DevicesApi {
    
    private DeviceService deviceService;
    
    @Autowired
    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }
    
   ...
    
}
```

As of Spring 4.3, we no longer need to supply the `@Autowired` annotation on the constructor, provided
we only have a single constructor. Combined with Lombok's `@RequiredArgConstructor` annotation, we can
reduce the amount of boilerplate code further:

#### **`controller/DeviceController.java`**
```java
@Controller
@RequiredArgsConstructor
public class DeviceController implements DevicesApi {
    
    private final DeviceService deviceService;
    
   ...
    
}
```

## Part 5 - Mapping between Entities and DTOs (MapStruct)
The repository that we created earlier deals with our `Device` entity, however what we receive and
return to clients are the DTO objects (`DeviceDTO`), that we defined in our OpenAPI specification. Therefore, we are going to need some way to map between
our entity types and our DTOs.

Now it's entirely possible to manually write a class that does this for us, it could look something like this:

#### **`mapper/DeviceMapper.java`**
```java
public class DeviceMapper {
    
    DeviceDTO toDTO(Device device) {
        DeviceDTO deviceDTO = new DeviceDTO();
        deviceDTO.setId(device.getId());
        deviceDTO.setSerialNumber(device.getSerialNumber());
        deviceDTO.setLifeCycleState(device.getLifeCycleState());
        return deviceDTO;
    }
    
}
```
For a simple class with only a few properties this isn't such a hassle, but when we are dealing with much
larger classes with more properties, and possibly needing to convert between different types, or different
nested structures, the mapping logic can become quite complicated and error-prone.

This is where MapStruct comes in, it allows us to define, with minimal configuration, a mapping between
two types. MapStruct will generate the necessary code to perform the mapping:

#### **`mapper/DeviceMapper.java`**
```java
@Mapper(componentModel = "spring")
public interface DeviceMapper {
    
    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "serialNumber", source = "serialNumber"),
            @Mapping(target = "lifeCycleState", source = "lifeCycleState")
    })
    DeviceDTO toDTO(Device device);

   @Mappings({
           @Mapping(target = "id", source = "id"),
           @Mapping(target = "serialNumber", source = "serialNumber"),
           @Mapping(target = "lifeCycleState", source = "lifeCycleState")
   })
    Device fromDTO(DeviceDTO deviceDTO);
    
}
```
You can find the generated code in
`target/generated-sources/annotations/nz/co/eroad/device/management/mapper/DeviceMapperImpl.java`.

As you can see, the generated code is very similar to what we wrote before.

With the above snipped, I have included the `@Mappings` annotation just to give you an idea of how a
more complicated configuration *could* look. However, given that the properties have the same names,
MapStruct is smart enough to infer the mapping, the following is equivalent:

#### **`mapper/DeviceMapper.java`**
```java
@Mapper(componentModel = "spring")
public interface DeviceMapper {
    
    DeviceDTO toDTO(Device device);

    Device fromDTO(DeviceDTO deviceDTO);
    
}
```
The `@Mapper` annotation tell MapStruct that this is a `Mapper` class so that MapStruct will generate
the necessary code for this interface. The `componentModel = "spring"` property tells MapStruct that
the generated mapper should be managed as a Spring Bean, this means we can inject it anywhere in our
application. Without this annotation we could simply get an instance of our `Mapper` via the static
factory method:
```java
DeviceMapper mapper = Mappers.getMapper(DeviceMapper.class);
```

## Part 6 - Implementing the Service Layer
Now we have all the necessary pieces to finish off the implementation of the service layer of our app.

## Task 4
Implement the methods `createDevice(DeviceDTO deviceDTO)` and `getDevice(String deviceId)` in
the `DeviceService` class. If you need help getting started, here is a skeleton version of the class:

#### **`service/DeviceService.java`**
```java
@Service
@RequiredArgsConstructor
public class DeviceService {
    
    private final DeviceRepository repository;
    private final DeviceMapper mapper;
    
    public DeviceDTO createDevice(DeviceDTO deviceDTO) {
        return null;
    }
    
    public DeviceDTO getDevice(String deviceId) {
        return null;
    }
    
}
```
When you are done, you can right-click on the component test `DeviceManagementComponentTest` and click
"Run", if it passes, your implementation is most likely correct.

### Tips
* You can call the `repository.save()` method to persist the device
* You can call the `repository.findById()` method to retrieve a device by its ID
* If no device with a given ID can be found, you can use the `Exceptions.deviceNotFound()` method to build an appropriate exception:

#### **`exception/Exceptions.class`**
```java
public final class Exceptions {

    public static final String DEVICE_NOT_FOUND_MESSAGE = "Device not found: [%s].";

    public static RuntimeException deviceNotFound(String deviceId) {
        return createException(
                HttpStatus.NOT_FOUND,
                String.format(DEVICE_NOT_FOUND_MESSAGE, deviceId)
        );
    }

    private static RuntimeException createException(HttpStatus httpStatus, String message) {
        return new ResponseStatusException(httpStatus, message);
    }

}
```

### Solution
<details>
  <summary>Click to expand!</summary>

#### **`service/DeviceService.class`**
```java
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceService {

   private final DeviceRepository repository;
   private final DeviceMapper mapper;

   public DeviceDTO createDevice(DeviceDTO deviceDTO) {
      return Optional.ofNullable(deviceDTO)
              .map(mapper::fromDTO)
              .map(repository::save)
              .map(mapper::toDTO)
              .orElse(null);
   }

   public DeviceDTO getDevice(String deviceId) {
      return repository.findById(UUID.fromString(deviceId))
              .map(entityMapper::toDTO)
              .orElseThrow(() -> Exceptions.deviceNotFound(deviceId));
   }

}
```
</details>

## Extra for experts
### Task 5
Implement the `GET /devices?serialNumber=<>` endpoint, that retrieves a device by its serial number
