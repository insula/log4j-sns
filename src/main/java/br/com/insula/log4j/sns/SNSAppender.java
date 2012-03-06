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

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.TriggeringEventEvaluator;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.AmazonSNSAsyncClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.PublishRequest;

public class SNSAppender extends AppenderSkeleton {

	private static final int SNS_MAXIMUM_MESSAGE_SIZE = 64 * 1024;

	private TriggeringEventEvaluator evaluator;

	private AmazonSNSAsync amazonSNSAsync;

	private String topicArn;

	private String accessKey;

	private String secretKey;

	private String topic;

	private String subject;

	public SNSAppender() {
	}

	public SNSAppender(AmazonSNSAsync amazonSNSAsync) {
		this.amazonSNSAsync = amazonSNSAsync;
	}

	@Override
	public void activateOptions() {
		createEvaluatorIfNeeded();
		createAmazonSNSAsyncIfNeeded();
		createTopicArn();
	}

	public boolean checkEntryConditions() {
		if (this.amazonSNSAsync == null) {
			errorHandler.error(String.format("AmazonSNSAsync object not configured for appender [%s].", name));
			return false;
		}
		if (this.evaluator == null) {
			errorHandler.error(String.format("No TriggeringEventEvaluator is set for appender [%s].", name));
			return false;
		}
		if (this.layout == null) {
			errorHandler.error(String.format("No layout set for appender named [%s].", name));
			return false;
		}
		if (this.topic == null) {
			errorHandler.error(String.format("No topic set for appender named [%s].", name));
			return false;
		}
		if (this.subject == null) {
			errorHandler.error(String.format("No subject set for appender named [%s].", name));
			return false;
		}
		return true;
	}

	@Override
	public void close() {
	}

	@Override
	public boolean requiresLayout() {
		return true;
	}

	@Override
	protected void append(LoggingEvent event) {
		if (!checkEntryConditions()) {
			return;
		}
		if (evaluator.isTriggeringEvent(event)) {
			publishMessage(createMessage(event));
		}
	}

	private void createEvaluatorIfNeeded() {
		if (this.evaluator == null) {
			this.evaluator = new QuietPeriodTriggeringEventEvaluator();
		}
	}

	private void createAmazonSNSAsyncIfNeeded() {
		if (this.amazonSNSAsync == null) {
			this.amazonSNSAsync = new AmazonSNSAsyncClient(new BasicAWSCredentials(accessKey, secretKey));
		}
	}

	private void createTopicArn() {
		this.topicArn = amazonSNSAsync.createTopic(new CreateTopicRequest(topic)).getTopicArn();
	}

	private String createMessage(LoggingEvent event) {
		StringBuilder sb = new StringBuilder();
		appendMessageHeader(sb);
		appendMessageEvent(sb, event);
		appendThrowableMessageBody(sb, event);
		appendMessageFooter(sb);
		return cutMessageTextIfExceedsMaximum(sb);
	}

	private void appendMessageHeader(StringBuilder sb) {
		String header = layout.getHeader();
		if (header != null) {
			sb.append(header);
		}
	}

	private void appendMessageEvent(StringBuilder sb, LoggingEvent event) {
		sb.append(layout.format(event));
	}

	private void appendThrowableMessageBody(StringBuilder sb, LoggingEvent event) {
		if (layout.ignoresThrowable()) {
			String[] throwableStrRep = event.getThrowableStrRep();
			if (throwableStrRep != null) {
				for (String line : throwableStrRep) {
					sb.append(line);
					sb.append(Layout.LINE_SEP);
				}
			}
		}
	}

	private void appendMessageFooter(StringBuilder sb) {
		String footer = layout.getFooter();
		if (footer != null) {
			sb.append(footer);
		}
	}

	private String cutMessageTextIfExceedsMaximum(StringBuilder sb) {
		if (sb.length() > SNS_MAXIMUM_MESSAGE_SIZE) {
			return sb.substring(0, SNS_MAXIMUM_MESSAGE_SIZE);
		}
		return sb.toString();
	}

	private void publishMessage(String message) {
		try {
			amazonSNSAsync.publishAsync(new PublishRequest(topicArn, message, subject));
		}
		catch (AmazonClientException ex) {
			errorHandler.error("Unable to publish log message to SNS.");
		}
	}

	public void setEvaluatorClass(String value) {
		this.evaluator = (TriggeringEventEvaluator) OptionConverter.instantiateByClassName(value,
				TriggeringEventEvaluator.class, evaluator);
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

}