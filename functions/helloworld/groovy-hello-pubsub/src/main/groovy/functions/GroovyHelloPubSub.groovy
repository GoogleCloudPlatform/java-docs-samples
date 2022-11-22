package functions

// [START functions_helloworld_pubsub]
import com.google.cloud.functions.BackgroundFunction
import com.google.cloud.functions.Context
import functions.eventpojos.PubSubMessage
import java.nio.charset.StandardCharsets
import java.util.logging.Logger

class GroovyHelloPubSub implements BackgroundFunction<PubSubMessage> {
    private static final Logger LOGGER = Logger.getLogger(GroovyHelloPubSub.class.name)

    @Override
    void accept(PubSubMessage message, Context context) {
        // name's default value is "world"
        String name = "world"

        if (message?.data) {
             name = new String(Base64.decoder.decode(message.data), StandardCharsets.UTF_8)
        }

        LOGGER.info("Hello ${name}!")
        return
    }
}
// [END functions_helloworld_pubsub]
