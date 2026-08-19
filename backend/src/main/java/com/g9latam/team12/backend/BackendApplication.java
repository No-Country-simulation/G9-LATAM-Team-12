package com.g9latam.team12.backend;

import org.flywaydb.core.Flyway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
	@Bean
	public Flyway flyway(DataSource dataSource) {
		Flyway flyway = Flyway.configure()
				.dataSource(dataSource)
				.locations("classpath:db/migration")
				.baselineOnMigrate(true)
				.load();

		flyway.migrate();

		return flyway;
	}

}
