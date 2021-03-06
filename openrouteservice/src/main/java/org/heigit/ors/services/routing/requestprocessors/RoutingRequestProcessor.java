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
package org.heigit.ors.services.routing.requestprocessors;

import com.graphhopper.util.Helper;

import org.heigit.ors.exceptions.EmptyElementException;


import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.routing.RouteResult;
import org.heigit.ors.routing.RoutingErrorCodes;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.routing.RoutingRequest;
import org.heigit.ors.globalresponseprocessor.GlobalResponseProcessor;
import org.heigit.ors.services.routing.requestprocessors.json.JsonRoutingResponseWriter;
import org.heigit.ors.servlet.http.AbstractHttpRequestProcessor;
import org.heigit.ors.servlet.util.ServletUtility;

import org.json.JSONObject;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class Processes a {@link HttpServletResponse} to the desired route output.
 * It needs to be instantiated through the super class call to {@link AbstractHttpRequestProcessor}.
 *
 * @author OpenRouteServiceTeam
 * @author Julian Psotta, julian@openrouteservice.org
 * @deprecated
 */
@Deprecated
public class RoutingRequestProcessor extends AbstractHttpRequestProcessor {

    public static final String KEY_FORMAT = "format";
    public static final String KEY_GEOJSON = "geojson";

    /**
     * {@link RoutingRequestProcessor} is the constructor and calls the {@link AbstractHttpRequestProcessor} as the super class.
     * The output can than be generated through a call to the process function.
     *
     * @param request The input is a {@link HttpServletRequest}
     * @throws Exception
     */
    public RoutingRequestProcessor(HttpServletRequest request) throws Exception {
        super(request);
    }

    /**
     * The function overrides the process function of the super class {@link AbstractHttpRequestProcessor}
     * It handles the creation of the route and the formatting to the desired output format.
     *
     * @param response The input is a {@link HttpServletResponse}
     * @throws Exception If the {@link HttpServletRequest} or the {@link HttpServletResponse} are malformed in some way an {@link Exception} error is raised
     */
    @Override
    public void process(HttpServletResponse response) throws Exception {
        // Get the routing Request to send it to the calculation function
        RoutingRequest rreq = RoutingRequestParser.parseFromRequestParams(request);
        JSONObject json = null;
        JSONObject geojson = null;
        String gpx;
        String respFormat = request.getParameter(KEY_FORMAT);
        String geometryFormat = rreq.getGeometryFormat();

        if (Helper.isEmpty(respFormat) || "json".equalsIgnoreCase(respFormat)) {
            RouteResult[] result = RoutingProfileManager.getInstance().computeRoute(rreq);
            json = JsonRoutingResponseWriter.toJson(rreq, result);
            ServletUtility.write(response, json, "UTF-8");

        } else if (KEY_GEOJSON.equalsIgnoreCase(respFormat)) {
            // Manually set the geometryFormat to geojson. Else an encoded polyline could be parsed by accident and cause problems.
            // Encoded polyline is anyway not needed in this export format.
            if (Helper.isEmpty(geometryFormat) || !geometryFormat.equals(KEY_GEOJSON)) {
                rreq.setGeometryFormat(KEY_GEOJSON);
            }
            RouteResult[] result = RoutingProfileManager.getInstance().computeRoute(rreq);
            geojson = new GlobalResponseProcessor(rreq, result).toGeoJson();
            if (geojson != null) {
                ServletUtility.write(response, geojson, "UTF-8");
            } else {
                throw new EmptyElementException(RoutingErrorCodes.EMPTY_ELEMENT, "GeoJSON was empty and therefore could not be exported.");
            }


        } else if ("gpx".equalsIgnoreCase(respFormat)) {
            // Manually set the geometryFormat to geojson. Else an encoded polyline could be parsed by accident and cause problems.
            // Encoded polyline is anyway not needed in this export format.
            if (Helper.isEmpty(geometryFormat) || !geometryFormat.equals(KEY_GEOJSON)) {
                rreq.setGeometryFormat(KEY_GEOJSON);
            }
            RouteResult[] result = RoutingProfileManager.getInstance().computeRoute(rreq);
            gpx = new GlobalResponseProcessor(rreq, result).toGPX();
            if (gpx != null) {
                ServletUtility.write(response, gpx);
            } else {
                throw new EmptyElementException(RoutingErrorCodes.EMPTY_ELEMENT, "GPX was empty and therefore could not be created.");
            }
        } else {
            throw new ParameterValueException(2003, KEY_FORMAT, request.getParameter(KEY_FORMAT).toLowerCase());
        }

    }
}
