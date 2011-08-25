/**
 * L2FProd.com Common Components 7.3 License.
 *
 * Copyright 2005-2007 L2FProd.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.l2fprod.common.swing.tips;

import com.l2fprod.common.swing.TipModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Loads tips from various sources.<br>
 */
public class TipLoader {

  /**
   * Initializes a TipModel from properties. Each tip is defined by two
   * properties, its name and its description:
   * 
   * <pre>
   *  tip.1.name=First Tip
   *  tip.1.description=This is the description
   *  
   *  tip.2.name=Second Tip
   *  tip.2.description=&lt;html&gt;This is an html description
   * </pre>
   * 
   * @param props
   * @return a TipModel
   * @throws IllegalArgumentException
   *           if a name is found without description
   */
  public static TipModel load(Properties props) {
    List tips = new ArrayList();

    int count = 1;
    while (true) {
      String nameKey = "tip." + count + ".name";
      String nameValue = props.getProperty(nameKey);

      String descriptionKey = "tip." + count + ".description";
      String descriptionValue = props.getProperty(descriptionKey);

      if (nameValue != null && descriptionValue == null) {
        throw new IllegalArgumentException(
          "No description for name " + nameValue);
      }
      
      if (descriptionValue == null) {
        break;
      }
      
      DefaultTip tip = new DefaultTip(nameValue, descriptionValue);
      tips.add(tip);

      count++;
    }

    return new DefaultTipModel(tips);
  }

}
