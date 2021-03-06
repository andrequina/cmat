package mat.client.myAccount;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mat.client.Mat;
import mat.client.MatPresenter;
import mat.client.myAccount.service.SaveMyAccountResult;
import mat.client.shared.ErrorMessageDisplayInterface;
import mat.client.shared.MatContext;
import mat.client.shared.SuccessMessageDisplayInterface;
import mat.shared.PasswordVerifier;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

public class ChangePasswordPresenter implements MatPresenter {
	public static interface Display {
		public HasValue<String> getPassword();
		public HasValue<String> getConfirmPassword();
		
		public HasClickHandlers getSubmit();
		public HasClickHandlers getReset();
		public ErrorMessageDisplayInterface getErrorMessageDisplay();
		public SuccessMessageDisplayInterface getSuccessMessageDisplay();
		
		public Widget asWidget();
	}
	
	private final Display display;
	private MyAccountModel myAccountModel;
	
	public ChangePasswordPresenter(Display displayArg) {
		this.display = displayArg;
		
		display.getReset().addClickHandler(new ClickHandler() {
			
			public void onClick(ClickEvent event) {
				clearValues();
			}
		});
		
		display.getSubmit().addClickHandler(new ClickHandler() {
			
			public void onClick(ClickEvent event) {
				try {
					submitChangePassword();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	@Override
	public Widget getWidget() {;
		return display.asWidget();
	}
	
	
	@Override
	public void beforeDisplay() {
		
		MatContext.get().getMyAccountService().getMyAccount(new AsyncCallback<MyAccountModel>() {
			
			public void onSuccess(MyAccountModel result) {
				myAccountModel = result;
			}
			
			public void onFailure(Throwable caught) {
				display.getErrorMessageDisplay().setMessage(MatContext.get().getMessageDelegate().getGenericErrorMessage());
				MatContext.get().recordTransactionEvent(null, null, null, "Unhandled Exception: "+caught.getLocalizedMessage(), 0);
			}
		});		
		clearValues();
		Mat.focusSkipLists("ChangePassword");
	}
	@Override 
	public void beforeClosingDisplay() {
		
	}
	
	
	private void clearValues() {
		display.getPassword().setValue("");
		display.getConfirmPassword().setValue("");
		display.getErrorMessageDisplay().clear();
		display.getSuccessMessageDisplay().clear();
	}
	private void submitChangePassword() throws IOException {

		PasswordVerifier verifier = new PasswordVerifier(
				myAccountModel.getLoginId(), 
				display.getPassword().getValue(), 
				display.getConfirmPassword().getValue());
		
		if(!verifier.isValid()) {
			display.getErrorMessageDisplay().setMessages(verifier.getMessages());
		}
		else {
			display.getErrorMessageDisplay().clear();
			MatContext.get().getMyAccountService().changePassword(display.getPassword().getValue(), 
					new AsyncCallback<SaveMyAccountResult>() {

						public void onFailure(Throwable caught) {
							display.getErrorMessageDisplay().setMessage(MatContext.get().getMessageDelegate().getGenericErrorMessage());
							MatContext.get().recordTransactionEvent(null, null, null, "Unhandled Exception: "+caught.getLocalizedMessage(), 0);
						}
						
						@Override
						public void onSuccess(SaveMyAccountResult result) {
							if(result.isSuccess()){
								clearValues();
								display.getSuccessMessageDisplay().setMessage( MatContext.get().getMessageDelegate().getPasswordSavedMessage());
							}else{
								List<String> messages = new ArrayList<String>();
								switch(result.getFailureReason()) {
									case SaveMyAccountResult.SERVER_SIDE_VALIDATION:
										messages = result.getMessages();
										break;
									case SaveMyAccountResult.DICTIONARY_EXCEPTION:
										messages.add(MatContext.get().getMessageDelegate().getMustNotContainDictionaryWordMessage());
										break;
									default:
										messages.add(MatContext.get().getMessageDelegate().getUnknownErrorMessage(result.getFailureReason()));
								}
								display.getErrorMessageDisplay().setMessages(messages);
								
							}
						}
					});
		}
		
	}
}
