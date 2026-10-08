package com.sevatrack.service;

/** A rule was violated (illegal transition, wrong owner, DB-enforced rule...). Safe to show to users. */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
