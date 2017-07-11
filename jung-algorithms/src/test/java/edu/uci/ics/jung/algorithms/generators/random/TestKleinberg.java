package edu.uci.ics.jung.algorithms.generators.random;


import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.common.base.Supplier;
import com.google.common.graph.MutableNetwork;

import edu.uci.ics.jung.algorithms.generators.Lattice2DGenerator;

/**
 * 
 * @author Joshua O'Madadhain
 */
@RunWith(JUnit4.class)
public class TestKleinberg {

	protected Supplier<String> vertexFactory;
	protected Supplier<Integer> edgeFactory;

	@Before
	public void setUp() {
		vertexFactory = new Supplier<String>() {
			int count;
			public String get() {
				return Character.toString((char)('a'+count++));
			}
		};
		edgeFactory = 
			new Supplier<Integer>() {
			int count;
			public Integer get() {
				return count++;
			}
		};
	}
	
	@Test
	public void testConnectionCount() {
		Lattice2DGenerator<String, Integer> generator =
				new Lattice2DGenerator<>(4, 4, true /* toroidal */);
		MutableNetwork<String, Integer> graph =
				generator.generateNetwork(true /* directed */, vertexFactory, edgeFactory);
		final int connectionCount = 2;
		
		KleinbergSmallWorld<String, Integer> ksw =
				KleinbergSmallWorld.<String, Integer>builder().connectionCount(connectionCount).build();
		ksw.addSmallWorldConnections(graph, generator.distance(graph.asGraph()), edgeFactory);
		
		for (String node : graph.nodes()) {
			assertEquals(graph.outDegree(node), 4 + connectionCount);
		}
	}
	
}
