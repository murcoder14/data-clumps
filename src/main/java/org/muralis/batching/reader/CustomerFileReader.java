package org.muralis.batching.reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.muralis.batching.model.Address;
import org.muralis.batching.model.Beneficiary;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerFileReader implements ItemReader<Beneficiary>, InitializingBean {

    private Resource resource;
    private Iterator<Beneficiary> beneficiaryIterator;

    public CustomerFileReader(Resource resource) {
        this.resource = resource;
    }

    @Override
    public Beneficiary read() {
        if (beneficiaryIterator != null && beneficiaryIterator.hasNext()) {
            return beneficiaryIterator.next();
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        List<CSVRecord> records = parseFile();
        List<Beneficiary> beneficiaries = transformToBeneficiaries(records);
        this.beneficiaryIterator = beneficiaries.iterator();
    }

    private List<CSVRecord> parseFile() throws Exception {
        List<CSVRecord> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            List<String> lines = reader.lines()
                    .filter(line -> line.startsWith("~PII~"))
                    .collect(Collectors.toList());

            for (String line : lines) {
                String data = line.substring("~PII~".length());
                try (CSVParser parser = CSVParser.parse(data, CSVFormat.DEFAULT.withDelimiter('|').withTrim())) {
                    records.addAll(parser.getRecords());
                }
            }
        }
        return records;
    }

    private List<Beneficiary> transformToBeneficiaries(List<CSVRecord> records) {
        List<Beneficiary> beneficiaries = new ArrayList<>();
        if (records.isEmpty()) {
            return beneficiaries;
        }

        List<CSVRecord> familyRecords = new ArrayList<>();
        String currentPersonId = getPersonIdFromRecord(records.get(0));

        for (CSVRecord record : records) {
            if (!getPersonIdFromRecord(record).equals(currentPersonId)) {
                beneficiaries.add(createFamily(familyRecords));
                familyRecords.clear();
                currentPersonId = getPersonIdFromRecord(record);
            }
            familyRecords.add(record);
        }
        beneficiaries.add(createFamily(familyRecords));

        return beneficiaries;
    }

    private Beneficiary createFamily(List<CSVRecord> familyRecords) {
        CSVRecord primaryRecord = familyRecords.get(0);
        Beneficiary primary = buildBeneficiary(primaryRecord);

        List<Beneficiary> dependents = familyRecords.stream()
                .skip(1)
                .map(this::buildBeneficiary)
                .collect(Collectors.toList());

        primary.setDependents(dependents);
        return primary;
    }

    private Beneficiary buildBeneficiary(CSVRecord record) {
        return Beneficiary.builder()
                .personId(Long.parseLong(record.get(0)))
                .firstName(record.get(1))
                .lastName(record.get(2))
                .address(Address.builder()
                        .address(record.get(3))
                        .city(record.get(4))
                        .state(record.get(5))
                        .zip(record.get(6))
                        .build())
                .build();
    }

    private String getPersonIdFromRecord(CSVRecord record) {
        return record.get(0);
    }
}
