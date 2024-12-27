package org.knowm.xchart.internal.chartpart;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;

public abstract class AbstractCursor extends MouseAdapter implements ChartPart {
  protected static final int LINE_SPACING = 5;
  protected static final int MOUSE_SPACING = 15;
  protected static final int MAX_LINES_AMOUNT = 12;
  protected static final int SCROLL_PANE_WIDTH = 8;
  protected static final int SCROLL_PANE_PADDING = 2;

  protected double mouseX;
  protected double mouseY;
  protected double startX;
  protected double startY;
  protected double textHeight;
  protected int firstSeriesIndex = 0;

  protected abstract void handleMouseMoved(MouseEvent e);

  @Override
  public void mouseMoved(MouseEvent e) {
    mouseX = e.getX();
    mouseY = e.getY();

    handleMouseMoved(e);

    e.getComponent().repaint();
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    if (e.getWheelRotation() < 0) {
      // Mouse wheel up
      if (firstSeriesIndex > 0) {
        firstSeriesIndex--;
      }
    } else {
      // Mouse wheel down
      if (firstSeriesIndex + MAX_LINES_AMOUNT < getMapSize()) {
        firstSeriesIndex++;
      }
    }
    e.getComponent().repaint();
  }

  protected abstract int getMapSize();

  protected void paintBackground(
      Graphics2D g,
      double backgroundWidth,
      double backgroundHeight,
      Color backgroundColor,
      boolean hasScrollPane,
      boolean withTitle,
      double size,
      Rectangle2D bounds) {
    startX = mouseX;
    startY = mouseY;
    if (mouseX + MOUSE_SPACING + backgroundWidth > bounds.getX() + bounds.getWidth()) {
      startX = mouseX - backgroundWidth - MOUSE_SPACING;
    }

    if (mouseY + MOUSE_SPACING + backgroundHeight > bounds.getY() + bounds.getHeight()) {
      startY = mouseY - backgroundHeight - MOUSE_SPACING;
    }

    g.setColor(backgroundColor);
    g.fillRect(
        (int) startX + MOUSE_SPACING,
        (int) startY + MOUSE_SPACING,
        (int) (backgroundWidth),
        (int) (backgroundHeight));

    if (hasScrollPane) {
      double oneColumnHeight = backgroundHeight / size;

      double scrollPaneStartY =
          firstSeriesIndex * oneColumnHeight + (withTitle ? (oneColumnHeight + LINE_SPACING) : 0);
      double scrollPaneHeightAdjustment =
          (size - (firstSeriesIndex + MAX_LINES_AMOUNT)) * oneColumnHeight;

      int scrollPaneX =
          (int)
              (startX + MOUSE_SPACING + backgroundWidth - SCROLL_PANE_WIDTH - SCROLL_PANE_PADDING);
      int scrollPaneY = (int) (startY + MOUSE_SPACING + scrollPaneStartY + SCROLL_PANE_PADDING);
      int scrollPaneHeight =
          (int)
              (backgroundHeight
                  - scrollPaneStartY
                  - scrollPaneHeightAdjustment
                  - SCROLL_PANE_PADDING * 2);

      g.setColor(Color.DARK_GRAY);
      g.fillRect(scrollPaneX, scrollPaneY, SCROLL_PANE_WIDTH, scrollPaneHeight);
    }
  }

  @Override
  public Rectangle2D getBounds() {
    return null;
  }
}
