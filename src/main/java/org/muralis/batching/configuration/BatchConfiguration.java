package org.muralis.batching.configuration;

import org.muralis.batching.model.Beneficiary;
import org.muralis.batching.reader.CustomerFileReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class BatchConfiguration {

    @Value("${app.input.file}")
    private Resource inputFile;

    @Value("${app.output.file.path}")
    private String outputFile;

    @Bean
    public CustomerFileReader customerFileReader() {
        return new CustomerFileReader(inputFile);
    }

    @Bean
    public StaxEventItemWriter<Beneficiary> customerItemWriter() throws IOException {
        Path path = Paths.get(outputFile);
        Files.createDirectories(path.getParent());

        StaxEventItemWriter<Beneficiary> writer = new StaxEventItemWriter<>();
        writer.setResource(new FileSystemResource(outputFile));
        writer.setMarshaller(marshaller());
        writer.setRootTagName("beneficiaries");
        return writer;
    }

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(Beneficiary.class);
        return marshaller;
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager, StaxEventItemWriter<Beneficiary> customerItemWriter, CustomerFileReader customerFileReader) throws IOException {
        return new StepBuilder("step1", jobRepository)
                .<Beneficiary, Beneficiary>chunk(10, transactionManager)
                .reader(customerFileReader)
                .writer(customerItemWriter)
                .build();
    }

    @Bean
    public Job customerFileLoadJob(JobRepository jobRepository, Step step1) {
        return new JobBuilder("customer-file-load", jobRepository)
                .start(step1)
                .build();
    }
}
