package VASSAL.build;

import VASSAL.build.module.PrototypesContainer;
import VASSAL.i18n.Resources;

import java.util.ArrayList;
import java.util.List;

public class Folder<T extends AbstractConfigurable> extends AbstractFolder {
  final Class<T> typeParameterClass;

  /**
   * This grotesque workaround allows us to access what should be available as "T.class"
   * @param typeParameterClass
   */
  public Folder(Class<T> typeParameterClass) {
    this.typeParameterClass = typeParameterClass;
  }

  /**
   * This returns an array of (a) allowable child components of our folders, plus the folder type itself
   * @return
   */
  @Override
  public Class<?>[] getAllowableConfigureComponents() {
    final List<Class<?>> allow = new ArrayList<>(getAllowableFolderComponents());
    allow.add(typeParameterClass);
    return (Class<?>[]) allow.toArray();
  }

  /**
   * @return name of Folder type
   */
  public static String getConfigureTypeName() {
    return Resources.getString("Editor.Folder.component_type"); //$NON-NLS-1$
  }
}

