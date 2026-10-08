package com.sevatrack.dao;

import com.sevatrack.model.EscalationRecord;
import com.sevatrack.model.StatusHistory;

import java.util.List;

public interface HistoryDao {
    List<StatusHistory> history(long complaintId);

    List<EscalationRecord> escalations(long complaintId);
}
