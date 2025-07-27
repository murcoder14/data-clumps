package org.muralis.batching.grpc;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * The server-side implementation of the BeneficiaryStreamer gRPC service.
 * This service listens for incoming streams of records from clients (like the {@link org.muralis.batching.writer.GrpcItemWriter})
 * and processes them. In this implementation, it logs the received records and returns a summary.
 *
 * @author T Murali
 * @version 1.0
 */
@Slf4j
@GrpcService
public class BeneficiaryStreamerService extends BeneficiaryStreamerGrpc.BeneficiaryStreamerImplBase {

    /**
     * Implements the client-streaming `streamRecords` RPC method.
     * This method returns a {@link StreamObserver} to the client, which the client uses to send a stream of records.
     * The server processes these records as they arrive.
     *
     * @param responseObserver An observer to which the server sends its response (a {@link StreamSummary}) once the client finishes streaming.
     * @return A {@link StreamObserver} for the client to write records to.
     */
    @Override
    public StreamObserver<Record> streamRecords(StreamObserver<StreamSummary> responseObserver) {
        return new StreamObserver<>() {
            private int validRecordsProcessed = 0;
            private int invalidRecordsProcessed = 0;

            /**
             * Processes the next record in the stream from the client.
             * @param record The record sent by the client.
             */
            @Override
            public void onNext(Record record) {
                if (record.hasValidRecord()) {
                    validRecordsProcessed++;
                    log.info("Received valid record: {}", record.getValidRecord().getBeneficiary().getPersonId());
                } else if (record.hasInvalidRecord()) {
                    invalidRecordsProcessed++;
                    log.warn("Received invalid record: {} with errors: {}",
                            record.getInvalidRecord().getBeneficiary().getPersonId(),
                            record.getInvalidRecord().getErrorsList());
                }
            }

            /**
             * Handles errors from the client stream.
             * @param t The error thrown by the client.
             */
            @Override
            public void onError(Throwable t) {
                log.error("Error during record stream", t);
            }

            /**
             * Called when the client has finished sending all its records.
             * The server sends the final summary and closes the connection.
             */
            @Override
            public void onCompleted() {
                StreamSummary summary = StreamSummary.newBuilder()
                        .setValidRecordsProcessed(validRecordsProcessed)
                        .setInvalidRecordsProcessed(invalidRecordsProcessed)
                        .build();
                responseObserver.onNext(summary);
                responseObserver.onCompleted();
                log.info("Record stream completed. Processed {} valid and {} invalid records.",
                        validRecordsProcessed, invalidRecordsProcessed);
            }
        };
    }
}
