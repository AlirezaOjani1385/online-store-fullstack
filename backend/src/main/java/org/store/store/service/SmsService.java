package org.store.store.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    public void sendSms(String phoneNumber, String message) {
        logger.info("SMS sent to {}: {}", phoneNumber, message);
    }
}