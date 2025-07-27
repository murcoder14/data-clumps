package org.muralis.batching.validator;

import io.vavr.collection.CharSeq;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.muralis.batching.model.Beneficiary;

/**
 * A validator for {@link Beneficiary} objects.
 * This class implements the {@link Validator} interface and provides specific validation logic
 * for the beneficiary's personal information.
 *
 * @author T Murali
 * @version 1.0
 */
public class BeneficiaryValidator implements Validator<Beneficiary> {

    private static final String VALID_NAME_CHARS = "[a-zA-Z ]";
    private static final long MIN_PERSON_ID = 1L;

    /**
     * Validates a {@link Beneficiary} object.
     * It combines the results of validating the person ID, first name, and last name.
     *
     * @param beneficiary The beneficiary to validate.
     * @return A {@link Validation} instance containing either a sequence of errors or the valid beneficiary.
     */
    @Override
    public Validation<Seq<String>, Beneficiary> validate(Beneficiary beneficiary) {
        return Validation.combine(
                validatePersonId(beneficiary.getPersonId()),
                validateName(beneficiary.getFirstName()).mapError(e -> "First " + e),
                validateName(beneficiary.getLastName()).mapError(e -> "Last " + e)
        ).ap((personId, firstName, lastName) -> beneficiary);
    }

    /**
     * Validates that a given string name is not blank and contains only valid characters.
     *
     * @param name The string name to validate.
     * @return A {@link Validation} result.
     */
    private Validation<String, String> validateName(String name) {
        if (name == null || name.isBlank()) {
            return Validation.invalid("Name cannot be blank");
        }
        return CharSeq.of(name).replaceAll(VALID_NAME_CHARS, "").transform(seq -> seq.isEmpty()
                ? Validation.valid(name)
                : Validation.invalid("Name contains invalid characters: '"
                + seq.distinct().sorted() + "'"));
    }

    /**
     * Validates the person ID.
     * It checks that the ID is not null and is at least the minimum allowed value.
     *
     * @param personId The person ID to validate.
     * @return A {@link Validation} result.
     */
    private Validation<String, Long> validatePersonId(Long personId) {
        return personId != null && personId >= MIN_PERSON_ID
                ? Validation.valid(personId)
                : Validation.invalid("Person ID must be at least " + MIN_PERSON_ID);
    }
}
