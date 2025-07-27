package org.muralis.batching.configuration;

import org.muralis.batching.model.Address;
import org.muralis.batching.model.Beneficiary;
import org.muralis.batching.processor.BeneficiaryValidationProcessor;
import org.muralis.batching.reader.CustomerFileReader;
import org.muralis.batching.validator.*;
import org.muralis.batching.writer.GrpcItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

/**
 * Spring Batch configuration class for the customer data processing job.
 * This class defines the beans required to configure the job, including the reader,
 * processor, writer, and the steps that make up the job.
 *
 * @author T Murali
 * @version 1.0
 */
@Configuration
public class BatchConfiguration {

    @Value("${app.input.file}")
    private Resource resource;

    /**
     * Creates a bean for the {@link CustomerFileReader} which reads beneficiary data from the input file.
     *
     * @return An instance of {@link CustomerFileReader}.
     */
    @Bean
    public CustomerFileReader customerFileReader() {
        return new CustomerFileReader(resource);
    }

    /**
     * Creates a bean for the {@link BeneficiaryValidationProcessor}.
     * This processor validates each beneficiary record using the composite validator.
     *
     * @param validator The composite validator to be injected.
     * @return An instance of {@link BeneficiaryValidationProcessor}.
     */
    @Bean
    public BeneficiaryValidationProcessor beneficiaryValidationProcessor(Validator<Validatable> validator) {
        return new BeneficiaryValidationProcessor(validator);
    }

    /**
     * Creates a bean for the {@link CompositeValidator}.
     * This validator orchestrates the validation of different {@link Validatable} types.
     *
     * @param validatorMap A map of validators for specific types.
     * @return An instance of {@link CompositeValidator}.
     */
    @Bean
    public Validator<Validatable> compositeValidator(Map<Class<? extends Validatable>, Validator<?>> validatorMap) {
        return new CompositeValidator(validatorMap);
    }

    /**
     * Creates a map of validators to be used by the {@link CompositeValidator}.
     * This allows for easy extension by adding new validator beans to the map.
     *
     * @param beneficiaryValidator The validator for {@link Beneficiary} objects.
     * @param addressValidator     The validator for {@link Address} objects.
     * @return A map of {@link Validatable} classes to their corresponding validators.
     */
    @Bean
    public Map<Class<? extends Validatable>, Validator<?>> validatorMap(BeneficiaryValidator beneficiaryValidator, AddressValidator addressValidator) {
        return Map.of(
                Beneficiary.class, beneficiaryValidator,
                Address.class, addressValidator
        );
    }

    /**
     * Creates a bean for the {@link BeneficiaryValidator}.
     *
     * @return An instance of {@link BeneficiaryValidator}.
     */
    @Bean
    public BeneficiaryValidator beneficiaryValidator() {
        return new BeneficiaryValidator();
    }

    /**
     * Creates a bean for the {@link AddressValidator}.
     *
     * @return An instance of {@link AddressValidator}.
     */
    @Bean
    public AddressValidator addressValidator() {
        return new AddressValidator();
    }

    /**
     * Defines the main batch processing step.
     * This step reads, processes, and writes the beneficiary data in chunks.
     *
     * @param jobRepository      The repository for storing job metadata.
     * @param transactionManager The transaction manager for the step.
     * @param reader             The item reader for the step.
     * @param processor          The item processor for the step.
     * @param writer             The item writer for the step.
     * @return A configured {@link Step}.
     */
    @Bean
    public Step step1(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager,
                      CustomerFileReader reader,
                      BeneficiaryValidationProcessor processor,
                      GrpcItemWriter writer) {
        return new StepBuilder("step1", jobRepository)
                .<Beneficiary, Object>chunk(100, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    /**
     * Defines the main batch job.
     *
     * @param jobRepository The repository for storing job metadata.
     * @param step1         The main step of the job.
     * @return A configured {@link Job}.
     */
    @Bean
    public Job customerFileLoadJob(JobRepository jobRepository, Step step1) {
        return new JobBuilder("customer-file-load", jobRepository)
                .start(step1)
                .build();
    }
}
