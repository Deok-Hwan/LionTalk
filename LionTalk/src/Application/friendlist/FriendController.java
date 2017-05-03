package Application.friendlist;


import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import Application.Method.MakeChatClass;
import Application.Method.SortClass;
import Application.model.Customer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

public class FriendController implements Initializable {
	@FXML ListView<HBox> listView;
	
	Customer customer = Customer.getCustomer() ;
	SortClass sort = new SortClass();
	MakeChatClass make = new MakeChatClass();
	
	List<Customer> list ;
	{
		list = customer.getFriendList();
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		try{
			for (int i = 0; i < list.size(); i++) {
				HBox hbox = (HBox) FXMLLoader.load(getClass().getResource("item.fxml"));
				ImageView image = (ImageView)hbox.lookup("#image");
				Label name = (Label)hbox.lookup("#name");
				Label talkName = (Label)hbox.lookup("#talkName");
				
				Customer friend = list.get(i);
				name.setText(friend.getName());
				
				talkName.setText(friend.getTalkName());
				
				listView.getItems().add(hbox);
				
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<HBox>() {
		
			@Override
			public void changed(ObservableValue<? extends HBox> observable, HBox oldValue, HBox newValue) {
				// TODO Auto-generated method stub
				ImageView image = (ImageView)newValue.lookup("#image");
				Label model = (Label) newValue.lookup("#name");
				Label descr = (Label) newValue.lookup("#talkName");
				listView.setOnMouseClicked(new EventHandler<MouseEvent>() {
					public void handle(MouseEvent event) {
						if(event.getButton().equals(MouseButton.PRIMARY)){
							if(event.getClickCount() == 2){
								int num = listView.getSelectionModel().getSelectedIndex();
								make.makeChatRoom(sort.SortChatId(customer.getId()+
										"/"+list.get(num).getId()));
							}
						};
					};
				});
				listView.setOnKeyPressed(new EventHandler<KeyEvent>() {
					public void handle(KeyEvent event) {
						if(event.getCode() == KeyCode.ENTER){
							int num = listView.getSelectionModel().getSelectedIndex();
							make.makeChatRoom(sort.SortChatId(customer.getId()+
									"/"+list.get(num).getId()));
						}
					};
				});
			}
		});
		
	}
	
	
}
