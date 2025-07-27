package org.muralis.batching.validator;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;

/**
 * A generic interface for validating objects that implement the {@link Validatable} interface.
 *
 * @param <T> the type of the object to validate
 */
@FunctionalInterface
public interface Validator<T extends Validatable> {

    /**
     * Validates the given object.
     *
     * @param object the object to validate
     * @return a {@link Validation} containing either a sequence of error messages or the valid object
     */
    Validation<Seq<String>, T> validate(T object);
}
