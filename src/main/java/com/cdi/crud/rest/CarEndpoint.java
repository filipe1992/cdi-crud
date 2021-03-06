package com.cdi.crud.rest;

import com.cdi.crud.model.Car;
import com.cdi.crud.model.Filter;
import com.cdi.crud.service.CarService;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import java.util.List;

/**
 *
 */
@Stateless
@Path("/cars")
public class CarEndpoint {
    @Inject
    CarService carService;

    /**
     * @description creates a new car
     * @status 400 Car model cannot be empty
     * @status 201 Car created successfully
     */
    @POST
    @Consumes("application/json")
    public Response create(Car entity) {
        carService.insert(entity);
        return Response.created(UriBuilder.fromResource(CarEndpoint.class).path(String.valueOf(entity.getId())).build()).build();
    }

    /**
     * @description deletes a car based on its ID
     * @param user name of the user to log in
     * @param id car ID
     * @status 401 only authorized users can access this resource
     * @status 403 only authenticated users can access this resource
     * @status 404 car not found
     * @status 204 Car deleted successfully
     */
    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    @RestSecured
    public Response deleteById(@HeaderParam("user") String user, @PathParam("id") Integer id) {
        Car entity = carService.findById(id);
        if (entity == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        carService.remove(entity);
        return Response.noContent().build();
    }

    /**
     * @description finds a car based on its ID
     * @responseType com.cdi.crud.model.Car
     * @param id car ID
     * @status 404 car not found
     * @status 200 car found successfully
     */
    @GET
    @Path("/{id:[0-9][0-9]*}")
    @Produces("application/json")
    public Response findById(@PathParam("id") Integer id) {
        Car entity;
        try {
            entity = carService.findById(id);
        } catch (NoResultException nre) {
            entity = null;
        }

        if (entity == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(entity).build();
    }



    /**
     * @requiredParams startPosition, maxResult, minPrice, maxPrice
     * @param startPosition initial list position
     * @param maxResult number of elements to retrieve
     * @param minPrice minimum car price
     * @param maxPrice maximum car price
     * @param model list cars with given model
     */
    @GET
    @Produces("application/json")
    @Path("list")
    public List<Car> list(@QueryParam("start") @DefaultValue("0") Integer startPosition,
                          @QueryParam("max") @DefaultValue("10") Integer maxResult,
                          @QueryParam("model") String model,
                          @QueryParam("minPrice") @DefaultValue("0") Double minPrice,
                          @QueryParam("maxPrice") @DefaultValue("20000") Double maxPrice) {
        Filter<Car> filter = new Filter<>();
        if(model != null){
            Car car = new Car();
            car.setModel(model);
            filter.setEntity(car);
        }
        filter.addParam("maxPrice",maxPrice);
        filter.addParam("minPrice",minPrice);
        filter.setFirst(startPosition).setPageSize(maxResult);
        final List<Car> results = carService.paginate(filter);
        return results;
    }

    /**
    * @status 400 Car model cannot be empty
    * @status 400 No Car informed to be updated
    * @status 404 No Car found with the given ID
    * @status 409 id passed in parameter is different from the Car to update
    * @status 204 Car updated successfully
    */
    @PUT
    @Path("/{id:[0-9][0-9]*}")
    @Consumes("application/json")
    public Response update(@PathParam("id") Integer id,  Car entity) {
        if (entity == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        if (!id.equals(entity.getId())) {
            return Response.status(Status.CONFLICT).entity(entity).build();
        }
        if (carService.crud().eq("id",id).count() == 0) {
            return Response.status(Status.NOT_FOUND).build();
        }
        try {
            carService.update(entity);
        } catch (OptimisticLockException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getEntity()).build();
        }

        return Response.noContent().build();
    }
}
