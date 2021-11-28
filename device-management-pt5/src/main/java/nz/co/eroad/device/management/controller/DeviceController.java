package nz.co.eroad.device.management.controller;

import lombok.RequiredArgsConstructor;
import nz.co.eroad.device.management.api.DevicesApi;
import nz.co.eroad.device.management.model.DeviceDTO;
import nz.co.eroad.device.management.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Controller
@RequiredArgsConstructor
public class DeviceController implements DevicesApi {

    private final DeviceService deviceService;

    @Override
    public ResponseEntity<DeviceDTO> createDevice(DeviceDTO deviceDTO) {
        DeviceDTO result = deviceService.createDevice(deviceDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.getId())
                .toUri();

        return ResponseEntity.created(location).body(result);
    }

    @Override
    public ResponseEntity<DeviceDTO> getDevice(String deviceId) {
        return ResponseEntity.ok(deviceService.getDevice(deviceId));
    }

}
