package org.muralis.batching.processor;

import org.muralis.batching.model.Beneficiary;
import org.muralis.batching.model.InvalidBeneficiary;
import org.muralis.batching.validator.Validatable;
import org.muralis.batching.validator.Validator;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BeneficiaryValidationProcessor implements ItemProcessor<Beneficiary, Object> {

    private final Validator<Validatable> validator;

    @Autowired
    public BeneficiaryValidationProcessor(Validator<Validatable> validator) {
        this.validator = validator;
    }

    @Override
    public Object process(Beneficiary beneficiary) {
        return validator.validate(beneficiary)
                .fold(
                        errors -> InvalidBeneficiary.builder()
                                .beneficiary(beneficiary)
                                .errors(errors.toJavaList())
                                .build(),
                        validBeneficiary -> validBeneficiary
                );
    }
}
