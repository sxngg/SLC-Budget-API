package com.slcbudget.eventmanager.domain.projections;

import java.math.BigDecimal;

public interface EventContactProjection {
    Long getEvent_contact_id();
    Long getContactId();
    String getContactEmail();
    String getContactName();
    String getContactLastName();
    String getContactUsername();
    String getContactProfileImage();
    BigDecimal getBalance();
}