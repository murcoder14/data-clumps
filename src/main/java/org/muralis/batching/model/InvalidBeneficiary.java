package org.muralis.batching.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * A wrapper class for a {@link Beneficiary} that has failed validation.
 * It holds the original beneficiary object and a list of validation error messages.
 *
 * @author T Murali
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvalidBeneficiary {

    private Beneficiary beneficiary;

    private List<String> errors;
}
