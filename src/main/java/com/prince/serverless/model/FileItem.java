package com.prince.serverless.model;

import lombok.Data;

/**
 * Individual line item in file
 *
 * @author Prince Raj
 */
@Data
public class FileItem {

    // user id
    private String userId;

    // email id
    private String emailId;

    // mobile number
    private String mobileNumber;

    // date
    private String date;

    // send email or not
    private boolean sendEmail;

    // send sms or not
    private boolean sendSms;

    // send android push or not
    private boolean sendAndroidPush;

    // send ios push or not
    private boolean sendIosPush;

    // send in-app or not
    private boolean sendInApp;
}
