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

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.AutoConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.DrawPile;
import VASSAL.configure.Configurer;
import VASSAL.configure.NamedHotKeyConfigurer;
import VASSAL.configure.PlayerIdFormattedStringConfigurer;
import VASSAL.counters.Deck;
import VASSAL.counters.KeyCommand;
import VASSAL.i18n.Resources;
import VASSAL.i18n.TranslatableConfigurerFactory;
import VASSAL.tools.FormattedString;
import VASSAL.tools.NamedKeyStroke;
import VASSAL.tools.NamedKeyStrokeListener;

import java.awt.event.ActionEvent;

/**
 * Superclass for menu/Hotley commands that can be attached to Decks
 */
public abstract class AbstractDeckKeyCommand extends AbstractConfigurable implements DeckKeyCommand {

  public static final String NAME = "name";
  public static final String DESCRIPTION = "description";
  public static final String HOTKEY = "hotkey";
  public static final String REPORT_FORMAT = "reportFormat";

  private String description;
  private final FormattedString reportFormat = new FormattedString();
  private NamedKeyStroke hotkey;
  private DrawPile drawPile;
  private NamedKeyStrokeListener listener;
  private KeyCommand keyCommand;

  @Override
  public KeyCommand getKeyCommand() {
    if (!isEnabled()) {
      return null;
    }
    if (keyCommand == null) {
      keyCommand = new KeyCommand(getConfigureName(), hotkey.getKeyStroke()) {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          performAction();
        }
      };
    }
    return keyCommand;
  }

  public Deck getDeck() {
    return drawPile.getDeck();
  }

  @Override
  public void addKeyStrokeListener() {
    if (listener == null) {
      listener = new NamedKeyStrokeListener(e -> performAction());
      listener.setKeyStroke(hotkey);
      GameModule.getGameModule().addKeyStrokeListener(listener);
    }
  }

  @Override
  public void removeKeyStrokeListener() {
    if (listener != null) {
      GameModule.getGameModule().removeKeyStrokeListener(listener);
      listener = null;
    }
  }

  @Override
  public String[] getAttributeNames() {
    return new String[] { DESCRIPTION, NAME, HOTKEY, REPORT_FORMAT };
  }

  @Override
  public void setAttribute(String key, Object value) {
    if (NAME.equals(key)) {
      setConfigureName((String) value);
    }
    else if (DESCRIPTION.equals(key)) {
      description = value == null ? "" : value.toString();
    }
    else if (REPORT_FORMAT.equals(key)) {
      reportFormat.setFormat((String) value);
    }
    else if (HOTKEY.equals(key)) {
      if (value instanceof String) {
        value = NamedHotKeyConfigurer.decode((String) value);
      }
      hotkey = (NamedKeyStroke) value;
    }
  }

  @Override
  public String getAttributeValueString(String key) {
    if (NAME.equals(key)) {
      return getConfigureName();
    }
    else if (DESCRIPTION.equals(key)) {
      return description;
    }
    else if (REPORT_FORMAT.equals(key)) {
      return reportFormat.getFormat();
    }
    else if (HOTKEY.equals(key)) {
      return NamedHotKeyConfigurer.encode(hotkey);
    }
    return null;
  }

  @Override
  public String[] getAttributeDescriptions() {
    return new String[] {
      Resources.getString("Editor.description_label"),
      Resources.getString("Editor.menu_command"),
      Resources.getString("Editor.hotkey_label"),
      Resources.getString("Editor.report_format")
    };
  }

  @Override
  public Class<?>[] getAttributeTypes() {
    return new Class[] {String.class, String.class, NamedKeyStroke.class, ReportFormatConfig.class};
  }

  @Override
  public void addTo(Buildable parent) {
    drawPile = (DrawPile) parent;
    drawPile.addDeckKeyCommand(this);
  }

  @Override
  public void removeFrom(Buildable parent) {
    drawPile.removeDeckKeyCommand(this);
  }

  @Override
  public HelpFile getHelpFile() {
    return null;
  }

  @Override
  public Class[] getAllowableConfigureComponents() {
    return new Class[0];
  }

  public static class ReportFormatConfig implements TranslatableConfigurerFactory {
    @Override
    public Configurer getConfigurer(AutoConfigurable c, String key, String name) {
      return new PlayerIdFormattedStringConfigurer(key, name, new String[0]);
    }
  }

  /**
   * Each subclass must implement performAction to
   * a) Check if it is valid to perform that action
   * b) perform and log the action.
   */
  public abstract void performAction();

}
