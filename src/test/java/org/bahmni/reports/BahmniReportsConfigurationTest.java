package org.bahmni.reports;

import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.junit.Before;
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
	SSLSocketFactory allTrustSSLSocketFactory;

	@Before
	public void setup(){
		initMocks(this);
	}

	@Test
	public void shouldReturnAllTrustSchemaRegistryIfNotSpecifiedInConfiguration(){

		when(bahmniReportsProperties.getTrustSSLConnection()).thenReturn("true");
		BahmniReportsConfiguration bahmniReportsConfiguration = new BahmniReportsConfiguration(bahmniReportsProperties);
		SchemeRegistry actualSchemeRegistry= bahmniReportsConfiguration.schemeRegistry(allTrustSSLSocketFactory);

		Scheme actualScheme = actualSchemeRegistry.get("https");
		assertTrue(actualScheme.getSchemeSocketFactory().equals(allTrustSSLSocketFactory));

	}

	@Test
	public void shouldReturnDefaultSchemaRegistryIfSpecifiedInConfiguration(){

		when(bahmniReportsProperties.getTrustSSLConnection()).thenReturn("false");
		BahmniReportsConfiguration bahmniReportsConfiguration = new BahmniReportsConfiguration(bahmniReportsProperties);
		SchemeRegistry actualSchemeRegistry= bahmniReportsConfiguration.schemeRegistry(allTrustSSLSocketFactory);

		Scheme actualScheme = actualSchemeRegistry.get("https");
		assertFalse(actualScheme.getSchemeSocketFactory().equals(allTrustSSLSocketFactory));
	}
}
