package com.phonecompany.billing.service;

import java.math.BigDecimal;


public interface TelephoneBillCalculator {

    BigDecimal calculate (String phoneLog);

}
