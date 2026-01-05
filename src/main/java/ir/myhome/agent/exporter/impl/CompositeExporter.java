package ir.myhome.agent.exporter.impl;

import ir.myhome.agent.core.Span;
import ir.myhome.agent.exporter.AgentExporter;

import java.util.ArrayList;
import java.util.List;

/**
 * می‌تواند هر تعداد اکسپورتر را به‌طور هم‌زمان مدیریت کرده و داده‌ها را به آن‌ها ارسال کند.
 * در مواردی مفید است که بخواهیم چندین اکسپورتر را به‌طور هم‌زمان استفاده کنیم بدون اینکه نیاز به مدیریت سیاست‌های پیچیده باشد
 */
public class CompositeExporter implements AgentExporter {
    private final List<AgentExporter> exporters = new ArrayList<>();

    public void addExporter(AgentExporter exporter) {
        exporters.add(exporter);
    }

    @Override
    public void export(List<Span> batch) {
        for (AgentExporter exporter : exporters) {
            exporter.export(batch);
        }
    }
}
