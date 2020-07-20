package VASSAL.configure;

import VASSAL.build.Configurable;
import VASSAL.build.module.documentation.HelpWindow;
import VASSAL.launch.EditorWindow;

/**
 * @deprecated use {@link VASSAL.configure.configuretree.ConfigureTree} instead
 */
@Deprecated
public class ConfigureTree extends VASSAL.configure.configuretree.ConfigureTree {

  /**
   * @deprecated use {@link VASSAL.configure.configuretree.ConfigureTree} instead
   */
  @Deprecated
  public ConfigureTree(Configurable root, HelpWindow helpWindow) {
    super(root, helpWindow);
  }

  /**
   * @deprecated use {@link VASSAL.configure.configuretree.ConfigureTree} instead
   */
  @Deprecated
  public ConfigureTree(Configurable root, HelpWindow helpWindow, EditorWindow editorWindow) {
    super(root, helpWindow, editorWindow);
  }

}
