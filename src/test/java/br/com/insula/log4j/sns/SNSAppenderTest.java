/*
 *  (C) Copyright 2012 Insula Tecnologia da Informacao Ltda.
 *
 *  This file is part of log4j-sns.
 *
 *  log4j-sns is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  log4j-sns is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with log4j-sns.  If not, see <http://www.gnu.org/licenses/>.
 */
package br.com.insula.log4j.sns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.PublishRequest;

public class SNSAppenderTest {

	private static final String SAMPLE_LOGGING_MESSAGE = "Sample logging message.";

	private static final String SUBJECT = "[WARN] SNS Logger";

	@Test
	public void testAppendLoggingEvent() {
		AmazonSNSAsync amazonSNSAsync = mock(AmazonSNSAsync.class);
		when(amazonSNSAsync.createTopic(any(CreateTopicRequest.class))).thenReturn(
				new CreateTopicResult().withTopicArn("arn:topic:test"));
		ArgumentCaptor<PublishRequest> publishRequestCaptor = ArgumentCaptor.forClass(PublishRequest.class);
		when(amazonSNSAsync.publishAsync(publishRequestCaptor.capture())).thenReturn(null);

		SNSAppender appender = new SNSAppender(amazonSNSAsync);
		appender.setTopic("test");
		appender.setSubject(SUBJECT);
		appender.setLayout(new PatternLayout("%d [%t] %-5p %c - %m%n"));
		appender.activateOptions();

		Logger logger = Logger.getLogger(SNSAppenderTest.class);
		LoggingEvent event = new LoggingEvent("", logger, Level.WARN, SAMPLE_LOGGING_MESSAGE, new Exception());
		appender.append(event);
		appender.append(event);
		appender.append(event);
		appender.append(event);
		appender.append(event);

		PublishRequest publishRequest = publishRequestCaptor.getValue();
		assertEquals(SUBJECT, publishRequest.getSubject());
		assertTrue(publishRequest.getMessage().contains(SAMPLE_LOGGING_MESSAGE));
		assertTrue(publishRequest.getMessage().contains("java.lang.Exception"));
		verify(amazonSNSAsync, times(1)).publishAsync(any(PublishRequest.class));
	}

}