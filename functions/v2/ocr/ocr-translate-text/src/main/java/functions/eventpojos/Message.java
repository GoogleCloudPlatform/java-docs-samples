package functions.eventpojos;

import java.time.OffsetDateTime;
import java.util.Map;

@lombok.Data
public class Message {
  private Map<String, String> attributes;
  private String data;
  private String messageID;
  private String orderingKey;
  private OffsetDateTime publishTime;
}
