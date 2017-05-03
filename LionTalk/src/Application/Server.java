package Application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Application.MemberDao.MemberDao;
import Application.model.ChattingRoom;
import Application.model.Customer;
import Application.model.MessageFormat;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Server extends Application{

	ServerSocket serverSocket;
	Socket socket;

	ArrayList<Customer> customs = new ArrayList<>(); 

	MemberDao memDao;

	ExecutorService executeService;
	List<Client> connections = new Vector<Client>();

	void startServer(){

		executeService = Executors.newFixedThreadPool(20);

		//		initialize1();//1:
		//		initialize1_1();
		//		initialize2();//

		memDao = MemberDao.getInstance();

		try{
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress("192.168.0.109", 9980));
		}catch(Exception e){
			if(!serverSocket.isClosed()){stopServer();}
			e.printStackTrace();

			return;
		}

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Platform.runLater(()->{ //
				});
				while(true){
					try{
						Socket socket = serverSocket.accept(); // 
						String message = "[message : "+socket.getRemoteSocketAddress() +
								": " + Thread.currentThread().getName() +"]";
						Client client = new Client(socket);
						Platform.runLater(()->displayText("["+message+"]"));

					}catch(Exception e ){
						if(!serverSocket.isClosed()){stopServer(); }
						e.printStackTrace();
						break;
					}
				}
			}
		};
		executeService.submit(runnable); //
	}
	void stopServer(){

		try{ //
			Iterator<Client> iterator = connections.iterator();
			while(iterator.hasNext()){
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			} //

			if(serverSocket != null && !serverSocket.isClosed()){
				serverSocket.close();
			} // ServerSocket 
			if(executeService != null && !executeService.isShutdown()){
				executeService.shutdown();
			} // ExecuteService
		}catch(Exception e ){}
	}


	class Client{ //
		Socket socket;
		ObjectInputStream ois;
		ObjectOutputStream oos;

		InputStreamReader is;
		BufferedReader br;
		OutputStreamWriter op;
		PrintWriter pw;

		Connection conn ; // JDBC Connection

		String Id;


		public Client(Socket socket) {
			// TODO Auto-generated constructor stub
			this.socket = socket;
			System.out.println("클라이언트 연결 완료");

			try{
				is = new InputStreamReader(socket.getInputStream());
				br = new BufferedReader(is);

				op = new OutputStreamWriter(socket.getOutputStream());
				pw = new PrintWriter(op);
				ois= new ObjectInputStream(socket.getInputStream());
				oos = new ObjectOutputStream(socket.getOutputStream());

				Customer cus = (Customer)ois.readObject();

				if(!cus.getTalkName().equals("#")){ // 회원가입이 아니라면
					Iterator<Client> iter1 = connections.iterator();
					while(iter1.hasNext()){
						if(iter1.next().Id.equals(cus.getId()) ){
							cus = new Customer("##", "##"); // 이미 로그인 되어 있는 회원
							oos.writeObject(cus);
							return;
						}
					}
				}

				if(cus.getId().substring(cus.getId().length()-1,cus.getId().length()).equals("#"))
				{// 기존 회원 가입인지 확인
					String cusId = cus.getId().substring(0,cus.getId().length()-1); //아이디에 #뺴기

					try {
						conn = memDao.getConnection();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					boolean result = memDao.IsUserChk(cusId);
					if(result == true){ // 기존에 회원이 있음
						cus = new Customer("#", "#");
						oos.writeObject(cus);
					}else{ // 가입 가능한 아이디
						cus.setId(cusId);

						// db에 회원 아이디 실패라면
						if(!memDao.insertCustomer(cus)) {
							cus = new Customer("#", "#");
							oos.writeObject(cus);
						}else{ // db 삽입 성공 후 클라이언트에게 결과 리턴
							oos.writeObject(cus);
							//						StopClient(this); // flag
							//						stopServer();
						}
					}

					return ;
				}else{ // 로그인

					connections.add(this);
					Platform.runLater(()->displayText("[connection 개수: "+connections.size()+"]"));

					String userid = cus.getId();
					String pwd = cus.getPw();

					try {
						conn = memDao.getConnection();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					// 사용자로 부터 로그인 정보를 받은 후 DB에 회원 정보를 가지고 옴
					int result =
							memDao.userCheck(userid, pwd);

					if(result ==1 ){ // 로그인 성공
						this.Id = userid;

						customs = memDao.ServerInit(); // 서버에서 모든 사용자의 정보를 가지고 있음

						cus = memDao.getMember(userid); // 로그인한 해당 사용자 customer 할당  + 고객 정보 확인 

						ArrayList<String> list = memDao.CusFriInit(userid); // 로그인한 친구 리스트 목록을 가져 온다. + 확인
						for (String string : list) {
							cus.getFriendList().add(FindMember(string));
						}
						cus.setChattingRoom(memDao.CusChatInit(userid)); 

						Iterator<ChattingRoom> iter = cus.getChattingRoom().iterator();

						while(iter.hasNext()){ // 
							ChattingRoom a = iter.next();
							a.chattingMessage = memDao.CusMessageInit(a.getChatId());
						}

					}else if(result ==0){ // 비밀번호 틀림
						cus = new Customer("#", userid);
					}else if(result == -1){ // 존재 하지 않는 회원
						cus = new Customer("#", "#");
					}
					oos.writeObject(cus);//

					System.out.println("flag2");
					receive();
				}

			}
			catch(IOException e)
			{
				StopClient(this);
				e.printStackTrace();
			} 
			catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				StopClient(this);
				e.printStackTrace();
			}
		}
		void receive(){
			Runnable runnable = new Runnable(){
				@Override
				public void run() {
					try{
						while(true)
						{

							String message ="[클라이언트 연결 개수 : "+
									connections.size()+"]";
							Platform.runLater(()->displayText(message));

							MessageFormat obj = null;
							int type = -1;
							try {
								obj = (MessageFormat)ois.readObject();
								System.out.println("입력 받은 메시지 : "+ obj.getMessage());
								type = obj.getType();

							} catch (ClassNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							switch(type){
							case 1:
								break;
							case 2: // 

								System.out.println(obj.getChatId()+" "+obj.getMessage());
								UpdateChat((MessageFormat)obj); //

								String id[] = obj.getChatId().split("/");


								for (Client client : connections) {
									for (int i = 0; i < id.length; i++) {
										System.out.println("client 아이디 : "+client.Id);
										if(client.Id.equals(id[i])){
											System.out.println(id[i]+"에게 보낼 메시지");
											client.send((MessageFormat)obj);
										}
									}
								}
								break;
							case 3 :
								oos.writeObject(new MessageFormat("",3));
								Customer cus3 = memDao.getMember(obj.getChatId()); // 상대방 아이디 찾음
								oos.writeObject(cus3); 

								if(!cus3.getId().equals("#")) // 사용자 존재 하지 않음
								UpdateFnd(obj.getMessage(), cus3.getId());

								// message에 찾고자 하는 id가 있음
								// id로 찾은 customer 리턴
								break;

							case 4 :
								String str4[] = obj.getChatId().split("#");
								System.out.println(str4[0]+" "+str4[1]);
								oos.writeObject(new MessageFormat("",4));
								Customer cus4 = memDao.getMember(str4[0], str4[1]);
								oos.writeObject(cus4); 

								UpdateFnd(obj.getMessage(), cus4.getId());


								// message에 찾고자 하는 id가 있음
								// id로 찾은 customer 리턴
								break;
							case 5 :
								System.out.println("case5 들어옴"); 
								Customer cus  = (Customer)ois.readObject(); // 
//								MakeImage(); // 이미지 정보 받음
								
								UpdateProfile(cus);
								break;

							case -1 :
								StopClient(Client.this);
								System.out.println("클라이언트 통신 안전한 종료!");
								break;
							}
						}
					}catch(IOException e){
						try {
							System.out.println("익셉션 확인");
							
							StopClient(Client.this);
							socket.close();
							return;
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}catch(Exception e){
						try{
							StopClient(Client.this);
							socket.close();
						}catch(IOException e2){}
					}

				}

				public void MakeImage() { // server , Image receive
					// TODO Auto-generated method stub
					try{

						String imageFileName = br.readLine();
						File createdFile = new File("C:/Users/user/Desktop/lion_talk개발/Profiles/", imageFileName); 
						
						byte [] b = (byte[])ois.readObject();

						FileOutputStream fos = new FileOutputStream(createdFile);
						fos.write(b);

						System.out.println("THE END --- makeImage");
					}catch(Exception e){
						e.printStackTrace();
					}
				}

			};

			executeService.submit(runnable);
		}
		void send(MessageFormat data){
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try{
						oos.writeObject(data);
						System.out.println(data+" 보냄");

					}catch(Exception e){
						try{
							e.printStackTrace();
							socket.close();
						}catch(IOException e2){}
					}

				}
			};
			executeService.submit(runnable); 
		}
	}
	void StopClient(Client c){
		connections.remove(c);
		String message ="[클라이언트 통신 종료 : "+
				c.socket.getRemoteSocketAddress() +
				": "+Thread.currentThread().getName()+" "+connections.size()+"]";
		Platform.runLater(()->displayText(message));
		try {
			c.socket.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	Customer FindMember(Customer cus){
		Iterator<Customer> iter = customs.iterator();

		while(iter.hasNext()){
			Customer c = iter.next();
			if(c.getId().equals(cus.getId()) && c.getPw().equals(cus.getPw())){ 
				return c;
			}
		}
		return new Customer("#","#","#");
	}
	Customer FindMember(String cusId){
		Iterator<Customer> iter = customs.iterator();

		while(iter.hasNext()){
			Customer c = iter.next();
			if(c.getId().equals(cusId) ){ 
				return c;
			}
		}
		System.out.println("찾는 멤버 없음");
		return new Customer("#","#","#");
	}

	TextArea txtDisplay;
	Button btnStartStop;
	@Override

	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		BorderPane root = new BorderPane();
		root.setPrefSize(500, 300);

		txtDisplay = new TextArea();
		txtDisplay.setEditable(false);
		BorderPane.setMargin(txtDisplay, new Insets(0,0,2,0));
		root.setCenter(txtDisplay);

		btnStartStop = new Button("start");
		btnStartStop.setPrefHeight(30);
		btnStartStop.setMaxHeight(Double.MAX_VALUE);

		startServer();

		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("app.css").toString());
		primaryStage.setScene(scene);
		primaryStage.setTitle("Server");
		primaryStage.setOnCloseRequest(event->stopServer());
		primaryStage.show();
	}
	void displayText(String text){
		txtDisplay.appendText(text+ "\n");
	}
	void UpdateChat(MessageFormat message){
		if(memDao.IsChatId(message.getChatId()) == false){ // 기존에 대화가 없는 방일 경우
			memDao.InputChat(message);
		}
		memDao.InputMessage(message); // 새로운 메시지 db에 저장
		memDao.UpdateChatTime(message); // 채팅방 시간 업데이트 
	}
	private void UpdateProfile(Customer cus) {
		memDao.updateProfile(cus);
	}
	void UpdateFnd(String from, String to){
		memDao.updateFnd(from, to);
	}
	public static void main(String[] args) {
		launch(args);
	}
}

