package edu.uci.ics.jung.layout.algorithms;

import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.layout.model.LayoutModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For Iterative algorithms that perform delayed operations on a Thread, save off the layoutModel so
 * that it can be accessed by the threaded code. The layoutModel could be removed and instead passed
 * via all of the iterative methods (for example step(layoutModel) instead of step() )
 *
 * @author Tom Nelson
 */
public abstract class AbstractIterativeLayoutAlgorithm<N, P> extends AbstractLayoutAlgorithm<N, P>
    implements LayoutAlgorithm<N, P>, IterativeContext {

  private static final Logger log = LoggerFactory.getLogger(AbstractIterativeLayoutAlgorithm.class);
  /**
   * because the IterativeLayoutAlgorithms use multithreading to continuously update node positions,
   * the layoutModel state is saved (during the visit method) so that it can be used continuously
   */
  protected LayoutModel<N, P> layoutModel;

  /**
   * because the IterativeLayoutAlgorithms use multithreading to continuously update node positions,
   * the layoutModel state is saved (during the visit method) so that it can be used continuously
   */
  public void visit(LayoutModel<N, P> layoutModel) {
    super.visit(layoutModel);
    log.trace("visiting " + layoutModel);
    this.layoutModel = layoutModel;
  }
}
