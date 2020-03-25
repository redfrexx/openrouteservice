/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library; 
 *  if not, see <https://www.gnu.org/licenses/>.  
 */
package heigit.ors.routing.graphhopper.extensions.weighting;

import com.graphhopper.routing.EdgeIteratorStateHelper;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.graphhopper.extensions.storages.NewGreenIndexGraphStorage;

public class NewGreenWeighting extends FastestWeighting {
    private NewGreenIndexGraphStorage _greenIndexStorage;
    private byte[] _buffer = new byte[1];
    private double _userWeighting;

    public NewGreenWeighting(FlagEncoder encoder, PMap map, GraphStorage graphStorage) {
        super(encoder, map);
        _userWeighting = map.getDouble("factor", 1);
        _greenIndexStorage = GraphStorageUtils.getGraphExtension(graphStorage, NewGreenIndexGraphStorage.class);
        if (_greenIndexStorage == null) {
            System.out.print("NewGreenIndexStorage not found.");
        }
    }

    private double calcGreenWeighting(int green_index_value) {
        double _amplifyer = 5.; // amplify influence of greenness
        double green_weight = (100. - green_index_value) * 0.01 * _amplifyer * _userWeighting;
        //System.out.print("Green: " + green_weight + "\n");
        return green_weight;
    }

    @Override
    public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
        int greenValue = _greenIndexStorage.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edgeState), _buffer);
        return calcGreenWeighting(greenValue);
        //return _defaultGreenWeight;
    }

    @Override
    public String getName() {
        return "green";
    }

}