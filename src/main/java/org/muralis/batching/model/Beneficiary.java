package org.muralis.batching.model;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@XmlRootElement(name = "beneficiary")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "personId", "firstName", "lastName", "address", "dependents" })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Beneficiary {

    private Long personId;
    private String firstName;
    private String lastName;

    private Address address;

    @XmlElementWrapper(name = "dependents")
    @XmlElement(name = "beneficiary")
    private List<Beneficiary> dependents;
}
