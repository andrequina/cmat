package mat.client.shared;

import mat.client.Enableable;
import mat.client.shared.search.MATAnchor;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
 
public class PreviousContinueButtonBar extends Composite implements HasVisible, Enableable {
	private MATAnchor previousButton = new MATAnchor("");
	private MATAnchor continueButton = new MATAnchor("");
	
	public int state =0;
	public int subState = 0; //can be extended further
	
	public void setPageNamesOnState(){
		if(state<=0){
			state =0;
			if(subState == 0){
				setPageNames("UNDEFINED", "Clause Workspace");				
				buttonPanel.remove(previousButton);
				buttonPanel.remove(continueButton);
				buttonPanel.add(continueButton);
			}
		}
		else if(state ==1){
			
			setPageNames("Measure Details", "Measure Packager");
			
			buttonPanel.remove(previousButton);
			buttonPanel.remove(continueButton);
			buttonPanel.add(previousButton);
			buttonPanel.add(continueButton);
		}
		else if(state >=2){
			
			state =2;
			setPageNames("Clause Workspace", "UNDEFINED");
			buttonPanel.remove(previousButton);
			buttonPanel.remove(continueButton);
			buttonPanel.add(previousButton);
			
		}
	}
	
	public void  setPageNames(String previousPage, String nextPage){
		previousButton.setText("<< Go to "+previousPage);
		continueButton.setText("Go to "+ nextPage +" >>");
	}
	
	private FlowPanel buttonPanel = new FlowPanel();
	
	public PreviousContinueButtonBar() {
		continueButton.addStyleName("continueButton");
		
		
		SimplePanel sPanel = new SimplePanel();
		sPanel.addStyleName("clearBoth");
		buttonPanel.add(sPanel);
		initWidget(buttonPanel);
		setPageNamesOnState();
	}
	
	public void setContinueButtonVisible(boolean visible) {
		MatContext.get().setVisible(continueButton, visible);
	}
	
	public void setPreviousButtonVisible(boolean visible) {
		MatContext.get().setVisible(previousButton, visible);
	}
	
	public HasClickHandlers getContinueButton() {
		return continueButton;
	}
	
	public HasClickHandlers getPreviousButton() {
		return previousButton;
	}
	
	public void setEnabled(boolean enabled){
		previousButton.setEnabled(enabled);
		continueButton.setEnabled(enabled);
	}
	
}
