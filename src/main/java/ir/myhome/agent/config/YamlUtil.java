package ir.myhome.agent.config;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;

public final class YamlUtil {

    private YamlUtil() {
    }

    public static <T> T loadYaml(String path, Class<T> type) {
        try (InputStream in = YamlUtil.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new RuntimeException("Cannot find resource: " + path);
            }

            // LoaderOptions required in SnakeYAML 2.5+
            LoaderOptions options = new LoaderOptions();
            Constructor constructor = new Constructor(type, options);
            Yaml yaml = new Yaml(constructor);

            return yaml.loadAs(in, type);
        } catch (Exception ex) {
            System.err.println("[YamlUtil] failed to read yaml: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }
}
