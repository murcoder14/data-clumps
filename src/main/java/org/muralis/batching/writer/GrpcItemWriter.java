package org.muralis.batching.writer;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.muralis.batching.grpc.*;
import org.muralis.batching.model.Beneficiary;
import org.muralis.batching.model.InvalidBeneficiary;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A Spring Batch {@link ItemWriter} that sends records to a gRPC service.
 * This writer establishes a connection to a gRPC server and streams processed items
 * (both valid and invalid) as Protobuf messages. It handles the lifecycle of the
 * gRPC channel and the client-side stream for each chunk.
 *
 * @author T Murali
 * @version 1.0
 */
@Slf4j
@Component
public class GrpcItemWriter implements ItemWriter<Object> {

    @Value("${grpc.server.port:9090}")
    private int grpcPort;

    private ManagedChannel channel;
    private BeneficiaryStreamerGrpc.BeneficiaryStreamerStub asyncStub;

    /**
     * Initializes the gRPC channel and the asynchronous stub.
     * This method is called after the bean has been constructed.
     */
    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder.forAddress("localhost", grpcPort)
                .usePlaintext()
                .build();
        asyncStub = BeneficiaryStreamerGrpc.newStub(channel);
    }

    /**
     * Shuts down the gRPC channel gracefully.
     * This method is called just before the bean is destroyed.
     *
     * @throws InterruptedException if the channel shutdown is interrupted.
     */
    @PreDestroy
    public void destroy() throws InterruptedException {
        if (channel != null) {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    /**
     * Writes a chunk of items to the gRPC stream.
     * It converts each item into a Protobuf {@link org.muralis.batching.grpc.Record} and sends it to the server.
     * It waits for the server to acknowledge the completion of the stream for the current chunk.
     *
     * @param chunk The chunk of items to be written.
     * @throws Exception if any error occurs during writing or waiting for the stream to complete.
     */
    @Override
    public void write(Chunk<?> chunk) throws Exception {
        final CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<StreamSummary> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(StreamSummary summary) {
                log.info("gRPC stream summary received: Valid={}, Invalid={}",
                        summary.getValidRecordsProcessed(), summary.getInvalidRecordsProcessed());
            }

            @Override
            public void onError(Throwable t) {
                log.error("gRPC stream failed", t);
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                log.info("gRPC stream completed successfully.");
                finishLatch.countDown();
            }
        };

        StreamObserver<org.muralis.batching.grpc.Record> requestObserver = asyncStub.streamRecords(responseObserver);

        try {
            for (Object item : chunk.getItems()) {
                org.muralis.batching.grpc.Record.Builder recordBuilder = org.muralis.batching.grpc.Record.newBuilder();

                if (item instanceof InvalidBeneficiary invalid) {
                    InvalidRecord invalidRecord = InvalidRecord.newBuilder()
                            .setBeneficiary(RecordMapper.toBeneficiaryMessage(invalid.getBeneficiary()))
                            .addAllErrors(invalid.getErrors())
                            .build();
                    recordBuilder.setInvalidRecord(invalidRecord);
                } else if (item instanceof Beneficiary valid) {
                    ValidRecord validRecord = ValidRecord.newBuilder()
                            .setBeneficiary(RecordMapper.toBeneficiaryMessage(valid))
                            .build();
                    recordBuilder.setValidRecord(validRecord);
                }

                requestObserver.onNext(recordBuilder.build());
            }
        } catch (Exception e) {
            requestObserver.onError(e);
            throw e;
        }

        requestObserver.onCompleted();

        if (!finishLatch.await(1, TimeUnit.MINUTES)) {
            log.warn("gRPC stream did not finish in 1 minute.");
        }
    }
}
