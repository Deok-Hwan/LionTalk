package Application.profile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import Application.Login.LoginController;
import Application.model.Customer;
import Application.model.MessageFormat;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class profileController implements Initializable {
	Customer cus = Customer.getCustomer();

	Socket socket = LoginController.socket;
	ObjectInputStream ois = LoginController.ois;
	ObjectOutputStream oos = LoginController.oos;

	@FXML private ImageView img;
	@FXML private TextField name;
	@FXML private Label id;
	@FXML private TextArea talkName;

	@FXML private Button cancel;
	@FXML private Button make;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		Customer c = Customer.getCustomer();
		//		System.out.println(c.getName()+"1");
		name.setText(c.getName().trim());
		id.setText(c.getId());
		talkName.setText(c.getTalkName());
		img.setOnMouseClicked(new EventHandler<Event>() {
			@Override
			public void handle(Event event) {
				// TODO Auto-generated method stub
				handleBtnImg(null);
			}
		});

	}
	File selectedFile = null;

	public void handleBtnImg(ActionEvent e){
		Stage primaryStage = (Stage)img.getScene().getWindow();
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(
				new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));

		selectedFile = fileChooser.showOpenDialog(primaryStage);
		if (selectedFile != null) {

			Platform.runLater(()->{
				try {
					img.setImage(new Image(selectedFile.toURI().toURL().toExternalForm()));
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				}
			});
		}

	}
	public void handleMakeAction(ActionEvent e) { // messageFormat type 5

		try {
			oos.writeObject(new MessageFormat(cus.getId(), 5)); // message : id, type은 5

			if(selectedFile != null){
				name.appendText("　");//이미지 선택했다면  특수문자를 추가해서 서버가 알 수 있게한다.
			}
			cus.setName(name.getText());
			cus.setTalkName(talkName.getText());
			
			oos.writeObject(cus);
			
//			sendImage(selectedFile);

			handleBtnExitAction(null);

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	// client.. Image파일 전송
	private void sendImage(File image) throws Exception {

		BufferedWriter bw = LoginController.bw;
		bw.write(Customer.getCustomer().getId() + "\n"); 
		bw.flush();
		
		FileInputStream fis = new FileInputStream(image.getAbsolutePath());
		byte [] b = new byte [20000];
		fis.read(b);
		

		try{
			oos.writeObject(b);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void handleBtnExitAction(ActionEvent e) { // 취소 버튼
		Stage stage = (Stage) cancel.getScene().getWindow();
		stage.close();
	}
	@FXML
	public void exitApplication(ActionEvent event) {
		handleBtnExitAction(null);
	}
}