package org.knowm.xchart.internal.chartpart;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import org.knowm.xchart.internal.series.MarkerSeries;
import org.knowm.xchart.internal.series.Series;
import org.knowm.xchart.style.XYStyler;

/** Cursor movement to display matching point data information. */
public class Cursor extends MouseAdapter implements ChartPart {

  private static final int LINE_SPACING = 5;
  private static final int MOUSE_SPACING = 15;
  private static final int MAX_LINES_AMOUNT = 12;
  private static final int SCROLL_PANE_WIDTH = 8;
  private static final int SCROLL_PANE_PADDING = 2;

  private final List<DataPoint> dataPointList = new ArrayList<>();
  private final List<DataPoint> matchingDataPointList = new ArrayList<>();

  private final Chart chart;
  private final XYStyler styler;

  private final Map<String, Series> seriesMap;

  private int firstSeriesIndex = 0;
  private double mouseX;
  private double mouseY;
  private double startX;
  private double startY;
  private double textHeight;

  /**
   * Constructor
   *
   * @param chart
   */
  public Cursor(Chart chart) {

    this.chart = chart;
    this.styler = (XYStyler) chart.getStyler();
    PlotContent_XY plotContent_xy = (PlotContent_XY) (chart.plot.plotContent);
    plotContent_xy.setCursor(this);

    // clear lists
    dataPointList.clear();

    this.seriesMap = chart.getSeriesMap();
  }

  private String currentHoverXValue;

