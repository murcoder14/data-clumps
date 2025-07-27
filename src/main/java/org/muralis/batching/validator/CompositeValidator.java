package org.muralis.batching.validator;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.muralis.batching.validator.Validatable;
import org.muralis.batching.model.Address;
import org.muralis.batching.model.Beneficiary;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A composite validator that orchestrates the validation of {@link Validatable} objects.
 * This validator can validate a main object and recursively validate any fields that are also
 * {@link Validatable} (e.g., a Beneficiary's Address) or collections of {@link Validatable} objects
 * (e.g., a Beneficiary's dependents).
 *
 * @author T Murali
 * @version 1.0
 */
public class CompositeValidator implements Validator<Validatable> {

    private final Map<Class<? extends Validatable>, Validator<?>> validatorMap;

    /**
     * Constructs a new CompositeValidator with a map of validators.
     *
     * @param validatorMap A map where keys are {@link Validatable} classes and values are their corresponding {@link Validator} instances.
     */
    public CompositeValidator(Map<Class<? extends Validatable>, Validator<?>> validatorMap) {
        this.validatorMap = validatorMap;
    }

    /**
     * Validates a {@link Validatable} object by applying the appropriate validator from the map
     * and then recursively validating its fields.
     *
     * @param validatable The object to validate.
     * @return A {@link Validation} instance containing either a sequence of all found errors or the valid object.
     */
    @Override
    public Validation<Seq<String>, Validatable> validate(Validatable validatable) {
        List<String> allErrors = new ArrayList<>();

        // Step 1: Validate the main object itself
        Validator<Validatable> mainValidator = (Validator<Validatable>) validatorMap.get(validatable.getClass());
        if (mainValidator != null) {
            mainValidator.validate(validatable).fold(
                    errors -> allErrors.addAll(errors.toJavaList()),
                    valid -> null
            );
        }

        // Step 2: Recursively validate fields
        for (Field field : validatable.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object fieldValue = field.get(validatable);

                if (fieldValue instanceof Validatable) {
                    validate( (Validatable) fieldValue).fold(
                            errors -> allErrors.addAll(errors.toJavaList()),
                            valid -> null
                    );
                }

                if (fieldValue instanceof List) {
                    for (Object item : (List<?>) fieldValue) {
                        if (item instanceof Validatable) {
                            validate((Validatable) item).fold(
                                    errors -> allErrors.addAll(errors.toJavaList()),
                                    valid -> null
                            );
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                // Handle exception, e.g., log it
            }
        }

        if (allErrors.isEmpty()) {
            return Validation.valid(validatable);
        } else {
            return Validation.invalid(io.vavr.collection.List.ofAll(allErrors));
        }
    }
}
