package org.muralis.batching.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.muralis.batching.validator.Validatable;

import java.util.List;

/**
 * Represents a beneficiary, who can be a primary person or a dependent.
 * This class holds personal information, an address, and a list of dependents.
 * It is marked as {@link Validatable} to be used with the validation framework.
 *
 * @author T Murali
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Beneficiary implements Validatable {

    private Long personId;
    private String firstName;
    private String lastName;

    private Address address;

    private List<Beneficiary> dependents;
}
