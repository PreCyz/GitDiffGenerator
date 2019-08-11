package pg.gipter.settings.dto;

import java.util.LinkedHashMap;
import java.util.Map;

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
}
