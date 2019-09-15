package com.prince.serverless.model;

import lombok.Data;

/**
 * Stats class to indicate total, success, failed and not applicable users count for each channel
 * type
 *
 * @author Prince Raj
 */
@Data
public class Stats {

    private int totalUsers = 0;

    private int successUsers = 0;

    private int failedUsers = 0;

    private int notApplicableUsers = 0;

    public void incrementSuccessUsersCount() {
        successUsers++;
        totalUsers++;
    }

    public void incrementFailedUsersCount() {
        failedUsers++;
        totalUsers++;
    }

    public void incrementNotApplicableUsersCount() {
        notApplicableUsers++;
        totalUsers++;
    }
}
