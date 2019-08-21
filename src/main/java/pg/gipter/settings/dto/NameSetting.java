package pg.gipter.settings.dto;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class NameSetting {

    private Map<String, NamePatternValue> nameSettings = new LinkedHashMap<>();

    public void addSetting(String key, NamePatternValue value) {
        nameSettings.put(key, value);
    }

    public Map<String, NamePatternValue> getNameSettings() {
        return nameSettings;
    }

    public void setNameSettings(Map<String, NamePatternValue> nameSettings) {
        this.nameSettings = nameSettings;
    }

    public void removeSettings(Set<String> keys) {
        for (String key : keys) {
            nameSettings.remove(key);
        }
    }
}
