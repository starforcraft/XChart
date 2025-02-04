package org.knowm.xchart.style;

import java.awt.Color;
import java.awt.Font;
import java.util.Comparator;
import java.util.function.Function;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.colors.ChartColor;
import org.knowm.xchart.style.theme.Theme;

public class XYStyler extends AxesChartStyler {

  private XYSeriesRenderStyle xySeriesRenderStyle;

  // Zoom ///////////////////////////
  private boolean isZoomEnabled;
  private Color zoomSelectionColor;
  private boolean zoomResetByDoubleClick;
  private boolean zoomResetByButton;

  // Cursor ////////////////////////////////

  private boolean isCursorEnabled;
  private Color cursorColor;
  private float cursorLineWidth;
  private Font cursorFont;
  private Color cursorFontColor;
  private Color cursorBackgroundColor;
  private Function<Double, String> customCursorXDataFormattingFunction;
  private Function<Double, String> customCursorYDataFormattingFunction;
  private Comparator<String> cursorOrder;
  private String cursorZeroString;

  /** Constructor */
  public XYStyler() {

    setAllStyles();
  }

  @Override
  protected void setAllStyles() {

    super.setAllStyles();

    // Zoom ///////////////////////////
    // TODO set this from the theme
    xySeriesRenderStyle = XYSeriesRenderStyle.Line; // set default to line
    isZoomEnabled = false; // set default to false
    zoomSelectionColor = ChartColor.LIGHT_GREY.getColorTranslucent();
    zoomResetByDoubleClick = true;
    zoomResetByButton = true;

    // Cursor ////////////////////////////////
    this.isCursorEnabled = theme.isCursorEnabled();
    this.cursorColor = theme.getCursorColor();
    this.cursorLineWidth = theme.getCursorSize();
    this.cursorFont = theme.getCursorFont();
    this.cursorFontColor = theme.getCursorFontColor();
    this.cursorBackgroundColor = theme.getCursorBackgroundColor();
  }

  /**
   * Set the theme the styler should use
   *
   * @param theme
   */
  public void setTheme(Theme theme) {

    this.theme = theme;
    setAllStyles();
  }

  public XYSeriesRenderStyle getDefaultSeriesRenderStyle() {

    return xySeriesRenderStyle;
  }

  /**
   * Sets the default series render style for the chart (line, scatter, area, etc.) You can override
   * the series render style individually on each Series object.
   *
   * @param xySeriesRenderStyle
   */
  public XYStyler setDefaultSeriesRenderStyle(XYSeriesRenderStyle xySeriesRenderStyle) {

    this.xySeriesRenderStyle = xySeriesRenderStyle;
    return this;
  }

  // Zoom ///////////////////////////////

  public boolean isZoomEnabled() {
    return isZoomEnabled;
  }

  public XYStyler setZoomEnabled(boolean isZoomEnabled) {

    this.isZoomEnabled = isZoomEnabled;
    return this;
  }

  public Color getZoomSelectionColor() {

    return zoomSelectionColor;
  }

  public XYStyler setZoomSelectionColor(Color zoomSelectionColor) {

    this.zoomSelectionColor = zoomSelectionColor;
    return this;
  }

  public boolean isZoomResetByDoubleClick() {

    return zoomResetByDoubleClick;
  }

  public XYStyler setZoomResetByDoubleClick(boolean zoomResetByDoubleClick) {

    this.zoomResetByDoubleClick = zoomResetByDoubleClick;
    return this;
  }

  public boolean isZoomResetByButton() {

    return zoomResetByButton;
  }

  public XYStyler setZoomResetByButton(boolean zoomResetByButton) {

    this.zoomResetByButton = zoomResetByButton;
    return this;
  }

  // Cursor ///////////////////////////////

  public boolean isCursorEnabled() {
    return isCursorEnabled;
  }

  public XYStyler setCursorEnabled(boolean isCursorEnabled) {

    this.isCursorEnabled = isCursorEnabled;
    return this;
  }

  public Color getCursorColor() {
    return cursorColor;
  }

  public XYStyler setCursorColor(Color cursorColor) {

    this.cursorColor = cursorColor;
    return this;
  }

  public float getCursorLineWidth() {

    return cursorLineWidth;
  }

  public XYStyler setCursorLineWidth(float cursorLineWidth) {

    this.cursorLineWidth = cursorLineWidth;
    return this;
  }

  public Font getCursorFont() {

    return cursorFont;
  }

  public XYStyler setCursorFont(Font cursorFont) {

    this.cursorFont = cursorFont;
    return this;
  }

  public Color getCursorFontColor() {

    return cursorFontColor;
  }

  public XYStyler setCursorFontColor(Color cursorFontColor) {

    this.cursorFontColor = cursorFontColor;
    return this;
  }

  public Color getCursorBackgroundColor() {

    return cursorBackgroundColor;
  }

  public XYStyler setCursorBackgroundColor(Color cursorBackgroundColor) {

    this.cursorBackgroundColor = cursorBackgroundColor;
    return this;
  }

  public Function<Double, String> getCustomCursorXDataFormattingFunction() {
    return customCursorXDataFormattingFunction;
  }

  /**
   * Set the custom function for formatting the cursor tooltip based on the series X-Axis data
   *
   * @param customCursorXDataFormattingFunction
   */
  public XYStyler setCustomCursorXDataFormattingFunction(
      Function<Double, String> customCursorXDataFormattingFunction) {
    this.customCursorXDataFormattingFunction = customCursorXDataFormattingFunction;
    return this;
  }

  public Function<Double, String> getCustomCursorYDataFormattingFunction() {
    return customCursorYDataFormattingFunction;
  }

  /**
   * Set the custom function for formatting the cursor tooltip based on the series Y-Axis data
   *
   * @param customCursorYDataFormattingFunction
   */
  public XYStyler setCustomCursorYDataFormattingFunction(
      Function<Double, String> customCursorYDataFormattingFunction) {
    this.customCursorYDataFormattingFunction = customCursorYDataFormattingFunction;
    return this;
  }

  public XYStyler setCursorOrder(Comparator<String> cursorOrder) {
    this.cursorOrder = cursorOrder;
    return this;
  }

  public Comparator<String> getCursorOrder() {
    return cursorOrder;
  }

  public XYStyler setCursorZeroString(String cursorZeroString) {
    this.cursorZeroString = cursorZeroString;
    return this;
  }

  public String getCursorZeroString() {
    return cursorZeroString;
  }
}
