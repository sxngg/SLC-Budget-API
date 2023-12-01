package com.slcbudget.eventmanager.domain.projections;

import java.math.BigDecimal;

public interface DebtProjection {
    String getUserDebtorId();
    String getUserDebtorName();
    String getUserDebtorEmail();
    String getUserDebtorPicture();
    String getUserCreditorId();
    String getUserCreditorName();
    String getUserCreditorEmail();
    String getUserCreditorPicture();
    BigDecimal getAmount();
    Boolean getIsPaid();
}

