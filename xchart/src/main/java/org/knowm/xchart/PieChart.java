package org.knowm.xchart;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.internal.chartpart.Legend_Pie;
import org.knowm.xchart.internal.chartpart.Plot_Pie;
import org.knowm.xchart.internal.series.Series;
import org.knowm.xchart.internal.style.SeriesColorMarkerLineStyle;
import org.knowm.xchart.internal.style.SeriesColorMarkerLineStyleCycler;
import org.knowm.xchart.style.PieStyler;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.theme.Theme;

public class PieChart extends Chart<PieStyler, PieSeries> {
  private final Map<String, PieSeries> otherSeriesMap = new HashMap<>();

  /**
   * Constructor - the default Chart Theme will be used (XChartTheme)
   *
   * @param width
   * @param height
   */
  public PieChart(int width, int height) {

    super(width, height, new PieStyler());
    plot = new Plot_Pie<PieStyler, PieSeries>(this);
    legend = new Legend_Pie<PieStyler, PieSeries>(this);
  }

  /**
   * Constructor
   *
   * @param width
   * @param height
   * @param theme - pass in a instance of Theme class, probably a custom Theme.
   */
  public PieChart(int width, int height, Theme theme) {

    this(width, height);
    styler.setTheme(theme);
  }

  /**
   * Constructor
   *
   * @param width
   * @param height
   * @param chartTheme - pass in the desired ChartTheme enum
   */
  public PieChart(int width, int height, ChartTheme chartTheme) {

    this(width, height, chartTheme.newInstance(chartTheme));
  }

  /**
   * Constructor
   *
   * @param chartBuilder
   */
  public PieChart(PieChartBuilder chartBuilder) {

    this(chartBuilder.width, chartBuilder.height, chartBuilder.chartTheme);
    setTitle(chartBuilder.title);
  }

  /**
   * Add a series for a Pie type chart
   *
   * @param seriesName
   * @param value
   * @return
   */
  public PieSeries addSeries(String seriesName, Number value) {

    PieSeries series = new PieSeries(seriesName, value);

    if (seriesMap.containsKey(seriesName)) {
      throw new IllegalArgumentException(
          "Series name >"
              + seriesName
              + "< has already been used. Use unique names for each series!!!");
    }
    seriesMap.put(seriesName, series);

    return series;
  }

  public int getSeriesAmount() {
    return seriesMap.size();
  }

  /**
   * Update a series by updating the pie slide value
   *
   * @param seriesName
   * @param value
   * @return
   */
  public PieSeries updatePieSeries(String seriesName, Number value) {

    Map<String, PieSeries> seriesMap = getSeriesMap();
    PieSeries series = seriesMap.get(seriesName);
    if (series == null) {
      throw new IllegalArgumentException("Series name >" + seriesName + "< not found!!!");
    }
    series.replaceData(value);

    return series;
  }

  @Override
  public void paint(Graphics2D g, int width, int height) {

    setWidth(width);
    setHeight(height);

    // set the series types if they are not set. Legend and Plot need it.
    for (PieSeries seriesPie : getSeriesMap().values()) {
      PieSeries.PieSeriesRenderStyle seriesType =
          seriesPie.getChartPieSeriesRenderStyle(); // would be directly set
      if (seriesType == null) { // wasn't overridden, use default from Style Manager
        seriesPie.setChartPieSeriesRenderStyle(getStyler().getDefaultSeriesRenderStyle());
      }
    }
    setSeriesStyles();

    paintBackground(g);

    plot.paint(g);
    chartTitle.paint(g);
    legend.paint(g);
    annotations.forEach(x -> x.paint(g));
  }

  /** set the series color based on theme */
  private void setSeriesStyles() {

    SeriesColorMarkerLineStyleCycler seriesColorMarkerLineStyleCycler =
        new SeriesColorMarkerLineStyleCycler(
            getStyler().getSeriesColors(),
            getStyler().getSeriesMarkers(),
            getStyler().getSeriesLines());
    for (Series series : getSeriesMap().values()) {

      SeriesColorMarkerLineStyle seriesColorMarkerLineStyle =
          seriesColorMarkerLineStyleCycler.getNextSeriesColorMarkerLineStyle();

      if (series.getFillColor() == null) { // wasn't set manually
        series.setFillColor(seriesColorMarkerLineStyle.getColor());
      }
    }
  }

  @Override
  public Map<String, PieSeries> getFilteredSeriesMap() {
    double total =
        seriesMap.values().stream()
            .filter(series -> series.isEnabled() && series.getValue() != null)
            .mapToDouble(series -> series.getValue().doubleValue())
            .sum();

    Map<String, PieSeries> filteredSeriesMap = new HashMap<>(seriesMap);

    if (styler.isCombineSmallSlices()) {
      otherSeriesMap.clear();

      double otherValue = 0.0;

      Iterator<Map.Entry<String, PieSeries>> iterator = filteredSeriesMap.entrySet().iterator();
      while (iterator.hasNext()) {
        Map.Entry<String, PieSeries> entry = iterator.next();
        PieSeries series = entry.getValue();

        if (!series.isEnabled() || series.getValue() == null) {
          continue;
        }

        if (series.getValue().doubleValue() / total <= 0.05) {
          otherValue += series.getValue().doubleValue();
          otherSeriesMap.put(series.getName(), series);
          iterator.remove();
        }
      }

      if (otherValue > 0.0) {
        PieSeries series = new PieSeries("Other", otherValue);
        series.setFillColor(Color.GRAY);
        series.setChartPieSeriesRenderStyle(styler.getDefaultSeriesRenderStyle());
        // TODO What if the map already contains a key called 'Other'?
        filteredSeriesMap.put("Other", series);
      }
    }

    return filteredSeriesMap;
  }

  public Map<String, PieSeries> getOtherSeriesMap() {
    return otherSeriesMap;
  }
}
