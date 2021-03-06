package mat.client.measurepackage.service;

import mat.client.measurepackage.MeasurePackageDetail;
import mat.client.measurepackage.MeasurePackageOverview;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface PackageServiceAsync {
	public void getClausesAndPackagesForMeasure(String measureId, AsyncCallback<MeasurePackageOverview> callback);
	public void save(MeasurePackageDetail detail, AsyncCallback<Void> callback);
	public void saveQDMData(MeasurePackageDetail detail, AsyncCallback<Void> callback);
	public void delete(MeasurePackageDetail pkg, AsyncCallback<Void> callback);
}
