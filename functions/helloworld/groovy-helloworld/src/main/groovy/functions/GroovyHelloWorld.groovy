package functions

// [START functions_helloworld_get]
import com.google.cloud.functions.HttpFunction
import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse

class GroovyHelloWorld implements HttpFunction {
    @Override
    void service(HttpRequest request, HttpResponse response) {
        response.writer.write("Hello World!")
    }
}
// [END functions_helloworld_get]
