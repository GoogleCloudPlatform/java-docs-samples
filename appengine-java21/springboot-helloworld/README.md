# appengine-spring-boot

Sample Google App Engine (standard) application using :

 * Java 21 : 
 * Spring Boot 3.3.5 : the application is packaged as an executable WAR also deployable on servlet containers
 * Jetty12 SpringBoot configuration instead of default Tomcat. This way it reuses the Jetty12 provided in AppEngine.
 * Latest AppEngine artifacts for Java 21 runtime
 * JSP : just to prove it works, you should probably use another template engine like thymeleaf

## How to test locally

 ```
mvn clean package appengine:run
```

This command above will start the local devappserver and you can see the home page: http://localhost:8080/ that will display these links to test:

```
Sample Spring Boot Application running as an App Engine Java21 Web App.!
This is the index.jsp. Try also the following urls:
/aliens
/admin
/actuator/metrics
/actuator/metrics/jvm.memory.max
/actuator/health
/actuator/env
/actuator/threaddump
/actuator/loggers
/actuator/beans
/actuator/health
```

## How to deploy

To deploy on App Engine, run `mvn appengine:update -Dappengine.app.id=your_appengine_application_id`.  
You can also add `-Dappengine.app.version=X` to override the default version (1).

If you only have one environment, you can set these properties directly in `pom.xml` or `appengine-web.xml`.

## What's in there

The home page is simple, it just proves Java 21 GAE + Spring Boot + GAE APIs (datastore) +JSPs work 

You can also hit `/aliens` to see a  HTTP  example using AppEngine Datastore APIs.

You can also curl the hell out of the actuator endpoints :

 * Health : `curl -i "https://your_appengine_application_id.appspot.com/health`
 * Sensitive endpoints (credentials in `application.yml`) : `curl -i "https://your_appengine_application_id.appspot.com/env --user "administrator:M4rSuP1aL-EsTh3T1qUE"` 

## Notes / known issues

 * If you want to  add global security constraints using the App Engine user APIs, you still need a `web.xml`