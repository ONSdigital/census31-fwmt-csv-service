package uk.gov.ons.census.fwmt.csvservice.messaging.pubsub;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.canonical.v1.CreateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.messaging.MessagingProperties;
import uk.gov.ons.census.fwmt.csvservice.messaging.GatewayActionPublisher;

@Component
@ConditionalOnProperty(name = MessagingProperties.PROVIDER, havingValue = MessagingProperties.PROVIDER_PUBSUB)
public class PubSubGatewayActionPublisher implements GatewayActionPublisher {

  @Override
  public void sendMessage(CreateFieldWorkerJobRequest dto) throws GatewayException {
    throw new UnsupportedOperationException(
        "Pub/Sub gateway actions publish is not implemented (Stage 2). Set app.messaging.provider=rabbit.");
  }
}
