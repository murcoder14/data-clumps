package org.muralis.batching.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "address", "city", "state", "zip" })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private String address;
    private String city;
    private String state;
    private String zip;
}
