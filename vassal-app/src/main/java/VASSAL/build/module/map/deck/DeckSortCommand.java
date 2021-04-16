/*
 *
 * Copyright (c) 2021 by The Vassal Development Team
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */
package VASSAL.build.module.map.deck;

import VASSAL.i18n.Resources;
import org.apache.commons.lang3.ArrayUtils;

public class DeckSortCommand extends AbstractDeckKeyCommand {

  public static final String SORT_PROPERTY = "sortProperty"; //NON-NLS
  public static final String SORT_DESCENDING = "sortDescending"; //NON-NLS

  private String sortKey;
  private boolean sortDescending;

  @Override
  public void performAction() {
    if (isEnabled()) {
      getDeck().sort(sortKey, sortDescending);
    }
  }

  public static String getConfigureTypeName() {
    return Resources.getString("Editor.Deck.sort_command");
  }

  @Override
  public boolean isEnabled() {
    return getDeck().isSortable();
  }

  @Override
  public void setAttribute(String key, Object value) {
    if (SORT_PROPERTY.equals(key)) {
      sortKey = (String) value;
    }
    else if (SORT_DESCENDING.equals(key)) {
      if (value instanceof Boolean) {
        sortDescending = Boolean.TRUE.equals(value);
      }
      else {
        sortDescending = "true".equals(value);
      }
    }
    super.setAttribute(key, value);
  }

  @Override
  public String getAttributeValueString(String key) {
    if (SORT_PROPERTY.equals(key)) {
      return sortKey;
    }
    else if (SORT_DESCENDING.equals(key)) {
      return String.valueOf(sortDescending);
    }
    return super.getAttributeValueString(key);
  }

  @Override
  public String[] getAttributeDescriptions() {
    return ArrayUtils.addAll(super.getAttributeDescriptions(),
      Resources.getString("Editor.Deck.sort_property"),
      Resources.getString("Editor.Deck.sort_descending"));
  }

  @Override
  public String[] getAttributeNames() {
    return ArrayUtils.addAll(super.getAttributeNames(), SORT_PROPERTY, SORT_DESCENDING);
  }

  @Override
  public Class<?>[] getAttributeTypes() {
    return ArrayUtils.addAll(super.getAttributeTypes(), String.class, Boolean.class);
  }
}
