package mat.client.measure.service;

import mat.client.measure.ManageMeasureDetailModel;
import mat.client.measure.ManageMeasureSearchModel;

import com.google.gwt.user.client.rpc.AsyncCallback;


public interface MeasureCloningServiceAsync {
	void clone(ManageMeasureDetailModel currentDetails, String loggedinUserId, boolean isDraftCreation, 
			AsyncCallback<ManageMeasureSearchModel.Result> callback);
}

