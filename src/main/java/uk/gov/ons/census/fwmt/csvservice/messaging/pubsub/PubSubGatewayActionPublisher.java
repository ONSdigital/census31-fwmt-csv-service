package uk.gov.ons.census.fwmt.csvservice.messaging.pubsub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.canonical.v1.CreateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.csvservice.messaging.GatewayActionPublisher;

@Slf4j
@Component
public class PubSubGatewayActionPublisher implements GatewayActionPublisher {

  @Override
  public void sendMessage(CreateFieldWorkerJobRequest dto) throws GatewayException {
    log.error(
      "CSV create-job request suppressed; publishing is disabled: caseId={}, actionType={}, "
        + "gatewayType={}, caseType={}, surveyType={}",
        dto.getCaseId(),
        dto.getActionType(),
        dto.getGatewayType(),
        dto.getCaseType(),
        dto.getSurveyType());
  }
}
