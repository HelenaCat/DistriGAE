package ds.gae.entities;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.PrimaryKey;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import ds.gae.ReservationException;

@Entity
public class CarRentalCompany {

	private static Logger logger = Logger.getLogger(CarRentalCompany.class.getName());
	@Id
	private String name;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;
	@OneToMany(cascade = CascadeType.ALL)
	private Set<CarType> carTypes = new HashSet<CarType>();

	/***************
	 * CONSTRUCTOR *
	 ***************/

	public CarRentalCompany(String name) {
		logger.log(Level.INFO, "<{0}> Car Rental Company {0} starting up...", name);
		setName(name);
	}
	
	public CarRentalCompany(){
		
	}

	/********
	 * NAME *
	 ********/

	public String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}

	/*************
	 * CAR TYPES *
	 *************/

	public Collection<CarType> getAllCarTypes() {
		return carTypes;
	}
	
	public Set<String> getAllCarTypeStrings(){
		Set<String> carTypes = new HashSet<String>();
		for(CarType type : this.getAllCarTypes()){
			carTypes.add(type.getName());
		}
		return carTypes;
	}
	
	public CarType getCarType(String carTypeName) {
		for(CarType type : carTypes){
			if(type.getName().equals(carTypeName)){
				return type;
			}
		}
		throw new IllegalArgumentException("<" + carTypeName + "> No car type of name " + carTypeName);
	}
	
	public boolean isAvailable(String carTypeName, Date start, Date end) {
		logger.log(Level.INFO, "<{0}> Checking availability for car type {1}", new Object[]{name, carTypeName});
		for(CarType type : carTypes){
			if(type.getName().equals(carTypeName)){
				return getAvailableCarTypes(start, end).contains(type);
			}
		}
		throw new IllegalArgumentException("<" + carTypeName + "> No car type of name " + carTypeName);
	}
	
	public Set<CarType> getAvailableCarTypes(Date start, Date end) {
		Set<CarType> availableCarTypes = new HashSet<CarType>();
		for (CarType type : carTypes){
			for (Car car: type.getCars()){
				if(car.isAvailable(start, end)){
					availableCarTypes.add(type);
					break;
				}
			}
		}
		return availableCarTypes;
	}
	
	public void addCarType(CarType type){
		System.out.println("CarRentalCompanyKey " + this.key); //TODO
		Key childKey = this.key.getChild(CarType.class.getName(), type.getName());
		type.setKey(childKey);
		this.carTypes.add(type);
	}
		
	/*********
	 * CARS *
	 *********/
	
	private Car getCar(int uid) {
		for (Car car : this.getCars()){
			if (car.getId() == uid)
				return car;
		}
		throw new IllegalArgumentException("<" + name + "> No car with uid " + uid);
	}
	
	public Set<Car> getCars() {
		Set<Car> cars = new HashSet<Car>();
		for (CarType type : carTypes){
			cars.addAll(type.getCars());
		}
    	return cars;
    }
	
	private List<Car> getAvailableCars(String carType, Date start, Date end) {
		List<Car> availableCars = new LinkedList<Car>();
		for (Car car : this.getCars()) {
			if (car.getType().equals(carType) && car.isAvailable(start, end)) {
				availableCars.add(car);
			}
		}
		return availableCars;
	}

	/****************
	 * RESERVATIONS *
	 ****************/

	public Quote createQuote(ReservationConstraints constraints, String client)
			throws ReservationException {
		logger.log(Level.INFO, "<{0}> Creating tentative reservation for {1} with constraints {2}", 
                        new Object[]{name, client, constraints.toString()});
		
		CarType type = getCarType(constraints.getCarType());
		
		if(!isAvailable(constraints.getCarType(), constraints.getStartDate(), constraints.getEndDate()))
			throw new ReservationException("<" + name
				+ "> No cars available to satisfy the given constraints.");
		
		double price = calculateRentalPrice(type.getRentalPricePerDay(),constraints.getStartDate(), constraints.getEndDate());
		
		return new Quote(client, constraints.getStartDate(), constraints.getEndDate(), getName(), constraints.getCarType(), price);
	}

	// Implementation can be subject to different pricing strategies
	private double calculateRentalPrice(double rentalPricePerDay, Date start, Date end) {
		return rentalPricePerDay * Math.ceil((end.getTime() - start.getTime())
						/ (1000 * 60 * 60 * 24D));
	}

	public Reservation confirmQuote(Quote quote) throws ReservationException {
		logger.log(Level.INFO, "<{0}> Reservation of {1}", new Object[]{name, quote.toString()});
		List<Car> availableCars = getAvailableCars(quote.getCarType(), quote.getStartDate(), quote.getEndDate());
		if(availableCars.isEmpty())
			throw new ReservationException("Reservation failed, all cars of type " + quote.getCarType()
	                + " are unavailable from " + quote.getStartDate() + " to " + quote.getEndDate());
		Car car = availableCars.get((int)(Math.random()*availableCars.size()));
		
		Reservation res = new Reservation(quote, car.getId());
		car.addReservation(res);
		return res;
	}

	public void cancelReservation(Reservation res) {
		logger.log(Level.INFO, "<{0}> Cancelling reservation {1}", new Object[]{name, res.toString()});
		getCar((int) res.getCarId()).removeReservation(res);
	}
}