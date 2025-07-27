package org.muralis.batching.validator;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;

/**
 * A generic functional interface for validating objects that implement {@link Validatable}.
 * It takes an object of type T and returns a {@link Validation} result,
 * which is either a sequence of error strings or the valid object.
 *
 * @param <T> The type of the object to validate, which must implement {@link Validatable}.
 * @author T Murali
 * @version 1.0
 */
@FunctionalInterface
public interface Validator<T extends Validatable> {

    /**
     * Validates the given object.
     *
     * @param object The object to validate.
     * @return A {@link Validation} instance containing either a sequence of errors or the valid object.
     */
    Validation<Seq<String>, T> validate(T object);
}
