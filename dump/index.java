//[START requests_and_servlets]
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MyServlet extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws IOException {
	resp.setContentType("text/plain");
	resp.getWriter().println("Hello, world");
	}
}
// [END requests_and_servlets]

// [START class_loader_JAR_ordering]
<class-loader-config>
<priority-specifier filename="mailapi.jar"/>
</class-loader-config>
// [END class_loader_JAR_ordering]

//[START no_signed_JAR_files]
	java.lang.SecurityException: SHA1 digest error for com/example/SomeClass.class
	at com.google.appengine.runtime.Request.process-d36f818a24b8cf1d(Request.java)
	at sun.security.util.ManifestEntryVerifier.verify(ManifestEntryVerifier.java:210)
	at java.util.jar.JarVerifier.processEntry(JarVerifier.java:218)
	at java.util.jar.JarVerifier.update(JarVerifier.java:205)
	at java.util.jar.JarVerifier$VerifierStream.read(JarVerifier.java:428)
	at sun.misc.Resource.getBytes(Resource.java:124)
	at java.net.URLClassLoader.defineClass(URLClassLoader.java:273)
	at sun.reflect.GeneratedMethodAccessor5.invoke(Unknown Source)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:616)
at java.lang.ClassLoader.loadClass(ClassLoader.java:266)
//[END no_signed_JAR_files]

//[START logging_1]
	import java.util.logging.Logger;
	...

	public class MyServlet extends HttpServlet {
		private static final Logger log = Logger.getLogger(MyServlet.class.getName());

		@Override
		public void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws IOException {

		log.info("An informational message.");

		log.warning("A warning message.");

		log.severe("An error message.");
		}
	} 
//[END logging_1]

//[START logging_2]
<appengine-web-app xmlns="http://appengine.google.com/ns/1.0">
...

<system-properties>
<property name="java.util.logging.config.file" value="WEB-INF/logging.properties" />
</system-properties>

</appengine-web-app>
//[END logging_2]

//[START the_environment]
if (SystemProperty.environment.value() ==
		SystemProperty.Environment.Value.Production) {
	The app is running on App Engine...
		}
//[END the_environment]

//[START request_IDs]
com.google.apphosting.api.ApiProxy.getCurrentEnvironment().getAttributes().get("com.google.appengine.runtime.request_log_id")
//[END request_IDs]
