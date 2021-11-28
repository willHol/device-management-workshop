package nz.co.eroad.device.management.mapper;

import nz.co.eroad.device.management.entity.Device;
import nz.co.eroad.device.management.model.DeviceDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DeviceMapper {

    DeviceDTO toDTO(Device device);

    Device fromDTO(DeviceDTO deviceDTO);

}
