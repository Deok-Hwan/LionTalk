package Application.chattinglist;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

import Application.Method.CalTimeClass;
import Application.Method.FindMemberClass;
import Application.Method.MakeChatClass;
import Application.model.ChattingRoom;
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

public class ChattingController implements Initializable {
	@FXML ListView<HBox> listView;

	Customer customer = Customer.getCustomer() ;
	MakeChatClass makeChat = new MakeChatClass();
	FindMemberClass find = new FindMemberClass();

	List<ChattingRoom> list ;
	{
		list = new ArrayList<>(customer.getChattingRoom());

	}
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		try{
			for (int i = 0; i < list.size(); i++) {
				HBox hbox = (HBox) FXMLLoader.load(getClass().getResource("item.fxml"));
				ImageView image = (ImageView)hbox.lookup("#image");
				Label name = (Label)hbox.lookup("#name");
				Label talkName = (Label)hbox.lookup("#lastTalk");
				Label time = (Label)hbox.lookup("#time");
				Label num = (Label)hbox.lookup("#num");

				ChattingRoom chat = list.get(i);
//				image.setImage(new Image(getClass().getResource("../friendImage/"+chat.getImage()).toString()));
				String []str = chat.getChatId().split("/");
				String chatName ="";
				for (int j = 0; j < str.length; j++) {
					if(str[j].equals(customer.getId() )) continue;
					chatName += find.FindMemberById(str[j]).getName()+", ";
				}
				chatName = chatName.substring(0, chatName.length()-2);

				name.setText(chatName);
				if(chat.getMessageNum() == 0 || chat.getMessageNum() == -1){
					num.setVisible(false);
				}else{
					num.setVisible(true);		
				}
				num.setText(Integer.toString(chat.getMessageNum()));
				

				time.setText(CalTimeClass.CalTime((chat.getCal() )) );
			
				talkName.setText(chat.getChattingMessage().get(chat.getChattingMessage().size()-1).toString());


				listView.getItems().add(hbox);

			}
		}catch(Exception e){
			e.printStackTrace();
		}

		listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<HBox>() {

			@Override
			public void changed(ObservableValue<? extends HBox> observable, HBox oldValue, HBox newValue) {
				// TODO Auto-generated method stub
				listView.setOnMouseClicked(new EventHandler<MouseEvent>() {
					public void handle(MouseEvent event) {
						if(event.getButton().equals(MouseButton.PRIMARY)){
							if(event.getClickCount() == 2){
								
								makeChat.makeChatRoom(list.get(listView.getSelectionModel().
										getSelectedIndex()).getChatId());
							}
						};
					};
					
				});
				listView.setOnKeyPressed(new EventHandler<KeyEvent>() {
					public void handle(KeyEvent event) {
						if(event.getCode() == KeyCode.ENTER){
							makeChat.makeChatRoom(list.get(listView.getSelectionModel().
									getSelectedIndex()).getChatId());
						}
					};
				});
			}
		});
	}
	public static String toString(Calendar date){
		return date.get(Calendar.YEAR)+"년 "+(date.getMaximum(Calendar.MONTH)+1) +"월 " + date.get(Calendar.DATE)+"일";
	}


}
