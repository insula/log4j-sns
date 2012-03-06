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

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.TriggeringEventEvaluator;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;

public class QuietPeriodTriggeringEventEvaluator implements TriggeringEventEvaluator {

	private static final int DEFAULT_QUIET_PERIOD = 15;

	private final Cache<List<String>, Boolean> lastThrowables;

	public QuietPeriodTriggeringEventEvaluator(int quietPeriod, TimeUnit timeUnit) {
		this.lastThrowables = CacheBuilder.newBuilder().expireAfterWrite(quietPeriod, timeUnit).build();
	}

	public QuietPeriodTriggeringEventEvaluator() {
		this(DEFAULT_QUIET_PERIOD, TimeUnit.MINUTES);
	}

	@Override
	public boolean isTriggeringEvent(LoggingEvent event) {
		String[] throwableStrRep = event.getThrowableStrRep();
		if (throwableStrRep != null) {
			final ImmutableList<String> throwableStrList = ImmutableList.copyOf(throwableStrRep);
			if (lastThrowables.getIfPresent(throwableStrList) == null) {
				lastThrowables.put(throwableStrList, true);
				return true;
			}
		}
		return false;
	}

}
