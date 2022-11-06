package com.infront.javaassessment.service;

import java.util.List;

import com.infront.javaassessment.dto.InstrumentShortingHistory;

public interface FinanstilsynetService {

	public List<InstrumentShortingHistory> getShortPosition(String isin, String fromDate, String toDate);
}
