package com.telstra.hack.application;

import org.osgi.dto.DTO;

public class AppSensorDataDTO extends DTO {
	public String name;
	public double longitude;
	public double latitude;
	// Absolute temperature value in Celsius
	public double temperature;
	// used to get the normalized
	public double sensorNormalized;
	public AppSensorMetadataDTO metadata = new AppSensorMetadataDTO();

}
