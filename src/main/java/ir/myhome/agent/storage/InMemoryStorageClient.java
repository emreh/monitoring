package ir.myhome.agent.storage;

import ir.myhome.agent.model.MonitoringRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InMemoryStorageClient implements StorageClient {

    private final List<MonitoringRecord> storage = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void store(MonitoringRecord record) {
        storage.add(record);
    }

    @Override
    public void storeBatch(List<MonitoringRecord> records) {
        storage.addAll(records);
    }

    public List<MonitoringRecord> getAll() {
        return new ArrayList<>(storage);
    }
}

