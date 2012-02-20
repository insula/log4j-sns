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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.TriggeringEventEvaluator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

public class QuietPeriodTriggeringEventEvaluator implements TriggeringEventEvaluator {

	private static final int DEFAULT_QUIET_PERIOD = 15;

	private final ScheduledExecutorService executorService;

	private final int quietPeriod;

	private final TimeUnit timeUnit;

	private ImmutableSet<ImmutableList<String>> lastThrowables = ImmutableSet.of();

	public QuietPeriodTriggeringEventEvaluator(ScheduledExecutorService executorService, int quietPeriod,
			TimeUnit timeUnit) {
		this.executorService = executorService;
		this.quietPeriod = quietPeriod;
		this.timeUnit = timeUnit;
	}

	public QuietPeriodTriggeringEventEvaluator(ScheduledExecutorService executorService) {
		this(executorService, DEFAULT_QUIET_PERIOD, TimeUnit.MINUTES);
	}

	public QuietPeriodTriggeringEventEvaluator(int quietPeriod, TimeUnit timeUnit) {
		this(Executors.newScheduledThreadPool(1, new DaemonThreadFactory()), quietPeriod, timeUnit);
	}

	public QuietPeriodTriggeringEventEvaluator() {
		this(DEFAULT_QUIET_PERIOD, TimeUnit.MINUTES);
	}

	@Override
	public boolean isTriggeringEvent(LoggingEvent event) {
		String[] throwableStrRep = event.getThrowableStrRep();
		if (throwableStrRep != null) {
			final ImmutableList<String> throwableStrList = ImmutableList.copyOf(throwableStrRep);
			if (!lastThrowables.contains(throwableStrList)) {
				lastThrowables = ImmutableSet.<ImmutableList<String>> builder().addAll(lastThrowables)
						.add(throwableStrList).build();
				executorService.schedule(new Runnable() {
					@Override
					public void run() {
						Builder<ImmutableList<String>> builder = ImmutableSet.<ImmutableList<String>> builder();
						for (ImmutableList<String> item : lastThrowables) {
							if (!item.equals(throwableStrList)) {
								builder.add(item);
							}
						}
						lastThrowables = builder.build();
					}
				}, quietPeriod, timeUnit);
				return true;
			}
		}
		return false;
	}

}
