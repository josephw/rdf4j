/*******************************************************************************
 * Copyright (c) 2016 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package org.eclipse.rdf4j.http.client;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicStatusLine;
import org.eclipse.rdf4j.http.protocol.UnauthorizedException;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class SesameSessionTest {

	@Rule
	public MockitoRule wireMocks = MockitoJUnit.rule();

	@Mock
	HttpClient client;

	@Mock
	ExecutorService executorService;

	@InjectMocks
	SesameSession session;

	@Test
	public void passingNullForDataFormatSendsAnUntypedEntity()
		throws UnauthorizedException, RDFParseException, RepositoryException, IOException
	{
		session.setQueryURL("http://localhost/");

		HttpResponse response = mock(HttpResponse.class);

		when(response.getEntity()).thenReturn(new StringEntity(""));
		when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "Ok"));

		ArgumentCaptor<HttpUriRequest> captor = ArgumentCaptor.forClass(HttpUriRequest.class);

		when(client.execute(Mockito.<HttpUriRequest> any(), Mockito.<HttpClientContext> any())).thenReturn(
				response);

		InputStream contents = new ByteArrayInputStream(new byte[0]);
		String baseURI = "http://localhost/";
		RDFFormat dataFormat = null;
		boolean overwrite = true;
		boolean preserveNodeIds = true;

		session.upload(contents, baseURI, dataFormat, overwrite, preserveNodeIds);

		verify(client).execute(captor.capture(), Mockito.<HttpClientContext> any());

		assertThat(captor.getValue(), org.hamcrest.Matchers.instanceOf(HttpPut.class));
		HttpPut put = (HttpPut)captor.getValue();
		assertThat(put.getEntity().getContentType(), Matchers.nullValue());
	}
}
