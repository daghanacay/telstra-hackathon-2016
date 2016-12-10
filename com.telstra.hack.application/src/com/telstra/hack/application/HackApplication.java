package com.telstra.hack.application;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Component;

import osgi.enroute.configurer.api.RequireConfigurerExtender;
import osgi.enroute.google.angular.capabilities.RequireAngularWebResource;
import osgi.enroute.rest.api.REST;
import osgi.enroute.rest.api.RESTRequest;
import osgi.enroute.twitter.bootstrap.capabilities.RequireBootstrapWebResource;
import osgi.enroute.webserver.capabilities.RequireWebServerExtender;
import org.osgi.dto.DTO;

@RequireAngularWebResource(resource = { "angular.js", "angular-resource.js", "angular-route.js" }, priority = 1000)
@RequireBootstrapWebResource(resource = "css/bootstrap.css")
@RequireWebServerExtender
@RequireConfigurerExtender
@Component(name = "com.telstra.hack")
public class HackApplication implements REST {

	private final Map<String, List<LocationDataTime>> m_history = new ConcurrentHashMap<>();

	// Model used to get/put data from backend
	public static class LocationDataTime extends DTO {
		public String lat;
		public String lon;
		public String time;
	}

	interface LocationPostRequest extends RESTRequest {
		LocationDataTime _body();
	}

	// POST http://localhost:8080/rest/location/xavier with a payload of
	// {"lat":"33", "lon":"56", "time":"2016-12-10T12:57:17.189+11:00" }
	// ==> returns {"lat":"33", "lon":"56",
	// "time":"2016-12-10T12:57:17.189+11:00" }
	// and saves it under xavier
	public LocationDataTime postLocation(LocationPostRequest rr, String name) {
		LocationDataTime dateTimeLocation = rr._body();

		if (m_history.get(name) == null) {
			m_history.put(name, new ArrayList<LocationDataTime>());
		}
		m_history.get(name).add(dateTimeLocation);
		StringBuilder st = new StringBuilder("Post an entry using payload: \n")
				.append("Lat: " + dateTimeLocation.lat + "\n").append("Lon: " + dateTimeLocation.lon + "\n")
				.append("time: " + dateTimeLocation.time + "\n");
		System.out.println(st.toString());
		return dateTimeLocation;
	}

	// GET http://localhost:8080/rest/location/xavier ==> returns
	// [{"lat":"10","long":"10", "time":
	// "2016-12-10T12:53:12.727+11:00[Australia/Melbourne]"},...] for xavier
	public List<LocationDataTime> getLocation(String name) {
		return m_history.get(name);
	}

	// Model used to get/put data from backend
	public static class LastLocationDataTime extends DTO {
		public String lat;
		public String lon;
		public String time;
		public List<ContactDetail> contacts;
	}

	// Model used to get/put data from backend
	public static class ContactDetail extends DTO {
		public String name;
		public String number;
	}

	// GET http://localhost:8080/rest/lastlocation/xavier ==> returns
	// {"lat":"10","long":"10", "time":
	// "2016-12-10T12:53:12.727+11:00[Australia/Melbourne]",
	// "relevantcontacts":[{"name":"ambulance", "number":"1111111"}]} for xavier
	public LastLocationDataTime getLastLocation(String name) {
		LastLocationDataTime returnVal= null;
		Optional<LocationDataTime> lastLoc = m_history.get(name).stream().reduce((first, second) -> second);
		if (lastLoc.isPresent()) {
			LocationDataTime lastLocData = lastLoc.get();
			returnVal = new LastLocationDataTime();
			returnVal.lat = lastLocData.lat;
			returnVal.lon = lastLocData.lon;
			returnVal.time = lastLocData.time;
			returnVal.contacts = getContacts(lastLocData);
		}

		return returnVal;
	}

	private List<ContactDetail> getContacts(LocationDataTime lastLocData) {
		ContactDetail detail = new ContactDetail();
		detail.name = "Ambulance";
		detail.number = "111111";
		return Arrays.asList(detail);
	}
}
