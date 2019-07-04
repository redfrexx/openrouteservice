package heigit.ors.routing.graphhopper.extensions.weighting;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.AbstractWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import heigit.ors.routing.graphhopper.extensions.weighting.AdditionWeighting.WeightCalc;

public class FuzzyWeighting extends AbstractWeighting {

  // Weight of distance
  private Weighting _superWeighting;
  private Weighting[] _weightings;
  private PMap _map;

  public FuzzyWeighting(Weighting[] weightings, Weighting superWeighting, FlagEncoder encoder, PMap map, GraphStorage graphStorage) {
    super(encoder);
    _superWeighting = superWeighting;
    _weightings = weightings;
    _map = map;
  }

  public abstract class WeightCalc
  {
    public abstract double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId);

    public abstract long calcMillis(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId);
  }

  public class OneWeightCalc extends WeightCalc {
    private Weighting _weighting;

      public OneWeightCalc(Weighting[] weightings)
    {
      _weighting = weightings[0];
    }

    public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId)
    {
      return _weighting.calcWeight(edgeState, reverse, prevOrNextEdgeId);
    }

    public long calcMillis(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId)
    {
      return _weighting.calcMillis(edgeState, reverse, prevOrNextEdgeId);
    }
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
     * cost = distance + ((criteria_1 * weight_1) * (criteria_2 * weight_2) * ... * (criteria_n * weight_n)) * distance
     */

    // Distance of edge
    double distance = _superWeighting.calcWeight(edgeState, reverse, prevOrNextEdgeId);
    //double time = _superWeighting.calcMillis(edgeState, reverse, prevOrNextEdgeId);
    double[] weights = new double[_weightings.length];

    // Additional health criteria (green and noise)
    double healthy_weight = 0.;
    for (int i = 0; i < _weightings.length; i++) {
      weights[i] = _weightings[i].calcWeight(edgeState, reverse, prevOrNextEdgeId);
      healthy_weight += weights[i];
      //System.out.print(_weightings[i].getName() + ":" + weights[i] + "\n");
    }
    healthy_weight = (healthy_weight / _weightings.length) * distance;
    //System.out.print("distance cost: " + distance + "\n");
    //System.out.print("additional cost:" + healthy_weight + "\n");
    return distance + healthy_weight;
  }

  @Override
  public String getName() { return "fuzzy";}
}
