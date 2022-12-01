package project;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
public class login {
	JPanel cardPanel;
	login l;
	CardLayout card; // 카드를 담는다
	
	public static void main(String[] args) {
		login l=new login();
		l.setFrame(l);
	}
	public void setFrame(login lpro) {
		JFrame jf = new JFrame();
		loginPanel l = new loginPanel(lpro); //로그인
		signupPanel sp = new signupPanel(lpro); //회원가입
		
		card = new CardLayout(); //'CardLayout'을 이용해 패널 화면 바뀌도록 설정
		
		cardPanel = new JPanel(card);
		cardPanel.add(l.mainPanel,"Login"); //카드패널중 로그인창
		cardPanel.add(sp.mainPanel,"Register");//카드패널중 회원가입창
		
		jf.add(cardPanel);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setSize(550,700);
		jf.setVisible(true);
				
	}
	public Connection getConnection() throws SQLException{
		Connection con = null;
		con = DriverManager.getConnection("jdbc:mysql://localhost:3306/'mysql에서 설정한 db이름'?serverTimezone=UTC","mysql아이디","mysql비밀번호");
		
		return con;
	}

}
class loginPanel extends JPanel implements ActionListener{
	JPanel mainPanel;
	JTextField idTextField;
	JPasswordField passTextField;
	
	String userMode="일반";
	login l;
	Font font = new Font("회원가입",Font.BOLD,40);
	String admin = "admin";
	
	public loginPanel(login l) {
		this.l=l;
		mainPanel=new JPanel();
		mainPanel.setLayout(new GridLayout(5,1));
		
		JPanel centerPanel = new JPanel();
		JLabel loginLabel = new JLabel("HongTalk",JLabel.CENTER);
		loginLabel.setHorizontalAlignment(SwingConstants.CENTER);
		loginLabel.setFont(font);
		centerPanel.add(loginLabel);
		centerPanel.setBackground(Color.white);
		
		JPanel userPanel = new JPanel();
		userPanel.setBackground(Color.LIGHT_GRAY);
		
		JPanel gridBagidInfo=new JPanel(new GridBagLayout());
		//각 컴포넌트에 위치를 배치시켜준다.
		gridBagidInfo.setBorder(BorderFactory.createEmptyBorder(30,30,30,30));
		GridBagConstraints c = new GridBagConstraints();
		gridBagidInfo.setBackground(Color.pink);
		
		JLabel idLabel = new JLabel("ID : ");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx=0;
		c.gridy=0;
		gridBagidInfo.add(idLabel, c);
		
		idTextField = new JTextField(15);
		c.insets=new Insets(0,5,0,0);
		c.gridx=1;
		c.gridy=0;
		gridBagidInfo.add(idTextField, c);
		
		JLabel passLabel = new JLabel("PASSWORD : ");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		c.insets = new Insets(20, 0, 0, 0);
		gridBagidInfo.add(passLabel, c);
		
		passTextField = new JPasswordField(15);
		c.insets = new Insets(20, 5, 0, 0);
		c.gridx = 1;
		c.gridy = 1;
		gridBagidInfo.add(passTextField, c);
		
		JPanel loginPanel = new JPanel();
		JButton loginButton = new JButton("로그인");
		loginPanel.add(loginButton);
		
		JPanel signupPanel = new JPanel();
		JButton signupButton = new JButton("회원가입");
		loginPanel.add(signupButton);
		
		mainPanel.add(centerPanel);
		mainPanel.add(userPanel);
		mainPanel.add(gridBagidInfo);
		mainPanel.add(loginPanel);
		mainPanel.add(signupPanel);
		
		loginButton.addActionListener(this);
		
		signupButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				l.card.next(l.cardPanel); //회원가입버튼을 누르면 창 이동
				}
			});
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		JButton jb = (JButton)e.getSource();
		
		switch(e.getActionCommand()) {
		case"일반":
			userMode="일반";
			break;
			
		case"관리자":
			userMode="관리자";
			break;
		case"로그인":
			String id=idTextField.getText();
			String pass = passTextField.getText();
			
			try {
				String sql_query=String.format("SELECT password FROM user_info WHERE id = '%s' AND passwrod = '%s'",id, pass);
				//SELECT를 통해 로그인기능 구현
				Connection con = l.getConnection();
				Statement stmt = con.createStatement();
				
				ResultSet rset=stmt.executeQuery(sql_query);
				rset.next();
				
				if(pass.equals(rset.getString(1))) {
					JOptionPane.showMessageDialog(this, "Login Success","로그인 성공",1);
					
				}else
					JOptionPane.showMessageDialog(this, "Login Failed","로그인 실패",1);
			}catch(SQLException ex) {
				JOptionPane.showMessageDialog(this, "Login Failed","로그인 실패",1);
				System.out.println("SQLException"+ex);
			}
			break;
			
		}
	}
}

