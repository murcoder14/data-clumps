package org.muralis.batching.validator;

import io.vavr.collection.CharSeq;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.muralis.batching.model.Address;

/**
 * A validator for {@link Address} objects.
 * This class implements the {@link Validator} interface and provides specific validation logic for address fields.
 *
 * @author T Murali
 * @version 1.0
 */
public class AddressValidator implements Validator<Address> {

    private static final String VALID_STREET_CHARS = "[a-zA-Z0-9. ]";
    private static final String VALID_CITY_CHARS = "[a-zA-Z ]";
    private static final String VALID_STATE_CHARS = "[A-Z]";
    private static final String VALID_ZIP_CHARS = "[0-9]";

    /**
     * Validates an {@link Address} object.
     * It combines the results of validating each individual field.
     *
     * @param address The address to validate.
     * @return A {@link Validation} instance containing either a sequence of errors or the valid address.
     */
    @Override
    public Validation<Seq<String>, Address> validate(Address address) {
        return Validation.combine(
                validateStreet(address.getStreet()),
                validateCity(address.getCity()),
                validateState(address.getState()),
                validateZip(address.getZip())
        ).ap((street, city, state, zip) -> address)
                .mapError(errors -> errors.map(error -> "Address: " + error));
    }

    /**
     * Validates a street address.
     * It checks that the street is not blank and contains only valid characters.
     *
     * @param street The street address to validate.
     * @return A {@link Validation} result.
     */
    private Validation<String, String> validateStreet(String street) {
        if (street == null || street.isBlank()) {
            return Validation.invalid("Street cannot be blank");
        }
        return CharSeq.of(street).replaceAll(VALID_STREET_CHARS, "").transform(seq -> seq.isEmpty()
                ? Validation.valid(street)
                : Validation.invalid("Street contains invalid characters: '"
                + seq.distinct().sorted() + "'"));
    }

    /**
     * Validates a city.
     * It checks that the city is not blank and contains only valid characters.
     *
     * @param city The city to validate.
     * @return A {@link Validation} result.
     */
    private Validation<String, String> validateCity(String city) {
        if (city == null || city.isBlank()) {
            return Validation.invalid("City cannot be blank");
        }
        return CharSeq.of(city).replaceAll(VALID_CITY_CHARS, "").transform(seq -> seq.isEmpty()
                ? Validation.valid(city)
                : Validation.invalid("City contains invalid characters: '"
                + seq.distinct().sorted() + "'"));
    }

    /**
     * Validates a state.
     * It checks that the state is not blank, is 2 characters long, and contains only valid characters.
     *
     * @param state The state to validate.
     * @return A {@link Validation} result.
     */
    private Validation<String, String> validateState(String state) {
        if (state == null || state.isBlank()) {
            return Validation.invalid("State cannot be blank");
        }
        if (state.length() != 2) {
            return Validation.invalid("State must be 2 characters long");
        }
        return CharSeq.of(state).replaceAll(VALID_STATE_CHARS, "").transform(seq -> seq.isEmpty()
                ? Validation.valid(state)
                : Validation.invalid("State contains invalid characters: '"
                + seq.distinct().sorted() + "'"));
    }

    /**
     * Validates a zip code.
     * It checks that the zip code is not blank, is 5 characters long, and contains only valid characters.
     *
     * @param zip The zip code to validate.
     * @return A {@link Validation} result.
     */
    private Validation<String, String> validateZip(String zip) {
        if (zip == null || zip.isBlank()) {
            return Validation.invalid("Zip cannot be blank");
        }
        if (zip.length() != 5) {
            return Validation.invalid("Zip must be 5 digits long");
        }
        return CharSeq.of(zip).replaceAll(VALID_ZIP_CHARS, "").transform(seq -> seq.isEmpty()
                ? Validation.valid(zip)
                : Validation.invalid("Zip contains invalid characters: '"
                + seq.distinct().sorted() + "'"));
    }
}
