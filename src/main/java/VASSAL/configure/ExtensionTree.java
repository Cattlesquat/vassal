package VASSAL.configure;

import VASSAL.build.Configurable;
import VASSAL.build.module.ModuleExtension;
import VASSAL.build.module.documentation.HelpWindow;
import VASSAL.launch.EditorWindow;

/**
 * @deprecated use {@link VASSAL.configure.configuretree.ExtensionTree} instead
 */
@Deprecated
public class ExtensionTree extends VASSAL.configure.configuretree.ExtensionTree {

  /**
   * @deprecated use {@link VASSAL.configure.configuretree.ExtensionTree} instead
   */
  @Deprecated
  public ExtensionTree(Configurable root, HelpWindow helpWindow,
                       ModuleExtension extention, EditorWindow editorWindow) {
    super(root, helpWindow, extention, editorWindow);
  }
}
