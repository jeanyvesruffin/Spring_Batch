package com.pluralsight.springbatch.patientbatchloader.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.pluralsight.springbatch.patientbatchloader.PatientBatchLoaderApp;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PatientBatchLoaderApp.class)
@ActiveProfiles("dev")
public class BatchJobConfigurationTest {
	@Autowired
	private Job job;
	@Test
	public void test() {
		assertNotNull(job);
		assertEquals(Constants.JOB_NAME, job.getName());
	}
}
