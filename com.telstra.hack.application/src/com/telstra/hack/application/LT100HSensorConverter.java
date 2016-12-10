package com.telstra.hack.application;

import com.easyiot.LT100H.device.api.dto.LT100HSensorDataDTO;

public class LT100HSensorConverter {
	public AppSensorDataDTO convert(String name, LT100HSensorDataDTO lt100hSensorDevice) {
		AppSensorDataDTO returnval = new AppSensorDataDTO();

		returnval.name = name;
		returnval.longitude = getLongitude(lt100hSensorDevice.longitude);
		returnval.latitude = getLatitude(lt100hSensorDevice.latitude);

		// No heat info on this sensor

		returnval.temperature = 50;
		returnval.sensorNormalized = .5;
		returnval.metadata = createMetaData(lt100hSensorDevice);
		return returnval;
	}

	private double getLatitude(String latitude) {
		// Convert (N or S)ddmm.mmmm
		double returnVal = 0;
		String direction = latitude.substring(0, 1);
		double latitudeDbl = Double.valueOf(latitude.substring(1)) / 100;
		if ("N".equalsIgnoreCase(direction)) {
			returnVal = latitudeDbl;
		} else if ("S".equalsIgnoreCase(direction)) {
			returnVal = -latitudeDbl;
		} else {
			System.err.println("corrupted data");
		}

		return returnVal;
	}

	private double getLongitude(String longitude) {
		// Convert // in (E or W)dddmm.mmmm format to double
		double returnVal = 0;
		String direction = longitude.substring(0, 1);
		double longitudeDbl = Double.valueOf(longitude.substring(1)) / 100;
		if ("E".equalsIgnoreCase(direction)) {
			returnVal = longitudeDbl;
		} else if ("W".equalsIgnoreCase(direction)) {
			returnVal = -longitudeDbl;
		} else {
			System.err.println("corrupted data");
		}

		return returnVal;
	}

	private AppSensorMetadataDTO createMetaData(LT100HSensorDataDTO sensorDto) {
		AppSensorMetadataDTO returnVal = new AppSensorMetadataDTO();
		returnVal.notes = String.format("Network name: %s, Gateway EUI: %s", sensorDto.metadata!=null?sensorDto.metadata.networkName:"N/A",
				sensorDto.metadata!=null?sensorDto.metadata.gateway_eui:"N/A");
		return returnVal;
	}

}
