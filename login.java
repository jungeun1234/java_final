import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;

// 몇몇 문제 때문에 login class안에 다 집어넣었습니다
// OOP가 서툴러서 죄송합니다...
public class login {

    // 통신을 위한 변수들 -----
    static final String serverIp = "192.168.0.14";
    static final int portNum = 9500;
    static public int getPortnum(){
        return portNum;
    }
    static String serverIP;

    static public String getServerIp() {
        return serverIP;
    }

    // login에서 '통신'이 먼저 이루어지기 떄문에 serverIP를 두번 입력받을 필요가 없도록
    // 이대로 Chatroom에서도 getServerIp라는 메소드를 이용해 받아갑니다.
    // 우선 통신이 2번 열리긴 합니다. 로그인에서 한 번 쳇룸에서 한 번.
    // 그렇게 하는 이유가 첫째, runnuble 의 사용법을 아직 완전히 익히지 못해서. 둘째, login.java가 닫히면 소켓통신이 끊기는지
    // 확인을 해보지 않아서.
    ObjectInputStream reader = null;
    ObjectOutputStream writer = null;
    public DefaultListModel listModel;
    Socket socket;
    // -------------------------
    public JFrame jf;
    JPanel cardPanel;
    login l;
    String id = null;
    CardLayout card; // 카드를 담는다

    public static void main(String[] args) {
        login l = new login();
        l.setFrame(l);
        l.service(); // 통신 부분

    }

    public void setFrame(login lpro) {
        jf = new JFrame();
        loginPanel l = new loginPanel(lpro); //로그인
        signupPanel sp = new signupPanel(lpro); //회원가입

        card = new CardLayout(); //'CardLayout'을 이용해 패널 화면 바뀌도록 설정

        cardPanel = new JPanel(card);
        cardPanel.add(l.mainPanel, "Login"); //카드패널중 로그인창
        cardPanel.add(sp.mainPanel, "Register");//카드패널중 회원가입창

        jf.add(cardPanel);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setSize(550, 700);
        jf.setVisible(true);


    }

    // [getConnection] 이 함수는 서버쪽에서 그대로 받아 사용합니다.
    // 이후에 모든 DB가 서버를 통해서만 통신하도록 수정되면 굳이 여기에 있을 필요가 없으므로
    // 서버나 핸들러 쪽에 옮겨두겠습니다.
    public Connection getConnection() throws SQLException {
        Connection con = null;
        String MySQlPW = "1215";
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/sampledb?serverTimezone=UTC", "root", MySQlPW);
        return con;
    }


    class loginPanel extends JPanel implements ActionListener {
        JPanel mainPanel;
        JTextField idTextField;
        JPasswordField passTextField;

        static String name = "login_nick";

        static public String getNAME() {
            return name;
        }

        String userMode = "일반";
        login l;
        Font font = new Font("회원가입", Font.BOLD, 40);
        String admin = "admin";

