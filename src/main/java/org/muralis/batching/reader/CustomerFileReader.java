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

/**
 * A Spring Batch {@link ItemReader} for reading customer data from a CSV file.
 * This reader is initialized with a resource (the input file) and reads the entire
 * file into a list of {@link Beneficiary} objects upon initialization. The {@link #read()}
 * method then iterates over this list.
 *
 * @author T Murali
 * @version 1.0
 */
public class CustomerFileReader implements ItemReader<Beneficiary>, InitializingBean {

    private Resource resource;
    private Iterator<Beneficiary> beneficiaryIterator;

    /**
     * Constructs a new CustomerFileReader with the given resource.
     *
     * @param resource the input file resource
     */
    public CustomerFileReader(Resource resource) {
        this.resource = resource;
    }

    /**
     * Reads the next {@link Beneficiary} from the iterator.
     *
     * @return The next beneficiary, or null if the iterator is empty.
     */
    @Override
    public Beneficiary read() {
        if (beneficiaryIterator != null && beneficiaryIterator.hasNext()) {
            return beneficiaryIterator.next();
        }
        return null;
    }

    /**
     * Initializes the reader by reading all beneficiaries from the CSV file
     * and populating an iterator. This method is called after the bean's properties
     * have been set.
     *
     * @throws Exception if any error occurs during initialization
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<CSVRecord> records = parseFile();
        List<Beneficiary> beneficiaries = transformToBeneficiaries(records);
        this.beneficiaryIterator = beneficiaries.iterator();
    }

    /**
     * Parses the CSV file into a list of {@link CSVRecord} objects.
     *
     * @return The list of CSV records
     * @throws Exception if any error occurs during parsing
     */
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

    /**
     * Transforms the list of {@link CSVRecord} objects into a list of {@link Beneficiary} objects.
     *
     * @param records The list of CSV records
     * @return The list of beneficiaries
     */
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

    /**
     * Creates a family of beneficiaries from the given list of {@link CSVRecord} objects.
     *
     * @param familyRecords The list of CSV records
     * @return The family of beneficiaries
     */
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

    /**
     * Builds a single beneficiary from the given {@link CSVRecord} object.
     *
     * @param record The CSV record
     * @return The beneficiary
     */
    private Beneficiary buildBeneficiary(CSVRecord record) {
        return Beneficiary.builder()
                .personId(Long.parseLong(record.get(0)))
                .firstName(record.get(1))
                .lastName(record.get(2))
                .address(Address.builder()
                        .street(record.get(3))
                        .city(record.get(4))
                        .state(record.get(5))
                        .zip(record.get(6))
                        .build())
                .build();
    }

    /**
     * Extracts the person ID from the given {@link CSVRecord} object.
     *
     * @param record The CSV record
     * @return The person ID
     */
    private String getPersonIdFromRecord(CSVRecord record) {
        return record.get(0);
    }
}
