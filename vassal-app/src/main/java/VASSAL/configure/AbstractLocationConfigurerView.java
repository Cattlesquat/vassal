package VASSAL.configure;

import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.module.map.boardPicker.board.MapGrid;
import VASSAL.counters.GamePiece;
import VASSAL.tools.ErrorDialog;
import VASSAL.tools.image.ImageUtils;
import VASSAL.tools.swing.SwingUtils;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.AlphaComposite;
import java.awt.Color;
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
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

// FIXME: check for duplication with PieceMover
public abstract class AbstractLocationConfigurerView extends JPanel implements DropTargetListener, DragGestureListener, DragSourceListener, DragSourceMotionListener {
  private static final long serialVersionUID = 1L;
  protected static final int CURSOR_ALPHA = 127;
  protected static final int EXTRA_BORDER = 4;
  protected Board myBoard;
  protected MapGrid myGrid;
  protected GamePiece myPiece;
  protected DragSource ds = DragSource.getDefaultDragSource();
  protected boolean isDragging = false;
  protected JLabel dragCursor;
  protected JLayeredPane drawWin;
  protected Point drawOffset = new Point();
  protected Rectangle boundingBox;
  protected int currentPieceOffsetX;
  protected int currentPieceOffsetY;
  protected int originalPieceOffsetX;
  protected int originalPieceOffsetY;
  protected Point lastDragLocation = new Point();
  protected AbstractLocationConfigurer myConfigurer;

  public AbstractLocationConfigurerView(AbstractLocationConfigurer configurer) {
    myConfigurer = configurer;
    myBoard = configurer.getBoard();
    myGrid = myBoard.getGrid();
    myPiece = configurer.getPiece();
    new DropTarget(this, DnDConstants.ACTION_MOVE, this);
    ds.createDefaultDragGestureRecognizer(this,
      DnDConstants.ACTION_MOVE, this);
    setFocusTraversalKeysEnabled(false);
  }

  @Override
  public void paint(Graphics g) {
    final Graphics2D g2d = (Graphics2D) g;

    g2d.addRenderingHints(SwingUtils.FONT_HINTS);
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON);

    final double os_scale = g2d.getDeviceConfiguration().getDefaultTransform().getScaleX();

    final AffineTransform orig_t = g2d.getTransform();
    g2d.setTransform(SwingUtils.descaleTransform(orig_t));

    myBoard.draw(g, 0, 0, os_scale, this);
    if (myGrid != null) {
      final Rectangle bounds = new Rectangle(new Point(), myBoard.bounds().getSize());
      bounds.width *= os_scale;
      bounds.height *= os_scale;
      myGrid.draw(g, bounds, bounds, os_scale, false);
    }

