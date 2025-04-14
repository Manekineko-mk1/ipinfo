package com.ipinfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipinfo.entity.IpInfo;
import com.ipinfo.repository.IpInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IpInfoApplicationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private IpInfoRepository repository;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private RestTemplate restTemplate;

	@BeforeEach
	void setUp() {
		// Clear database before each test
		repository.deleteAll();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void testPostIpAdd_Success() throws Exception {
		// Setup test data
		String ipAddress = "128.129.49.14";
		Map<String, Object> mockResponse = Map.of(
				"ip", ipAddress,
				"city", "Montreal",
				"region", "Quebec",
				"country", "Canada"
		);
		when(restTemplate.getForObject(anyString(), eq(Map.class)))
				.thenReturn(mockResponse);

		// Execute test
		mockMvc.perform(post("/Ip/Add")
						.param("ipAddress", ipAddress)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(content().string("IP data saved successfully"));

		// Assert test result
		IpInfo saved = repository.findByIpAddress(ipAddress).orElse(null);
		assertNotNull(saved);
		assertEquals(ipAddress, saved.getIpAddress());
		assertEquals(objectMapper.writeValueAsString(mockResponse), saved.getPayload());
	}

	@Test
	void testPostIpAdd_MissingIpAddress() throws Exception {
		mockMvc.perform(post("/Ip/Add")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("IP address is required"));
	}

	@Test
	void testGetIp_Success() throws Exception {
		String ipAddress = "128.129.49.14";
		Map<String, Object> payload = Map.of(
				"ip", ipAddress,
				"city", "Montreal",
				"region", "Quebec",
				"country", "Canada"
		);
		IpInfo ipInfo = new IpInfo();
		ipInfo.setIpAddress(ipAddress);
		ipInfo.setPayload(objectMapper.writeValueAsString(payload));
		repository.save(ipInfo);

		mockMvc.perform(get("/Ip/Get")
						.param("ipAddress", ipAddress)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().json(objectMapper.writeValueAsString(payload)));
	}

	@Test
	void testGetIp_NotFound() throws Exception {
		// Act
		mockMvc.perform(get("/Ip/Get")
						.param("ipAddress", "1.1.1.1")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(content().string("No data found for IP address: 1.1.1.1"));
	}

	@Test
	void testGetIp_MissingIpAddress() throws Exception {
		// Act
		mockMvc.perform(get("/Ip/Get")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("IP address is required"));
	}
}