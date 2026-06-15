package uk.gov.ons.fwmt.csvservice.message;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.ons.census.fwmt.canonical.v1.CreateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.csvservice.messaging.GatewayActionJsonCodec;
import uk.gov.ons.census.fwmt.csvservice.messaging.pubsub.PubSubGatewayActionPublisher;
import uk.gov.ons.fwmt.csvservice.helper.FieldWorkerRequestMessageBuilder;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;

@RunWith(MockitoJUnitRunner.class)
public class GatewayActionProducerTest {

  @InjectMocks
  private PubSubGatewayActionPublisher gatewayActionProducer;

  @Mock
  private PubSubTemplate pubSubTemplate;

  @Mock
  private GatewayActionJsonCodec codec;

  @Test(expected = IllegalStateException.class)
  public void sendBadMessage() throws GatewayException {
    FieldWorkerRequestMessageBuilder messageBuilder = new FieldWorkerRequestMessageBuilder();
    CreateFieldWorkerJobRequest createJobRequest = messageBuilder.buildCreateFieldWorkerJobRequestCCS();
    when(codec.toPubsubMessage(eq(createJobRequest))).thenThrow(new IllegalStateException("Failed to encode"));

    gatewayActionProducer.sendMessage(createJobRequest);
  }
}
