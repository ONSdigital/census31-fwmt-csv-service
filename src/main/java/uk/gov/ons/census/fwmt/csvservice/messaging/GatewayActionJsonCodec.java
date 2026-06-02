package uk.gov.ons.census.fwmt.csvservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import java.io.IOException;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.canonical.v1.CreateFieldWorkerJobRequest;

@Component
public class GatewayActionJsonCodec {

  private final ObjectMapper objectMapper = new ObjectMapper();

  public PubsubMessage toPubsubMessage(CreateFieldWorkerJobRequest dto) {
    try {
      String json = objectMapper.writeValueAsString(dto);
      return PubsubMessage.newBuilder()
          .setData(ByteString.copyFromUtf8(json))
          .putAttributes("contentType", "application/json")
          .build();
    } catch (IOException e) {
      throw new IllegalStateException("Failed to encode gateway action message", e);
    }
  }
}
