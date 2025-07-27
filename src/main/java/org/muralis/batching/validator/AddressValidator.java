package org.muralis.batching.validator;

import io.vavr.collection.CharSeq;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.muralis.batching.model.Address;

public class AddressValidator implements Validator<Address> {

    private static final String VALID_STREET_CHARS = "[a-zA-Z0-9. ]";
    private static final String VALID_CITY_CHARS = "[a-zA-Z ]";
    private static final String VALID_STATE_CHARS = "[A-Z]";
    private static final String VALID_ZIP_CHARS = "[0-9]";

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

    private Validation<String, String> validateStreet(String street) {
        if (street == null || street.isBlank()) {
            return Validation.invalid("Street cannot be blank");
        }
        return CharSeq.of(street).replaceAll(VALID_STREET_CHARS, "").transform(seq -> seq.isEmpty()
                ? Validation.valid(street)
                : Validation.invalid("Street contains invalid characters: '"
                + seq.distinct().sorted() + "'"));
    }

    private Validation<String, String> validateCity(String city) {
        if (city == null || city.isBlank()) {
            return Validation.invalid("City cannot be blank");
        }
        return CharSeq.of(city).replaceAll(VALID_CITY_CHARS, "").transform(seq -> seq.isEmpty()
                ? Validation.valid(city)
                : Validation.invalid("City contains invalid characters: '"
                + seq.distinct().sorted() + "'"));
    }

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
