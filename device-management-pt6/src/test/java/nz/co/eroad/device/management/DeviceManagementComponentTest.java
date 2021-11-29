package nz.co.eroad.device.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import nz.co.eroad.device.management.model.DeviceDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static nz.co.eroad.device.management.model.DeviceDTO.LifeCycleStateEnum.PENDING_INSTALL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class DeviceManagementComponentTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void createDevice_withValidParameters_andSucceeds_thenCanRetrieveById_andCanRetrieveAll() throws Exception {
        // Setup
        String serialNumber = "D" + randomAlphanumeric(10);

        DeviceDTO deviceDTO = new DeviceDTO()
                .serialNumber(serialNumber)
                .lifeCycleState(PENDING_INSTALL);

        // Create the device
        MockHttpServletResponse response = mockMvc.perform(
                        post("/devices")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(deviceDTO))
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().exists("Location"))
                .andReturn()
                .getResponse();

        // Assert on response
        DeviceDTO createdDeviceDTO = objectMapper.readValue(response.getContentAsString(), DeviceDTO.class);
        assertThat(createdDeviceDTO.getId()).isNotNull();
        assertThat(createdDeviceDTO.getSerialNumber()).isEqualTo(serialNumber);
        assertThat(createdDeviceDTO.getLifeCycleState()).isEqualTo(PENDING_INSTALL);

        // Attempt to retrieve the device by id
        response = mockMvc.perform(
                        get(response.getHeader("Location"))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // Assert that the retrieved device is the same as the one we previously created
        assertThat(objectMapper.readValue(response.getContentAsString(), DeviceDTO.class)).isEqualTo(createdDeviceDTO);
    }

}
