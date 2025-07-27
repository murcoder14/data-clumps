package org.muralis.batching.validator;

import io.vavr.collection.CharSeq;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.muralis.batching.model.Beneficiary;

public class BeneficiaryValidator implements Validator<Beneficiary> {

    private static final String VALID_NAME_CHARS = "[a-zA-Z ]";
    private static final long MIN_PERSON_ID = 1L;

    @Override
    public Validation<Seq<String>, Beneficiary> validate(Beneficiary beneficiary) {
        return Validation.combine(
                validatePersonId(beneficiary.getPersonId()),
                validateName(beneficiary.getFirstName()).mapError(e -> "First " + e),
                validateName(beneficiary.getLastName()).mapError(e -> "Last " + e)
        ).ap((personId, firstName, lastName) -> beneficiary);
    }

    private Validation<String, String> validateName(String name) {
        if (name == null || name.isBlank()) {
            return Validation.invalid("Name cannot be blank");
        }
        return CharSeq.of(name).replaceAll(VALID_NAME_CHARS, "").transform(seq -> seq.isEmpty()
                ? Validation.valid(name)
                : Validation.invalid("Name contains invalid characters: '"
                + seq.distinct().sorted() + "'"));
    }

    private Validation<String, Long> validatePersonId(Long personId) {
        return personId != null && personId >= MIN_PERSON_ID
                ? Validation.valid(personId)
                : Validation.invalid("Person ID must be at least " + MIN_PERSON_ID);
    }
}
