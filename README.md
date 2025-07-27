# Spring Batch gRPC Streaming Example

This project demonstrates a robust and scalable ETL (Extract, Transform, Load) pipeline built with Spring Batch. It reads beneficiary records from a flat file, validates them using a flexible and extensible validation framework, and streams the results (both valid and invalid records) to a downstream service using gRPC.

This application has been refactored from an older, file-based XML output to a modern, network-based streaming architecture to handle very large datasets with high performance and a low memory footprint.

## Core Technologies

- **Spring Boot 3**
- **Spring Batch 5**
- **gRPC & Protocol Buffers** for efficient, high-performance data streaming.
- **Java 21**
- **Lombok**
- **Apache Commons CSV**
- **Maven**

## Architecture

The application follows a standard Spring Batch architecture with a single job and step:

1.  **`ItemReader` (`CustomerFileReader`):** Reads `Beneficiary` records one by one from the input file `beneficiaries.csv`.

2.  **`ItemProcessor` (`BeneficiaryValidationProcessor`):**
    - Receives a `Beneficiary` record.
    - Uses a `CompositeValidator` to perform validation on the `Beneficiary` and its nested objects (like `Address` and dependents).
    - The validation logic is extensible, allowing new validators for new domain models to be easily added.
    - If the record is valid, it's passed on.
    - If the record is invalid, it's wrapped in an `InvalidBeneficiary` object containing the original record and a list of validation errors.

3.  **`ItemWriter` (`GrpcItemWriter`):**
    - Receives a chunk of processed items (both valid `Beneficiary` and `InvalidBeneficiary` objects).
    - Establishes a client-side stream to the gRPC server.
    - Maps the Java objects to Protobuf messages and sends them over the stream.

4.  **gRPC Service (`BeneficiaryStreamerService`):**
    - An embedded gRPC server that listens for incoming record streams.
    - In this example, it simply logs the valid and invalid records it receives.
    - In a real-world scenario, this service could be a separate microservice responsible for persisting the records to a database, forwarding them to a message queue, or performing further processing.

## Benefits of gRPC Streaming over XML Files

The primary architectural goal of this project was to move away from writing XML files to a more scalable solution. Here are the key benefits of using gRPC streaming:

- **Performance and Efficiency:** gRPC uses Protocol Buffers (Protobuf), a binary serialization format. It is significantly more compact and faster to serialize/deserialize than text-based XML. This results in lower CPU usage, reduced network bandwidth, and faster processing times.

- **Scalability and Low Memory Footprint:** Streaming allows the application to process massive files (e.g., 10GB+) without loading the entire dataset into memory. Records are processed in small chunks and sent over the network immediately, keeping memory usage consistently low. Generating a large XML file, by contrast, can consume significant memory and disk I/O resources.

- **Decoupling and Interoperability:** A file-based approach tightly couples systems to a shared file system. gRPC decouples the batch job (the producer) from the record handling service (the consumer). This consumer can be an independent microservice written in any language that gRPC supports (Go, Python, C++, etc.), enabling true polyglot microservice architectures.

- **Strongly-Typed Contracts:** The `.proto` file defines a strict, language-agnostic contract for the data structures and service endpoints. This eliminates ambiguity and reduces the risk of runtime errors, providing a more reliable integration point than a less-structured XML file and its optional XSD schema.

- **Real-time Processing:** Streaming sends data as it becomes available. This is a step towards real-time data processing, where downstream systems can react to validated records almost instantly, rather than waiting for a large batch file to be completely written and closed.

## How to Run

1.  Ensure you have Java 21 and Maven installed.
2.  Make sure no other process is running on port `9090`.
3.  From the project root directory, run the following command:

    ```bash
    mvn clean spring-boot:run
    ```

4.  The application will start, run the batch job, and then shut down. You can observe the log output to see the gRPC server starting, the batch job executing, and the records being received and logged by the service.
