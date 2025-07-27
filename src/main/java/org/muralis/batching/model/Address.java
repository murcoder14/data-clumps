package org.muralis.batching.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.muralis.batching.validator.Validatable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "street", "city", "state", "zip" })
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
