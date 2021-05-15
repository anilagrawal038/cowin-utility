package com.san;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.san.service.JobProcessService;

@SpringBootApplication
public class Main {

	static Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(Main.class, args);
		logger.info("Application started.");
		try {
			ctx.getBean(JobProcessService.class).start();
		} catch (Exception e) {
			logger.error("Exception occurred", e);
		}
		ctx.close();
		logger.info("Application closed.");
	}

}
