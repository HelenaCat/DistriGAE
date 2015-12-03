package ds.gae.servlets;

import java.util.List;

import ds.gae.CarRentalModel;
import ds.gae.ReservationException;
import ds.gae.entities.Quote;

import com.google.appengine.api.taskqueue.DeferredTask;

public class ConfirmQuotesTask implements DeferredTask {

	private List<Quote> quotes;
	public ConfirmQuotesTask(List<Quote> quotes){
		this.quotes = quotes;
	}
	
	@Override
	public void run() {
		try {
			CarRentalModel.get().confirmQuotes(quotes);
			//TODO log if it succeeds
		} catch (ReservationException e) {
			//TODO log if confirming doesn't succeed
		}
	}
}
