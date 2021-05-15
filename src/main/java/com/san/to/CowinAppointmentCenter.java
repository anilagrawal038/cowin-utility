package com.san.to;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CowinAppointmentCenter {

	public int center_id;
	public String name;
	public String address;
	public String state_name;
	public String district_name;
	public String block_name;
	public int pincode;
	public int lat;
	public int _long;
	public String from;
	public String to;
	public String fee_type;
	public List<CowinAppointmentSession> sessions = new ArrayList<>();
	public List<CowinAppointmentSession> availableSessions = new ArrayList<>();

	public int getCenter_id() {
		return center_id;
	}

	public void setCenter_id(int center_id) {
		this.center_id = center_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getState_name() {
		return state_name;
	}

	public void setState_name(String state_name) {
		this.state_name = state_name;
	}

	public String getDistrict_name() {
		return district_name;
	}

	public void setDistrict_name(String district_name) {
		this.district_name = district_name;
	}

	public String getBlock_name() {
		return block_name;
	}

	public void setBlock_name(String block_name) {
		this.block_name = block_name;
	}

	public int getPincode() {
		return pincode;
	}

	public void setPincode(int pincode) {
		this.pincode = pincode;
	}

	public int getLat() {
		return lat;
	}

	public void setLat(int lat) {
		this.lat = lat;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getFee_type() {
		return fee_type;
	}

	public void setFee_type(String fee_type) {
		this.fee_type = fee_type;
	}

	public List<CowinAppointmentSession> getSessions() {
		return sessions;
	}

	public void setSessions(List<CowinAppointmentSession> sessions) {
		this.sessions = sessions;
	}

	public int getLong() {
		return _long;
	}

	public void setLong(int _long) {
		this._long = _long;
	}

	public List<CowinAppointmentSession> getAvailableSessions() {
		return availableSessions;
	}

	public void setAvailableSessions(List<CowinAppointmentSession> availableSessions) {
		this.availableSessions = availableSessions;
	}

	public void addAvailableSession(CowinAppointmentSession availableSession) {
		this.availableSessions.add(availableSession);
	}

	public String fetchAvailabilityInfo() {
		StringBuilder info = new StringBuilder();
		info.append("PIN : " + pincode + ", Name : " + name);
		List<String> slots = new ArrayList<>();
		for (CowinAppointmentSession session : availableSessions) {
			slots.add(session.getDate() + "(" + session.available_capacity + ")");
		}
		info.append(", Slots : [" + String.join(", ", slots) + "]");
		return info.toString();
	}
}
