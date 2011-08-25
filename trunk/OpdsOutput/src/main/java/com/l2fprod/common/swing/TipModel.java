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
package com.l2fprod.common.swing;

/**
 * A model for {@link com.l2fprod.common.swing.JTipOfTheDay}.<br>
 */
public interface TipModel {

  /**
   * @return the number of tips in this model
   */
  int getTipCount();

  /**
   * @param index
   * @return the tip at <code>index</code>
   * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &gt;= getTipCount()).
   */
  Tip getTipAt(int index);

  /**
   * A tip.<br>
   */
  interface Tip {

    /**
     * @return very short (optional) description for the tip
     */
    String getTipName();

    /**
     * The tip object to show. See {@link JTipOfTheDay} for supported object
     * types.
     * 
     * @return the tip to display
     */
    Object getTip();
  }

}
