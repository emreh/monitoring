package ir.myhome.agent.storage;

import ir.myhome.agent.model.MonitoringRecord;

import java.util.List;

public interface StorageClient {
    void store(MonitoringRecord record);

    void storeBatch(List<MonitoringRecord> records);
}
