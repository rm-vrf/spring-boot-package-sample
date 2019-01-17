package com.mydomain.app.package_;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.SpringApplication;

public class PackageSampleApplicationTests {

	@Test
	public void testDefaultSettings() {
		Assert.assertTrue(SpringApplication.exit(SpringApplication.run(Main.class)) == 0);
	}

}