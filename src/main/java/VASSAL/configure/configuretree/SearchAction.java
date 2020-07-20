package VASSAL.configure.configuretree;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import VASSAL.build.Configurable;
import VASSAL.build.module.Chatter;
import VASSAL.i18n.Resources;

public class SearchAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private final ConfigureTree configureTree;
  private final String searchCmd;
  private final Frame ancestor;
  private final SearchParameters searchParams;
  private Chatter chatter;

  public SearchAction(ConfigureTree configureTree, String searchCmd, Frame ancestor, SearchParameters searchParams, Chatter chatter) {
    super(searchCmd);
    this.configureTree = configureTree;
    this.searchCmd = searchCmd;
    this.ancestor = ancestor;
    this.searchParams = searchParams;
    this.chatter = chatter;
  }

  @Override
  public void actionPerformed(ActionEvent e) {

    final JDialog d = new JDialog(ancestor, true);
    d.setTitle(searchCmd);
    d.setLayout(new BoxLayout(d.getContentPane(), BoxLayout.Y_AXIS));
    Box box = Box.createHorizontalBox();
    box.add(new JLabel("String to find: "));
    box.add(Box.createHorizontalStrut(10));

    final JTextField search = new JTextField(searchParams.getSearchString(), 32);
    box.add(search);

    d.add(box);

    box = Box.createHorizontalBox();
    final JCheckBox sensitive = new JCheckBox(Resources.getString("Editor.search_case"), searchParams.isMatchCase());
    box.add(sensitive);
    final JCheckBox names = new JCheckBox(Resources.getString("Editor.search_names"), searchParams.isMatchNames());
    box.add(names);
    final JCheckBox types = new JCheckBox(Resources.getString("Editor.search_types"), searchParams.isMatchTypes());
    box.add(types);
    d.add(box);

    box = Box.createHorizontalBox();
    JButton find = new JButton(Resources.getString("Editor.search_next"));
    find.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        boolean anyChanges = (!searchParams.getSearchString().equals(search.getText())) ||
                             (searchParams.isMatchCase() != sensitive.isSelected()) ||
                             (searchParams.isMatchNames() != names.isSelected()) ||
                             (searchParams.isMatchTypes() != types.isSelected());

        searchParams.setSearchString(search.getText());
        searchParams.setMatchCase(sensitive.isSelected());
        searchParams.setMatchNames(names.isSelected());
        searchParams.setMatchTypes(types.isSelected());

        if (!searchParams.isMatchNames() && !searchParams.isMatchTypes()) {
          searchParams.setMatchNames(true);
          names.setSelected(true);
          chat (Resources.getString("Editor.search_all_off"));
        }

        if (!searchParams.getSearchString().isEmpty()) {
          if (anyChanges) {
            int matches = getNumMatches(searchParams.getSearchString());
            chat (matches + " " + Resources.getString("Editor.search_count") + searchParams.getSearchString());
          }

          DefaultMutableTreeNode node = findNode(searchParams.getSearchString());
          if (node != null) {
            TreePath path = new TreePath(node.getPath());
            configureTree.setSelectionPath(path);
            configureTree.scrollPathToVisible(path);
          }
          else {
            chat (Resources.getString("Editor.search_none_found") + searchParams.getSearchString());
          }
        }
      }
    });


    JButton cancel = new JButton(Resources.getString(Resources.CANCEL));
    cancel.addActionListener(e1 -> d.dispose());

    box.add(find);
    box.add(cancel);
    d.add(box);

    d.getRootPane().setDefaultButton(find); // Enter key activates search

    // Esc Key cancels
    KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    int w = JComponent.WHEN_IN_FOCUSED_WINDOW;
    d.getRootPane().registerKeyboardAction(ee -> d.dispose(), k, w);

    d.pack();
    d.setLocationRelativeTo(d.getParent());
    d.setVisible(true);
  }

  /**
   * Search through the tree, starting at the currently selected location (and wrapping around if needed)
   * Compare nodes until we find our search string (or have searched everything we can search)
   * @return the node we found, or null if none
   */
  public final DefaultMutableTreeNode findNode(String searchString) {
    List<DefaultMutableTreeNode> searchNodes = getSearchNodes((DefaultMutableTreeNode)configureTree.getModel().getRoot());
    DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)configureTree.getLastSelectedPathComponent();

    DefaultMutableTreeNode foundNode = null;
    int bookmark = -1;

    if (currentNode != null) {
      for (int index = 0; index < searchNodes.size(); index++) {
        if (searchNodes.get(index) == currentNode) {
          bookmark = index;
          break;
        }
      }
    }

    for (int index = bookmark + 1; index < searchNodes.size(); index++) {
      if (checkNode(searchNodes.get(index), searchString)) {
        foundNode = searchNodes.get(index);
        break;
      }
    }

    if (foundNode == null) {
      for (int index = 0; index <= bookmark; index++) {
        if (checkNode(searchNodes.get(index), searchString)) {
          foundNode = searchNodes.get(index);
          break;
        }
      }
    }

    return foundNode;
  }

  /**
   * @return how many total nodes match the search string
   */
  public final int getNumMatches(String searchString) {
    List<DefaultMutableTreeNode> searchNodes = getSearchNodes((DefaultMutableTreeNode)configureTree.getModel().getRoot());
    return (int) searchNodes.stream().filter(node -> checkNode(node, searchString)).count();
  }

  /**
   * Enumerates our configure tree in preparation for searching it
   * @param root - root of our module's tree.
   * @return a list of search nodes
   */
  private List<DefaultMutableTreeNode> getSearchNodes(DefaultMutableTreeNode root) {
    List<DefaultMutableTreeNode> searchNodes = new ArrayList<>();

    Enumeration<?> e = root.preorderEnumeration();
    while(e.hasMoreElements()) {
      searchNodes.add((DefaultMutableTreeNode)e.nextElement());
    }
    return searchNodes;
  }

  /**
   * @param node - any node of our module tree
   * @param searchString - our search string
   * @return true if the node matches our searchString based on search configuration ("match" checkboxes)
   */
  public final boolean checkNode(DefaultMutableTreeNode node, String searchString) {
    Configurable c;
    c = (Configurable) node.getUserObject();

    if (searchParams.isMatchNames()) {
      String objectName = c.getConfigureName();
      if (objectName != null && checkString(objectName, searchString)) {
        return true;
      }
    }

    if (searchParams.isMatchTypes()) {
      String className = ConfigureTree.getConfigureName(c.getClass());
      if (className != null && checkString(className, searchString)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Checks a single string against our search parameters
   * @param target - string to check
   * @param searchString - our search string
   * @return true if this is a match based on our "matchCase" checkbox
   */
  public final boolean checkString(String target, String searchString) {
    if (searchParams.isMatchCase()) {
      return target.contains(searchString);
    }
    else {
      return target.toLowerCase().contains(searchString.toLowerCase());
    }
  }

  private void chat (String text) {
    if (chatter != null) {
      chatter.show("- " + text);
    }
  }

}
