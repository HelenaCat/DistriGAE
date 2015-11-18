package ds.gae.listener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import ds.gae.CarRentalModel;
import ds.gae.EMF;
import ds.gae.entities.Car;
import ds.gae.entities.CarRentalCompany;
import ds.gae.entities.CarType;

public class CarRentalServletContextListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// This will be invoked as part of a warming request, 
		// or the first user request if no warming request was invoked.
		// check if dummy data is available, and add if necessary
		//if(!isDummyDataAvailable()) { //TODO is weg zodat de lijn hieronder uitgevoerd wordt (gebeurde anders niet)
			addDummyData();
		//}
	}
	
	private boolean isDummyDataAvailable() {
		// If the Hertz car rental company is in the datastore, we assume the dummy data is available
		EntityManager em = EMF.get().createEntityManager();
		try{
			return em.find(CarRentalCompany.class, "Hertz") != null;
		//return (em.createQuery( TODO wegdoen
		//		 "SELECT c.name "
		//	     + "FROM CarRentalCompany c"
		//		 + "WHERE c.name = :name").setParameter("name", "Hertz").getResultList()).size() > 0;
		}
		finally{
			em.close();
		}

	}
	
	private void addDummyData() {
		loadRental("Hertz","hertz.csv");
        loadRental("Dockx","dockx.csv");
	}
	
	private void loadRental(String name, String datafile) {
		Logger.getLogger(CarRentalServletContextListener.class.getName()).log(Level.INFO, "loading {0} from file {1}", new Object[]{name, datafile});
		EntityManager em = EMF.get().createEntityManager();
		try {
        	System.out.println("loadRental opgeroepen"); //TODO
            Set<CarType> carTypes = loadData(name, datafile);
            CarRentalCompany company = new CarRentalCompany(name);//TODO
            em.persist(company);
            for(CarType type : carTypes){ //TODO
            	company.addCarType(type);
            }
            for(Car car: cars){
            	company.getCarType(car.getType()).addCar(car);
            }
            cars.clear();
            

        } catch (NumberFormatException ex) {
            Logger.getLogger(CarRentalServletContextListener.class.getName()).log(Level.SEVERE, "bad file", ex);
        } catch (IOException ex) {
            Logger.getLogger(CarRentalServletContextListener.class.getName()).log(Level.SEVERE, null, ex);
        }
		finally{
			em.close();
		}
	}
	
	private static Set<Car> cars = new HashSet<Car>();//TODO
	
	public static Set<CarType> loadData(String name, String datafile) throws NumberFormatException, IOException {
		System.out.println("loadData opgeroepen"); //TODO
		Set<CarType> carTypes = new HashSet<CarType>();
		int carId = 1;

		//open file from jar
		BufferedReader in = new BufferedReader(new InputStreamReader(CarRentalServletContextListener.class.getClassLoader().getResourceAsStream(datafile)));
		//while next line exists
		while (in.ready()) {
			//read line
			String line = in.readLine();
			//if comment: skip
			if (line.startsWith("#")) {
				continue;
			}
			//tokenize on ,
			StringTokenizer csvReader = new StringTokenizer(line, ",");
			//create new car type from first 5 fields
			CarType type = new CarType(csvReader.nextToken(),
					Integer.parseInt(csvReader.nextToken()),
					Float.parseFloat(csvReader.nextToken()),
					Double.parseDouble(csvReader.nextToken()),
					Boolean.parseBoolean(csvReader.nextToken()));
			carTypes.add(type);
			//create N new cars with given type, where N is the 5th field
			for (int i = Integer.parseInt(csvReader.nextToken()); i > 0; i--) {
				cars.add(new Car(carId++, type.getName()));//TODO
			}
		}

		return carTypes;
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// App Engine does not currently invoke this method.
	}
}