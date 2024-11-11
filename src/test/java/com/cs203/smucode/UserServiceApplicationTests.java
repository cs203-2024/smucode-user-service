package com.cs203.smucode;

import com.cs203.smucode.configs.TestSecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = {UserServiceApplication.class, TestSecurityConfiguration.class})
class UserServiceApplicationTests {

	@Test
	void contextLoads() {
	}
}