package org.muralis.batching.grpc;

import org.muralis.batching.model.Address;
import org.muralis.batching.model.Beneficiary;

import java.util.stream.Collectors;

/**
 * A utility class for mapping domain models to their corresponding Protobuf messages.
 * This class provides static methods to convert {@link Beneficiary} and {@link Address}
 * objects into {@link BeneficiaryMessage} and {@link AddressMessage} respectively.
 *
 * @author T Murali
 * @version 1.0
 */
public class RecordMapper {

    /**
     * Converts a {@link Beneficiary} domain object into a {@link BeneficiaryMessage} Protobuf message.
     * This method handles null checks and recursively converts nested objects like addresses and dependents.
     *
     * @param beneficiary The source {@link Beneficiary} object.
     * @return The corresponding {@link BeneficiaryMessage}.
     */
    public static BeneficiaryMessage toBeneficiaryMessage(Beneficiary beneficiary) {
        if (beneficiary == null) {
            return BeneficiaryMessage.newBuilder().build();
        }

        BeneficiaryMessage.Builder builder = BeneficiaryMessage.newBuilder();

        if (beneficiary.getPersonId() != null) {
            builder.setPersonId(beneficiary.getPersonId());
        }
        if (beneficiary.getFirstName() != null) {
            builder.setFirstName(beneficiary.getFirstName());
        }
        if (beneficiary.getLastName() != null) {
            builder.setLastName(beneficiary.getLastName());
        }
        if (beneficiary.getAddress() != null) {
            builder.setAddress(toAddressMessage(beneficiary.getAddress()));
        }
        if (beneficiary.getDependents() != null) {
            builder.addAllDependents(beneficiary.getDependents().stream()
                    .map(RecordMapper::toBeneficiaryMessage)
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }

    /**
     * Converts an {@link Address} domain object into an {@link AddressMessage} Protobuf message.
     *
     * @param address The source {@link Address} object.
     * @return The corresponding {@link AddressMessage}.
     */
    public static AddressMessage toAddressMessage(Address address) {
        if (address == null) {
            return AddressMessage.newBuilder().build();
        }

        AddressMessage.Builder builder = AddressMessage.newBuilder();

        if (address.getStreet() != null) {
            builder.setStreet(address.getStreet());
        }
        if (address.getCity() != null) {
            builder.setCity(address.getCity());
        }
        if (address.getState() != null) {
            builder.setState(address.getState());
        }
        if (address.getZip() != null) {
            builder.setZip(address.getZip());
        }

        return builder.build();
    }
}
