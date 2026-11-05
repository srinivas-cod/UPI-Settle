package com.upisettle.service;

import com.upisettle.model.ExceptionReport;
import com.upisettle.model.ReconciliationResult;

import java.util.ArrayList;
import java.util.List;

public class ExceptionReportService {

    public List<ExceptionReport> generateExceptionReport(
            List<ReconciliationResult> results) {

        List<ExceptionReport> exceptions = new ArrayList<>();

        for (ReconciliationResult result : results) {

            if (!result.getResult().equals("MATCHED")) {

                ExceptionReport report = new ExceptionReport(
                        result.getTransactionId(),
                        result.getResult(),
                        result.getPaymentAmount(),
                        result.getSettlementAmount(),
                        result.getAmountDifference(),
                        result.getReason(),
                        result.getDuplicateCount()
                );

                exceptions.add(report);
            }
        }

        return exceptions;
    }
}
