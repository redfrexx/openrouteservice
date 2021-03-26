package org.heigit.ors.routing.graphhopper.extensions.weighting;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.AbstractWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;

public class FuzzyWeighting extends AbstractWeighting {

  // Weight of distance
  private Weighting _superWeighting;
  private Weighting[] _weightings;

  public FuzzyWeighting(Weighting[] weightings, Weighting superWeighting, FlagEncoder encoder) {
    super(encoder);
    _superWeighting = superWeighting;
    _weightings = weightings;
  }

  @Override
  public double getMinWeight(double distance) {
    return 0;
  }

  @Override
  public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
    /**
     * Calculates the costs of a route segment based on multiple routing criteria and the distance
     * using a linear combination of the form:
     * cost = distance + (((criteria_1 * weight_1) + (criteria_2 * weight_2) + ... + (criteria_n * weight_n)) * duration)
     */

    // Distance of edge
    double distance = _superWeighting.calcWeight(edgeState, reverse, prevOrNextEdgeId);

    //double time = _superWeighting.calcMillis(edgeState, reverse, prevOrNextEdgeId);
    double[] weights = new double[_weightings.length];

    // Additional health criteria (green, noise, steepness)
    double health_weight = 0.;
    for (int i = 0; i < _weightings.length; i++) {
      weights[i] = _weightings[i].calcWeight(edgeState, reverse, prevOrNextEdgeId);
      health_weight += weights[i];
    }
    //System.out.print("Health weight: " + health_weight + "\n");

    // Weight healthy criteria with duration or distance
    double health_costs = (health_weight / _weightings.length) * distance;
    System.out.print("duration cost: " + distance + "\n");
    System.out.print("health cost:" + health_costs + "\n");
    return distance + health_costs;
  }

  @Override
  public String getName() { return "fuzzy";}
}
