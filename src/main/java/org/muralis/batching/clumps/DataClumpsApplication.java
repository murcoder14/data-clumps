package org.muralis.batching.clumps;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableBatchProcessing
@ComponentScan(basePackages = "org.muralis.batching")
public class DataClumpsApplication {

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext context = SpringApplication.run(DataClumpsApplication.class, args);
		JobLauncher jobLauncher = context.getBean(JobLauncher.class);
		Job job = context.getBean("customerFileLoadJob", Job.class);
		int exitCode = jobLauncher.run(job, new JobParameters()).getExitStatus().getExitCode().equals("COMPLETED") ? 0 : 1;
		System.exit(exitCode);
	}

}
