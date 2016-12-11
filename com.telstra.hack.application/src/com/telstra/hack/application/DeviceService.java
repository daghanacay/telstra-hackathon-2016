package com.telstra.hack.application;

import java.util.List;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.easyiot.LT100H.device.api.dto.LT100HSensorDataDTO;
import com.easyiot.base.api.Device;
import com.easyiot.base.api.Device.DeviceExecutorMethodTypeEnum;
import com.easyiot.base.api.exception.NoSuchDeviceException;
import com.easyiot.base.executor.DeviceExecutorService;

import osgi.enroute.scheduler.api.Scheduler;

@Component(immediate = true, service = DeviceService.class)
public class DeviceService {
	LT100HSensorConverter converter = new LT100HSensorConverter();
	@Reference
	Scheduler scheduler;

	@Reference
	HackApplication hackApp;

	@Reference
	private DeviceExecutorService rm;

	@Reference(target = "(service.factoryPid=com.easyiot.LT100H.device)")
	volatile List<Device> lt100hSensors;

	@Activate
	public void updateDeviceData() throws Exception {
		// update every ten second
		scheduler.schedule(() -> getDeviceData(), 60000);
	}

	public void getDeviceData() {
		try {
			LT100HSensorDataDTO lt100hSensorData = rm.activateResource(lt100hSensors.get(0).getId(), null,
					LT100HSensorDataDTO.class, DeviceExecutorMethodTypeEnum.GET);

			AppSensorDataDTO data = converter.convert(lt100hSensors.get(0).getId(), lt100hSensorData);
			hackApp.postTestData("JOHN_satellite", String.valueOf(data.latitude), String.valueOf(data.longitude),
					"2016-12-04T07:41:35.412Z");
		} catch (NoSuchDeviceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
