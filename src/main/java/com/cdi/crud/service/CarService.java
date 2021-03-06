/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdi.crud.service;

import com.cdi.crud.exception.CustomException;
import com.cdi.crud.model.Car;
import com.cdi.crud.model.Filter;
import com.cdi.crud.model.Movie;
import com.cdi.crud.security.Admin;
import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.List;

/**
 *
 * @author rmpestano
 *
 *         Car Business logic
 */
    @Stateless
    public class CarService extends CrudService<Car> {
	
	@Inject
	MovieService movieService;//just to show dependency between tenants

	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public List<Car> listByModel(String model) {
		return crud().ilike("model", model, MatchMode.ANYWHERE).list();

	}

	@Override
	public Criteria configPagination(Filter<Car> filter) {
		if (filter.hasParam("id")) {
			crud().eq("id",
                    Integer.parseInt(filter.getParam("id").toString()));
		}

		// see index.xhtml 'model' column facet name filter
		if (filter.getEntity() != null && filter.getEntity().getModel() != null
				&& !"".equals(filter.getEntity().getModel())) {
			crud().ilike("model", filter.getEntity().getModel(), MatchMode.ANYWHERE);
		}

		// see index.xhtml 'price' column facet name filter
		if (filter.getEntity() != null && filter.getEntity().getPrice() != null) {
			crud().eq("price", filter.getEntity().getPrice());
		}
        if(filter.hasParam("minPrice") && filter.hasParam("maxPrice")){
             crud().between("price",(Double)filter.getParam("minPrice"),(Double)filter.getParam("maxPrice"));
        }
		return crud().getCriteria();
	}

	public List<String> getModels(String query) {
		return crud().criteria().ilike("model", query, MatchMode.ANYWHERE)
				.projection(Projections.property("model")).list();
	}

	@Override
	public void beforeInsert(Car car) {
		if (car.getModel() == null || "".equals(car.getModel().trim())) {
			throw new CustomException("Car model cannot be empty");
		}
	}

    @Override
    public void beforeUpdate(Car entity) {
        this.beforeInsert(entity);
    }

    public void initDatabase() {
		if (crud().countAll() == 0) {
			for (int i = 1; i <= 10; i++) {
				Car c = new Car("Car" + i, i);
				this.insert(c);
			}
		}
		
	}

	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public Movie findMovie(String string) {
		Movie example = new Movie();
		example.setName(string);
		return movieService.findByExample(example,MatchMode.ANYWHERE);
	}

    @Override
    @Admin
    public void remove(Car car) {
        super.remove(car);
    }
}
