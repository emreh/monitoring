package ir.myhome.agent.status;

public class UIAssets {

    public static String getHtmlPage() {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Java Monitoring Dashboard</title>\n" +
                "    <script src=\"/js/chart.min.js\"></script>\n" +
                "    <style>\n" +
                "        body { font-family: sans-serif; background: #1e1e1e; color: #fff; padding: 20px; }\n" +
                "        .container { max-width: 800px; margin: auto; }\n" +
                "        .card { background: #2d2d2d; padding: 20px; border-radius: 8px; margin-bottom: 20px; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <h1>Core Monitoring System - Phase 13</h1>\n" +
                "        <div class=\"card\"><canvas id=\"memChart\"></canvas></div>\n" +
                "        <div id=\"stats\">Loading...</div>\n" +
                "    </div>\n" +
                "    <script>\n" +
                "        const ctx = document.getElementById('memChart').getContext('2d');\n" +
                "        const chart = new Chart(ctx, {\n" +
                "            type: 'line',\n" +
                "            data: {\n" +
                "                labels: [],\n" +
                "                datasets: [{ label: 'Used Heap (MB)', data: [], borderColor: '#4caf50', fill: false }]\n" +
                "            }\n" +
                "        });\n" +
                "        function update() {\n" +
                "            fetch('/api/metrics').then(r => r.json()).then(data => {\n" +
                "                document.getElementById('stats').innerHTML = `Threads: ${data.threads} | Memory: ${data.usedMemory}MB / ${data.maxMemory}MB`;\n" +
                "                if(chart.data.labels.length > 20) chart.data.labels.shift(), chart.data.datasets[0].data.shift();\n" +
                "                chart.data.labels.push(new Date().toLocaleTimeString());\n" +
                "                chart.data.datasets[0].data.push(data.usedMemory);\n" +
                "                chart.update();\n" +
                "            });\n" +
                "        }\n" +
                "        setInterval(update, 2000);\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }
}