package org.muralis.batching.writer;

import org.muralis.batching.model.Beneficiary;
import org.muralis.batching.model.InvalidBeneficiary;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemWriter;

public class BeneficiaryClassifierCompositeWriter implements ItemWriter<Object>, ItemStream {

    private final ItemWriter<Object> validBeneficiaryWriter;
    private final ItemWriter<Object> invalidBeneficiaryWriter;
    private boolean opened = false;

    public BeneficiaryClassifierCompositeWriter(ItemWriter<Object> validBeneficiaryWriter, ItemWriter<Object> invalidBeneficiaryWriter) {
        this.validBeneficiaryWriter = validBeneficiaryWriter;
        this.invalidBeneficiaryWriter = invalidBeneficiaryWriter;
    }

    @Override
    public void write(Chunk<? extends Object> chunk) throws Exception {
        Chunk<Object> validItems = new Chunk<>();
        Chunk<Object> invalidItems = new Chunk<>();

        for (Object item : chunk) {
            if (item instanceof Beneficiary) {
                validItems.add(item);
            } else if (item instanceof InvalidBeneficiary) {
                invalidItems.add(item);
            } else {
                throw new IllegalArgumentException("Unknown item type: " + item.getClass().getName());
            }
        }

        if (!validItems.isEmpty()) {
            validBeneficiaryWriter.write(validItems);
        }
        if (!invalidItems.isEmpty()) {
            invalidBeneficiaryWriter.write(invalidItems);
        }
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        if (!opened) {
            if (validBeneficiaryWriter instanceof ItemStream) {
                ((ItemStream) validBeneficiaryWriter).open(executionContext);
            }
            if (invalidBeneficiaryWriter instanceof ItemStream) {
                ((ItemStream) invalidBeneficiaryWriter).open(executionContext);
            }
            opened = true;
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        if (opened) {
            if (validBeneficiaryWriter instanceof ItemStream) {
                ((ItemStream) validBeneficiaryWriter).update(executionContext);
            }
            if (invalidBeneficiaryWriter instanceof ItemStream) {
                ((ItemStream) invalidBeneficiaryWriter).update(executionContext);
            }
        }
    }

    @Override
    public void close() throws ItemStreamException {
        if (opened) {
            try {
                if (validBeneficiaryWriter instanceof ItemStream) {
                    ((ItemStream) validBeneficiaryWriter).close();
                }
            } catch (Exception e) {
                // Log but don't propagate close exceptions
            }
            try {
                if (invalidBeneficiaryWriter instanceof ItemStream) {
                    ((ItemStream) invalidBeneficiaryWriter).close();
                }
            } catch (Exception e) {
                // Log but don't propagate close exceptions
            }
            opened = false;
        }
    }
}
