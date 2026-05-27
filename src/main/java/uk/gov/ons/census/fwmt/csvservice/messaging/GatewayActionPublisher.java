package uk.gov.ons.census.fwmt.csvservice.messaging;

import uk.gov.ons.census.fwmt.canonical.v1.CreateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;

/**
 * Port for publishing canonical create-job actions to the gateway actions lane.
 */
public interface GatewayActionPublisher {

  void sendMessage(CreateFieldWorkerJobRequest dto) throws GatewayException;
}
