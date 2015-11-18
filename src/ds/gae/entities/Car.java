package ds.gae.entities;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.google.appengine.api.datastore.Key;

@Entity
public class Car {

    private long id;
    private String type;
    @OneToMany(cascade = CascadeType.ALL)
    private Set<Reservation> reservations;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;

    /***************
     * CONSTRUCTOR *
     ***************/
    
    public Car(long uid, String type) {
    	this.id = uid;
        this.type = type;
        this.reservations = new HashSet<Reservation>();
    }
    
    public Car(){
    	
    }
    
    /*******
     * KEY *
     ******/
    
    public Key getKey(){
    	return key;
    }

    /******
     * ID *
     ******/
    
    public long getId() {
    	return id;
    }
    
    /************
     * CAR TYPE *
     ************/
    
    public String getType() {
        return type;
    }

    /****************
     * RESERVATIONS *
     ****************/
    
    public Set<Reservation> getReservations() {
    	return reservations;
    }

    public boolean isAvailable(Date start, Date end) {
        if(!start.before(end))
            throw new IllegalArgumentException("Illegal given period");

        for(Reservation reservation : reservations) {
            if(reservation.getEndDate().before(start) || reservation.getStartDate().after(end))
                continue;
            return false;
        }
        return true;
    }
    
    public void addReservation(Reservation res) {
        reservations.add(res);
    }
    
    public void removeReservation(Reservation reservation) {
        // equals-method for Reservation is required!
        reservations.remove(reservation);
    }
}