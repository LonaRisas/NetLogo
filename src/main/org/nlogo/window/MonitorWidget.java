// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.agent.Observer;
import org.nlogo.api.AgentKind;
import org.nlogo.api.AgentKindJ;
import org.nlogo.api.Dump;
import org.nlogo.api.Editable;
import org.nlogo.api.I18N;
import org.nlogo.api.ModelReader;
import org.nlogo.api.Property;

import java.awt.event.MouseEvent;
import java.util.List;

public strictfp class MonitorWidget
    extends JobWidget
    implements Editable,
    Events.RuntimeErrorEventHandler,
    Events.PeriodicUpdateEventHandler,
    Events.JobRemovedEventHandler,
    java.awt.event.MouseListener {

  private boolean jobRunning = false;
  private boolean hasError = false;

  public MonitorWidget(org.nlogo.util.MersenneTwisterFast random) {
    super(random);
    setOpaque(true);
    addMouseListener(this);
    setBackground(InterfaceColors.MONITOR_BACKGROUND);
    setBorder(widgetBorder());
    org.nlogo.awt.Fonts.adjustDefaultFont(this);
    fontSize = getFont().getSize();
  }

  private String name = "";

  public void name(String name) {
    this.name = name;
    chooseDisplayName();
  }

  public String name() {
    return name;
  }

  // initialized in constructor
  private int fontSize = 11;

  public void fontSize(int size) {
    this.fontSize = size;
    // If we are zoomed, we need to zoom the input font size and then
    // set that as our widget font
    if (originalFont() != null) {
      int zoomDiff = getFont().getSize() - originalFont().getSize();
      setFont(getFont().deriveFont(Float.valueOf(size + zoomDiff).floatValue()));
    } else {
      setFont(getFont().deriveFont(Float.valueOf(size).floatValue()));
    }

    if (originalFont() != null) {
      originalFont_$eq(originalFont().deriveFont(Float.valueOf(size).floatValue()));
    }
    // These should reset the cached values in the Zoomer, but
    // after one finishes a property widget edit, it appears to
    // still cache the min height. -- CLB
    resetZoomInfo();
    resetSizeInfo();
  }

  @Override
  public void setFont(java.awt.Font f) {
    if (isZoomed() || getFont() == null) {
      super.setFont(f);
    } else {
      super.setFont(getFont().deriveFont(Float.valueOf(fontSize).floatValue()));
    }
  }

  public int fontSize() {
    return fontSize;
  }

  @Override
  public String classDisplayName() {
    return I18N.guiJ().get("tabs.run.widgets.monitor");
  }

  @Override
  public AgentKind kind() {
    return AgentKindJ.Observer();
  }

  @Override
  public boolean ownsPrimaryJobs() {
    return false; // we own secondary jobs, not primary
  }

  @Override
  public void procedure(org.nlogo.nvm.Procedure procedure) {
    super.procedure(procedure);
    setForeground(procedure == null ? java.awt.Color.RED : null);
    halt();
    if (procedure != null) {
      hasError = false;
      new Events.AddJobEvent(this, agents(), procedure())
          .raise(this);
      jobRunning = true;
    }
    repaint();
  }

  private Object value = null;

  public Object value() {
    return value;
  }

  private String valueString = "";

  public String valueString() {
    return valueString;
  }

  public void value(Object value) {
    this.value = value;
    String newString;
    if (value instanceof Double) {
      newString = Dump.number(
        org.nlogo.api.Approximate.approximate(
          ((Double) value).doubleValue(), decimalPlaces));
    }
    else {
      newString = Dump.logoObject(value);
    }
    if (!newString.equals(valueString)) {
      valueString = newString;
      repaint();
    }
  }

  public List<Property> propertySet() {
    return Properties.monitor();
  }

  private void chooseDisplayName() {
    if (name() == null || name().equals("")) {
      displayName(getSourceName());
    } else {
      displayName(name());
    }
  }

  private String getSourceName() {
    // behold the mighty regular expression
    return innerSource.trim().replaceAll("\\s+", " ");
  }

  @Override
  public void removeNotify() {
    // This is a little kludgy.  Normally removeNotify would run on the
    // event thread, but in an applet context, when the applet
    // shuts down, removeNotify can run on some other thread. But
    // actually this stuff doesn't need to happen in the applet,
    // so we can just skip it in that context. - ST 10/12/03, 10/16/03
    if (java.awt.EventQueue.isDispatchThread()) {
      halt();
    }
    super.removeNotify();
  }

  @Override
  public void suppressRecompiles(boolean suppressRecompiles) {
    if (innerSource().trim().equals("")) {
      recompilePending = false;
    }
    super.suppressRecompiles(suppressRecompiles);
  }

  private static final int MIN_WIDTH = 50;

  @Override
  public java.awt.Dimension getMinimumSize() {
    int h = (fontSize * 4) + 1;
    return new java.awt.Dimension(MIN_WIDTH, h);
  }

  @Override
  public java.awt.Dimension getMaximumSize() {
    int h = (fontSize * 4) + 1;
    return new java.awt.Dimension(10000, h);
  }

  @Override
  public java.awt.Dimension getPreferredSize(java.awt.Font font) {
    java.awt.Dimension size = getMinimumSize();
    int pad = 12;
    java.awt.FontMetrics fontMetrics = getFontMetrics(font);
    size.width = StrictMath.max(size.width, fontMetrics.stringWidth(displayName()) + pad);
    return size;
  }

  @Override
  public void paintComponent(java.awt.Graphics g) {
    super.paintComponent(g); // paint background
    MonitorPainter.paint(g, getSize(), getForeground(), displayName(), valueString);
  }

  private int decimalPlaces = 17;

  public int decimalPlaces() {
    return decimalPlaces;
  }

  public void decimalPlaces(int decimalPlaces) {
    if (decimalPlaces != this.decimalPlaces) {
      this.decimalPlaces = decimalPlaces;
      wrapSource(innerSource());
    }
  }

  @Override
  public void innerSource(String innerSource) {
    super.innerSource(innerSource);
    chooseDisplayName();
  }

  // fragile -- depends on the details of what code wrapSource wraps
  // the user's reporter and what the resulting compiled code looks
  // like. model runs code calls this to grab the Reporter to run
  // at tick time - ST 10/11/12
  public scala.Option<org.nlogo.nvm.Reporter> reporter() {
    org.nlogo.nvm.Procedure p = procedure();
    return scala.Option.apply(
      p == null ? null : p.code()[0].args[0]);
  }

  public void wrapSource(String innerSource) {
    if (innerSource.trim().equals("")) {
      source(null, "", null);
      halt();
    } else {
      source("to __monitor [] __observercode loop [ __updatemonitor (",
             innerSource, "\n)] end");
    }
    chooseDisplayName();
  }

  public String wrapSource() {
    return innerSource();
  }

  public void handle(Events.RuntimeErrorEvent e) {
    if (this == e.jobOwner()) {
      hasError = true;
      halt();
    }
  }

  public void handle(Events.PeriodicUpdateEvent e) {
    if (!jobRunning && procedure() != null) {
      hasError = false;
      jobRunning = true;
      new Events.AddJobEvent(this, agents(), procedure())
          .raise(this);
    }
  }

  public void handle(Events.JobRemovedEvent e) {
    if (e.owner() == this) {
      jobRunning = false;
      value(hasError ? "N/A" : "");
    }
  }

  private void halt() {
    new Events.RemoveJobEvent(this).raise(this);
  }

  @Override
  public String save() {
    StringBuilder s = new StringBuilder();
    s.append("MONITOR\n");
    s.append(getBoundsString());
    if ((null != name()) && (!name().trim().equals(""))) {
      s.append(name() + "\n");
    } else {
      s.append("NIL\n");
    }
    if (!innerSource.trim().equals("")) {
      s.append(ModelReader.stripLines(innerSource()) + "\n");
    } else {
      s.append("NIL\n");
    }

    s.append(decimalPlaces + "\n");
    s.append("1\n");  // for compatability

    // what is the terminating 1 for??? -- CLB
    s.append(fontSize + "\n");
    //s.append( "1\n" ) ;  // for compatability

    return s.toString();
  }

  @Override
  public Object load(scala.collection.Seq<String> strings, Widget.LoadHelper helper) {
    String displayName = strings.apply(5);
    String source = ModelReader.restoreLines(strings.apply(6));

    if (displayName.equals("NIL")) {
      name("");
    } else {
      name(displayName);
    }
    if (strings.size() > 7) {
      decimalPlaces = Integer.parseInt(strings.apply(7));
    }
    if (strings.size() > 9) {
      fontSize(Integer.parseInt(strings.apply(9)));
    }
    if (!source.equals("NIL")) {
      wrapSource(helper.convert(source, true));
    }
    int x1 = Integer.parseInt(strings.apply(1));
    int y1 = Integer.parseInt(strings.apply(2));
    int x2 = Integer.parseInt(strings.apply(3));
    int y2 = Integer.parseInt(strings.apply(4));
    setSize(x2 - x1, y2 - y1);
    chooseDisplayName();
    return this;
  }

  // This is the same as the button so right-click on a monitor w/ error
  // brings up the popup menu not the edit dialog. ev 1/4/06

  private boolean lastMousePressedWasPopupTrigger = false;

  public void mouseClicked(MouseEvent e) {
    if (!e.isPopupTrigger() && error() != null && !lastMousePressedWasPopupTrigger) {
      new Events.EditWidgetEvent(this).raise(this);
      return;
    }
  }

  public void mousePressed(MouseEvent e) {
    lastMousePressedWasPopupTrigger = e.isPopupTrigger();
  }

  public void mouseReleased(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

}
