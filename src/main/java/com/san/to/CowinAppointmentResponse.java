package com.san.to;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CowinAppointmentResponse {

	public List<CowinAppointmentCenter> centers = new ArrayList<>();

	public List<CowinAppointmentCenter> getCenters() {
		return centers;
	}

	public void setCenters(List<CowinAppointmentCenter> centers) {
		this.centers = centers;
	}
}
