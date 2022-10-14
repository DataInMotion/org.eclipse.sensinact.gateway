package org.eclipse.sensinact.sensorthings.sensing.rest;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Self;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PropFilter;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.RefFilter;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

@Produces(APPLICATION_JSON)
@Path("/v1.1/Sensors({id})")
public interface SensorsAccess {
    
    @GET
    public Sensor getSensor(@PathParam("id") String id);
    
    @Path("{prop}")
    @GET
    @PropFilter
    public Sensor getSensorProp(@PathParam("id") String id);

    @Path("{prop}/$value")
    @GET
    @PropFilter
    public Sensor getSensorPropValue(@PathParam("id") String id);

    @Path("Datastreams")
    @GET
    public ResultList<Datastream> getSensorDatastreams(@PathParam("id") String id);

    @Path("Datastreams/$ref")
    @GET
    @RefFilter
    public ResultList<Self> getSensorDatastreamRef(@PathParam("id") String id);

    @Path("Datastreams({id2})")
    @GET
    public Datastream getSensorDatastream(@PathParam("id") String id, @PathParam("id2") String id2);
    
    @Path("Datastreams({id2})/{prop}")
    @GET
    @PropFilter
    public Datastream getSensorDatastreamProp(@PathParam("id") String id, @PathParam("id2") String id2);

    @Path("Datastreams(id2)/Observations")
    @GET
    public ResultList<Observation> getSensorDatastreamObservations(@PathParam("id") String id, @PathParam("id2") String id2);
    
    @Path("Datastreams({id2})/ObservedProperty")
    @GET
    public ObservedProperty getSensorDatastreamObservedProperty(@PathParam("id") String id, @PathParam("id2") String id2);

    @Path("Datastreams({id2})/Sensor")
    @GET
    public Sensor getSensorDatastreamSensor(@PathParam("id") String id, @PathParam("id2") String id2);
    
    @Path("Datastreams({id2})/Thing")
    @GET
    public Thing getSensorDatastreamThing(@PathParam("id") String id, @PathParam("id2") String id2);
    
}