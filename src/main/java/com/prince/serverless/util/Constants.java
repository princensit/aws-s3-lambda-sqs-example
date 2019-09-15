package com.prince.serverless.util;

/**
 * Constants class
 *
 * @author Prince Raj
 */
public interface Constants {

    String ACTIVITY_NAME = "Notification";

    String NAME = "name";

    String DATE = "date";

    String EMAIL_ID = "email";

    String MOBILE = "mobile";

    String TEMPLATE_ID = "templateId";

    String TOTAL_EXECUTION_TIME = "totalExecutionTime";

    String FILE_NAME = "fileName";

    String FILE_TYPE = "fileType";

    String UTF_8 = "UTF-8";

    String NOTIFICATION_ACTIVITY_SUBJECT_TEMPLATE = "${fileType} Notification Activity - ${date}";

    String NOTIFICATION_ACTIVITY_BODY_TEMPLATE = "templates/NotificationActivityStatus.html";

    String[] INPUT_FILE_HEADERS = {"userid", "emailid", "mobile", "date", "sendemail", "sendsms"};

    String[] OUTPUT_FILE_HEADERS = {"userid", "sendemailstatus", "sendsmsstatus"};

    String UNDERSCORE = "_";

    String FORWARD_SLASH = "/";

    String DOT = ".";

    String TEMP_FOLDER = "/tmp/";

    String BASE_INPUT_FOLDER_NAME = ACTIVITY_NAME + FORWARD_SLASH + "input" + FORWARD_SLASH;

    String BASE_OUTPUT_FOLDER_NAME = ACTIVITY_NAME + FORWARD_SLASH + "output" + FORWARD_SLASH;

    String OUTPUT_FILE_SEPARATOR = "output";

    String SUBJECT = "subject";

    String BODY = "message";

    String TEMPLATE_LOG_TAG = "Template";

    String EMAIL_GROUP_ID = "email-group";

    String SMS_GROUP_ID = "sms-group";

    String ADMIN_GROUP_ID = "admin-group";

    String EMAIL_TOTAL_USERS_COUNT = "emailTotalUsersCount";

    String EMAIL_SUCCESS_USERS_COUNT = "emailSuccessUsersCount";

    String EMAIL_FAILED_USERS_COUNT = "emailFailedUsersCount";

    String EMAIL_NOT_APPLICABLE_USERS_COUNT = "emailNotApplicableUsersCount";

    String SMS_TOTAL_USERS_COUNT = "smsTotalUsersCount";

    String SMS_SUCCESS_USERS_COUNT = "smsSuccessUsersCount";

    String SMS_FAILED_USERS_COUNT = "smsFailedUsersCount";

    String SMS_NOT_APPLICABLE_USERS_COUNT = "smsNotApplicableUsersCount";
}
