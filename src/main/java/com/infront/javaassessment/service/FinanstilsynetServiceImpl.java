package com.infront.javaassessment.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.infront.javaassessment.dto.InstrumentShortingHistory;
import com.infront.javaassessment.exception.InstrumentNotFoundException;
import com.infront.javaassessment.exception.InvalidInputException;
import com.infront.javaassessment.util.FinanstilsynetConstants;
import com.infront.javaassessment.util.FinanstilsynetUtils;

@Service
public class FinanstilsynetServiceImpl implements FinanstilsynetService {

	private Logger log = LoggerFactory.getLogger(FinanstilsynetServiceImpl.class);

	@Autowired
	private FinanstilsynetUtils finanstilsynetUtils;


	/*
	 * Method to get short position events based on isin and  to/from dates
	 * 
	 * @param isin, fromDate, toDate
	 * 
	 * @return List<InstrumentShortingHistory>
	 * 
	 */
	@Override
	public List<InstrumentShortingHistory> getShortPosition(String isin, String fromDate, String toDate) {
		log.debug("ISIN is : {}, From Date: {}, To Date: {}", isin, fromDate, toDate);

		List<InstrumentShortingHistory> instrumentListResp = new ArrayList<>();
		var response = finanstilsynetUtils.getInstrumentDetails();

		if (response != null) {
			var instrumentList = Arrays.asList(response).stream().filter(i -> i.getIsin().equals(isin))
					.collect(Collectors.toList());
			log.debug("Instrument List: {}", instrumentList);
			
			if (instrumentList.isEmpty()) {
				throw new InstrumentNotFoundException(FinanstilsynetConstants.ERROR_MSG);
			}

			if (isValidDate(fromDate, toDate)) {
				var formatter = DateTimeFormatter.ofPattern(FinanstilsynetConstants.DATE_FORMAT);
				var eventList = instrumentList.get(0).getEvents().stream().filter(e -> !LocalDate
						.parse(e.getDate(), formatter).isBefore(finanstilsynetUtils.getLocalDate(fromDate))
						&& !LocalDate.parse(e.getDate(), formatter).isAfter(finanstilsynetUtils.getLocalDate(toDate)))
						.collect(Collectors.toList());
				var insResponse = InstrumentShortingHistory.of(isin, instrumentList.get(0).getIssuerName(), eventList);
				instrumentListResp.add(insResponse);
			} else {
				if (fromDate != null && toDate == null || fromDate == null && toDate != null || 
						fromDate != null && fromDate.isBlank() || toDate != null && toDate.isBlank()) {
					throw new InvalidInputException(FinanstilsynetConstants.MISSING_DATE_VALIDATION);
				} else {
					return instrumentList;
				}
			}
		}
		return instrumentListResp;
	}

	/*
	 * Method to validate the dates
	 * 
	 * @param fromDate, toDate
	 * 
	 * @return boolean
	 * 
	 */
	private boolean isValidDate(String fromDate, String toDate) {
		boolean isValid = false;
		if (fromDate != null && toDate != null && !fromDate.isBlank() && !toDate.isBlank()) {
			if (!finanstilsynetUtils.getLocalDate(fromDate).isAfter(finanstilsynetUtils.getLocalDate(toDate))) {
				isValid = true;
			} else {
				throw new InvalidInputException(FinanstilsynetConstants.FROM_TO_DATE_VALIDATION);
			}
		}
		return isValid;
	}

}
