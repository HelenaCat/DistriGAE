package ds.gae.servlets;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ds.gae.CarRentalModel;
import ds.gae.ReservationException;
import ds.gae.entities.Quote;

import com.google.appengine.api.taskqueue.DeferredTask;

public class ConfirmQuotesTask implements DeferredTask {

	private static final Logger log = Logger.getLogger(ConfirmQuotesTask.class.getName());
	
	private List<Quote> quotes;
	public ConfirmQuotesTask(List<Quote> quotes){
		this.quotes = quotes;
	}
	
	@Override
	public void run() {
		try {
			CarRentalModel.get().confirmQuotes(quotes);
			//log.info("Confirming quote(s) succeeded!");
			log.log(Level.INFO, "Confirming quote(s) succeeded!");
		} catch (ReservationException e) {
			//log.warning("Confirming quote(s) failed!");
			log.log(Level.INFO, "Confirming quote(s) failed!");
		}
	}
}
