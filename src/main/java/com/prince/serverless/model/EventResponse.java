package com.prince.serverless.model;

import java.util.Map;

import lombok.Data;

import com.prince.serverless.util.ChannelType;

/**
 * This represents response when file is completely processed which were in input event
 *
 * @author Prince Raj
 */
@Data
public class EventResponse {

    private String fileName;

    private long totalExecutionTime;

    private Map<ChannelType, Stats> channelTypeStatsMap;
}
