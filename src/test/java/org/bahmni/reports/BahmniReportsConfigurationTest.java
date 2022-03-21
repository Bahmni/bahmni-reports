package org.bahmni.reports;

import org.apache.http.config.Registry;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BahmniReportsConfigurationTest {

	@Mock
	private BahmniReportsProperties bahmniReportsProperties;

	@Mock
	SSLConnectionSocketFactory allTrustSSLSocketFactory;

	@Before
	public void setup(){
		initMocks(this);
	}

	@Test
	public void shouldReturnAllTrustSchemaRegistryIfNotSpecifiedInConfiguration(){

		when(bahmniReportsProperties.getTrustSSLConnection()).thenReturn("true");
		BahmniReportsConfiguration bahmniReportsConfiguration = new BahmniReportsConfiguration(bahmniReportsProperties);
		Registry<ConnectionSocketFactory> actualSchemeRegistry= bahmniReportsConfiguration.schemeRegistry(allTrustSSLSocketFactory);
		ConnectionSocketFactory actualScheme = actualSchemeRegistry.lookup("https");
		assertTrue(actualScheme.equals(allTrustSSLSocketFactory));

	}

	@Ignore
	@Test
	public void shouldReturnDefaultSchemaRegistryIfSpecifiedInConfiguration(){

		when(bahmniReportsProperties.getTrustSSLConnection()).thenReturn("false");
		BahmniReportsConfiguration bahmniReportsConfiguration = new BahmniReportsConfiguration(bahmniReportsProperties);
		Registry<ConnectionSocketFactory> actualSchemeRegistry= bahmniReportsConfiguration.schemeRegistry(allTrustSSLSocketFactory);

		ConnectionSocketFactory actualScheme = actualSchemeRegistry.lookup("https");
		assertFalse(actualScheme.equals(allTrustSSLSocketFactory));
	}
}
