package com.myfinance.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ApiApplicationTests {

	@Test
	void contextLoads() {
		assertTrue(true);
	}

	@Test
	void basicEqualityCheck() {
		int expected = 42;
		int actual = 42;
		assertEquals(expected, actual, "Basic equality should hold");
	}

	@Test
	void sampleExceptionAssertion() {
		assertThrows(IllegalArgumentException.class, () -> {
			throw new IllegalArgumentException("sample");
		}, "Expected IllegalArgumentException to be thrown");
	}
}