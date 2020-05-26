# Spring_batch

## Ajouter les dependences spring batch

Dans le fichier build.gradle ajouter les lignes suivantes:

	compile group: 'org.springframework.boot', name: 'spring-boot-starter-batch', version: '2.3.0.RELEASE'


	testCompile group: 'org.springframework.batch', name: 'spring-batch-test', version: '4.2.2.RELEASE'

## Configurer l'application Spring batch

1 . Ajouter une classe de configuration à votre projet avec les imports nécessaire, avec la declaration de 3 attributs membres de classes JobRepository, JobExplorer et JobLauncher.

	```java
	import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
	import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
	import org.springframework.batch.core.explore.JobExplorer;
	import org.springframework.batch.core.launch.JobLauncher;
	import org.springframework.batch.core.repository.JobRepository;
	import org.springframework.stereotype.Component;
	import org.springframework.transaction.PlatformTransactionManager;
	@Component
	@EnableBatchProcessing
	public class BatchConfiguration implements BatchConfigurer {
		private JobRepository jobRepository;
		private JobExplorer jobExplorer;
		private JobLauncher jobLauncher;
	}
	```

* JobRepository: Conserve les métadonnées sur le job par lots

* JobExplorer: Recupere les métadonnées du repository

* JobLauncher: Exécute des jobs avec des paramètres donnés


2 . Cabler deux attributs membres de la classe PlatformTransactionManager et DataSource

	```java
	@Autowired
	@Qualifier(value="batchTransactionManager")
	private PlatformTransactionManager batchTransactionManager;
	@Autowired
	@Qualifier(value="bqtchDataSource")
	private DataSource batchDataSource;
	```

3 . Remplir le contrat d'interface de BatchConfigurer.

	```java
	@Override
	public PlatformTransactionManager getTransactionManager() throws Exception {
		return this.batchTransactionManager;
	}
	@Override
	public JobExplorer getJobExplorer() throws Exception {
		return this.jobExplorer;
	}
	```
	
4 . Ajouter la methode de creation d'une execution de job (launcher + repository + afterPropertiesSet)

	```java
	protected JobLauncher createJobLauncher() throws Exception {
		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
		jobLauncher.setJobRepository(jobRepository);
		jobLauncher.afterPropertiesSet();
		return jobLauncher;
	}
	protected JobRepository createJobRepository() throws Exception {
		JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
		factory.setDataSource(this.batchDataSource);
		factory.setTransactionManager(getTransactionManager());
		factory.afterPropertiesSet();
		return factory.getObject();
	}
	@PostConstruct
	public void afterPropertiesSet()throws Exception {
		this.jobRepository = createJobRepository();
		JobExplorerFactoryBean jobExplorerFactoryBean = new JobExplorerFactoryBean();
		jobExplorerFactoryBean.setDataSource(this.batchDataSource);
		jobExplorerFactoryBean.afterPropertiesSet();
		this.jobExplorer = jobExplorerFactoryBean.getObject();
		this.jobLauncher = createJobLauncher();
	}
	```
	
5 . Ajouter à votre fichier de configuration application.yml

	spring:
		application:
			name: PatientBatchLoader
				batch:
					job:
						enable: false
	...
	application:
		batch:
			inputPath: D:\WORK\WorkSpace\SandBox\Spring_batch\Spring_batch\data
			
6 . On ajoute dans le fichier ApplicationProperties les propriétés spécifiques à l'application

	```java
	@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
	public class ApplicationProperties {
		private final Batch batch = new Batch();
		public Batch getBatch() {
			return batch;
		}
		public static class Batch{
			private String inputPath = "D:/WORK/WorkSpace/SandBox/Spring_batch/Spring_batch/data";
			public String getInputPath() {
				return this.inputPath;
			}
			public void setInputPath(String inputPath) {
				this.inputPath = inputPath;
			}
		}	
	}
	```java
		
## Ajout de l'objet base de donnée à Spring Batch

![Batch Term](Documents/batchTerm)

