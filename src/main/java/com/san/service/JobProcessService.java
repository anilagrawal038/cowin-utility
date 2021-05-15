package com.san.service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.san.to.CowinAppointmentCenter;
import com.san.to.CowinAppointmentResponse;
import com.san.to.CowinAppointmentSession;
import com.san.util.CommonUtil;

@Component
public class JobProcessService {

	// API Ref : https://apisetu.gov.in/public/api/cowin#/Appointment%20Availability%20APIs/findByPin
	// https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByPin?pincode=110096&date=14-05-2021
	// Note : 100 calls in 5 minutes are allowed
	Logger logger = LoggerFactory.getLogger(JobProcessService.class);
	final String uri = "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByPin";
	final long sleepTime = 30 * 1000;
	final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);

	@Value("${cowin.utility.pinCodes}")
	public String pinCodesStr;

	@Value("${cowin.utility.nixExecutableFile}")
	public String nixExecutable;

	@Value("${cowin.utility.winExecutableFile}")
	public String winExecutable;

	@Value("${cowin.utility.onlyFreeDosage}")
	public boolean onlyFreeDosage;

	private List<Integer> pinCodes = new ArrayList<>();

	private File executableFile;

	@Autowired
	EmailService emailService;

	@Autowired
	HttpClientService httpClientService;

	@PostConstruct
	public void init() {
		if (pinCodesStr != null && !pinCodesStr.isEmpty()) {
			String[] pinArr = pinCodesStr.split("\\,");
			if (pinArr != null && pinArr.length > 0) {
				for (String pin : pinArr) {
					try {
						pinCodes.add(Integer.parseInt(pin.trim()));
					} catch (Exception e) {
						logger.error("Invalid PIN : " + pin);
					}
				}
			}
		}

		try {
			File file = null;
			if (CommonUtil.isWindows()) {
				file = new File(winExecutable);
			} else {
				file = new File(nixExecutable);
			}
			if (file != null && file.exists() && file.isFile()) {
				// file.canExecute()
				executableFile = file;
			} else {
				logger.info("Either file not present or not a valid executable : " + file.getAbsolutePath());
			}
		} catch (Exception e) {
			logger.error("Exception occurred while initialization", e);
		}
	}

	public void start() {
		boolean status = true;
		while (status) {
			try {
				new Thread() {
					public void run() {
						try {
							execute();
						} catch (Exception e) {
							logger.error("Exception in job", e);
						}
					}
				}.start();
				Thread.sleep(sleepTime);
			} catch (Exception e) {
				logger.error("Thread Intrrupted", e);
			}
		}
	}

	private void execute() throws JsonGenerationException, JsonMappingException, IOException {
		for (int pin : pinCodes) {
			String tempURI = uri + "?pincode=" + pin + "&date=" + dateFormatter.format(new Date());
			try {
				CowinAppointmentResponse data = httpClientService.getData(tempURI);
				// String responseString = CommonUtil.convertToJsonString(data);
				// logger.info(responseString);
				List<String> availableCenters = checkValidity(data);
				if (availableCenters != null && availableCenters.size() > 0) {
					informUser(availableCenters);
				}
				Thread.sleep(10 * 1000);
			} catch (Exception e) {
				logger.error("Exception for API : " + tempURI, e);
			}
		}
	}

	private List<String> checkValidity(CowinAppointmentResponse data) {
		if (data.getCenters() == null || data.getCenters().size() < 1) {
			return null;
		}
		for (CowinAppointmentCenter center : data.getCenters()) {
			List<CowinAppointmentSession> sessions = center.getSessions();
			if (sessions != null && sessions.size() > 0) {
				for (CowinAppointmentSession session : sessions) {
					if (session.getMin_age_limit() < 45 && session.getAvailable_capacity() > 0 && (!onlyFreeDosage || center.getFee_type().equalsIgnoreCase("Free"))) {
						center.addAvailableSession(session);
					}
				}
			}
		}
		List<String> availableCenters = new ArrayList<>();
		for (CowinAppointmentCenter center : data.getCenters()) {
			if (center.getAvailableSessions().size() > 0) {
				availableCenters.add(center.fetchAvailabilityInfo());
			}
		}
		return availableCenters;
	}

	private void informUser(List<String> availableCenters) {
		logger.info("Available Slots : " + availableCenters);
		StringBuilder mailBody = new StringBuilder();
		for (String availableCenter : availableCenters) {
			mailBody.append(availableCenter + "\n");
		}
		emailService.sendSimpleMessage(new String[] { "anilagrawal038@gmail.com", "satish.glanmc@gmail.com" }, "COVID Vaccine Available Slots from spring-boot-mail-utility", mailBody.toString());
	}

}
