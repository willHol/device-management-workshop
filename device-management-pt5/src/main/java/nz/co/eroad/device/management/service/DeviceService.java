package nz.co.eroad.device.management.service;

import lombok.RequiredArgsConstructor;
import nz.co.eroad.device.management.model.DeviceDTO;
import nz.co.eroad.device.management.repository.DeviceRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository repository;

    public DeviceDTO createDevice(DeviceDTO deviceDTO) {
        throw new UnsupportedOperationException();
    }

    public DeviceDTO getDevice(String deviceId) {
        throw new UnsupportedOperationException();
    }

}
