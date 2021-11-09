/*
 *
 * Copyright (c) 2004-2009 by Rodney Kinney, Joel Uckelman
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

package VASSAL.configure;


import VASSAL.build.GameModule;
import VASSAL.build.module.map.DrawPile;
import VASSAL.build.module.map.MenuDisplayer;
import VASSAL.build.module.map.SetupStack;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.module.map.boardPicker.board.MapGrid;
import VASSAL.build.widget.PieceSlot;
import VASSAL.counters.GamePiece;
import VASSAL.i18n.Resources;
import VASSAL.tools.AdjustableSpeedScrollPane;
import VASSAL.tools.ErrorDialog;
import VASSAL.tools.image.ImageUtils;
import VASSAL.tools.menu.MenuManager;
import VASSAL.tools.swing.SwingUtils;
import org.apache.commons.lang3.SystemUtils;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DragSourceMotionListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

import static VASSAL.build.module.map.SetupStack.getCachedBoard;
import static VASSAL.build.module.map.SetupStack.isShowOthers;

public abstract class AbstractLocationConfigurer extends JFrame implements ActionListener, KeyListener, MouseListener {
  private static final long serialVersionUID = 1L;

  protected static final Dimension DEFAULT_SIZE = new Dimension(800, 600);
  protected static final int DELTA = 1;
  protected static final int FAST = 10;
  protected static final int FASTER = 5;
  protected static final int DEFAULT_DUMMY_SIZE = 50;


  protected Rectangle cachedBoundingBox;

  protected AbstractLocationConfigurerView view;
  protected JScrollPane scroll;
  protected Point savePosition;
  protected Dimension dummySize;
  protected BufferedImage dummyImage;
  protected JLabel coords;

  protected Point pos;
  protected Board board;
  protected GamePiece piece;

  public AbstractLocationConfigurer(String title) {
    this(title, new Dimension(DEFAULT_DUMMY_SIZE, DEFAULT_DUMMY_SIZE));
  }

  public AbstractLocationConfigurer(String title, Dimension dummySize) {
    super(Resources.getString("Editor.SetupStack.adjust_at_start_stack"));
    setJMenuBar(MenuManager.getInstance().getMenuBarFor(this));
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        cancel();
      }
    });
  }

  public Point getPos() {
    return pos;
  }

  public void setPos(Point pos) {
    this.pos = pos;
  }

  public Board getBoard() {
    return board;
  }

  public void setBoard(Board board) {
    this.board = board;
  }

  public GamePiece getPiece() {
    return piece;
  }

  public void setPiece(GamePiece p) {
    piece = p;
  }


  protected abstract AbstractLocationConfigurerView createView();

  protected Box createTextPanel() {
    final Box textPanel = Box.createVerticalBox();
    coords = new JLabel(pos.x + ", " + pos.y);
    textPanel.add(coords);
    //textPanel.add(new JLabel(Resources.getString("Editor.SetupStack.arrow_keys_move_stack")));
    //textPanel.add(new JLabel(Resources.getString(SystemUtils.IS_OS_MAC ? "Editor.SetupStack.shift_command_keys_move_stack_faster_mac" : "Editor.SetupStack.ctrl_shift_keys_move_stack_faster")));
    return textPanel;
  }

  protected Box createDisplayPanel() {
    return Box.createHorizontalBox();
  }

  protected Box createButtonPanel() {
    final Box buttonPanel = Box.createHorizontalBox();
    final JButton snapButton = new JButton(Resources.getString("Editor.SetupStack.snap_to_grid"));
    snapButton.addActionListener(e -> {
      snap();
      view.grabFocus();
    });
    buttonPanel.add(snapButton);
    return buttonPanel;
  }

  protected JPanel createOkPanel() {
    final JButton okButton = new JButton(Resources.getString("General.ok"));
    okButton.addActionListener(e -> {
      ok();
      setVisible(false);
    });
    final JPanel okPanel = new JPanel();
    okPanel.add(okButton);

    final JButton canButton = new JButton(Resources.getString("General.cancel"));
    canButton.addActionListener(e -> {
      cancel();
      setVisible(false);
    });
    okPanel.add(canButton);

    return okPanel;
  }

  // Main Entry Point
  protected void init() {
    view = createView(); //new AbstractLocationConfigurerView(board, myStack);

    view.addKeyListener(this);
    view.addMouseListener(this);
    view.setFocusable(true);

    scroll =
        new AdjustableSpeedScrollPane(
            view,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

    scroll.setPreferredSize(DEFAULT_SIZE);

    add(scroll, BorderLayout.CENTER);

    final Box textPanel = createTextPanel();
    final Box displayPanel = createDisplayPanel();
    final Box buttonPanel = createButtonPanel();
    final JPanel okPanel = createOkPanel();

    final Box controlPanel = Box.createHorizontalBox();
    controlPanel.add(textPanel);
    controlPanel.add(displayPanel);
    controlPanel.add(buttonPanel);

    final Box mainPanel = Box.createVerticalBox();
    mainPanel.add(controlPanel);
    mainPanel.add(okPanel);

    add(mainPanel, BorderLayout.SOUTH);

    scroll.revalidate();
    updateDisplay();
    pack();
    repaint();
  }

  protected void ok() {
    //
  }

  protected void cancel() {
    pos.x = savePosition.x;
    pos.y = savePosition.y;
  }


  public void updateCoords(String text) {
    coords.setText(text);
  }

  public void updateCoords() {
    coords.setText(pos.x + ", " + pos.y);
  }

  public void updateDisplay() {
    if (!view.getVisibleRect().contains(pos)) {
      view.center(new Point(pos.x, pos.y));
    }
  }

  protected void snap() {
    final MapGrid grid = board.getGrid();
    if (grid != null) {
      final Point snapTo = grid.snapTo(pos);
      pos.x = snapTo.x;
      pos.y = snapTo.y;
      updateCoords();
      updateDisplay();
      repaint();
    }
  }

  public JScrollPane getScroll() {
    return scroll;
  }

  /*
   * If the piece to be displayed does not have an Image, then we
   * need to supply a dummy one.
   */
  public BufferedImage getDummyImage() {
    if (dummyImage == null) {
      dummyImage = ImageUtils.createCompatibleTranslucentImage(
        dummySize.width * 2, dummySize.height * 2);
      final Graphics2D g = dummyImage.createGraphics();
      g.setColor(Color.white);
      g.fillRect(0, 0, dummySize.width, dummySize.height);
      g.setColor(Color.black);
      g.drawRect(0, 0, dummySize.width, dummySize.height);
      g.dispose();
    }
    return dummyImage;
  }

  public void drawDummyImage(Graphics g, int x, int y) {
    drawDummyImage(g, x - dummySize.width / 2, y - dummySize.height / 2, null, 1.0);
  }

  public void drawDummyImage(Graphics g, int x, int y, Component obs, double zoom) {
    final Graphics2D g2d = (Graphics2D) g;
    final AffineTransform orig_t = g2d.getTransform();
    final double os_scale = g2d.getDeviceConfiguration().getDefaultTransform().getScaleX();
    final AffineTransform scaled_t = new AffineTransform(orig_t);
    scaled_t.scale(os_scale, os_scale);
    g2d.setTransform(scaled_t);

    x /= os_scale;
    y /= os_scale;

    g.drawImage(getDummyImage(), x, y, obs);

    g2d.setTransform(orig_t);
  }

  public void drawImage(Graphics g, int x, int y, Component obs, double zoom) {
    final Rectangle r = piece == null ? null : piece.boundingBox();
    if (r == null || r.width == 0 || r.height == 0) {
      drawDummyImage(g, x, y);
    }
    else {
      piece.draw(g, x, y, obs, zoom);
    }
  }

  public Rectangle getPieceBoundingBox() {
    final Rectangle r = piece == null ? new Rectangle() : piece.boundingBox();
    if (r.width == 0 || r.height == 0) {
      r.width = dummySize.width;
      r.height = dummySize.height;
      r.x = -r.width / 2;
      r.y = -r.height / 2;
    }

    return r;
  }

  public void cacheBoundingBox() {
    cachedBoundingBox = getPieceBoundingBox();
  }

  public Rectangle getCachedBoundingBox() {
    return cachedBoundingBox;
  }

  @Override
  public void actionPerformed(ActionEvent e) {

  }

  @Override
  public void keyPressed(KeyEvent e) {

    switch (e.getKeyCode()) {
    case KeyEvent.VK_UP:
      adjustY(-1, e);
      break;
    case KeyEvent.VK_DOWN:
      adjustY(1, e);
      break;
    case KeyEvent.VK_LEFT:
      adjustX(-1, e);
      break;
    case KeyEvent.VK_RIGHT:
      adjustX(1, e);
      break;
    default :
      if (piece != null) {
        piece.keyEvent(SwingUtils.getKeyStrokeForEvent(e));
      }
      break;
    }
    updateDisplay();
    repaint();
    e.consume();
  }

  protected void adjustX(int direction, KeyEvent e) {
    int delta = direction * DELTA;
    if (e.isShiftDown()) {
      delta *= FAST;
    }
    if (SwingUtils.isModifierKeyDown(e)) {
      delta *= FASTER;
    }
    int newX = pos.x + delta;
    if (newX < 0) newX = 0;
    if (newX >= board.getSize().getWidth()) newX = (int) board.getSize().getWidth() - 1;
    pos.x = newX;
    updateCoords();
  }

  protected void adjustY(int direction, KeyEvent e) {
    int delta = direction * DELTA;
    if (e.isShiftDown()) {
      delta *= FAST;
    }
    if (SwingUtils.isModifierKeyDown(e)) {
      delta *= FASTER;
    }
    int newY = pos.y + delta;
    if (newY < 0) newY = 0;
    if (newY >= board.getSize().getHeight()) newY = (int) board.getSize().getHeight() - 1;
    pos.y = newY;
    updateCoords();
  }

  @Override
  public void keyReleased(KeyEvent e) {
  }

  @Override
  public void keyTyped(KeyEvent e) {
  }

  @Override
  public void mouseClicked(MouseEvent e) {
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }

  protected void maybePopup(MouseEvent e) {
    if (!e.isPopupTrigger() || piece == null) {
      return;
    }

    final Rectangle r = getPieceBoundingBox();
    r.translate(pos.x, pos.y);
    if (r.contains(e.getPoint())) {
      final JPopupMenu popup = MenuDisplayer.createPopup(piece);
      if (popup != null) {
        popup.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
          @Override
          public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            view.repaint();
          }

          @Override
          public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            view.repaint();
          }

          @Override
          public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
          }
        });
        if (view.isShowing()) {
          popup.show(view, e.getX(), e.getY());
        }
      }
    }
  }

  @Override
  public void mousePressed(MouseEvent e) {
    maybePopup(e);
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    maybePopup(e);
  }
}


