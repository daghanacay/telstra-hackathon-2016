package com.telstra.hack.application;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.fluent.Request;
import org.osgi.dto.DTO;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import osgi.enroute.configurer.api.RequireConfigurerExtender;
import osgi.enroute.google.angular.capabilities.RequireAngularWebResource;
import osgi.enroute.rest.api.REST;
import osgi.enroute.rest.api.RESTRequest;
import osgi.enroute.twitter.bootstrap.capabilities.RequireBootstrapWebResource;
import osgi.enroute.webserver.capabilities.RequireWebServerExtender;

@RequireAngularWebResource(resource = { "angular.js", "angular-resource.js", "angular-route.js" }, priority = 1000)
@RequireBootstrapWebResource(resource = "css/bootstrap.css")
@RequireWebServerExtender
@RequireConfigurerExtender
@Component(name = "com.telstra.hack", service = { REST.class, HackApplication.class })
public class HackApplication implements REST {

	private final Map<String, List<LocationDataTime>> m_history = new ConcurrentHashMap<>();
	private final Map<String, String> m_status = new ConcurrentHashMap<>();

	@Activate
	public void setUp() {
		postTestData("test", "10.788037247049667", "106.59048386984685", "2016-12-07T15:41:35.412Z");
		postTestData("test", "10.89422964626165", "106.1795883479868", "2016-12-06T19:41:35.412Z");
		postTestData("test", "10.614101128457742", "105.92195711504108", "2016-12-05T23:41:35.412Z");
		postTestData("test", "10.358069865773288", "105.46792962756278", "2016-12-05T03:41:35.412Z");
		postTestData("test", "10.584736614037618", "105.78181966390942", "2016-12-04T07:41:35.412Z");
		postTestData("test", "10.29651382767102", "105.73727286453364", "2016-12-03T11:41:35.412Z");
		postTestData("test", "10.709797114802402", "105.33215562691426", "2016-12-02T15:41:35.412Z");
		postTestData("test", "11.151025342183182", "106.36867638314023", "2016-12-08T11:41:35.412Z");// LAST
		// known
		// location
	}

	// Model used to get/put status data from backend
	public static class StatusData extends DTO {
		public String status;
	}

	// GET http://localhost:8080/rest/status/xavier ==> {"status":"OK"} for
	// xavier
	public StatusData getStatus(RESTRequest request, String name) {
		request._response().addHeader("Access-Control-Allow-Origin", "*");
		StatusData statusData = new StatusData();
		String stat = m_status.get(name);
		if (stat == null) {
			statusData.status = "OK";
		} else {
			statusData.status = m_status.get(name);
		}

		return statusData;
	}

	// POST http://localhost:8080/rest/status/xavier/OK ==> {"status":"OK"} for
	// xavier
	public StatusData postStatus(RESTRequest request, String nullVal, String name, String statu) {
		request._response().addHeader("Access-Control-Allow-Origin", "*");
		StatusData statusData = new StatusData();
		statusData.status = statu;
		m_status.put(name, statu);
		return statusData;
	}

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
			m_history.put(name, new CopyOnWriteArrayList<LocationDataTime>());
		}
		m_history.get(name).add(dateTimeLocation);
		StringBuilder st = new StringBuilder(name.toUpperCase() +
				 " has sent location: \n")
				.append("Lat: " + dateTimeLocation.lat + "\n").append("Lon: " + dateTimeLocation.lon + "\n")
				.append("time: " + dateTimeLocation.time + "\n");
		System.out.println(st.toString());
		return dateTimeLocation;
	}

	// GET http://localhost:8080/rest/location/xavier ==> returns
	// [{"lat":"10","long":"10", "time":
	// "2016-12-10T12:53:12.727+11:00[Australia/Melbourne]"},...] for xavier
	public List<LocationDataTime> getLocation(RESTRequest request, String name) {
		request._response().addHeader("Access-Control-Allow-Origin", "*");
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
	public LastLocationDataTime getLastLocation(RESTRequest request, String name) {
		LastLocationDataTime returnVal = null;
		request._response().addHeader("Access-Control-Allow-Origin", "*");
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

		try {
			String result = Request.Get("http://emergency-phone-numbers.herokuapp.com/country/US").execute()
					.returnContent().asString();
			System.out.println(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ContactDetail detail = new ContactDetail();
		detail.name = "Ambulance";
		detail.number = "111111";
		return Arrays.asList(detail);
	}

	public void postTestData(String person, String lat, String lon, String time) {
		LocationPostRequest request = new LocationPostRequest() {

			@Override
			public HttpServletResponse _response() {
				return null;
			}

			@Override
			public HttpServletRequest _request() {
				return null;
			}

			@Override
			public String _host() {
				return null;
			}

			@Override
			public LocationDataTime _body() {
				LocationDataTime temp = new LocationDataTime();
				temp.lat = lat;
				temp.lon = lon;
				temp.time = time;
				return temp;
			}
		};

		postLocation(request, person);
	}

}
