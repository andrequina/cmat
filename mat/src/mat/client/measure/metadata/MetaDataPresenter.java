package mat.client.measure.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mat.client.Mat;
import mat.client.MatPresenter;
import mat.client.MeasureComposerPresenter;
import mat.client.codelist.ListBoxCodeProvider;
import mat.client.event.MeasureEditEvent;
import mat.client.event.MeasureSelectedEvent;
import mat.client.measure.ManageMeasureDetailModel;
import mat.client.measure.service.MeasureServiceAsync;
import mat.client.measure.service.SaveMeasureResult;
import mat.client.shared.DateBoxWithCalendar;
import mat.client.shared.HasVisible;
import mat.client.shared.ListBoxMVP;
import mat.client.shared.MatContext;
import mat.client.shared.MessageDelegate;
import mat.client.shared.ReadOnlyHelper;
import mat.client.shared.search.SearchView;
import mat.model.Author;
import mat.model.MeasureType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class MetaDataPresenter extends BaseMetaDataPresenter implements MatPresenter{
	
	
	public static interface MetaDataDetailDisplay extends BaseMetaDataDisplay{
		public Label getMeasureName();
		public Label getShortName();
		
		//US 421. Measure scoring choice is now part of measure creation process. So, this module just displays the choice.
		public Label getMeasureScoring();
		
		public HasClickHandlers getEditAuthorsButton();
		public HasClickHandlers getEditMeasureTypeButton();
		public HasKeyDownHandlers getFocusPanel();
		public Label getVersionNumber();
		//public HasValue<String> getMeasureId();
		public HasValue<String> getSetName();
		public Label geteMeasureIdentifier();
		public HasValue<String> getNqfId();
		public Label getFinalizedDate();
		public String getMeasurementFromPeriod();
		public DateBoxWithCalendar getMeasurementFromPeriodInputBox();
		public String getMeasurementToPeriod();
		public DateBoxWithCalendar getMeasurementToPeriodInputBox();
		public String getMeasureType();
		public ListBoxMVP getMeasureSteward();

		//US 413. Introduced Measure Steward Other option.
		public HasValue<String> getMeasureStewardListBox();		
		public String getMeasureStewardValue();
		public TextBox getMeasureStewardOther();
		public String getMeasureStewardOtherValue();
		
		public HasValue<Boolean> getEndorsebyNQF();
		public HasValue<Boolean> getNotEndorsebyNQF();
		public ListBoxMVP getMeasureStatus();
		public String getMeasureStatusValue();
		public String getAuthor();
		public void setAuthorsList(List<Author> author);
		public void setMeasureTypeList(List<MeasureType> measureType);
		public HasValue<String> getDescription();
		public HasValue<String> getCopyright();
		public HasValue<String> getClinicalRecommendation();
		public HasValue<String> getDefinitions();
		public HasValue<String> getGuidance();
		public HasValue<String> getTransmissionFormat();
		public HasValue<String> getRationale();
		public HasValue<String> getImprovementNotation();
		public HasValue<String> getStratification();
		public HasValue<String> getRiskAdjustment();
		public HasValue<String> getReference();
		public HasValue<String> getSupplementalData();
		public HasValue<String> getDisclaimer();
		public HasValue<String> getInitialPatientPop();
		public HasValue<String> getDenominator();
		public HasValue<String> getDenominatorExclusions();
		public HasValue<String> getNumerator();
		public HasValue<String> getNumeratorExclusions();
		public HasValue<String> getDenominatorExceptions();
		public HasValue<String> getMeasurePopulation();
		public HasValue<String> getMeasureObservations();
		public HasValue<String> getRateAggregation();
		public HasValue<String> getEmeasureId();
		public HasClickHandlers getGenerateEmeasureIdButton();
		public void setGenerateEmeasureIdButtonEnabled(boolean b);
		
		public List<String> getReferenceValues();
		public void setReferenceValues(List<String> values, boolean editable);
		public void setAddEditButtonsVisible(boolean b);
		public void enableEndorseByRadioButtons(boolean b);
		public void setSaveButtonEnabled(boolean b);
		public HasClickHandlers getSaveButton();
		
		//US 413. Interfaces to show or clear out Steward Other text boxes.
		public void showOtherTextBox();
		public void hideOtherTextBox();
			
		
	}
	
	public static interface AddEditAuthorsDisplay extends BaseAddEditDisplay<Author> {
		public String getAuthor();
		public HasValue<String> getAuthorInputBox();
		public HasValue<String> getOtherAuthor();
	}
	
	public static interface AddEditMeasureTypeDisplay extends BaseAddEditDisplay<MeasureType>{
		public String getMeasureType();
		public HasValue<String> getMeasureTypeInputBox();
		public HasValue<String> getOtherMeasureType();
	}
	
	private SimplePanel panel = new SimplePanel();
	private MetaDataDetailDisplay metaDataDisplay;
	private AddEditAuthorsDisplay addEditAuthorsDisplay;
	private AddEditMeasureTypeDisplay addEditMeasureTypeDisplay;
	private ManageMeasureDetailModel currentMeasureDetail;
	private ManageAuthorsModel currentAuthorsList;
	private ManageMeasureTypeModel currentMeasureTypeList;
	private List<Author> authorList = new ArrayList<Author>();
	private List<MeasureType> measureTypeList = new ArrayList<MeasureType>();
	private SimplePanel emptyWidget = new SimplePanel();
	private HasVisible previousContinueButtons;
	private long lastRequestTime;
	private int maxEmeasureId;
	private boolean editable = false;
	
	
	public MetaDataPresenter(MetaDataDetailDisplay mDisplay,AddEditAuthorsDisplay aDisplay,AddEditMeasureTypeDisplay mtDisplay,HasVisible pcButtons,ListBoxCodeProvider lp){
		super(mDisplay,aDisplay,mtDisplay,lp);
		previousContinueButtons = pcButtons;
		this.metaDataDisplay = mDisplay;
		this.addEditAuthorsDisplay = aDisplay;
		this.addEditMeasureTypeDisplay = mtDisplay;
		HandlerManager eventBus = MatContext.get().getEventBus();
		metaDataDisplay.getEditAuthorsButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				displayAddEditAuthors();
			}
		});
		
		metaDataDisplay.getEditMeasureTypeButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				displayAddEditMeasureType();
			}
		});
		
		addEditAuthorsDisplay.getCancelButton().addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				addEditAuthorsDisplay.getAuthorInputBox().setValue("");
				addEditAuthorsDisplay.hideTextBox();
			}
		});
		addEditAuthorsDisplay.getReturnButton().addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				backToDetail();
			}
		});
		
		addEditAuthorsDisplay.getSaveButton().addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				if(addEditAuthorsDisplay.getAuthor().equals(MatContext.PLEASE_SELECT)) {
					//do nothing
				}
				else if(!addEditAuthorsDisplay.getAuthor().startsWith("Other")){
					if(!addEditAuthorsDisplay.getAuthor().equals("")){
						addToAuthorsList(addEditAuthorsDisplay.getAuthor());
				    	addEditAuthorsDisplay.getAuthorInputBox().setValue("");
					}
				}else{
					  if(!addEditAuthorsDisplay.getOtherAuthor().getValue().equals("")){
						  addToAuthorsList(addEditAuthorsDisplay.getOtherAuthor().getValue());
						  addEditAuthorsDisplay.getOtherAuthor().setValue("");
					  }
				}
			}
		});
		addEditAuthorsDisplay.getRemoveButton().addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
			     removeSelectedAuthor();	 
			}
		});
		addEditMeasureTypeDisplay.getCancelButton().addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				addEditMeasureTypeDisplay.getMeasureTypeInputBox().setValue("");
				addEditMeasureTypeDisplay.hideTextBox();
			}
		});
		addEditMeasureTypeDisplay.getReturnButton().addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				backToDetail();
			}
		});
		
		
		addEditMeasureTypeDisplay.getSaveButton().addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				if(addEditMeasureTypeDisplay.getMeasureType().equals(MatContext.PLEASE_SELECT)) {
					//do nothing
				}
				else if(!(addEditMeasureTypeDisplay.getMeasureType().startsWith("Other"))){
					if(!addEditMeasureTypeDisplay.getMeasureType().equals("")){
						addToMeasureTypeList(addEditMeasureTypeDisplay.getMeasureType());
						addEditMeasureTypeDisplay.getMeasureTypeInputBox().setValue("");
					}
				}else{
					if(!addEditMeasureTypeDisplay.getOtherMeasureType().getValue().equals("")){
						addToMeasureTypeList(addEditMeasureTypeDisplay.getOtherMeasureType().getValue());
						addEditMeasureTypeDisplay.getOtherMeasureType().setValue("");
					}
				}
					
			}
		});
		addEditMeasureTypeDisplay.getRemoveButton().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				removeSelectedMeasureType();
				
			}
		});
		addEditAuthorsDisplay.getAuthorInputBox().addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				String authorValue = event.getValue();
				String changedAuthorValue = addEditAuthorsDisplay.getAuthor();
				if(changedAuthorValue.startsWith("Other")){
					addEditAuthorsDisplay.showTextBox();
					}
				else{
					addEditAuthorsDisplay.hideTextBox();
				}
				
			}
		});
		addEditMeasureTypeDisplay.getMeasureTypeInputBox().addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
			    String measureType = addEditMeasureTypeDisplay.getMeasureType();
				if(measureType.startsWith("Other")){
					addEditMeasureTypeDisplay.showTextBox();
					}
				else{
					addEditMeasureTypeDisplay.hideTextBox();
				}
				
			}
		});
		
		//US 413. Added value change listener to show or clear out Steward Other text box based on the selection.
		metaDataDisplay.getMeasureStewardListBox().addValueChangeHandler(new ValueChangeHandler<String>() {
				
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {				
					String value = metaDataDisplay.getMeasureStewardValue();
					if(value.startsWith("Other")){
						metaDataDisplay.showOtherTextBox();
						}
					else{
						metaDataDisplay.hideOtherTextBox();
					}
					
				}
			});
		
		metaDataDisplay.getSaveButton().addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				saveMetaDataInformation(true);
			}
			
		});
		metaDataDisplay.getFocusPanel().addKeyDownHandler(new KeyDownHandler(){
			
			@Override
			public void onKeyDown(KeyDownEvent event) {
				//control-alt-s is save
				if(event.isAltKeyDown() && event.isControlKeyDown() && event.getNativeKeyCode()==83){
					saveMetaDataInformation(true);
				}
			}
		});
		//This event will be called when measure has been selected from measureLibrary
		eventBus.addHandler(MeasureSelectedEvent.TYPE, new MeasureSelectedEvent.Handler() {
			@Override
			public void onMeasureSelected(MeasureSelectedEvent event) {
				if(event.getMeasureId() != null) {
				    getMeasureDetail();
				}
				else {
					displayEmpty();
				}
			}
		});
		
		metaDataDisplay.getGenerateEmeasureIdButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				generateAndSaveNewEmeasureid();
			}
		});
		
		
		emptyWidget.add(new Label("No Measure Selected"));
	}
	
	private void generateAndSaveNewEmeasureid(){
		MeasureServiceAsync service = MatContext.get().getMeasureService();
		service.generateAndSaveMaxEmeasureId(currentMeasureDetail, new AsyncCallback<Integer>() {

			@Override
			public void onFailure(Throwable caught) {
				metaDataDisplay.getErrorMessageDisplay().setMessage(MatContext.get().getMessageDelegate().getGenericErrorMessage());
				MatContext.get().recordTransactionEvent(null, null, null, "Unhandled Exception: "+caught.getLocalizedMessage(), 0);
				
			}

			@Override
			public void onSuccess(Integer result) {
				maxEmeasureId = result.intValue();
				if(maxEmeasureId < 1000000){
					metaDataDisplay.setGenerateEmeasureIdButtonEnabled(false);
					metaDataDisplay.getEmeasureId().setValue(maxEmeasureId+"");
					((TextBox)metaDataDisplay.getEmeasureId()).setFocus(true);
				}
			}
			
		});
	}
	
