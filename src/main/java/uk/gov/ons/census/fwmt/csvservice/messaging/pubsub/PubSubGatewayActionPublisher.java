package uk.gov.ons.census.fwmt.csvservice.messaging.pubsub;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.pubsub.v1.PubsubMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.canonical.v1.CreateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.csvservice.messaging.GatewayActionJsonCodec;
import uk.gov.ons.census.fwmt.csvservice.messaging.GatewayActionPublisher;

@Slf4j
@Component
@RequiredArgsConstructor
public class PubSubGatewayActionPublisher implements GatewayActionPublisher {

  private final PubSubTemplate pubSubTemplate;
  private final GatewayActionJsonCodec codec;

  @Value("${app.messaging.destinations.gatewayActions:Gateway.Actions.Exchange}")
  private String gatewayActionsTopic;

  @Override
  @Retryable
  public void sendMessage(CreateFieldWorkerJobRequest dto) throws GatewayException {
    PubsubMessage message = codec.toPubsubMessage(dto);
    log.debug("Publishing gateway action to topic {}", gatewayActionsTopic);
    pubSubTemplate.publish(gatewayActionsTopic, message);
  }
}
