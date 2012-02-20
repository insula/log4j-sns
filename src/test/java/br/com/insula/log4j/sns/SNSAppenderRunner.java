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

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class SNSAppenderRunner {

	public static void main(String[] args) throws InterruptedException {
		PropertyConfigurator.configure(SNSAppenderRunner.class.getResource("/test-log4j.properties"));

		Logger logger = Logger.getLogger(SNSAppenderRunner.class);
		for (int i = 0; i < 5; i++) {
			logger.warn("Warning Message", new Exception());
		}
		Thread.sleep(5000);
	}

}