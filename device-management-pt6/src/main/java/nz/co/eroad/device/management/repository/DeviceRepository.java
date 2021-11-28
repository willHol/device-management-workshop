package nz.co.eroad.device.management.repository;

import nz.co.eroad.device.management.entity.Device;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeviceRepository extends CrudRepository<Device, UUID> {

}