  @Override
  public void mouseMoved(MouseEvent e) {

    //    // don't draw anything
    //    if (!styler.isCursorEnabled() || seriesMap == null) {
    //      return;
    //    }

    mouseX = e.getX();
    mouseY = e.getY();
    if (isMouseOutOfPlotContent()) {

      if (!matchingDataPointList.isEmpty()) {
        matchingDataPointList.clear();
        e.getComponent().repaint();
      }
      return;
    }
    calculateMatchingDataPoints();

    // Check if hover x value has changed
    if (!matchingDataPointList.isEmpty()) {
      DataPoint dataPoint = matchingDataPointList.get(0);
      if (!dataPoint.getXValue().equals(currentHoverXValue)) {
        currentHoverXValue = dataPoint.getXValue();
        firstSeriesIndex = 0;
      }
    }

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
      if (firstSeriesIndex + MAX_LINES_AMOUNT < matchingDataPointList.size()) {
        firstSeriesIndex++;
      }
    }
    e.getComponent().repaint();
  }

  private boolean isMouseOutOfPlotContent() {

    return !chart.plot.plotContent.getBounds().contains(mouseX, mouseY);
  }

  @Override
  public Rectangle2D getBounds() {
    return null;
  }

  @Override
  public void paint(Graphics2D g) {

    //    if (!styler.isCursorEnabled()) {
    //      return;
    //    }

    if (!matchingDataPointList.isEmpty()) {
      DataPoint firstDataPoint = matchingDataPointList.get(0);

      TextLayout xValueTextLayout =
          new TextLayout(
              firstDataPoint.xValue,
              styler.getCursorFont(),
              new FontRenderContext(null, true, false));
      textHeight = xValueTextLayout.getBounds().getHeight();

      paintVerticalLine(g, firstDataPoint);

      paintBackground(g, xValueTextLayout);

      paintDataPointInfo(g, xValueTextLayout);
    }
  }

  private void paintVerticalLine(Graphics2D g, DataPoint dataPoint) {

    BasicStroke stroke =
        new BasicStroke(styler.getCursorLineWidth(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    g.setStroke(stroke);
    g.setColor(styler.getCursorColor());
    Line2D.Double line = new Line2D.Double();
    line.setLine(
        dataPoint.x,
        chart.plot.plotContent.getBounds().getY(),
        dataPoint.x,
        chart.plot.plotContent.getBounds().getY() + chart.plot.plotContent.getBounds().getHeight());
    g.draw(line);
  }

  private void paintBackground(Graphics2D g, TextLayout xValueTextLayout) {

    int size = Math.min(matchingDataPointList.size(), MAX_LINES_AMOUNT);
    boolean hasScrollPane = matchingDataPointList.size() > MAX_LINES_AMOUNT;
    boolean isZero =
        matchingDataPointList.size() == 1
            && matchingDataPointList.get(0).yValue.equals(styler.getCursorZeroString());
    double maxLineWidth = xValueTextLayout.getBounds().getWidth();
    TextLayout dataPointTextLayout;
    Rectangle2D dataPointRectangle;
    if (!isZero) {
      for (int i = firstSeriesIndex; i < size + firstSeriesIndex; i++) {
        DataPoint dataPoint = matchingDataPointList.get(i);
        dataPointTextLayout =
            new TextLayout(
                dataPoint.seriesName + ": " + dataPoint.yValue,
                styler.getCursorFont(),
                new FontRenderContext(null, true, false));
        dataPointRectangle = dataPointTextLayout.getBounds();
        if (maxLineWidth < dataPointRectangle.getWidth()) {
          maxLineWidth = dataPointRectangle.getWidth();
        }
      }
    }

    double backgroundWidth =
        styler.getCursorFont().getSize()
            + maxLineWidth
            + (!isZero ? 3 * LINE_SPACING : 0)
            + (hasScrollPane ? SCROLL_PANE_WIDTH : 0);
    double backgroundHeight =
        textHeight * (1 + (!isZero ? size : 0)) + (2 + (!isZero ? size : 0)) * LINE_SPACING;

    startX = mouseX;
    startY = mouseY;
    if (mouseX + MOUSE_SPACING + backgroundWidth
        > chart.plot.plotContent.getBounds().getX()
            + chart.plot.plotContent.getBounds().getWidth()) {
      startX = mouseX - backgroundWidth - MOUSE_SPACING;
    }

    if (mouseY + MOUSE_SPACING + backgroundHeight
        > chart.plot.plotContent.getBounds().getY()
            + chart.plot.plotContent.getBounds().getHeight()) {
      startY = mouseY - backgroundHeight - MOUSE_SPACING;
    }

    g.setColor(styler.getCursorBackgroundColor());
    g.fillRect(
        (int) startX + MOUSE_SPACING,
        (int) startY + MOUSE_SPACING,
        (int) (backgroundWidth),
        (int) (backgroundHeight));

    if (hasScrollPane) {
      double additionalStartY = textHeight + LINE_SPACING;
      double scrollPaneStartY = firstSeriesIndex * textHeight + additionalStartY;
      double scrollPaneHeightAdjustment =
          (matchingDataPointList.size() - (firstSeriesIndex + MAX_LINES_AMOUNT)) * textHeight;

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

  private void paintDataPointInfo(Graphics2D g, TextLayout xValueTextLayout) {

    int size = Math.min(matchingDataPointList.size(), MAX_LINES_AMOUNT);
    AffineTransform orig = g.getTransform();
    AffineTransform at = new AffineTransform();
    at.translate(
        startX + MOUSE_SPACING + LINE_SPACING, startY + textHeight + MOUSE_SPACING + LINE_SPACING);
    g.transform(at);
    g.setColor(styler.getCursorFontColor());
    g.fill(xValueTextLayout.getOutline(null));

    MarkerSeries series;
    TextLayout dataPointTextLayout;
    Shape circle;
    for (int i = firstSeriesIndex; i < size + firstSeriesIndex; i++) {
      DataPoint dataPoint = matchingDataPointList.get(i);

      at = new AffineTransform();
      at.translate(0, textHeight + LINE_SPACING);
      g.transform(at);
      series = (MarkerSeries) seriesMap.get(dataPoint.seriesName);
      if (series == null) {
        continue;
      }
      g.setColor(series.getMarkerColor());
      circle = new Ellipse2D.Double(0, -textHeight, textHeight, textHeight);
      g.fill(circle);

      at = new AffineTransform();
      at.translate(textHeight + LINE_SPACING, 0);
      g.transform(at);
      g.setColor(styler.getCursorFontColor());
      dataPointTextLayout =
          new TextLayout(
              dataPoint.seriesName + ": " + dataPoint.yValue,
              styler.getCursorFont(),
              new FontRenderContext(null, true, false));
      g.fill(dataPointTextLayout.getOutline(null));

      at = new AffineTransform();
      at.translate(-textHeight - LINE_SPACING, 0);
      g.transform(at);
    }
    g.setTransform(orig);
  }

  void addData(double xOffset, double yOffset, String xValue, String yValue, String seriesName) {

    DataPoint dataPoint = new DataPoint(xOffset, yOffset, xValue, yValue, seriesName);
    dataPointList.add(dataPoint);
  }

  void clearDataPoints() {
    dataPointList.clear();
  }

  /** One DataPoint per series, keep the DataPoint closest to mouseX */
  private void calculateMatchingDataPoints() {

    List<DataPoint> dataPoints = new ArrayList<>();
    for (DataPoint dataPoint : dataPointList) {
      if (dataPoint.shape.contains(mouseX, dataPoint.shape.getBounds().getCenterY())
          && chart.plot.plotContent.getBounds().getY() < mouseY
          && chart.plot.plotContent.getBounds().getY()
                  + chart.plot.plotContent.getBounds().getHeight()
              > mouseY) {
        dataPoints.add(dataPoint);
      }
    }

    if (!dataPoints.isEmpty()) {
      String cursorZeroString = styler.getCursorZeroString();
      boolean fullNotZero = cursorZeroString != null;
      if (fullNotZero) {
        for (DataPoint dataPoint : dataPoints) {
          if (!dataPoint.yValue.equals(cursorZeroString)) {
            fullNotZero = false;
            break;
          }
        }
      }

      if (!fullNotZero) {
        LinkedHashMap<String, DataPoint> map = new LinkedHashMap<>();
        String seriesName;
        for (DataPoint dataPoint : dataPoints) {
          seriesName = dataPoint.seriesName;
          if (dataPoint.yValue.equals(cursorZeroString)) {
            continue;
          }
          if (map.containsKey(seriesName)) {
            if (Math.abs(dataPoint.x - mouseX) < Math.abs(map.get(seriesName).x - mouseX)) {
              map.put(seriesName, dataPoint);
            }
          } else {
            map.put(seriesName, dataPoint);
          }
        }

        // Order map
        if (styler.getCursorOrder() != null) {
          map =
              map.entrySet().stream()
                  .sorted(
                      Map.Entry.comparingByValue(
                          Comparator.comparing(DataPoint::getYValue, styler.getCursorOrder())
                              .reversed()
                              .thenComparing(DataPoint::getSeriesName, Comparator.naturalOrder())))
                  .collect(
                      Collectors.toMap(
                          Map.Entry::getKey,
                          Map.Entry::getValue,
                          (e1, e2) -> e1,
                          LinkedHashMap::new));
        }

        matchingDataPointList.clear();
        matchingDataPointList.addAll(map.values());
      } else {
        matchingDataPointList.clear();
        matchingDataPointList.add(
            new DataPoint(
                dataPoints.get(0).x,
                -1,
                dataPoints.get(0).xValue,
                styler.getCursorZeroString(),
                null));
      }
    }
  }

  private static class DataPoint {

    // edge detection
    private static final int MARGIN = 5;

    // Used to determine the point that the mouse has passed vertically
    final Shape shape;
    final double x;
    final double y;
    final String xValue;
    final String yValue;
    final String seriesName;

    public DataPoint(double x, double y, String xValue, String yValue, String seriesName) {

      double halfSize = MARGIN * 1.5;
      double markerSize = MARGIN * 3;

      this.x = x;
      this.y = y;
      this.shape =
          new Ellipse2D.Double(this.x - halfSize, this.y - halfSize, markerSize, markerSize);

      this.xValue = xValue;
      this.yValue = yValue;
      this.seriesName = seriesName;
    }

    public String getSeriesName() {
      return seriesName;
    }

    public String getXValue() {
      return xValue;
    }

    public String getYValue() {
      return yValue;
    }
  }
}
