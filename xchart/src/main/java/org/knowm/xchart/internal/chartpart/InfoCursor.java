package org.knowm.xchart.internal.chartpart;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.LinkedHashMap;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieSeries;
import org.knowm.xchart.style.PieStyler;

/** InfoCursor movement to display other slice information. */
public class InfoCursor extends AbstractCursor {
  private final PieChart chart;
  private final LinkedHashMap<String, PieSeries> otherSeriesMap;
  private final PieStyler styler;

  private Rectangle2D iconBounds;
  private boolean isOverInfoIcon;

  /**
   * Constructor
   *
   * @param chart
   */
  public InfoCursor(PieChart chart) {

    this.chart = chart;
    this.otherSeriesMap = chart.getOtherSeriesMap();
    this.styler = chart.getStyler();
    PlotContent_Pie plotContent_pie = (PlotContent_Pie) (chart.plot.plotContent);
    plotContent_pie.setCursor(this);
  }

  public void setIconBounds(Rectangle2D iconBounds) {
    this.iconBounds = iconBounds;
  }

  @Override
  protected void handleMouseMoved(final MouseEvent e) {
    isOverInfoIcon = isMouseOverInfoIcon();
  }

  @Override
  protected int getMapSize() {
    return otherSeriesMap.size();
  }

  private boolean isMouseOverInfoIcon() {

    return iconBounds != null && iconBounds.contains(mouseX, mouseY);
  }

  @Override
  public void paint(Graphics2D g) {

    //    if (!styler.isCursorEnabled()) {
    //      return;
    //    }

    if (isOverInfoIcon) {
      TextLayout xValueTextLayout =
          new TextLayout("Other", styler.getCursorFont(), new FontRenderContext(null, true, false));
      textHeight = xValueTextLayout.getBounds().getHeight();

      paintBackground(g);

      paintDataPointInfo(g);
    }
  }

  private void paintBackground(Graphics2D g) {

    int size = Math.min(otherSeriesMap.size(), MAX_LINES_AMOUNT);
    boolean hasScrollPane = otherSeriesMap.size() > MAX_LINES_AMOUNT;
    double maxLineWidth = 0.0;
    TextLayout dataPointTextLayout;
    Rectangle2D dataPointRectangle;
    for (int i = firstSeriesIndex; i < size + firstSeriesIndex; i++) {
      String seriesName = (String) otherSeriesMap.keySet().toArray()[i];
      PieSeries series = otherSeriesMap.get(seriesName);
      String value =
          new Formatter_Custom(styler.getCustomCursorDataFormattingFunction())
              .format(series.getValue());
      dataPointTextLayout =
          new TextLayout(
              "+" + value + ": " + seriesName,
              styler.getCursorFont(),
              new FontRenderContext(null, true, false));
      dataPointRectangle = dataPointTextLayout.getBounds();
      if (maxLineWidth < dataPointRectangle.getWidth()) {
        maxLineWidth = dataPointRectangle.getWidth();
      }
    }

    double backgroundWidth =
        styler.getCursorFont().getSize()
            + maxLineWidth
            + 3 * LINE_SPACING
            + (hasScrollPane ? SCROLL_PANE_WIDTH : 0);
    double backgroundHeight = textHeight * size + (1 + size) * LINE_SPACING;

    super.paintBackground(
        g,
        backgroundWidth,
        backgroundHeight,
        styler.getCursorBackgroundColor(),
        hasScrollPane,
        false,
        otherSeriesMap.size(),
        chart.plot.plotContent.getBounds());
  }

  private void paintDataPointInfo(Graphics2D g) {

    int size = Math.min(otherSeriesMap.size(), MAX_LINES_AMOUNT);
    AffineTransform orig = g.getTransform();
    AffineTransform at = new AffineTransform();
    at.translate(startX + MOUSE_SPACING, startY + MOUSE_SPACING);
    g.transform(at);

    TextLayout dataPointTextLayout;
    for (int i = firstSeriesIndex; i < size + firstSeriesIndex; i++) {
      String seriesName = (String) otherSeriesMap.keySet().toArray()[i];

      at = new AffineTransform();
      at.translate(0, textHeight + LINE_SPACING);
      g.transform(at);
      PieSeries series = otherSeriesMap.get(seriesName);
      if (series == null) {
        continue;
      }

      at = new AffineTransform();
      at.translate(LINE_SPACING, 0);
      g.transform(at);
      g.setColor(styler.getCursorFontColor());
      String value =
          new Formatter_Custom(styler.getCustomCursorDataFormattingFunction())
              .format(series.getValue());
      dataPointTextLayout =
          new TextLayout(
              "+" + value + ": " + seriesName,
              styler.getCursorFont(),
              new FontRenderContext(null, true, false));
      g.fill(dataPointTextLayout.getOutline(null));

      at = new AffineTransform();
      at.translate(-LINE_SPACING, 0);
      g.transform(at);
    }
    g.setTransform(orig);
  }
}
