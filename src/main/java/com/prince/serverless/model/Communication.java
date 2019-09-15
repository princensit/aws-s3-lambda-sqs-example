package com.prince.serverless.model;

import java.io.Serializable;
import java.util.Map;

import lombok.Data;

/**
 * Communication model
 *
 * @author Prince Raj
 */
@Data
public class Communication implements Serializable {

    private static final long serialVersionUID = 7955878429831876811L;

    // template id
    private Integer templateId;

    // email id
    private String emailId;

    // subject
    private String subject;

    // body
    private String body;

    // data map for placeholders
    private Map<String, String> dataMap;
}
