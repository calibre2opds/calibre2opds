import static org.junit.Assert.*;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;

import com.gmail.dpierron.calibre.configuration.ReadOnlyStanzaConfigurationInterface;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.opds.i18n.LocalizationHelper;
import com.gmail.dpierron.tools.Helper;



public class TestLocalisation {
  
  @Test
  public void test() {
    System.out.println(LocalizationHelper.INSTANCE.getAvailableLocalizations());
  }
  
  @Test
  public void testAllOptions() {
    List<String> languages = LocalizationHelper.INSTANCE.getAvailableLocalizations();
    for (String language : languages) {
      Localization.Main.reloadLocalizations(language);
      for (Method getter : ReadOnlyStanzaConfigurationInterface.class.getMethods()) {
        String getterName = getter.getName();
        String optionName = getterName.substring(3);
        // skip DeviceMode
        if ("DeviceMode".equals(optionName))
          continue;
        String labelKey = "config."+optionName+".label";
        String label = Localization.Main.getText(labelKey);
        assertTrue(language + "->"+labelKey, Helper.isNotNullOrEmpty(label) && !label.equals(labelKey));
        String descriptionKey = "config."+optionName+".description";
        String description = Localization.Main.getText(descriptionKey);
        assertTrue(language + "->"+descriptionKey, Helper.isNotNullOrEmpty(description) && !description.equals(descriptionKey));
      }
    }

  }
}
