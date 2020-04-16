package org.heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.VirtualEdgeIteratorState;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.storage.RAMDirectory;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Helper;
import org.heigit.ors.routing.graphhopper.extensions.ORSDefaultFlagEncoderFactory;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;
import org.heigit.ors.routing.graphhopper.extensions.storages.NewGreenIndexGraphStorage;
import java.util.HashMap;
import java.util.Map;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;


public class NewGreenIndexGraphStorageBuilderTest {
    private NewGreenIndexGraphStorageBuilder builder;
    private GraphExtension storage;
    private NewGreenIndexGraphStorage storage_test;
    private final EncodingManager encodingManager= EncodingManager.create(new ORSDefaultFlagEncoderFactory(), FlagEncoderNames.CAR_ORS, 4);

    public NewGreenIndexGraphStorageBuilderTest() {
        builder = new NewGreenIndexGraphStorageBuilder();
    }


    private VirtualEdgeIteratorState generateEdge(int id) {
        IntsRef intsRef = encodingManager.createEdgeFlags();
// TODO GH0.10:
//        return new VirtualEdgeIteratorState(0, id, id, 1, 2, 10,
//                encoder.setProperties(10, true, true), "test", Helper.createPointList(51,0,51,1));
        VirtualEdgeIteratorState ve =  new VirtualEdgeIteratorState(0, id, id, 1, 2, 10,
            intsRef, "test", Helper.createPointList(51,0,51,1),false);
        return ve;
    }

    @Before
    public void reset() {
        builder = new NewGreenIndexGraphStorageBuilder();
        Map _params = new HashMap();
        _params.put("filepath", "/Users/chludwig/Development/meinGruen/code/ors_instances/openrouteservice/openrouteservice/data/test/green_index_test.csv");
        //_params.put("filepath", "/Users/chludwig/Development/meinGruen/code/ors_instances/openrouteservice/openrouteservice/data/green_index_hd_dd_new.csv");
        builder.setParameters(_params);

        try {
            storage = builder.init(null);
            storage.init(null, new RAMDirectory(""));
            storage.create(1);
        } catch (Exception e) {

        };
    }

    /*
    @Test
    public void TestProcessEdgesBuildAll() {

        byte[] byteValues = new byte[1];

        for (int i=0 ; i<599942772 - 1 ; i++) {
            ReaderWay way = new ReaderWay(i);
            EdgeIteratorState edge = new VirtualEdgeIteratorState(1,i,i,1,2,1,1,"",null);
            builder.processEdge(way, edge);
            storage_test = builder.get_storage();
            int green_value = storage_test.getEdgeValue(i, byteValues);
            if (green_value != 45) {
                System.out.print("---" + i + " : " + green_value + "\n");
            }
        }
    }*/

    @Test
    public void TestProcessEdges() {

        byte[] byteValues = new byte[1];

        // Test edge with id 2
        int id = 2;
        int expected = 2;
        ReaderWay way = new ReaderWay(id);
        EdgeIteratorState edge = generateEdge(id);
        builder.processEdge(way, edge);
        storage_test = builder.get_storage();
        int green_value = storage_test.getEdgeValue(id, byteValues);
        Assert.assertEquals(expected, green_value);

        // Test edge with id 100
        int id2 = 100;
        int expected2 = 100;
        ReaderWay way2 = new ReaderWay(id2);
        EdgeIteratorState edge2 = generateEdge(id2);
        builder.processEdge(way2, edge2);
        storage_test = builder.get_storage();
        int green_value2 = storage_test.getEdgeValue(id2, byteValues);
        Assert.assertEquals(expected2, green_value2);

    }

    @Test
    public void TestProcessEdgeNoData() {

        // Test non existent OSM id 101
        int id = 10001;
        int expected = 30;
        ReaderWay way = new ReaderWay(id);
        EdgeIteratorState edge = generateEdge(id);
        builder.processEdge(way, edge);
        storage_test = builder.get_storage();
        byte[] byteValues = new byte[1];
        int green_value = storage_test.getEdgeValue(id, byteValues);
        Assert.assertEquals(expected, green_value);
    }

    @Test
    public void TestProcessEdgeExceedsMaxValue() {

        // Test non existent OSM id 101
        int id = 101;
        int expected = 100;
        ReaderWay way = new ReaderWay(id);
        EdgeIteratorState edge = generateEdge(id);
        builder.processEdge(way, edge);
        storage_test = builder.get_storage();
        byte[] byteValues = new byte[1];
        int green_value = storage_test.getEdgeValue(id, byteValues);
        Assert.assertEquals(expected, green_value);
    }

}
