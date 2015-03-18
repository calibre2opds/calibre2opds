import com.gmail.dpierron.calibre.configuration.GetConfigurationInterface;
import com.gmail.dpierron.tools.i18n.Localization;
import com.gmail.dpierron.tools.Helper;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertTrue;


public class TestLocalisation {

  @Test
  public void test() {
    System.out.println(Localization.Main.getAvailableLocalizationsAsIso2());
  }

  @Test
  public void testAllOptions() {
    List<Locale> languages = Localization.Main.getAvailableLocalizationsAsLocales();
    for (Locale language : languages) {
      Localization.Main.reloadLocalizations(language);
      for (Method getter : GetConfigurationInterface.class.getMethods()) {
        String getterName = getter.getName();
        String optionName = getterName.substring(3);
        // skip DeviceMode
        if ("DeviceMode".equals(optionName))
          continue;
        // skip CustomCatalogs
        if ("CustomCatalogs".equals(optionName))
          continue;
        String labelKey = "config." + optionName + ".label";
        String label = Localization.Main.getText(labelKey);
        assertTrue(language + "->" + labelKey, Helper.isNotNullOrEmpty(label) && !label.equals(labelKey));
        String descriptionKey = "config." + optionName + ".tooltip";
        String description = Localization.Main.getText(descriptionKey);
        assertTrue(language + "->" + descriptionKey, Helper.isNotNullOrEmpty(description) && !description.equals(descriptionKey));
      }
    }
  }

  @Test
  public void testFallbackToEnglish() {
    Localization.Main.reloadLocalizations(Locale.FRENCH);
    String key = "title.numberOfPages";
    String value = Localization.Main.getText(key);
    assertTrue(Helper.isNotNullOrEmpty(value));

  }
}
