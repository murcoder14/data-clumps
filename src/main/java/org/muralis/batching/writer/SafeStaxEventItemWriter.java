package org.muralis.batching.writer;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.xml.StaxEventItemWriter;

public class SafeStaxEventItemWriter<T> implements ItemWriter<T>, ItemStream {

    private final StaxEventItemWriter<T> delegate;
    private boolean opened = false;

    public SafeStaxEventItemWriter(StaxEventItemWriter<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void write(Chunk<? extends T> chunk) throws Exception {
        delegate.write(chunk);
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        if (!opened) {
            delegate.open(executionContext);
            opened = true;
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        if (opened) {
            delegate.update(executionContext);
        }
    }

    @Override
    public void close() throws ItemStreamException {
        if (opened) {
            try {
                delegate.close();
            } catch (Exception e) {
                // Suppress close exceptions to prevent shutdown warnings
            } finally {
                opened = false;
            }
        }
    }
}