        public loginPanel(login l) {
            this.l = l;
            mainPanel = new JPanel();
            mainPanel.setLayout(new GridLayout(5, 1));

            JPanel centerPanel = new JPanel();
            JLabel loginLabel = new JLabel("HongTalk", JLabel.CENTER);
            loginLabel.setHorizontalAlignment(SwingConstants.CENTER);
            loginLabel.setFont(font);
            centerPanel.add(loginLabel);
            centerPanel.setBackground(Color.white);

            JPanel userPanel = new JPanel();
            userPanel.setBackground(Color.LIGHT_GRAY);

            JPanel gridBagidInfo = new JPanel(new GridBagLayout());
            //각 컴포넌트에 위치를 배치시켜준다.
            gridBagidInfo.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
            GridBagConstraints c = new GridBagConstraints();
            gridBagidInfo.setBackground(Color.pink);

            JLabel idLabel = new JLabel("ID : ");
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 0;
            gridBagidInfo.add(idLabel, c);

            idTextField = new JTextField(15);
            c.insets = new Insets(0, 5, 0, 0);
            c.gridx = 1;
            c.gridy = 0;
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
            JButton jb = (JButton) e.getSource();

            switch (e.getActionCommand()) {
                case "일반":
                    userMode = "일반";
                    break;

                case "관리자":
                    userMode = "관리자";
                    break;
                case "로그인":
                    String id = idTextField.getText();
                    String pass = passTextField.getText();

                    // 클라이언트 ->(쿼리 정보)-> 서버 -> 서버의 DB
                    // 서버의 DB를 통해 DB 접속이 이루어 집니다.


                    // 1. 정보 설정
                    String sql_query = String.format("SELECT password FROM student WHERE id = '%s' AND password = '%s'", id, pass);
                    //쿼리문 설정
                    InfoDTO dto = new InfoDTO();
                    dto.setCommand(Info.SENDDB);
                    dto.setMessage(sql_query);
                    // dto Command와 Message를 설정하는 부분입니다.


                    // 2. 전송
                    // 설정한 dto를 전송합니다.
                    // 원래 writer.writeObject(dto); / writer.flush(); 두 줄로 이루어졌으나 어쩐지... 익셉션(예외) 달라고해서...
                    try {
                        writer.writeObject(dto);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    try {
                        writer.flush();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }

                    // 3. 수신
                    // reader 용 dto 설정합니다.
                    try {
                        dto = (InfoDTO) reader.readObject();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    } catch (ClassNotFoundException ex) {
                        throw new RuntimeException(ex);
                    }

                    // 이 이후는 원래 로그인이 이루어지던 절차 그대로입니다.
                    if (pass.equals(dto.getMessage())) {

                        // 1. 정보 설정
                        sql_query = String.format("SELECT name FROM student WHERE id = '%s'", id);
                        dto = new InfoDTO();
                        dto.setCommand(Info.SENDDB);
                        dto.setMessage(sql_query);

                        // 2. 전송
                        try {
                            writer.writeObject(dto);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        try {
                            writer.flush();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }

                        // 3. 수신
                        try {
                            dto = (InfoDTO) reader.readObject();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        } catch (ClassNotFoundException ex) {
                            throw new RuntimeException(ex);
                        }
                        System.out.println(dto.getMessage());
                        // 받은 값 닉네임으로, 이 창 닫기, Chatroom 열기
                        name = dto.getMessage();
                        jf.dispose();
                        new Chatroom();
                    }
                    // 혹여나 통신이 실패했을 경우 뜨는 메시지 하나 정도 있어도 괜찮을 것 같습니다.
                    break;
            }
        }

    }

    // 회원가입은 아직 설정이 되어있지 않습니다.
    class signupPanel extends JPanel {
        JTextField idTF;
        JPasswordField passTF;
        JPasswordField passReTF;
        JTextField nameTF;
        JTextField yearTF;
        JTextField phoneTF;
        JPanel mainPanel;
        JPanel subPanel;
        JRadioButton menButton;
        JRadioButton girlButton;
        JButton registerButton;
        JButton back;
        Font font = new Font("회원가입", Font.BOLD, 40);

        String year = "", month = "", day = "";
        static String id = "", pass = "", passRe = "", name = "", gender = "", phone = "";

        login l;

        public signupPanel(login l) {
            this.l = l;
            subPanel = new JPanel();
            subPanel.setLayout(new GridBagLayout());
            subPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

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
            subPanel.add(new JLabel("영문+숫자+특수문자"), c); //보안설정

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
            back = new JButton("로그인 화면");
            back.setAlignmentX(Component.CENTER_ALIGNMENT);

            mainPanel.add(signupLabel);
            mainPanel.add(subPanel);
            mainPanel.add(registerButton);
            mainPanel.add(back);

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

                    String sql = "insert into student(id, password, name, birthday, gender, phoneNumber) values (?,?,?,?,?,?)";
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
            back.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    l.card.previous(l.cardPanel);
                }
            });

        }
    }


    public void service() {

        serverIP = JOptionPane.showInputDialog(null, "서버IP를 입력하세요", serverIp);
        if (serverIP == null || serverIP.length() == 0) {
            System.out.println("서버IP가 입력되지 않았습니다.");
            System.exit(0);
        }


        try {
            socket = new Socket(serverIP, portNum);
            reader = new ObjectInputStream(socket.getInputStream());
            writer = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("전송 준비 완료!");

        } catch (UnknownHostException e) {
            System.out.println("서버를 찾을 수 없습니다.");
            e.printStackTrace();
            System.exit(0);
        } catch (IOException e) {
            System.out.println("서버와 통신 불가.");
            e.printStackTrace();
            System.exit(0);
        }

        // run이 필요하지 않습니다.


    }

}