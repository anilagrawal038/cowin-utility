package com.san.service;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.san.to.CowinAppointmentResponse;

@Component
public class HttpClientService {

	Logger logger = LoggerFactory.getLogger(HttpClientService.class);

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	EmailService emailService;

	public CowinAppointmentResponse getData(String uri) throws JsonGenerationException, JsonMappingException, IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.add("user-agent", "cowin-utility");
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		HttpEntity<CowinAppointmentResponse> requestEntity = new HttpEntity<>(headers);
		ResponseEntity<CowinAppointmentResponse> response = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, CowinAppointmentResponse.class);
		if (response.getStatusCode() != HttpStatus.OK) {
			logger.error("Response Code : " + response.getStatusCodeValue() + ", API : " + uri);
		}
		return response.getBody();
	}

}
