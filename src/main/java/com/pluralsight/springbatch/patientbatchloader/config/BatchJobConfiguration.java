package com.pluralsight.springbatch.patientbatchloader.config;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;

import com.pluralsight.springbatch.patientbatchloader.domain.PatientRecord;

import antlr.preprocessor.Preprocessor;

@Configuration
public class BatchJobConfiguration {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;
	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private ApplicationProperties applicationProperties;

	@Bean
	JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
		JobRegistryBeanPostProcessor postProcessor = new JobRegistryBeanPostProcessor();
		postProcessor.setJobRegistry(jobRegistry);
		return postProcessor;
	}

	@Bean
	public Job job(Step step) throws Exception {
		return this.jobBuilderFactory.get(Constants.JOB_NAME).validator(validator()).start(step).build();
	}

	@Bean
	public JobParametersValidator validator() {
		return new JobParametersValidator() {
			@Override
			public void validate(JobParameters parameters) throws JobParametersInvalidException {
				String fileName = parameters.getString(Constants.JOB_PARAM_FILE_NAME);
				// System.out.println("filename :" + fileName);
				if (StringUtils.isBlank(fileName)) {
					throw new JobParametersInvalidException("Le parametre de patient-batch-loader.fileName est requis");
				}
				try {
					Path file = Paths.get(applicationProperties.getBatch().getInputPath() + File.separator + fileName);
					if (Files.notExists(file) || !Files.isReadable(file)) {
						throw new Exception("Le fichier n'existe pas ou n'est pas lisable");
					}
				} catch (Exception e) {
					throw new JobParametersInvalidException(
							"le parametre chemin d'acces + patient-batch-loader.filename doit etre un emplacement de fichier valide");
				}
			}
		};
	}

	@Bean
	public Step step(ItemReader<PatientRecord> itemReader) throws Exception {
		return this.stepBuilderFactory.get(Constants.STEP_NAME).<PatientRecord, PatientRecord>chunk(2)
				.reader(itemReader).processor(processor()).writer(writer()).build();
	}

	@Bean
	@StepScope
	public PassThroughItemProcessor<PatientRecord> processor(){
		return new PassThroughItemProcessor<>();
	}
	
	@Bean
	@StepScope
	public ItemWriter<PatientRecord> writer(){
		return new ItemWriter<PatientRecord>() {
			
			@Override
			public void write(List<? extends PatientRecord> items) throws Exception {
				for (PatientRecord patientRecord : items) {
					System.err.println("Ecriture item : " + patientRecord.toString());
				}
			}
		};
	}
	
	
	@Bean
	@StepScope
	public FlatFileItemReader<PatientRecord> reader(@Value("#{jobParameters['" + Constants.JOB_PARAM_FILE_NAME + "']}") String fileName) {
		return new FlatFileItemReaderBuilder<PatientRecord>()
				.name(Constants.ITEM_READER_NAME)
				.resource(new PathResource(Paths.get(applicationProperties.getBatch().getInputPath() + File.separator + fileName)))
				.linesToSkip(1).lineMapper(lineMapper()).build();
	}

	@Bean
	public LineMapper<PatientRecord> lineMapper() {
		DefaultLineMapper<PatientRecord> mapper = new DefaultLineMapper<>();
		mapper.setFieldSetMapper((fieldSet)->new PatientRecord(
				fieldSet.readString(0), fieldSet.readString(1),
				fieldSet.readString(2), fieldSet.readString(3),
				fieldSet.readString(4), fieldSet.readString(5),
				fieldSet.readString(6), fieldSet.readString(7),
				fieldSet.readString(8), fieldSet.readString(9),
				fieldSet.readString(10), fieldSet.readString(11),
				fieldSet.readString(12)));
		mapper.setLineTokenizer(new DelimitedLineTokenizer());
		return mapper;
	}
	
	/*
	 * Premier step realisé comme test avec un simple tasklet creer danns la console
	 * si l'execution du job c'est bien deroulé
	 * 
	 * @Bean public Step step() throws Exception { return this.stepBuilderFactory
	 * .get(Constants.STEP_NAME) .tasklet(new Tasklet() {
	 * 
	 * @Override public RepeatStatus execute(StepContribution contribution,
	 * ChunkContext chunkContext) throws Exception {
	 * System.err.println("Hello World!!"); return RepeatStatus.FINISHED; }
	 * 
	 * }) .build(); }
	 * 
	 * 
	 * @Bean public Step step(ItemReader<PatientRecord> itemReader) throws Exception
	 * { return this.stepBuilderFactory .get(Constants.STEP_NAME) .<PatientRecord,
	 * PatientRecord>chunk(2) .reader(itemReader) .processor(processor())
	 * .writer(writer()) .build(); }
	 * 
	 * @Bean
	 * 
	 * @StepScope public PassThroughItemProcessor<PatientRecord> processor() {
	 * return new PassThroughItemProcessor<>(); }
	 * 
	 * @Bean
	 * 
	 * @StepScope public ItemWriter<PatientRecord> writer() { return new
	 * ItemWriter<PatientRecord>() {
	 * 
	 * @Override public void write(List<? extends PatientRecord> items) throws
	 * Exception { for (PatientRecord patientRecord : items) {
	 * System.err.println("Writing item: " + patientRecord.toString()); } } }; }
	 */
}
