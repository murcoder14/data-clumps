package org.muralis.batching.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.muralis.batching.validator.Validatable;

/**
 * Represents a physical address.
 * This class is a simple data holder for address information and is marked as {@link Validatable}.
 *
 * @author T Murali
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address implements Validatable {
    private String street;
    private String city;
    private String state;
    private String zip;
}
