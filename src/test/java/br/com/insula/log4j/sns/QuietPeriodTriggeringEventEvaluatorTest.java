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

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.TriggeringEventEvaluator;
import org.junit.Test;

public class QuietPeriodTriggeringEventEvaluatorTest {

	private static final String SAMPLE_LOGGING_MESSAGE = "Sample logging message.";

	@Test
	public void testIsTriggeringEvent() throws InterruptedException {
		TriggeringEventEvaluator evaluator = new QuietPeriodTriggeringEventEvaluator(1, TimeUnit.SECONDS);
		Logger logger = Logger.getLogger(QuietPeriodTriggeringEventEvaluatorTest.class);
		LoggingEvent event = new LoggingEvent("", logger, Level.WARN, SAMPLE_LOGGING_MESSAGE, new Exception());
		assertTrue(evaluator.isTriggeringEvent(event));
		assertFalse(evaluator.isTriggeringEvent(event));
		assertFalse(evaluator.isTriggeringEvent(event));
		Thread.sleep(2000);
		assertTrue(evaluator.isTriggeringEvent(event));
		assertFalse(evaluator.isTriggeringEvent(event));
		assertFalse(evaluator.isTriggeringEvent(event));
	}

}
