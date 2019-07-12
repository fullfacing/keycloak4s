# keycloak4s
[![CircleCI](https://circleci.com/gh/fullfacing/keycloak4s.svg?style=shield&circle-token=0788f14be0abb7f8ab8194fbd2cd179122b3ee85)](https://circleci.com/gh/fullfacing/keycloak4s)
[![codecov](https://codecov.io/gh/fullfacing/keycloak4s/branch/master/graph/badge.svg?token=WKbJaagGhz)](https://codecov.io/gh/fullfacing/keycloak4s)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/com.fullfacing/keycloak4s-core_2.12.svg)](https://oss.sonatype.org/content/repositories/snapshots/com/fullfacing/keycloak4s-core_2.12/)

**A Scala-based middleware API for [Keycloak](https://www.keycloak.org/)**  
*Based on version 6.0.1*

keycloak4s is an opinionated Scala built API that serves as a bridge between any Scala project and a Keycloak server, allowing access to the server's [Admin API](https://www.keycloak.org/docs-api/6.0/rest-api/index.html) as well as providing adapters to validate Keycloak's bearer tokens and authorize requests via a JSON config file inspired by their [policy enforcement configuration][Policy-Configuration].

The project is split into the following modules, each as a separate dependency:
* `keycloak4s-core`: Contains core functionality and shared models across other modules.
* `keycloak4s-admin`: Allows access to Keycloak's [Admin API][Admin-API]. Utilizes SoftwareMill's [sttp](https://github.com/softwaremill/sttp) and Cats Effect' [Concurrent](https://typelevel.org/cats-effect/typeclasses/concurrent.html) to allow for customization of the HTTP client and concurrency monad to be used, respectively.
* `keycloak4s-admin-monix`: A more concrete implementation of keycloak-admin using [Monix][Monix]. Contains additional reactive streaming functionality.
* `keycloak4s-akka-http`: A client adapter capable of validating Keycloak's bearer tokens and providing authorization for [Akka-HTTP][Akka-Http] requests.

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

Each module can be pulled into a project separately via the following SBT dependencies:
* keycloak4s-core:        `"com.fullfacing" %% "keycloak4s-core" % "1.0"`
* keycloak4s-admin:       `"com.fullfacing" %% "keycloak4s-admin" % "1.0"`
* keycloak4s-admin-monix: `"com.fullfacing" %% "keycloak4s-monix" % "1.0"`
* keycloak4s-akka-http:   `"com.fullfacing" %% "keycloak4s-akka-http" % "1.0"`

The core module is a dependency for all other modules and therefore is automatically pulled in when using any other module.

## Module: keycloak4s-core <a name="keycloak4s-core"></a>
The core module contains functionality (such as logging and error handling) and models shared between modules.

Of note is the KeycloakConfig model that contains the details of a Keycloak server, it is often required. It consists of the following:
* The URL scheme, host and port of the Keycloak server.
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
The module uses the client credential flow behind the scenes to simplify access to Keycloak's [Admin API][Admin-API]. In order to make calls to it a client needs to be created with the correct server details and credentials to connect to the Keycloak server, followed by a service handler that can invoke the calls. The process can be broken down into the following steps:

**1 - Create a KeycloakConfig**<br/>
Refer to the keycloak4s-core segment for details on KeycloakConfig. Note that the authorization realm must be a [service account](https://www.keycloak.org/docs/latest/server_admin/index.html#_service_accounts) enabled admin client.

**2 - Create a KeycloakClient**<br/>
The KeycloakClient handles the HTTP calls to the KeycloakServer, it requires a KeycloakConfig and a sttp backend (the KeycloakClient and the sttp backend must match parametric types). Alternatively the Akka/Monix alternative module can be used for a concrete implementation, see the keycloak4s-admin-monix segment for more details.

*Example:*
```scala
implicit val backend: SttpBackend[Task, Observable[ByteBuffer]] = AsyncHttpClientBackend()
implicit val keycloakClient: KeycloakClient[Task, Observable[ByteBuffer]] = new KeycloakClient[Task, Observable[ByteBuffer]](config)
```

**3 - Create a service handler**<br/>
A service handler in this context contains the admin calls relevant to a specific aspect of Keycloak. For example the Users service contains calls that creates, retrieves and manipulates Keycloak Users. It requires an implicit KeycloakClient in scope.

*Example:*
```scala
val usersService = Keycloak.Users[Task, Observable[ByteBuffer, Any]]
val rolesService = Keycloak.Roles[Task, Observable[ByteBuffer, Any]]
```

**4 - Invoke the calls**<br/> 
The relevant admin API calls can be invoked from a service handler, which will automatically handle calls for access and refresh tokens in the background through the client credentials flow.

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

The majority of the functions in a service handler directly corresponds to an admin API route, however a few are composite functions created for convenience, such as the `createAndRetrieve` function seen in the above example which chains a create call and a fetch call.

## Module: keycloak4s-admin-monix <a name="keycloak4s-admin-monix"></a>
An alternative of keycloak4s-admin typed to Monix with [Tasks][Task] as the response wrapper and [Observables][Observable] as the streaming type, removing the need to set up the types for KeycloakClient or the service handlers. Additionally this module contains reactive streaming variants of the fetch calls allowing for batch retrieval and processing, and a Monix-wrapped Akka-HTTP based sttp backend ready for use. 

The steps to make calls remains mostly the same as in the keycloak4s-admin, below is an example with the prebuilt sttp backend specifically, refer to [Module: keycloak4s-admin](#keycloak4s-admin) for additional information.

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
*Please note: This module is especially opinionated and was designed with our company's needs in mind, however there was still an effort to keep it as abstract as possible to allow for repurposing. Feedback on its usability is encouraged.*

A client adapter for Akka-HTTP that allows the service to validate Keycloak's bearer tokens (through use of [Nimbus JOSE + JWT][Nimbus]) and provides a high-level RBAC implementation to authorize requests via Akka-HTTP's directives and a JSON policy enforcement configuration.

**Token Validation**<br/> <a name="token-validation"></a>
With the adapter plugged in all requests are expected to contain an authorization header with a bearer token, additionally an ID token can also be passed along with the header `Id-Token`.

The tokens are first parsed and then validated for the following:
* The token cannot be expired.  
(`exp` field is checked, and must be present)
* The token cannot be used before it's not-before date.  
(`nbf` field is checked, however validation passes if the field is absent)
* The token's issuer must match the Keycloak server's URL.  
(`iss` field is checked, and must be present)
* The token's issued must be in the past.  
(`iat` field is checked, and must be present)
* The token's signature must be valid.  
(verification of the signature requires a JSON Web Key with the same ID as the token's kid header)

To allow the adapter to validate tokens it requires an implicit `TokenValidator` in scope, and to create an instance of the validator requires a JSON Web Key set (JWKS) and a KeycloakConfig. Provided in the adapter are two different methods of constructing a TokenValidator:
* Statically - The JWKS is provided in the constructor. This is the simplest way to construct a TokenValidator and does not require a connection to Keycloak. However the validator's JWKS cannot be modified.
* Dynamically - The JWKS is automatically pulled from the Keycloak server, then cached. This requires a working connection to the Keycloak server, however it also allows for automatic recaching of the JWKS (once per request) in case the set changes. The server details are taken from the KeycloakConfig.

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

Alternatively a TokenValidator with custom JWKS handling can be created. To do so requires writing a concrete implementation of the `JwksCache` trait, which contains the functionality of how the JWKS is handled. The concrete trait then simply needs to be mixed into the abstract TokenValidator class.

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

keycloak4s' request authorization is performed by evaluating a request against a set of policy enforcement rules. The rules for the service are represented by a policy configuration object parsed from a JSON structure inspired by Keycloak's [policy enforcement JSON configuration][Policy-Configuration]. Incoming requests are compared to these rules to determine which permissions a bearer token is required to contain in order to be authorized.

The configuration JSON file must be placed in a project's `resources` folder. This allows for the construction of the policy enforcement object simply by specifying the file name:
`val policyConfig = PolicyBuilders.buildPathAuthorization("config_name.json")`. 

In our intended use case the clients of a Keycloak realm each represent a specific API. Client-level roles are then created for each client as available permissions for the API. The roles can then be assigned to users to grant permissions as required.

Example of an access token payload for a user authorized with admin access to one API, and read/write access for a particular resource on another:
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
      "path" : "/v1/{{resource1}}"
    },
    {
      "path" : "/v1/{{resource1}}/{id}"
    },
    {
      "path" : "/v1/{{resource1}}/{id}/another-resource"
    },
    {
      "path" : "/v1/{{resource1}}/{id}/another-resource/{id}/action",
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

* `service` - Example: "api-reporting".
              The name of the API that is represented in Keycloak as a client, used to check the user's access token for permissions for the service. 
            
* `enforcementMode` - An enum that determines how requests with no matching policy rule are handled. The three options are:       
   * "ENFORCING"  - Requests with no matching policy rule are denied.
   * "PERMISSIVE" - Requests with no matching policy rule are accepted. Requests that match a policy rule are evaluated to determine access.
   * "DISABLED"   - No authorization evaluation takes place. A valid access token is still required.
            
* `paths` - A list of policy rules for paths that determine which permissions are required for requests to any of the included paths.

    * `path` - Example: "/v2/resource/segment/action".
               The path here is reliant on where the `secure` directive is plugged into your Akka-HTTP routes. The request path within the `secure` directive will be matched against the configured path defined here.
    
     >Special segments:<br> 
     A "wildcard" path/segment can be configured using a "\*" segment. E.g. A path configured simply as "/\*" means this rule will apply to any request. A path configured as "/v1/segment/*" will apply to any request starting with the path "/v1/segment/".<br>
     "{id}" is used to denote a valid UUID path segment, e.g. "/v1/resource/{id}". A possible request matching this rule would be: "/v1/resource/689c4936-5274-4543-85d7-296cc456100b"
    
    * `methodRoles` - A list of HTTP methods and the permissions required for each method.
    
        * `method` - The HTTP method to which this rule applies. Set this field to "*" in order to make the rule apply to any HTTP method.
        
        * `roles` - The permissions for a method.
           - A single string will resolve to a single role: e.g. "admin"
           - A list of strings will by default resolve to Or evaluation: e.g. [ "admin", "read", "write" ], this effectively means the user requires only one of any of the listed permissions.
           - For more complex permission logic (e.g. a mix of optional and required roles) the RequiredRoles data construct was created:
           
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

* `segments` - An optional field to setup rules for segments that repeat in the configured paths. To reference a created segment use the syntax `{{segment}}` e.g. "/v1/{{segment}}/action". This reduces retyping of method roles for each configured path in which this segment is used.

    * `segment` - The segment string that will appear in the path. Wildcard and {id} segments are not allowed.
    
    * `methodRoles` - The configured HTTP methods and the corresponding roles for each request.
    
        * `method` - The HTTP method to which this rule applies. The wildcard method may be used here.
        
        * `roles`  - A list of roles required for this method. The user requires at least one role from the list.
```json
{
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

Note: In the event that an incoming request has multiple unique rules that can apply to it (E.g a rule with both a wildcard segment/method and a concrete rule), the request will be evaluated using both rules and will be accepted if either succeeds.
 
**Plugging in the Adapter**<br/> <a name="adapter-plugin"></a>
In order for the adapter to validate and authorize requests it needs to be plugged into the Akka-HTTP routes, for which there are two requirements:
* A policy enforcement configuration for the adapter needs to be created. (Refer to [Policy Enforcement Configuration](#policy-enforcement))
* A `TokenValidator` needs to be created and passed implicitly into scope. (Refer to [Token Validation](#token-validation))

With the validator and configuration at the ready the adapter can be plugged in:
1. Mix in the `SecurityDirectives` trait into the class containing the routes, this provides the `secure` directive which plugs in the adapter.
2. Invoke `secure` with the policy enforcement configuration, and wrap the *entire* Akka-HTTP Route structure that you want to secure inside the directive.

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
After validation the `secure` directive provides the payloads of the bearer tokens for further information extraction or processing. The payloads are in a JSON structure native to [Nimbus JOSE + JWT][Nimbus], however to simplify extraction this module includes implicits with safe extraction functionality.

To gain access to the extractors the implicits need to be in scope, after which a set of generic extractors can be used on any Nimbus Payload object.

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

By default the parametric extractors use the internal [json4s](http://json4s.org/) serialization `Formats`, but they will pass through a custom Formats either implicitly (if implicitly in scope) or when passed through explicitly.

Alongside the generic extractors are additional extractors for commonly required values, such as `extractScopes`, `extractEmail`, etc.

## Logging <a name="Logging"></a>
keycloak4s has customized logging spanning over `trace`, `debug` and `error` levels using [SLF4J](https://www.slf4j.org/), for restricting logging output the following Logger names should be referenced:
* Top level: `keycloak4s`
* keycloak4s-admin module: `keycloak4s.admin`
* keycloak4s-akka-http module: `keycloak4s.auth`

Internal correlation UUIDs are passed between function calls of the same request to assist in tracing logs and debugging. Normally a correlation ID is generated for each request, however a UUID can be passed along for a request if need be. To do so requires passing the UUID into the `secure` directive (refer to [Plugging in the Adapter](#adapter-plugin)) along with the policy enforcement configuration as a Tuple.

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
When an exception is captured instead of thrown inside keycloak4s it is converted into a `KeycloakError` subtype, depending on the cause or location of the error. `KeycloakError` extends `Throwable` and can thus still be thrown or processed as one.

The subtypes of `KeycloakError` are as follows:
* `KeycloakThrowable` - Simply a wrapper for a Throwable.
* `KeycloakException` - Contains a status code, status text, an error message and optionally finer details, useful for creating HTTP responses.
* `KeycloakSttpException` - Contains the HTTP response details sent back from the sttp client, along with information of the corresponding request. Useful for debugging intergration issues between keycloak4s and the Keycloak server, faulty requests sent from keycloak4s or errors thrown from within the Keycloak server.

[Monix]: https://monix.io/
[Task]: https://monix.io/docs/3x/eval/task.html
[Observable]: https://monix.io/docs/3x/reactive/observable.html
[Akka-HTTP]: https://doc.akka.io/docs/akka-http/current/introduction.html
[Admin-API]: https://www.keycloak.org/docs-api/5.0/rest-api/index.html
[Nimbus]: https://connect2id.com/products/nimbus-jose-jwt
[Policy-Configuration]: https://www.keycloak.org/docs/latest/authorization_services/index.html#_enforcer_filter)
