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
 *
 */
package heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Helper;
import heigit.ors.routing.graphhopper.extensions.storages.NewGreenIndexGraphStorage;
import heigit.ors.routing.graphhopper.extensions.storages.NoiseIndexGraphStorage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * This class builds imports the green index data as a new the NewGreenGraphStorage.
 *
 * @author Christina Ludwig, christina.ludwig@uni-heidelberg.de
 */
public class NewGreenIndexGraphStorageBuilder extends AbstractGraphStorageBuilder {
    private NewGreenIndexGraphStorage _storage;
    private Map<Long, Integer> osm_greenindex_lookup = new HashMap<>();
    private int max_level = 100;
    private final int no_data = 30;

    public NewGreenIndexGraphStorageBuilder() {

    }

    @Override
    public GraphExtension init(GraphHopper graphhopper) throws Exception {
        if (_storage != null)
            throw new Exception("GraphStorageBuilder has been already initialized.");

        // TODO Check if the _noiseIndexFile exists
        String csvFile = _parameters.get("filepath");
        readNoiseIndicesFromCSV(csvFile);
        _storage = new NewGreenIndexGraphStorage();

        return _storage;
    }

    private void readNoiseIndicesFromCSV(String csvFile) throws IOException {
        BufferedReader csvBuffer = null;
        try {
            String row;
            csvBuffer = new BufferedReader(new FileReader(csvFile));
            // Jump the header line
            csvBuffer.readLine();
            String[] rowValues = new String[2]; 
            while ((row = csvBuffer.readLine()) != null) 
            {
                if (!parseCSVrow(row, rowValues)) 
                	continue;
                
                osm_greenindex_lookup.put(Long.parseLong(rowValues[0]), Integer.parseInt(rowValues[1]));
            }

        } catch (IOException openFileEx) {
            openFileEx.printStackTrace();
            throw openFileEx;
        } finally {
            if (csvBuffer != null) 
            	csvBuffer.close();
        }
    }

    private boolean parseCSVrow(String row,  String[] rowValues) {
        if (Helper.isEmpty(row))
        	return false;
        
        int pos = row.indexOf(',');
        if (pos > 0)
        {
        	rowValues[0] = row.substring(0, pos).trim();
        	rowValues[1] = row.substring(pos+1, row.length()).trim();
        	// read, check and push "osm_id" and "noise level" values
        	if (Helper.isEmpty(rowValues[0]) || Helper.isEmpty(rowValues[1])) 
        		return false;
        	
        	return true;
        }
        else
        	return false;
    }

    @Override
    public void processWay(ReaderWay way) {

    }

    @Override
    public void processEdge(ReaderWay way, EdgeIteratorState edge) {
        //_storage.setEdgeValue(edge.getEdge(), getGreenIndex(way.getId()));
        byte green_index =  getGreenIndex(way.getId());
    	_storage.setEdgeValue(edge.getEdge(), green_index);
    }

    private byte getGreenIndex(long id) {
        Integer green_value = osm_greenindex_lookup.get(id);

        if (green_value == null)
            return (byte) no_data;

        if (green_value > max_level) {
            System.out.print("\nThe green index value of osm way, id = " + id + " is " + green_value
                + ", which is larger than than max level!");
            return (byte) max_level;
        }

        return (byte) (green_value.intValue());
    }

    @Override
    public String getName() {
        return "GreenIndex";
    }

    public NewGreenIndexGraphStorage get_storage() {
        return _storage;
    }

}