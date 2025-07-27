package org.muralis.batching.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@XmlRootElement(name = "invalid-beneficiary")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvalidBeneficiary {

    @XmlElement(name = "beneficiary")
    private Beneficiary beneficiary;

    @XmlElementWrapper(name = "errors")
    @XmlElement(name = "error")
    private List<String> errors;
}