/*
 * Commenting this method as generate EmeasureIdentifier has to save and return the saved value.	
 * private void pullCurrentEmeasureId(){
		MeasureServiceAsync service = MatContext.get().getMeasureService();
		service.getMaxEMeasureId(new AsyncCallback<Integer>(){
			@Override
			public void onFailure(Throwable caught) {
				metaDataDisplay.getErrorMessageDisplay().setMessage(MatContext.get().getMessageDelegate().getGenericErrorMessage());
				MatContext.get().recordTransactionEvent(null, null, null, "Unhandled Exception: "+caught.getLocalizedMessage(), 0);
			}
			@Override
			public void onSuccess(Integer result) {
				maxEmeasureId = result.intValue()+1;
				if(maxEmeasureId < 1000000){
					metaDataDisplay.setGenerateEmeasureIdButtonEnabled(false);
					metaDataDisplay.getEmeasureId().setValue(maxEmeasureId+"");
					((TextBox)metaDataDisplay.getEmeasureId()).setFocus(true);
				}
			}
		});
	}
	*/
	
	private void setAuthorsListOnView() {
		Collections.sort(currentMeasureDetail.getAuthorList(), new Author.Comparator());
		if(currentMeasureDetail.getAuthorList()!= null){
			currentAuthorsList = new ManageAuthorsModel(currentMeasureDetail.getAuthorList());
			currentAuthorsList.setPageSize(SearchView.PAGE_SIZE_ALL);
			addEditAuthorsDisplay.buildDataTable(currentAuthorsList);
			
		}
		
	}
	
	private void setMeasureTypeOnView(){
		Collections.sort(currentMeasureDetail.getMeasureTypeList(), new MeasureType.Comparator());
		if(currentMeasureDetail.getMeasureTypeList()!= null){
			currentMeasureTypeList = new ManageMeasureTypeModel(currentMeasureDetail.getMeasureTypeList());
			currentMeasureTypeList.setPageSize(SearchView.PAGE_SIZE_ALL);
			addEditMeasureTypeDisplay.buildDataTable(currentMeasureTypeList);
		}	    
	}
	
	protected void removeSelectedMeasureType() {
		List<MeasureType> selectedMt = currentMeasureTypeList.getSelectedMeasureType();
		for(MeasureType mt: selectedMt){
			currentMeasureDetail.getMeasureTypeList().remove(mt);
		}
		metaDataDisplay.setMeasureTypeList(currentMeasureDetail.getMeasureTypeList());
		setMeasureTypeOnView();
	}

	protected void removeSelectedAuthor() {
		List<Author> selectedAuthor = currentAuthorsList.getSelectedAuthor();
		for(Author a: selectedAuthor){
			currentMeasureDetail.getAuthorList().remove(a);
		}
		metaDataDisplay.setAuthorsList(currentMeasureDetail.getAuthorList());
		setAuthorsListOnView();
		
	}

	public Widget getWidget() {
		return panel;
	}
	
	private void displayEmpty() {
		previousContinueButtons.setVisible(false);
		panel.clear();
		panel.add(emptyWidget);
	}
	
	public void displayDetail(){
		previousContinueButtons.setVisible(true);
		prepopulateFields();
		panel.clear();
		if(editable){
			if("0".equals(metaDataDisplay.getEmeasureId().getValue())){
				metaDataDisplay.setGenerateEmeasureIdButtonEnabled(true);
				metaDataDisplay.getEmeasureId().setValue("");
			}else if(metaDataDisplay.getEmeasureId() != null){
				metaDataDisplay.setGenerateEmeasureIdButtonEnabled(false);
			}
		}else{
			metaDataDisplay.setGenerateEmeasureIdButtonEnabled(false);
			if("0".equals(metaDataDisplay.getEmeasureId().getValue())){
				metaDataDisplay.getEmeasureId().setValue("");
			}
		}
		
		panel.add(metaDataDisplay.asWidget());
	}
	
	private void backToDetail(){
		previousContinueButtons.setVisible(true);
		panel.clear();
		panel.add(metaDataDisplay.asWidget());
		Mat.focusSkipLists("MeasureComposer");
	}
	
	private void prepopulateFields() {
		metaDataDisplay.getNqfId().setValue(currentMeasureDetail.getNqfId());
		metaDataDisplay.geteMeasureIdentifier().setText(currentMeasureDetail.getMeasureSetId());
		metaDataDisplay.getSetName().setValue(currentMeasureDetail.getGroupName());
		metaDataDisplay.getMeasureName().setText(currentMeasureDetail.getName());
		metaDataDisplay.getShortName().setText(currentMeasureDetail.getShortName());
		metaDataDisplay.getMeasureScoring().setText(currentMeasureDetail.getMeasScoring());
		metaDataDisplay.getClinicalRecommendation().setValue(currentMeasureDetail.getClinicalRecomms());
		metaDataDisplay.getDefinitions().setValue(currentMeasureDetail.getDefinitions());
		metaDataDisplay.getDescription().setValue(currentMeasureDetail.getDescription());
		
		metaDataDisplay.getDisclaimer().setValue(currentMeasureDetail.getDisclaimer());
		metaDataDisplay.getRiskAdjustment().setValue(currentMeasureDetail.getRiskAdjustment());
		metaDataDisplay.getRateAggregation().setValue(currentMeasureDetail.getRateAggregation());
		metaDataDisplay.getInitialPatientPop().setValue(currentMeasureDetail.getInitialPatientPop());
		metaDataDisplay.getDenominator().setValue(currentMeasureDetail.getDenominator());
		metaDataDisplay.getDenominatorExclusions().setValue(currentMeasureDetail.getDenominatorExclusions());
		metaDataDisplay.getNumerator().setValue(currentMeasureDetail.getNumerator());
		metaDataDisplay.getNumeratorExclusions().setValue(currentMeasureDetail.getNumeratorExclusions());
		metaDataDisplay.getDenominatorExceptions().setValue(currentMeasureDetail.getDenominatorExceptions());
		metaDataDisplay.getMeasurePopulation().setValue(currentMeasureDetail.getMeasurePopulation());
		metaDataDisplay.getMeasureObservations().setValue(currentMeasureDetail.getMeasureObservations());

				
		metaDataDisplay.getCopyright().setValue(currentMeasureDetail.getCopyright());
		if(currentMeasureDetail.getEndorseByNQF()!= null && currentMeasureDetail.getEndorseByNQF().equals(true)){
			metaDataDisplay.getEndorsebyNQF().setValue(true);
		}else{
			metaDataDisplay.getNotEndorsebyNQF().setValue(true);
		}
		metaDataDisplay.getMeasureStatus().setValueMetadata(currentMeasureDetail.getMeasureStatus());
		metaDataDisplay.getGuidance().setValue(currentMeasureDetail.getGuidance());
		metaDataDisplay.getTransmissionFormat().setValue(currentMeasureDetail.getTransmissionFormat());
		metaDataDisplay.getImprovementNotation().setValue(currentMeasureDetail.getImprovNotations());
		metaDataDisplay.getSupplementalData().setValue(currentMeasureDetail.getSupplementalData());
		metaDataDisplay.getFinalizedDate().setText(currentMeasureDetail.getFinalizedDate());
		metaDataDisplay.getMeasurementFromPeriodInputBox().setValue(currentMeasureDetail.getMeasFromPeriod());
		metaDataDisplay.getMeasurementToPeriodInputBox().setValue(currentMeasureDetail.getMeasToPeriod());		
		metaDataDisplay.getVersionNumber().setText(currentMeasureDetail.getVersionNumber());
		
		//US 413. Populate Steward and Steward Other value if any.
		String steward= currentMeasureDetail.getMeasSteward();
		metaDataDisplay.getMeasureSteward().setValueMetadata(currentMeasureDetail.getMeasSteward());
		boolean setSteward = steward != null && steward.equalsIgnoreCase("Other"); 
		if(setSteward){
			metaDataDisplay.showOtherTextBox();			
			metaDataDisplay.getMeasureStewardOther().setValue(currentMeasureDetail.getMeasStewardOther());
			
		}else{
			metaDataDisplay.hideOtherTextBox();
		}
		
		metaDataDisplay.getRationale().setValue(currentMeasureDetail.getRationale());
		metaDataDisplay.getStratification().setValue(currentMeasureDetail.getStratification());
		metaDataDisplay.getRiskAdjustment().setValue(currentMeasureDetail.getRiskAdjustment());
		if(currentMeasureDetail.getAuthorList() != null){
		metaDataDisplay.setAuthorsList(currentMeasureDetail.getAuthorList());
		}
		authorList = currentMeasureDetail.getAuthorList();
		if(currentMeasureDetail.getMeasureTypeList()!= null){
			metaDataDisplay.setMeasureTypeList(currentMeasureDetail.getMeasureTypeList());
		}
		measureTypeList = currentMeasureDetail.getMeasureTypeList();
		editable = MatContext.get().getMeasureLockService().checkForEditPermission();
		if(currentMeasureDetail.getReferencesList()!= null){
			metaDataDisplay.setReferenceValues(currentMeasureDetail.getReferencesList(), editable);
		}
		metaDataDisplay.setAddEditButtonsVisible(editable);
		ReadOnlyHelper.setReadOnlyForCurrentMeasure(metaDataDisplay.asWidget(),editable);
		metaDataDisplay.enableEndorseByRadioButtons(editable);
		metaDataDisplay.setSaveButtonEnabled(editable);
		metaDataDisplay.getEmeasureId().setValue(currentMeasureDetail.geteMeasureId()+"");
		//metaDataDisplay.getMaxEMeasureId()
		
	}

	private void saveMetaDataInformation(final boolean dispSuccessMsg){
		metaDataDisplay.getErrorMessageDisplay().clear();
		metaDataDisplay.getSuccessMessageDisplay().clear();
		updateModelDetailsFromView();
		if(MatContext.get().getMeasureLockService().checkForEditPermission()) {
			Mat.showLoadingMessage();
			MatContext.get().getSynchronizationDelegate().setSavingMeasureDetails(true);
			MatContext.get().getMeasureService().saveMeasureDetails(currentMeasureDetail, new AsyncCallback<SaveMeasureResult>() {
				
				@Override
				public void onSuccess(SaveMeasureResult result) {
					
					if(result.isSuccess()) {
						Mat.hideLoadingMessage();
						if(dispSuccessMsg){
							metaDataDisplay.getSuccessMessageDisplay().setMessage(MatContext.get().getMessageDelegate().getChangesSavedMessage());
						}
						currentMeasureDetail.setAuthorList(result.getAuthorList());
						currentMeasureDetail.setMeasureTypeList(result.getMeasureTypeList());
						MatContext.get().getSynchronizationDelegate().setSavingMeasureDetails(false);
					}
					else{
						//Window.alert("I FAIL1");
						Mat.hideLoadingMessage();
						MatContext.get().getSynchronizationDelegate().setSavingMeasureDetails(false);
						metaDataDisplay.getErrorMessageDisplay().setMessage(MessageDelegate.getMeasureSaveServerErrorMessage(  result.getFailureReason()));
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
					//Window.alert("I FAIL2");
					Mat.hideLoadingMessage();
					MatContext.get().getSynchronizationDelegate().setSavingMeasureDetails(false);
					metaDataDisplay.getErrorMessageDisplay().setMessage(caught.getLocalizedMessage());
				}
			});
		}
	}
	
	private void updateModelDetailsFromView(){
		currentMeasureDetail.setName(metaDataDisplay.getMeasureName().getText());
		currentMeasureDetail.setShortName(metaDataDisplay.getShortName().getText());
		currentMeasureDetail.setFinalizedDate(metaDataDisplay.getFinalizedDate().getText());
		currentMeasureDetail.setClinicalRecomms(metaDataDisplay.getClinicalRecommendation().getValue());
		currentMeasureDetail.setDefinitions(metaDataDisplay.getDefinitions().getValue());
		currentMeasureDetail.setDescription(metaDataDisplay.getDescription().getValue());
		currentMeasureDetail.setDisclaimer (metaDataDisplay.getDisclaimer().getValue());
		currentMeasureDetail.setRiskAdjustment(metaDataDisplay.getRiskAdjustment().getValue());
		currentMeasureDetail.setRateAggregation(metaDataDisplay.getRateAggregation().getValue());
		currentMeasureDetail.setInitialPatientPop(metaDataDisplay.getInitialPatientPop().getValue());
		currentMeasureDetail.setDenominator(metaDataDisplay.getDenominator().getValue());
		currentMeasureDetail.setDenominatorExclusions(metaDataDisplay.getDenominatorExclusions().getValue());
		currentMeasureDetail.setNumerator(metaDataDisplay.getNumerator().getValue());
		currentMeasureDetail.setNumeratorExclusions(metaDataDisplay.getNumeratorExclusions().getValue());
		currentMeasureDetail.setDenominatorExceptions(metaDataDisplay.getDenominatorExceptions().getValue());
		currentMeasureDetail.setMeasurePopulation(metaDataDisplay.getMeasurePopulation().getValue());
		currentMeasureDetail.setMeasureObservations(metaDataDisplay.getMeasureObservations().getValue());
		
		
		currentMeasureDetail.setCopyright(metaDataDisplay.getCopyright().getValue());
		currentMeasureDetail.setEndorseByNQF(metaDataDisplay.getEndorsebyNQF().getValue());
		currentMeasureDetail.setGuidance(metaDataDisplay.getGuidance().getValue());
		currentMeasureDetail.setTransmissionFormat(metaDataDisplay.getTransmissionFormat().getValue());
		currentMeasureDetail.setImprovNotations(metaDataDisplay.getImprovementNotation().getValue());
		currentMeasureDetail.setMeasFromPeriod(metaDataDisplay.getMeasurementFromPeriod());

		//US 413. Update Steward and Steward Other values from the UI. 		
		String stewardValue = metaDataDisplay.getMeasureStewardValue();
		if(nullCheck(stewardValue)){
			currentMeasureDetail.setMeasSteward(stewardValue);			
		}else{
			currentMeasureDetail.setMeasSteward(null);
		}
		currentMeasureDetail.setMeasStewardOther(metaDataDisplay.getMeasureStewardOtherValue());		
		currentMeasureDetail.setMeasToPeriod(metaDataDisplay.getMeasurementToPeriod());
		currentMeasureDetail.setSupplementalData(metaDataDisplay.getSupplementalData().getValue());
		if(nullCheck(metaDataDisplay.getMeasureStatusValue())){
			currentMeasureDetail.setMeasureStatus(metaDataDisplay.getMeasureStatusValue());}
		currentMeasureDetail.setRationale(metaDataDisplay.getRationale().getValue());
		currentMeasureDetail.setReferencesList(metaDataDisplay.getReferenceValues());
		currentMeasureDetail.setMeasureSetId(metaDataDisplay.geteMeasureIdentifier().getText());
		currentMeasureDetail.setGroupName(metaDataDisplay.getSetName().getValue());
		currentMeasureDetail.setStratification(metaDataDisplay.getStratification().getValue());
		currentMeasureDetail.setRiskAdjustment(metaDataDisplay.getRiskAdjustment().getValue());
		currentMeasureDetail.setVersionNumber(metaDataDisplay.getVersionNumber().getText());
		currentMeasureDetail.setAuthorList(authorList);
		currentMeasureDetail.setMeasureTypeList(measureTypeList);
		currentMeasureDetail.setNqfId(metaDataDisplay.getNqfId().getValue());
		if(metaDataDisplay.getEmeasureId().getValue() != null && !metaDataDisplay.getEmeasureId().getValue().equals("")){
			currentMeasureDetail.seteMeasureId(new Integer(metaDataDisplay.getEmeasureId().getValue()));
		}
	}
	
	
	
	private boolean nullCheck(String value){
		return  !value.equalsIgnoreCase("--Select--") && !value.equals("");
	}
	
	
	private void displayAddEditAuthors(){
		addEditAuthorsDisplay.setReturnToLink("Return to Previous");
		currentAuthorsList = new ManageAuthorsModel(currentMeasureDetail.getAuthorList());
		currentAuthorsList.setPageSize(SearchView.PAGE_SIZE_ALL);
		addEditAuthorsDisplay.buildDataTable(currentAuthorsList);
		panel.clear();
		panel.add(addEditAuthorsDisplay.asWidget());
		previousContinueButtons.setVisible(false);
		Mat.focusSkipLists("MeasureComposer");
	}
	
	private void displayAddEditMeasureType(){
		addEditMeasureTypeDisplay.setReturnToLink("Return to Previous");
		currentMeasureTypeList = new ManageMeasureTypeModel(currentMeasureDetail.getMeasureTypeList());
		currentMeasureTypeList.setPageSize(SearchView.PAGE_SIZE_ALL);
		
		addEditMeasureTypeDisplay.buildDataTable(currentMeasureTypeList);
		panel.clear();
		panel.add(addEditMeasureTypeDisplay.asWidget());
		previousContinueButtons.setVisible(false);	
		Mat.focusSkipLists("MeasureComposer");
	}

	private void addToAuthorsList(String selectedAuthor){
		Author author = new Author();
		author.setAuthorName(selectedAuthor);
		authorList.add(author);
		currentMeasureDetail.setAuthorList(authorList);
		metaDataDisplay.setAuthorsList(currentMeasureDetail.getAuthorList());
		setAuthorsListOnView();
		
	}
	
	private void addToMeasureTypeList(String selectedMeasureType){
		MeasureType mt = new MeasureType();
		mt.setDescription(selectedMeasureType);
		measureTypeList.add(mt);
		currentMeasureDetail.setMeasureTypeList(measureTypeList);
		metaDataDisplay.setMeasureTypeList(currentMeasureDetail.getMeasureTypeList());
		setMeasureTypeOnView();
	}


	@Override
	public void beforeDisplay() {
		if(MatContext.get().getCurrentMeasureId() == null ||
				MatContext.get().getCurrentMeasureId().equals("")) {
			displayEmpty();
		}
		else {
			currentMeasureDetail = null;
			lastRequestTime = System.currentTimeMillis();
			getMeasureDetail();
		}
		MeasureComposerPresenter.setSubSkipEmbeddedLink("MetaData");
		Mat.focusSkipLists("MeasureComposer");
		clearMessages();
	}
	@Override 
	public void beforeClosingDisplay() {
		if(currentMeasureDetail != null) {
			saveMetaDataInformation(false);
		}
		clearMessages();
	}
	

	private void getMeasureDetail(){
		MatContext.get().getMeasureService().getMeasure(MatContext.get().getCurrentMeasureId(), 
				new AsyncCallback<ManageMeasureDetailModel>(){
			final long callbackRequestTime = lastRequestTime;
			@Override
			public void onFailure(Throwable caught) {
				metaDataDisplay.getErrorMessageDisplay().setMessage(MatContext.get().getMessageDelegate().getGenericErrorMessage());
				MatContext.get().recordTransactionEvent(null, null, null, "Unhandled Exception: "+caught.getLocalizedMessage(), 0);
			}
			@Override
			public void onSuccess(ManageMeasureDetailModel result) {
				if(callbackRequestTime == lastRequestTime) {
					currentMeasureDetail = result;
					displayDetail();
					fireMeasureEditEvent();
				}
//				else {
//					// A new request for details has been made, ignore this one
//					Window.alert("Callback for old request");
//				}
			}
		});
	}
	
	private void fireMeasureEditEvent() {
		MeasureEditEvent evt = new MeasureEditEvent();
		MatContext.get().getEventBus().fireEvent(evt);
	}
	
	private void  clearMessages(){
		metaDataDisplay.getErrorMessageDisplay().clear();
		metaDataDisplay.getSuccessMessageDisplay().clear();
	}
	
	
}
