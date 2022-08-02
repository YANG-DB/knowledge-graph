import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class ECSProcessor {
    /**
     * experiment with ecs generated nested yaml data
     * @param args
     */
    public static void main(String[] args) {
        InputStream inputStream = ECSProcessor.class.getResourceAsStream("ecs.yml");
        Yaml yaml = new Yaml();
//        Map<String, Object> data = yaml.load(inputStream);
        Map<String,Entity> data = yaml.load(inputStream);
        System.out.println(data);

//        data.entrySet().stream().map(e->e.getValue().)
    }

}