class signupPanel extends JPanel{
	JTextField idTF;
	JPasswordField passTF;
	JPasswordField passReTF;
	JTextField nameTF;
	JTextField yearTF;
	JTextField phoneTF;
	JPanel mainPanel;
	JPanel subPanel;
	JComboBox<String> monthComboBox;
	JComboBox<String> dayComboBox;
	JRadioButton menButton;
	JRadioButton girlButton;
	JButton registerButton;
	Font font = new Font("회원가입", Font.BOLD, 40);
	
	String year = "", month = "", day = "";
	String id = "", pass = "", passRe = "", name = "", gender = "", phone = "";
	login l;
	
	public signupPanel(login l) {
		this.l=l;
		subPanel=new JPanel();
		subPanel.setLayout(new GridBagLayout());
		subPanel.setBorder(BorderFactory.createEmptyBorder(25,25,25,25));
		
		
		JLabel idLabel = new JLabel("아이디 : ");
		JLabel passLabel = new JLabel("비밀번호 : ");
		JLabel passReLabel = new JLabel("비밀번호 재확인 : ");
		JLabel nameLabel = new JLabel("이름 : ");
		JLabel birthLabel = new JLabel("생년월일 : ");
		JLabel genderLabel = new JLabel("성별 : ");
		JLabel phoneLabel = new JLabel("핸드폰번호 : ");
		//회원 정보는 DB에 저장
		idTF = new JTextField(15);
		passTF = new JPasswordField(15);
		passReTF = new JPasswordField(15);
		nameTF = new JTextField(15);
		yearTF = new JTextField(4);
		phoneTF = new JTextField(11);
		
		monthComboBox = new JComboBox<String>(
				new String[] { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" });
		dayComboBox = new JComboBox<String>(new String[] { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10",
				"11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27",
				"28", "29", "30", "31" });
		
		menButton = new JRadioButton("남자");
		girlButton = new JRadioButton("여자");
		ButtonGroup genderGroup = new ButtonGroup();
		genderGroup.add(menButton);
		genderGroup.add(girlButton);
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(15, 5, 0, 0);

		c.gridx = 0;
		c.gridy = 0;
		subPanel.add(idLabel, c);

		c.gridx = 1;
		c.gridy = 0;
		subPanel.add(idTF, c); // 아이디

		c.gridx = 0;
		c.gridy = 1;
		subPanel.add(passLabel, c);

		c.gridx = 1;
		c.gridy = 1;
		subPanel.add(passTF, c); // 패스워드
		
		c.gridx = 2;
		c.gridy = 1; 
		subPanel.add(new JLabel("영문+숫자+특수문자"),c); //보안설정

		c.gridx = 0;
		c.gridy = 2;
		subPanel.add(passReLabel, c);

		c.gridx = 1;
		c.gridy = 2;
		subPanel.add(passReTF, c); // 패스워드 재확인

		c.gridx = 0;
		c.gridy = 3;
		subPanel.add(nameLabel, c);

		c.gridx = 1;
		c.gridy = 3;
		subPanel.add(nameTF, c); // 이름

		c.gridx = 0;
		c.gridy = 4;
		subPanel.add(birthLabel, c);

		c.gridx = 1;
		c.gridy = 4;
		c.weightx = 0.6;
		subPanel.add(yearTF, c); //생년

		c.gridx = 2;
		c.gridy = 4;
		c.weightx = 0.2;
		subPanel.add(monthComboBox, c);//월

		c.gridx = 3;
		c.gridy = 4;
		c.weightx = 0.2;
		subPanel.add(dayComboBox, c);//일

		c.gridx = 0;
		c.gridy = 5;
		subPanel.add(genderLabel, c);//성별

		c.gridx = 1;
		c.gridy = 5;
		subPanel.add(menButton, c);//남자

		c.gridx = 2;
		c.gridy = 5;
		subPanel.add(girlButton, c);//여자

		c.gridx = 0;
		c.gridy = 6;
		subPanel.add(phoneLabel, c);

		c.gridx = 1;
		c.gridy = 6;
		subPanel.add(phoneTF, c);//핸드폰번호
		
		mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		JLabel signupLabel = new JLabel("");
		signupLabel.setFont(font);
		signupLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		registerButton = new JButton("회원가입");
		registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		mainPanel.add(signupLabel);
		mainPanel.add(subPanel);
		mainPanel.add(registerButton);
		
		monthComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(e.getSource()==monthComboBox) {
					JComboBox monthBox=(JComboBox)e.getSource();
					month=(String)monthBox.getSelectedItem();
					System.out.println(month);
				}
			}
		});
		menButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				gender = e.getActionCommand();
			}
		});
		
		girlButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				gender = e.getActionCommand();
			}
		});
		registerButton.addActionListener(new ActionListener() { //회원가입버튼
			@Override
			public void actionPerformed(ActionEvent e) {
				id = idTF.getText();
				pass = new String(passTF.getPassword());
				passRe = new String(passReTF.getPassword());
				name = nameTF.getText();
				year = yearTF.getText();
				phone = phoneTF.getText();

				String sql = "insert into user_info(id, password, name, birthday, gender, phoneNumber) values (?,?,?,?,?,?)";
				//INSERT로 회원가입기능 구현
				//이후에 PreparedStatement를 이용해 값을 넘겨준다.

				Pattern passPattern1 = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*\\W).{8,20}$"); //8자 영문+특문+숫자
				Matcher passMatcher = passPattern1.matcher(pass);

				if (!passMatcher.find()) {
					JOptionPane.showMessageDialog(null, "비밀번호는 영문+특수문자+숫자 8자로 구성되어야 합니다", "비밀번호 오류", 1);
				} else if (!pass.equals(passRe)) {
					JOptionPane.showMessageDialog(null, "비밀번호가 서로 일치하지 않습니다", "비밀번호 오류", 1);

				} else {
					try {
						Connection con = l.getConnection();

						PreparedStatement pstmt = con.prepareStatement(sql);

						String date = yearTF.getText() + "-" + month + "-" + day;

						pstmt.setString(1, idTF.getText());
						pstmt.setString(2, pass);
						pstmt.setString(3, nameTF.getText());
						pstmt.setString(4, date);
						pstmt.setString(5, gender);
						pstmt.setString(6, phoneTF.getText());

						int r = pstmt.executeUpdate();
						System.out.println("변경된 row " + r);
						JOptionPane.showMessageDialog(null, "회원 가입 완료", "회원가입", 1);
						l.card.previous(l.cardPanel); //회원가입이 완료되면 다시 로그인창으로 이동
					} catch (SQLException e1) {
						System.out.println("SQL error" + e1.getMessage());
						if (e1.getMessage().contains("PRIMARY")) {
							JOptionPane.showMessageDialog(null, "아이디 중복", "아이디 중복 오류", 1);
						} else
							JOptionPane.showMessageDialog(null, "정보를 제대로 입력해주세요", "오류", 1);
					} 
				}
			}
		});
	}
}
