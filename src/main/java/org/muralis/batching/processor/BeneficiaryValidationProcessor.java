package org.muralis.batching.processor;

import lombok.RequiredArgsConstructor;
import org.muralis.batching.model.Beneficiary;
import org.muralis.batching.model.InvalidBeneficiary;
import org.muralis.batching.validator.Validatable;
import org.muralis.batching.validator.Validator;
import org.springframework.batch.item.ItemProcessor;

/**
 * A Spring Batch {@link ItemProcessor} that validates {@link Beneficiary} records.
 * This processor uses a generic {@link Validator} to perform the validation.
 * If a beneficiary is valid, it is passed through. If it is invalid, an
 * {@link InvalidBeneficiary} object is created containing the original record and the validation errors.
 *
 * @author T Murali
 * @version 1.0
 */
@RequiredArgsConstructor
public class BeneficiaryValidationProcessor implements ItemProcessor<Beneficiary, Object> {

    private final Validator<Validatable> validator;

    /**
     * Processes and validates a single {@link Beneficiary} item.
     *
     * @param item The beneficiary to process.
     * @return Either the original {@link Beneficiary} if valid, or an {@link InvalidBeneficiary} if invalid.
     * @throws Exception if any error occurs during processing.
     */
    @Override
    public Object process(Beneficiary item) throws Exception {
        return validator.validate(item)
                .fold(
                        errors -> new InvalidBeneficiary(item, errors.toJavaList()),
                        valid -> item
                );
    }
}
