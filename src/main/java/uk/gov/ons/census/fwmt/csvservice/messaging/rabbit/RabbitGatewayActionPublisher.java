package uk.gov.ons.census.fwmt.csvservice.messaging.rabbit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.canonical.v1.CreateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.messaging.MessagingProperties;
import uk.gov.ons.census.fwmt.csvservice.config.GatewayActionsQueueConfig;
import uk.gov.ons.census.fwmt.csvservice.config.GatewayEventsConfig;
import uk.gov.ons.census.fwmt.csvservice.messaging.GatewayActionPublisher;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;

@Component
@ConditionalOnProperty(name = MessagingProperties.PROVIDER, havingValue = MessagingProperties.PROVIDER_RABBIT, matchIfMissing = true)
public class RabbitGatewayActionPublisher implements GatewayActionPublisher {

  private final RabbitTemplate rabbitTemplate;
  private final DirectExchange gatewayActionsExchange;
  private final ObjectMapper objectMapper;
  private final GatewayEventManager gatewayEventManager;

  public RabbitGatewayActionPublisher(
      RabbitTemplate rabbitTemplate,
      @Qualifier("gatewayActionsExchange") DirectExchange gatewayActionsExchange,
      ObjectMapper objectMapper,
      GatewayEventManager gatewayEventManager) {
    this.rabbitTemplate = rabbitTemplate;
    this.gatewayActionsExchange = gatewayActionsExchange;
    this.objectMapper = objectMapper;
    this.gatewayEventManager = gatewayEventManager;
  }

  @Override
  @Retryable
  public void sendMessage(CreateFieldWorkerJobRequest dto) throws GatewayException {
    String jsonJobRequest = convertToJson(dto);
    Message gatewayMessage = convertJsonToMessage(jsonJobRequest);
    rabbitTemplate.convertAndSend(
        gatewayActionsExchange.getName(),
        GatewayActionsQueueConfig.GATEWAY_ACTIONS_ROUTING_KEY,
        gatewayMessage);
  }

  private String convertToJson(CreateFieldWorkerJobRequest dto) throws GatewayException {
    try {
      return objectMapper.writeValueAsString(dto);
    } catch (JsonProcessingException e) {
      String msg = "Failed to process JSON.";
      gatewayEventManager.triggerErrorEvent(
          getClass(), e, msg, String.valueOf(dto.getCaseId()), GatewayEventsConfig.FAILED_TO_MARSHALL_CANONICAL);
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, msg, e);
    }
  }

  private static Message convertJsonToMessage(String messageJson) {
    MessageProperties messageProperties = new MessageProperties();
    messageProperties.setContentType("application/json");
    MessageConverter messageConverter = new Jackson2JsonMessageConverter();
    return messageConverter.toMessage(messageJson, messageProperties);
  }
}
