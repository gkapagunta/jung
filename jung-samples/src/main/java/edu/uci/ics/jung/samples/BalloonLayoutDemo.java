/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.samples;

import edu.uci.ics.jung.graph.CTreeNetwork;
import edu.uci.ics.jung.graph.MutableCTreeNetwork;
import edu.uci.ics.jung.graph.TreeNetworkBuilder;
import edu.uci.ics.jung.layout.algorithms.BalloonLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.TreeLayoutAlgorithm;
import edu.uci.ics.jung.layout.util.LayoutAlgorithmTransition;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.MultiLayerTransformer;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.*;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.transform.HyperbolicTransformer;
import edu.uci.ics.jung.visualization.transform.LayoutLensSupport;
import edu.uci.ics.jung.visualization.transform.Lens;
import edu.uci.ics.jung.visualization.transform.LensSupport;
import edu.uci.ics.jung.visualization.transform.LensTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformerDecorator;
import edu.uci.ics.jung.visualization.transform.shape.HyperbolicShapeTransformer;
import edu.uci.ics.jung.visualization.transform.shape.ViewLensSupport;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import javax.swing.*;

/**
 * Demonstrates the visualization of a Tree using TreeLayout and BalloonLayout. An examiner lens
 * performing a hyperbolic transformation of the view is also included.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class BalloonLayoutDemo extends JApplet {

  CTreeNetwork<String, Integer> graph;

  VisualizationServer.Paintable rings;

  TreeLayoutAlgorithm<String, Point2D> treeLayoutAlgorithm;

  BalloonLayoutAlgorithm<String, Point2D> radialLayoutAlgorithm;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Integer> vv;

  /** provides a Hyperbolic lens for the view */
  LensSupport hyperbolicViewSupport;

  LensSupport hyperbolicSupport;

  public BalloonLayoutDemo() {

    // create a simple graph for the demo
    graph = createTree();

    treeLayoutAlgorithm = new TreeLayoutAlgorithm<>();
    radialLayoutAlgorithm = new BalloonLayoutAlgorithm<>();

    vv =
        new VisualizationViewer<>(
            graph, treeLayoutAlgorithm, new Dimension(900, 900), new Dimension(600, 600));
    vv.setBackground(Color.white);
    vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.line());
    vv.getRenderContext().setVertexLabelTransformer(Object::toString);
    // add a listener for ToolTips
    vv.setVertexToolTipTransformer(Object::toString);
    vv.getRenderContext().setArrowFillPaintTransformer(a -> Color.lightGray);
    rings = new Rings(radialLayoutAlgorithm);

    Container content = getContentPane();
    final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
    content.add(panel);

    final DefaultModalGraphMouse<String, Integer> graphMouse = new DefaultModalGraphMouse<>();

    vv.setGraphMouse(graphMouse);
    vv.addKeyListener(graphMouse.getModeKeyListener());

    Lens lens = new Lens(vv);
    hyperbolicViewSupport =
        new ViewLensSupport<>(
            vv,
            new HyperbolicShapeTransformer(
                lens, vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)),
            new ModalLensGraphMouse());
    hyperbolicSupport =
        new LayoutLensSupport<>(
            vv,
            new HyperbolicTransformer(
                lens,
                vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)),
            new ModalLensGraphMouse());

    graphMouse.addItemListener(hyperbolicViewSupport.getGraphMouse().getModeListener());
    graphMouse.addItemListener(hyperbolicSupport.getGraphMouse().getModeListener());

    JComboBox<?> modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(graphMouse.getModeListener());
    graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);

    final ScalingControl scaler = new CrossoverScalingControl();

    vv.scaleToLayout(scaler);

    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));

    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

    JToggleButton radial = new JToggleButton("Balloon");
    final JRadioButton animateTransition = new JRadioButton("Animate Transition");

    radial.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            ((JToggleButton) e.getSource()).setText("Tree");
            if (animateTransition.isSelected()) {
              LayoutAlgorithmTransition.animate(vv.getModel(), radialLayoutAlgorithm);
            } else {
              LayoutAlgorithmTransition.apply(vv.getModel(), radialLayoutAlgorithm);
            }

            vv.getRenderContext()
                .getMultiLayerTransformer()
                .getTransformer(Layer.LAYOUT)
                .setToIdentity();
            vv.addPreRenderPaintable(rings);

          } else {
            ((JToggleButton) e.getSource()).setText("Balloon");
            if (animateTransition.isSelected()) {
              LayoutAlgorithmTransition.animate(vv.getModel(), treeLayoutAlgorithm);
            } else {
              LayoutAlgorithmTransition.apply(vv.getModel(), treeLayoutAlgorithm);
            }

            vv.getRenderContext()
                .getMultiLayerTransformer()
                .getTransformer(Layer.LAYOUT)
                .setToIdentity();
            vv.removePreRenderPaintable(rings);
          }
          vv.repaint();
        });

    final JRadioButton hyperView = new JRadioButton("Hyperbolic View");
    hyperView.addItemListener(
        e -> hyperbolicViewSupport.activate(e.getStateChange() == ItemEvent.SELECTED));
    final JRadioButton hyperLayout = new JRadioButton("Hyperbolic Layout");
    hyperLayout.addItemListener(
        e -> hyperbolicSupport.activate(e.getStateChange() == ItemEvent.SELECTED));
    final JRadioButton noLens = new JRadioButton("No Lens");

    ButtonGroup radio = new ButtonGroup();
    radio.add(hyperView);
    radio.add(hyperLayout);
    radio.add(noLens);

    JPanel scaleGrid = new JPanel(new GridLayout(1, 0));
    scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));
    JPanel viewControls = new JPanel();
    viewControls.setLayout(new GridLayout(0, 1));

    JPanel controls = new JPanel();
    scaleGrid.add(plus);
    scaleGrid.add(minus);
    controls.add(radial);
    controls.add(scaleGrid);
    controls.add(modeBox);
    viewControls.add(hyperView);
    viewControls.add(hyperLayout);
    viewControls.add(noLens);
    viewControls.add(animateTransition);
    controls.add(viewControls);
    content.add(controls, BorderLayout.SOUTH);
  }

  class Rings implements VisualizationServer.Paintable {

    BalloonLayoutAlgorithm<String, Point2D> layoutAlgorithm;

    public Rings(BalloonLayoutAlgorithm<String, Point2D> layoutAlgorithm) {
      this.layoutAlgorithm = layoutAlgorithm;
    }

    public void paint(Graphics g) {
      g.setColor(Color.gray);

      Graphics2D g2d = (Graphics2D) g;

      Ellipse2D ellipse = new Ellipse2D.Double();
      for (String v : vv.getModel().getNetwork().nodes()) {
        Double radius = layoutAlgorithm.getRadii().get(v);
        if (radius == null) {
          continue;
        }
        Point2D p = vv.getModel().getLayoutModel().apply(v);
        ellipse.setFrame(-radius, -radius, 2 * radius, 2 * radius);
        AffineTransform at = AffineTransform.getTranslateInstance(p.getX(), p.getY());
        Shape shape = at.createTransformedShape(ellipse);

        //        MutableTransformer viewTransformer =
        //            vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW);
        MultiLayerTransformer multiLayerTransformer =
            vv.getRenderContext().getMultiLayerTransformer();

        MutableTransformer viewTransformer = multiLayerTransformer.getTransformer(Layer.VIEW);
        MutableTransformer layoutTransformer = multiLayerTransformer.getTransformer(Layer.LAYOUT);

        if (viewTransformer instanceof LensTransformer) {
          shape = multiLayerTransformer.transform(shape);
        } else if (layoutTransformer instanceof LensTransformer) {
          HyperbolicShapeTransformer shapeChanger =
              new HyperbolicShapeTransformer(vv, viewTransformer);
          LensTransformer lensTransformer = (LensTransformer) layoutTransformer;
          shapeChanger.getLens().setLensShape(lensTransformer.getLens().getLensShape());
          MutableTransformer layoutDelegate =
              ((MutableTransformerDecorator) layoutTransformer).getDelegate();
          shape = shapeChanger.transform(layoutDelegate.transform(shape));
        } else {
          shape = vv.getRenderContext().getMultiLayerTransformer().transform(Layer.LAYOUT, shape);
        }

        //        if (viewTransformer instanceof MutableTransformerDecorator) {
        //          shape = vv.getRenderContext().getMultiLayerTransformer().transform(shape);
        //        } else {
        //          shape = vv.getRenderContext().getMultiLayerTransformer().transform(Layer.LAYOUT, shape);
        //        }

        g2d.draw(shape);
      }
    }

    public boolean useTransform() {
      return true;
    }
  }

  /** */
  private CTreeNetwork<String, Integer> createTree() {
    MutableCTreeNetwork<String, Integer> tree =
        TreeNetworkBuilder.builder().expectedNodeCount(27).build();

    int edgeId = 0;
    tree.addNode("A0");
    tree.addEdge("A0", "B0", edgeId++);
    tree.addEdge("A0", "B1", edgeId++);
    tree.addEdge("A0", "B2", edgeId++);

    tree.addEdge("B0", "C0", edgeId++);
    tree.addEdge("B0", "C1", edgeId++);
    tree.addEdge("B0", "C2", edgeId++);
    tree.addEdge("B0", "C3", edgeId++);

    tree.addEdge("C2", "H0", edgeId++);
    tree.addEdge("C2", "H1", edgeId++);

    tree.addEdge("B1", "D0", edgeId++);
    tree.addEdge("B1", "D1", edgeId++);
    tree.addEdge("B1", "D2", edgeId++);

    tree.addEdge("B2", "E0", edgeId++);
    tree.addEdge("B2", "E1", edgeId++);
    tree.addEdge("B2", "E2", edgeId++);

    tree.addEdge("D0", "F0", edgeId++);
    tree.addEdge("D0", "F1", edgeId++);
    tree.addEdge("D0", "F2", edgeId++);

    tree.addEdge("D1", "G0", edgeId++);
    tree.addEdge("D1", "G1", edgeId++);
    tree.addEdge("D1", "G2", edgeId++);
    tree.addEdge("D1", "G3", edgeId++);
    tree.addEdge("D1", "G4", edgeId++);
    tree.addEdge("D1", "G5", edgeId++);
    tree.addEdge("D1", "G6", edgeId++);
    tree.addEdge("D1", "G7", edgeId++);

    return tree;
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    content.add(new BalloonLayoutDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
