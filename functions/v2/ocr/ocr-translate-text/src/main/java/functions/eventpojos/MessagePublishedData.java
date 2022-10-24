package functions.eventpojos;

@lombok.Data
public class MessagePublishedData {
  private Message message;
  private String subscription;
}
