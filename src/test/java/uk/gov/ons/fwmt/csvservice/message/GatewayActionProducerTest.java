package uk.gov.ons.fwmt.csvservice.message;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import uk.gov.ons.census.fwmt.common.canonical.v1.CreateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.csvservice.messaging.pubsub.PubSubGatewayActionPublisher;
import uk.gov.ons.fwmt.csvservice.helper.FieldWorkerRequestMessageBuilder;

public class GatewayActionProducerTest {

  @Test
  public void suppressesCreateJobRequestAndLogsError() throws GatewayException {
    FieldWorkerRequestMessageBuilder messageBuilder = new FieldWorkerRequestMessageBuilder();
    CreateFieldWorkerJobRequest createJobRequest = messageBuilder.buildCreateFieldWorkerJobRequestCCS();
    Logger logger = (Logger) LoggerFactory.getLogger(PubSubGatewayActionPublisher.class);
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);

    try {
      new PubSubGatewayActionPublisher().sendMessage(createJobRequest);

      assertThat(appender.list).hasSize(1);
      ILoggingEvent event = appender.list.get(0);
      assertThat(event.getLevel()).isEqualTo(Level.ERROR);
      assertThat(event.getFormattedMessage())
          .contains("CSV create-job request suppressed", "publishing is disabled")
          .contains(createJobRequest.getCaseId().toString());
    } finally {
      logger.detachAppender(appender);
      appender.stop();
    }
  }
}
