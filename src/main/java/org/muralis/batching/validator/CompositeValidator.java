package org.muralis.batching.validator;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static io.vavr.API.Invalid;
import static io.vavr.API.Valid;

public class CompositeValidator implements Validator<Validatable> {

    private final Map<Class<? extends Validatable>, Validator<?>> validatorMap;

    public CompositeValidator(Map<Class<? extends Validatable>, Validator<?>> validatorMap) {
        this.validatorMap = validatorMap;
    }

    @Override
    public Validation<Seq<String>, Validatable> validate(Validatable object) {
        java.util.List<String> errors = new ArrayList<>();
        validateRecursively(object, errors);
        if (errors.isEmpty()) {
            return Valid(object);
        } else {
            return Invalid(io.vavr.collection.List.ofAll(errors));
        }
    }

    @SuppressWarnings("unchecked")
    private void validateRecursively(Validatable object, java.util.List<String> errors) {
        if (object == null) {
            return;
        }

        // Apply the validator for the object itself
        Validator<Validatable> validator = (Validator<Validatable>) validatorMap.get(object.getClass());
        if (validator != null) {
            validator.validate(object).mapError(e -> errors.addAll(e.toJavaList()));
        }

        // Recursively validate nested fields
        for (Field field : object.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(object);
                if (fieldValue instanceof Validatable) {
                    validateRecursively((Validatable) fieldValue, errors);
                } else if (fieldValue instanceof Collection) {
                    for (Object item : (Collection<?>) fieldValue) {
                        if (item instanceof Validatable) {
                            validateRecursively((Validatable) item, errors);
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                errors.add("Error accessing field: " + field.getName());
            }
        }
    }
}