    /*
    if (isShowOthers()) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F));

      final Rectangle r = getVisibleRect();
      r.x *= os_scale;
      r.y *= os_scale;
      r.width *= os_scale;
      r.height *= os_scale;
    }
    */

    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP));

    final int x = (int)(myConfigurer.pos.x * os_scale);
    final int y = (int)(myConfigurer.pos.y * os_scale);
    myConfigurer.drawImage(g, x, y, this, os_scale);

    g2d.setTransform(orig_t);
  }

  @Override
  public void update(Graphics g) {
    // To avoid flicker, don't clear the display first *
    paint(g);
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(
      myBoard.bounds().width,
      myBoard.bounds().height);
  }

  public void center(Point p) {
    final Rectangle r = this.getVisibleRect();
    if (r.width == 0) {
      r.width = AbstractLocationConfigurer.DEFAULT_SIZE.width;
      r.height = AbstractLocationConfigurer.DEFAULT_SIZE.height;
    }
    int x = p.x - r.width / 2;
    int y = p.y - r.height / 2;
    if (x < 0) x = 0;
    if (y < 0) y = 0;
    scrollRectToVisible(new Rectangle(x, y, r.width, r.height));
  }

  @Override
  public void dragEnter(DropTargetDragEvent arg0) {
  }

  @Override
  public void dragOver(DropTargetDragEvent e) {
    scrollAtEdge(e.getLocation(), 15);

    final Point pos = e.getLocation();
    pos.translate(currentPieceOffsetX, currentPieceOffsetY);
    myConfigurer.updateCoords(pos.x + ", " + pos.y);
  }

  public void scrollAtEdge(Point evtPt, int dist) {
    final JScrollPane scroll = myConfigurer.getScroll();

    final Point p = new Point(evtPt.x - scroll.getViewport().getViewPosition().x,
      evtPt.y - scroll.getViewport().getViewPosition().y);
    int dx = 0, dy = 0;
    if (p.x < dist && p.x >= 0)
      dx = -1;
    if (p.x >= scroll.getViewport().getSize().width - dist
      && p.x < scroll.getViewport().getSize().width)
      dx = 1;
    if (p.y < dist && p.y >= 0)
      dy = -1;
    if (p.y >= scroll.getViewport().getSize().height - dist
      && p.y < scroll.getViewport().getSize().height)
      dy = 1;

    if (dx != 0 || dy != 0) {
      Rectangle r = new Rectangle(scroll.getViewport().getViewRect());
      r.translate(2 * dist * dx, 2 * dist * dy);
      r = r.intersection(new Rectangle(new Point(0, 0), getPreferredSize()));
      scrollRectToVisible(r);
    }
  }

  @Override
  public void dropActionChanged(DropTargetDragEvent arg0) {
  }

  @Override
  public void drop(DropTargetDropEvent event) {
    removeDragCursor();
    final Point pos = event.getLocation();
    pos.translate(currentPieceOffsetX, currentPieceOffsetY);
    myConfigurer.setPos(pos);
    myConfigurer.updateCoords();
    myConfigurer.updateDisplay();
    repaint();
  }

  @Override
  public void dragExit(DropTargetEvent arg0) {
  }

  @Override
  public void dragEnter(DragSourceDragEvent arg0) {
  }

  @Override
  public void dragOver(DragSourceDragEvent arg0) {
  }

  @Override
  public void dropActionChanged(DragSourceDragEvent arg0) {
  }

  @Override
  public void dragDropEnd(DragSourceDropEvent arg0) {
    removeDragCursor();
  }

  @Override
  public void dragExit(DragSourceEvent arg0) {
  }

  @Override
  public void dragGestureRecognized(DragGestureEvent dge) {
    if (!SwingUtils.isDragTrigger(dge)) {
      return;
    }

    final Point mousePosition = dge.getDragOrigin();
    final Point piecePosition = new Point(myConfigurer.pos);

    // Check drag starts inside piece
    final Rectangle r = myConfigurer.getPieceBoundingBox();
    r.translate(piecePosition.x, piecePosition.y);
    if (!r.contains(mousePosition)) {
      return;
    }

    originalPieceOffsetX = piecePosition.x - mousePosition.x;
    originalPieceOffsetY = piecePosition.y - mousePosition.y;

    drawWin = null;

    makeDragCursor();
    setDragCursor();

    SwingUtilities.convertPointToScreen(mousePosition, drawWin);
    moveDragCursor(mousePosition.x, mousePosition.y);

    // begin dragging
    try {
      dge.startDrag(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR),
        new StringSelection(""), this); // DEBUG
      dge.getDragSource().addDragSourceMotionListener(this);
    }
    catch (final InvalidDnDOperationException e) {
      ErrorDialog.bug(e);
    }
  }

  protected void setDragCursor() {
    final JRootPane rootWin = SwingUtilities.getRootPane(this);
    if (rootWin != null) {
      // remove cursor from old window
      if (dragCursor.getParent() != null) {
        dragCursor.getParent().remove(dragCursor);
      }
      drawWin = rootWin.getLayeredPane();

      calcDrawOffset();
      dragCursor.setVisible(true);
      drawWin.add(dragCursor, JLayeredPane.DRAG_LAYER);
    }
  }

  /** Moves the drag cursor on the current draw window */
  protected void moveDragCursor(int dragX, int dragY) {
    if (drawWin != null) {
      dragCursor.setLocation(dragX - drawOffset.x, dragY - drawOffset.y);
    }
  }

  protected void removeDragCursor() {
    if (drawWin != null) {
      if (dragCursor != null) {
        dragCursor.setVisible(false);
        drawWin.remove(dragCursor);
      }
      drawWin = null;
    }
  }

  /** calculates the offset between cursor dragCursor positions */
  protected void calcDrawOffset() {
    if (drawWin != null) {
      // drawOffset is the offset between the mouse location during a drag
      // and the upper-left corner of the cursor
      // accounts for difference between event point (screen coords)
      // and Layered Pane position, boundingBox and off-center drag
      drawOffset.x = -boundingBox.x - currentPieceOffsetX + EXTRA_BORDER;
      drawOffset.y = -boundingBox.y - currentPieceOffsetY + EXTRA_BORDER;
      SwingUtilities.convertPointToScreen(drawOffset, drawWin);
    }
  }

  protected void makeDragCursor() {
    //double zoom = 1.0;
    // create the cursor if necessary
    if (dragCursor == null) {
      dragCursor = new JLabel();
      dragCursor.setVisible(false);
    }

    //dragCursorZoom = zoom;
    currentPieceOffsetX = originalPieceOffsetX;
    currentPieceOffsetY = originalPieceOffsetY;

    // Record sizing info and resize our cursor
    boundingBox =  myConfigurer.getPieceBoundingBox();
    calcDrawOffset();

    final int w = boundingBox.width + EXTRA_BORDER * 2;
    final int h = boundingBox.height + EXTRA_BORDER * 2;

    final BufferedImage image =
      ImageUtils.createCompatibleTranslucentImage(w, h);

    final Graphics2D g = image.createGraphics();
    g.addRenderingHints(SwingUtils.FONT_HINTS);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON);

    myConfigurer.drawImage(
      g,
      EXTRA_BORDER - boundingBox.x,
      EXTRA_BORDER - boundingBox.y, dragCursor, 1.0
    );

    // make the drag image transparent
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN));
    g.setColor(new Color(0xFF, 0xFF, 0xFF, CURSOR_ALPHA));
    g.fillRect(0, 0, image.getWidth(), image.getHeight());

    g.dispose();

    dragCursor.setSize(w, h);

    // store the bitmap in the cursor
    dragCursor.setIcon(new ImageIcon(image));
  }

  @Override
  public void dragMouseMoved(DragSourceDragEvent event) {
    if (!event.getLocation().equals(lastDragLocation)) {
      lastDragLocation = event.getLocation();
      moveDragCursor(event.getX(), event.getY());
      if (dragCursor != null && !dragCursor.isVisible()) {
        dragCursor.setVisible(true);
      }
    }
  }
}


