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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class DefaultTipModel implements TipModel {

  private List tips = new ArrayList();

  public DefaultTipModel() {}

  public DefaultTipModel(Tip[] tips) {
    this(Arrays.asList(tips));
  }

  public DefaultTipModel(Collection tips) {
    this.tips.addAll(tips);
  }

  public Tip getTipAt(int index) {
    return (Tip) tips.get(index);
  }

  public int getTipCount() {
    return tips.size();
  }

  public void add(Tip tip) {
    tips.add(tip);
  }

  public void remove(Tip tip) {
    tips.remove(tip);
  }

  public Tip[] getTips() {
    return (Tip[]) tips.toArray(new Tip[tips.size()]);
  }

  public void setTips(Tip[] tips) {
    this.tips.clear();
    this.tips.addAll(Arrays.asList(tips));
  }

}
