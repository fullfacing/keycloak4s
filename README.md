# keycloak4s
[![CircleCI](https://circleci.com/gh/fullfacing/keycloak4s.svg?style=shield&circle-token=0788f14be0abb7f8ab8194fbd2cd179122b3ee85)](https://circleci.com/gh/fullfacing/keycloak4s)
[![codecov](https://codecov.io/gh/fullfacing/keycloak4s/branch/master/graph/badge.svg?token=WKbJaagGhz)](https://codecov.io/gh/fullfacing/keycloak4s)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/com.fullfacing/keycloak4s-core_2.12.svg)](https://oss.sonatype.org/content/repositories/snapshots/com/fullfacing/keycloak4s-core_2.12/)

**A Scala-based middleware API for [Keycloak](https://www.keycloak.org/)**  
*Based on version 6.0.1*

keycloak4s is an opinionated Scala-built API that serves as a bridge between any Scala project and a Keycloak server. It allows access to the server's [Admin API](https://www.keycloak.org/docs-api/6.0/rest-api/index.html), and provides adapters that validates Keycloak's bearer tokens. It authorizes requests via a JSON config file inspired by their [policy enforcement configuration](https://www.keycloak.org/docs/latest/authorization_services/index.html#_enforcer_filter).

The project is split into the following modules, each as a separate dependency:
* `keycloak4s-core`: Contains core functionality and shared models across other modules.
* `keycloak4s-admin`: Allows access to Keycloak's [Admin API][Admin-API]. Utilizes SoftwareMill's [sttp](https://github.com/softwaremill/sttp) and Cats Effect's [Concurrent](https://typelevel.org/cats-effect/typeclasses/concurrent.html) which respectively allows for customization of the HTTP client.
* `keycloak4s-admin-monix`: A more concrete implementation of keycloak-admin using [Monix][Monix]. Contains additional reactive streaming functionality.
* `keycloak4s-akka-http`: A client adapter capable of validating Keycloak's bearer tokens, and providing authorization for [Akka-HTTP][Akka-Http] requests.

### Contents
1. [Installation](#Installation)
2. [Module: keycloak4s-core](#keycloak4s-core)
3. [Module: keycloak4s-admin](#keycloak4s-admin)
4. [Module: keycloak4s-admin-monix](#keycloak4s-admin-monix)
5. [Module: keycloak4s-akka-http](#keycloak4s-akka-http)
    1. [Token Validation](#token-validation)
    2. [Policy Enforcement Configuration](#policy-enforcement)
    3. [Plugging in the Adapter](#adapter-plugin)
    4. [Payload Extraction](#payload-extractors)
6. [Logging](#Logging)
7. [Error Messages](#Errors)

## Installation

Each module can be pulled into a project separately using the following SBT dependencies:
* keycloak4s-core:        `"com.fullfacing" %% "keycloak4s-core" % "1.0"`
* keycloak4s-admin:       `"com.fullfacing" %% "keycloak4s-admin" % "1.0"`
* keycloak4s-admin-monix: `"com.fullfacing" %% "keycloak4s-monix" % "1.0"`
* keycloak4s-akka-http:   `"com.fullfacing" %% "keycloak4s-akka-http" % "1.0"`

The core module is a dependency for all other modules, and is automatically pulled in when using any other module.

## Module: keycloak4s-core <a name="keycloak4s-core"></a>
The core module shares common functionality (such as logging and error handling) and data models with its dependent modules.

It is important to note that the KeycloakConfig model contains the Keycloak server information. This information will be required often. Included in this set of information, is the following:
* The Keycloak server's URL scheme, host and port.
* The name, client ID and client secret of the Keycloak realm providing authorization.
* The name of the Keycloak realm to be targeted.

*Example:*
```scala
val authConfig = KeycloakConfig.Auth(
    realm         = "master",
    clientId      = "admin-cli",
    clientSecret  = "b753f3ba-c4e7-4f3f-ac16-a074d4d89353"
)
   
val keycloakConfig = KeycloakConfig(
    scheme  = "http",
    host    = "fullfacing.com/keycloak",
    port    = 8080,
    realm   = "demo",
    authn   = authConfig
)
```

## Module: keycloak4s-admin <a name="keycloak4s-admin"></a>
The module uses the client credential flow behind the scenes to simplify access to Keycloak's [Admin API][Admin-API]. In order to make calls to it, a client needs to be created with the correct server details and credentials to connect to the Keycloak server, followed by a service handler that can invoke the calls. The process can be broken down into the following steps:

**1 - Create a KeycloakConfig**<br/>
Refer to the keycloak4s-core segment for details on KeycloakConfig. Please note that the authorization realm must be a [service account](https://www.keycloak.org/docs/latest/server_admin/index.html#_service_accounts)-enabled admin client.

**2 - Create a KeycloakClient**<br/>
The KeycloakClient handles the HTTP calls to the KeycloakServer. It requires a KeycloakConfig, and a sttp backend (the KeycloakClient and the sttp backend must match parametric types). Alternatively, the Akka/Monix module can be used for a concrete implementation. For more information, refer to the [Module: keycloak4s-admin-monix](#keycloak4s-admin-monix) module of this document.

*Example:*
```scala
implicit val backend: SttpBackend[Task, Observable[ByteBuffer]] = AsyncHttpClientBackend()
implicit val keycloakClient: KeycloakClient[Task, Observable[ByteBuffer]] = new KeycloakClient[Task, Observable[ByteBuffer]](config)
```

**3 - Create a service handler**<br/>
In this context, a service handler contains the admin calls that are relevant to a specific aspect of Keycloak. For example, the Users service contains calls that creates, retrieves and manipulates Keycloak Users. Please note: When creating a service handler, an implicit KeycloakClient must be in scope.

*Example:*
```scala
val usersService = Keycloak.Users[Task, Observable[ByteBuffer, Any]]
val rolesService = Keycloak.Roles[Task, Observable[ByteBuffer, Any]]
```

**4 - Invoke the calls**<br/> 
The relevant admin API calls can be invoked from a service handler. This will automatically handle calls for access, and refresh tokens in the background through the client credentials flow.

*Example:*
```scala
val newUser = User.Create(username = "ff_keycloak_user_01", enabled = true)
val newGroup = Group.Create(name = "ff_keycloak_group_01")

for {
  u <- EitherT(usersService.createAndRetrieve(newUser))
  g <- EitherT(groupsService.createAndRetrieve(newGroup))
  _ <- EitherT(usersService.addToGroup(u.id, g.id))
} yield (u, g)
```

The majority of the functions in a service handler corresponds directly with an admin API route. However, a few are composite functions created for convenience. An example of this can be seen in the aforementioned example where the`createAndRetrieve` function which chains a create- and a fetch call.

## Module: keycloak4s-admin-monix <a name="keycloak4s-admin-monix"></a>
keycloak4s-admin-monix can be used as an alternative to keycloak4S-admin-monix. This module is typed to Monix with [Tasks][Task] as the response wrapper and [Observables][Observable] as the streaming type. This removes the need to set up the types for KeycloakClient or the service handlers. Additionally, this module contains reactive streaming variants of the fetch calls, which allows for batch retrieval and processing, as well as a Monix-wrapped Akka-HTTP based sttp backend that is ready for use. 

The steps to make these calls remains the same as in the keycloak4s-admin. The following example provide the specific pre-built sttp backend. For more information, refer to [Module: keycloak4s-admin](#keycloak4s-admin) in this document.

*Example:*
```scala
implicit val backend: SttpBackend[Task, Observable[ByteString]] = AkkaMonixHttpBackend()
implicit val monixClient: KeycloakClient = new KeycloakClient(...) // truncated, see keycloak4s-core segment for details

val usersService = Keycloak.Users

usersService.fetch()
```

To use the streaming variants of this module simply replace any `fetch` call with `fetchS`, which takes an additional `batchSize` parameter. The function returns an [Observable][Observable] and operates in batches by calling to retrieve the specified batch size, processing the batched results, then making the call for the next batch.

*Example:*
```scala
val usersService = Keycloak.Users

// return the IDs of all disabled Users
usersService.fetchS(batchSize = 20)
  .dropWhile(!_.enabled)
  .map(_.id)
  .toListL
```

A `fetchL` variant is also available which performs the same batch streaming, but automatically converts the Observable to a List of Task when the stream has completed.

## Module: keycloak4s-akka-http <a name="keycloak4s-akka-http"></a>
*Please note: This module is especially opinionated and was designed with our company's needs in mind. However, an effort was made to keep it as abstract as possible to allow for repurposed use. Feedback on its usability is encouraged.*

This module is a client adapter for Akka-HTTP that allows the service to validate Keycloak's bearer tokens (through the use of [Nimbus JOSE + JWT][Nimbus]). It provides high-level RBAC authorization for requests via Akka-HTTP's directives, and a JSON policy enforcement configuration.

**Token Validation**<br/> <a name="token-validation"></a>
With the adapter plugged in, all requests are expected to contain an authorization header with a bearer token. Additionally, an ID token can be passed along with the header `Id-Token`.

The tokens are first parsed, and then validated for the following:
* The token cannot be expired.  
(`exp` field (mandatory) is checked)
* The token cannot be used before its not-before date.  
(`nbf` field (optional) is checked, however validation passes if the field is absent)
* The token's issuer must match the Keycloak server's URL.  
(`iss` field (mandatory) is checked)
* The token's "issued at" date must be in the past.  
(`iat` field (mandatory) is checked)
* The token's signature must be valid.  
( Verification of the signature requires a JSON Web Key with the same ID as the token's kid header)

To validate tokens the adapter requires an implicit `TokenValidator` in scope, and to create an instance of the validator the adapter requires a JSON Web Key set (JWKS) as well as a KeycloakConfig. This adapter provides two different methods of constructing a TokenValidator:
* Statically - The JWKS is provided in the constructor. This is the simplest way to construct a TokenValidator and does not require a connection to Keycloak. However, the validator's JWKS cannot be modified.
* Dynamically - The JWKS is automatically pulled from the Keycloak server, then cached. This requires a working connection to the Keycloak server. However, it also allows for automatic re-caching of the JWKS (once per request) in the event that the set changes. The server details are taken from the KeycloakConfig.

*Example:*<br/>
```scala
val keycloakConfig = KeycloakConfig(...) // truncated, see core segment for details

// creating a static TokenValidator

val file = new File("/keycloak4s/jwks.json")
val jwks = JWKSet.load(file)

implicit val staticValidator: TokenValidator = TokenValidator.Static(jwks, keycloakConfig)

// creating a dynamic TokenValidator

implicit val dynamicValidator: TokenValidator = TokenValidator.Dynamic(keycloakConfig)
```

Alternatively, a TokenValidator with custom JWKS handling can be created. To do so requires writing a concrete implementation of the `JwksCache` trait, which contains the functionality of how the JWKS is handled. The concrete trait then simply needs to be mixed into the abstract TokenValidator class.

*Example:<br/>*
```scala
class CustomJwksCache extends JwksCache {
  // concrete implementations of the JwksCache abstract functions
}

class CustomValidator(config: KeycloakConfig)(implicit ec: ExecutionContext = global)
  extends TokenValidator(config) with CustomJwksCache

val keycloakConfig = KeycloakConfig(...) // truncated, see keycloak4s-core segment for details

implicit val customValidator: TokenValidator = new CustomValidator(keycloakConfig)
```


**Policy Enforcement Configuration**<br/> <a name="policy-enforcement"></a>
Request authorization in keycloak4s is performed by evaluating it against a set of policy-enforcement rules. The service's rules are represented by a policy configuration object. This object is parsed from a JSON structure, inspired by Keycloak's [policy enforcement JSON configuration][Policy-Configuration]. Incoming requests are compared to these rules in order to determine which permissions a bearer token needs to contain.

The configuration JSON file must be placed in a project's `resources` folder. Specifying the file name allows for the construction of the policy enforcement object:
`val policyConfig = PolicyBuilders.buildPathAuthorization("config_name.json")`. 

In our intended use case, the clients of a Keycloak realm each represent a specific API. Client-level roles are then created for each client as available permissions for the API, after which the roles can be assigned to users, granting permissions as required.

The following is an example of an access token payload for a user that is authorized with admin access to one API, and read/write access for a particular resource on another:
```json
{
  "resource_access": {
    "api-one": {
      "roles": [
        "admin"
      ]
    },
    "api-two": {
      "roles": [
        "read-resource1", "write-resource1"
      ]
    }
  },
  "iss": "https://com.fullfacing:8080/auth",
  "exp": 1562755799,
  "iat": 1562755739
}
```

Example of the Policy Configuration JSON Structure:
```json
{
  "service" : "api-one",
  "enforcementMode" : "ENFORCING",
  "paths" : [
    {
      "path" : "/v1/resource1/{id}/another-resource/{id}/action",
      "methodRoles": [
        {
          "method" : "*",
          "roles" : "action-admin"
        },
        {
          "method" : "POST",
          "roles" : [ "action-post", "action-admin" ]
        }
      ]
    },
    {
      "path" : "/v1/*",
      "methodRoles" : [
        {
          "method" : "*",
          "roles" : "admin"
        }
      ]
    }
  ]
}
```

* `service` - Example: "api-reporting".
              This is the API's name that is represented in Keycloak as a client. It is used to check the user's access token for permissions for the service. 
            
* `enforcementMode` - This is an enum that determines how requests that has no matching policy rule are handled. The three options are:       
   * "ENFORCING"  - Requests with no matching policy rule are denied.
   * "PERMISSIVE" - Requests with no matching policy rule are accepted. Requests that match a policy rule are evaluated to determine access.
   * "DISABLED"   - No authorization evaluation takes place. A valid access token is still required.
            
* `paths` - This field defines the roles in each path. It determines the permissions that are required for each request to access the specified path.

    * `path` - Example: "/v2/resource/segment/action".
               The request path within the `secure` directive will be matched against the configured path defined here.
    
     >Special segments:<br> 
     A "wildcard" path/segment can be configured using a "\*" segment. For example, a path configured simply as "/\*" means this rule will apply to any request. A path configured as "/v1/segment/*" will apply to any request starting with the path "/v1/segment/".<br>
     "{id}" is used to denote a valid UUID path segment, for example, "/v1/resource/{id}". A possible request matching this rule would be: "/v1/resource/689c4936-5274-4543-85d7-296cc456100b"
    
    * `methodRoles` - A list of HTTP methods and the permissions required for each method.
    
        * `method` - The HTTP method to which this rule applies. Set this field to "*" in order to make the rule apply to any HTTP method.
        
        * `roles` - The permissions for a method.
           - A single string will resolve to a single role: for example, "admin"
           - A list of strings will by default resolve to Or evaluation: for example, [ "admin", "read", "write" ]. This effectively means the user requires only one of any of the listed permissions.
           - For more complex permission logic (for example, a mix of optional and required roles) use the RequiredRoles data construct:
           
*Scala Representation:*           
```scala
sealed trait RequiredRoles

final case class And(and: List[Either[RequiredRoles, String]]) extends RequiredRoles
final case class Or(or: List[Either[RequiredRoles, String]])  extends RequiredRoles
```          

*JSON Representation:*              
```json
{
  "roles" : {
    "and" : [
      {
        "or" : [ "resource-read", "resource-write", "resource-delete" ]
      },
      {
        "or" : [ "resource2-read", "resource2-write", "resource2-delete" ]
      },
      {
        "or" : [ "segment-read", "segment-write", "segment-delete" ]
      }
    ]
  }
}
```

* `segments` - This is an optional field allowing you to set up rules for segments that repeat in the configured paths. To reference a created segment, use the syntax `{{segment}}` for example, "/v1/{{segment}}/action". This reduces duplicating method roles for each configured path in which this segment is used.

    * `segment` - The segment string that will appear in the path. Wildcard and {id} segments are not allowed.
    
    * `methodRoles` - The configured HTTP methods and the corresponding roles for each request.
    
        * `method` - The HTTP method for which this rule applies. The wildcard method may be used here.
        
        * `roles`  - A list of roles required for this method. The user requires at least one role from the list.
```json
{
  "paths": [
    {
      "path" : "/v1/{{resource1}}"
    },
    {
      "path" : "/v1/{{resource1}}/{id}"
    },
    {
      "path" : "/v1/{{resource1}}/{id}/another-resource"
    }
  ],
  "segments" : [
    {
      "segment" : "resource1",
      "methodRoles" : [
        {
          "method" : "GET",
          "roles" : [ "read", "write", "delete" ]
        },
        {
          "method" : "POST",
          "roles" : [ "write", "delete" ]
        }
      ]
    }
  ]
}
```

Note: Should an incoming request have multiple, unique rules that apply to it (for example, a rule with both a wildcard segment/method and a concrete rule), the request is evaluated using both rules, and will be accepted if either succeeds.

**Plugging in the Adapter**<br/> <a name="adapter-plugin"></a>
In order for the adapter to validate and authorize requests, it needs to be plugged into the Akka-HTTP routes, for which there are two requirements:
* A policy enforcement configuration for the adapter needs to be created. (Refer to [Policy Enforcement Configuration](#policy-enforcement))
* A `TokenValidator` needs to be created and passed implicitly into scope. (Refer to [Token Validation](#token-validation))

Once the validator and configuration is ready, the adapter can be plugged in:
1. Mix the `SecurityDirectives` trait into the class containing the routes. This provides the `secure` directive which plugs the adapter's functionality in.
2. Invoke `secure` with the policy enforcement configuration, and wrap the *entire* Akka-HTTP Route structure inside the directive.

*Example:*<br/>
```scala
object AkkaHttpRoutes extends SecurityDirectives {
  val enforcementConfig: PathAuthorisation = PolicyEnforcement.buildPathAuthorisation("enforcement.json")
  
  val keycloakConfig = KeycloakConfig(...) // truncated, see core segment for details
  implicit val validator: TokenValidator = TokenValidator.Dynamic(keycloakConfig)
  
  secure(enforcementConfig) { payloads =>
    path("api" / "v2") {
      // akka-http route structure
    }
  }
}
```

**Token Payload Extractors**<br/> <a name="payload-extractors"></a>
After validation, the `secure` directive provides the payloads of the bearer tokens for further information extraction or processing. The payloads are in a JSON structure native to [Nimbus JOSE + JWT][Nimbus]. However, to simplify extraction, this module includes implicits with safe extraction functionality.

To gain access to the extractors, the implicits need to be in scope. After it has been defined to be in scope, a set of generic extractors can be used on any Nimbus Payload object.

*Example:*<br/>
```scala
import com.fullfacing.keycloak4s.auth.akka.http.PayloadImplicits._

val payload: Payload = ... // truncated

// extracts the value for a given key as a String
val tokenType: Option[String] = payload.extract("typ")

// extracts the value for a given key as the given type
val sessionState: Option[UUID] = payload.extractAs[UUID]("session_state")

// extracts the value for a given key as a List of Strings
val audiences: List[String] = payload.extractList("aud")

// extracts the value for a given key as a List of the given type
val resourceAccess: List[UUID] = payload.extractAsListOf[UUID]("allowed_ids")
```

The parametric extractors use the internal [json4s](http://json4s.org/) serializers by default, but can be customized by passing a json4s `Formats` instance explicitly to the function, or by declaring it implicitly in scope.

Alongside the generic extractors are additional extractors for commonly required values; such as `extractScopes`, `extractEmail`, etc.

## Logging <a name="Logging"></a>
keycloak4s has customized logging spanning over `trace`, `debug` and `error` levels using [SLF4J](https://www.slf4j.org/). To restrict logging output, the following Logger names should be referenced:
* Top level: `keycloak4s`
* keycloak4s-admin module: `keycloak4s.admin`
* keycloak4s-akka-http module: `keycloak4s.auth`

Internal correlation UUIDs are passed between function calls of the same request to assist in tracing logs and debugging. Normally, a correlation ID is generated for each request. However, a UUID can be passed along for a request if there is a need for it. To do so requires passing the UUID and policy enforcement configuration as a Tuple into the `secure` directive (refer to [Plugging in the Adapter](#adapter-plugin)).

*Example:*<br/>
```scala
// an example function to extract a UUID from a request sent through Postman
def contextFromPostman: Directive1[UUID] = {
  optionalHeaderValueByName("Postman-Token").flatMap { cId =>
    provide {
      cId.fold(UUID.randomUUID())(UUID.fromString)
    }
  }
}

contextFromPostman { cId =>
  secure((enforcementConfig, cId)) { payloads =>
    path("api" / "v2") {
      // akka-http route structure
    }
  }
}
```

## Error Messages <a name="Errors"></a>
When an exception inside keycloak4s is captured instead of thrown, it is converted into a `KeycloakError` subtype, depending on the cause or location of the error. `KeycloakError` extends `Throwable` and can thus still be thrown or processed as one.

The subtypes of `KeycloakError` are as follows:
* `KeycloakThrowable` - Simply a wrapper for a Throwable.
* `KeycloakException` - Contains a status code, status text, an error message and optionally finer details, useful for creating HTTP responses.
* `KeycloakSttpException` - Contains the HTTP response details sent back from the sttp client, along with information of the corresponding request. Useful for debugging integration issues between keycloak4s and the Keycloak server, faulty requests sent from keycloak4s, or errors thrown from within the Keycloak server.

[Monix]: https://monix.io/
[Task]: https://monix.io/docs/3x/eval/task.html
[Observable]: https://monix.io/docs/3x/reactive/observable.html
[Akka-HTTP]: https://doc.akka.io/docs/akka-http/current/introduction.html
[Admin-API]: https://www.keycloak.org/docs-api/5.0/rest-api/index.html
[Nimbus]: https://connect2id.com/products/nimbus-jose-jwt
[Policy-Configuration]: https://www.keycloak.org/docs/latest/authorization_services/index.html#_enforcer_filter