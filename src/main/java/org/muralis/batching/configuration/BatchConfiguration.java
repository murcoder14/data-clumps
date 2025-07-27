package org.muralis.batching.configuration;

import org.muralis.batching.model.Address;
import org.muralis.batching.model.Beneficiary;
import org.muralis.batching.model.InvalidBeneficiary;
import org.muralis.batching.processor.BeneficiaryValidationProcessor;
import org.muralis.batching.reader.CustomerFileReader;
import org.muralis.batching.validator.*;
import org.muralis.batching.writer.BeneficiaryClassifierCompositeWriter;
import org.muralis.batching.writer.SafeStaxEventItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
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
import java.util.HashMap;
import java.util.Map;

@Configuration
public class BatchConfiguration {

    @Value("${app.input.file}")
    private Resource inputFile;

    @Value("${app.output.file.path}")
    private String outputFile;

    @Value("${app.output.file.path.invalid}")
    private String invalidOutputFile;

    @Bean
    public Validator<Validatable> compositeValidator(Map<Class<? extends Validatable>, Validator<?>> validatorMap) {
        return new CompositeValidator(validatorMap);
    }

    @Bean
    public Map<Class<? extends Validatable>, Validator<?>> validatorMap() {
        Map<Class<? extends Validatable>, Validator<?>> map = new HashMap<>();
        map.put(Beneficiary.class, new BeneficiaryValidator());
        map.put(Address.class, new AddressValidator());
        return map;
    }

    @Bean
    public CustomerFileReader customerFileReader() {
        return new CustomerFileReader(inputFile);
    }

    @Bean(destroyMethod = "")
    public SafeStaxEventItemWriter<Beneficiary> customerItemWriter() throws IOException {
        return new SafeStaxEventItemWriter<>(createWriter(outputFile, "beneficiaries", Beneficiary.class));
    }

    @Bean(destroyMethod = "")
    public SafeStaxEventItemWriter<InvalidBeneficiary> invalidCustomerItemWriter() throws IOException {
        return new SafeStaxEventItemWriter<>(createWriter(invalidOutputFile, "invalid-beneficiaries", InvalidBeneficiary.class));
    }

    private <T> StaxEventItemWriter<T> createWriter(String filePath, String rootTagName, Class<T> clazz) throws IOException {
        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());

        StaxEventItemWriter<T> writer = new StaxEventItemWriter<>();
        writer.setResource(new FileSystemResource(filePath));
        writer.setMarshaller(marshaller(clazz));
        writer.setRootTagName(rootTagName);
        writer.setSaveState(false);
        return writer;
    }

    private Jaxb2Marshaller marshaller(Class<?> type) {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(type);
        return marshaller;
    }

    @Bean
    public BeneficiaryClassifierCompositeWriter compositeItemWriter(
            SafeStaxEventItemWriter<Beneficiary> customerItemWriter,
            SafeStaxEventItemWriter<InvalidBeneficiary> invalidCustomerItemWriter) {
        return new BeneficiaryClassifierCompositeWriter((ItemWriter) customerItemWriter, (ItemWriter) invalidCustomerItemWriter);
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                      BeneficiaryClassifierCompositeWriter compositeItemWriter, CustomerFileReader customerFileReader,
                      BeneficiaryValidationProcessor validationProcessor) {
        return new StepBuilder("step1", jobRepository)
                .<Beneficiary, Object>chunk(10, transactionManager)
                .reader(customerFileReader)
                .processor(validationProcessor)
                .writer(compositeItemWriter)
                .build();
    }

    @Bean
    public Job customerFileLoadJob(JobRepository jobRepository, Step step1) {
        return new JobBuilder("customer-file-load", jobRepository)
                .start(step1)
                .build();
    }
}
