package mat.server;

import mat.client.measurepackage.MeasurePackageDetail;
import mat.client.measurepackage.MeasurePackageOverview;
import mat.client.measurepackage.service.PackageService;
import mat.server.service.PackagerService;

public class PackageServiceImpl extends SpringRemoteServiceServlet implements PackageService{
	private static final long serialVersionUID = -1789210947786753971L;

	@Override
	public MeasurePackageOverview getClausesAndPackagesForMeasure(
			String measureId) {
		MeasurePackageOverview overview = buildOverview(measureId);
		return overview;
	}
	private MeasurePackageOverview buildOverview(String measureId) {
		return getPackagerService().buildOverviewForMeasure(measureId);
	}

	@Override
	public void save(MeasurePackageDetail detail) {
		getPackagerService().save(detail);
	}

	private PackagerService getPackagerService() {
		return (PackagerService)context.getBean("packagerService");
	}
	@Override
	public void delete(MeasurePackageDetail detail) {
		getPackagerService().delete(detail);
	}
	
	@Override
	public void saveQDMData(MeasurePackageDetail detail) {
		getPackagerService().saveQDMData(detail);
	}

	
}
