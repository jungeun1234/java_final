import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.JScrollPane;
import javax.swing.event.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




public class Chatroom extends JFrame {

    public JLabel chatNick;
    public String loginNick;
    JTextArea output;
    JTextField input;
    JButton sendBtn;
    Socket socket;
    ObjectInputStream reader = null;
    ObjectOutputStream writer = null;
    String nickName;
    public DefaultListModel listModel;

    //--------------------------------------UI START--------------------------------------------------//
    public Chatroom() {
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Toppanel Top = new Toppanel();
        add(Top, BorderLayout.NORTH);


        Middlepanel Middle = new Middlepanel();
        add(Middle, BorderLayout.CENTER);


        Bottompanel Bottom = new Bottompanel();

        add(Bottom, BorderLayout.SOUTH);


        setVisible(true);

        Bottom.service();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    //InfoDTO dto = new InfoDTO(nickName,Info.EXIT);
                    InfoDTO dto = new InfoDTO();
                    dto.setNickName(nickName);
                    dto.setCommand(Info.EXIT);
                    writer.writeObject(dto);
                    writer.flush();
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }
        });

    }

    public static void main(String[] args) {
        new Chatroom();
    }


    // ----------------------------------------- Top --------------------------------------- //
    class Toppanel extends JPanel {

        Toppanel() {
            setBackground(Color.gray);
            Top_right TR = new Top_right();
            setLayout(new BorderLayout());
            add(TR, BorderLayout.EAST);


        }

        class Top_right extends JPanel {
            Top_right() {
                setBackground(Color.gray);
                add(new JButton("쪽지보관함"));
                JButton memberInfo = new JButton("회원정보수정");

                memberInfo.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            new myPage();
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                });

                add(memberInfo);
                add(new JButton("로그아웃"));
            }
        }
    }

    // 회원 정보 수정 Dialog
    // 내장 DB와 연동되어 있습니다
    public class myPage {

        myPage() throws SQLException {
            // 1. 아무것도 입력하지 않을경우 걸러내기
            // 2. 취소를 누르면 NULL값이 됨
            // 3. 여기서 수정하면 pw가 일정한 형식(영어, 숫자, 특수문자 혼용)없이도 통과됨. 그거 확인 추가

            login l = new login();
            Connection con = null;
            PreparedStatement pstmt = null;

            con = l.getConnection();

            String id;


            String sql_query = String.format("SELECT id from student WHERE name = '%s'", nickName);
            Statement stmt = con.createStatement();

            ResultSet rset = stmt.executeQuery(sql_query);
            rset.next();

            id = rset.getString(1);


            String[] options = {"이름", "비밀번호", "생일", "탈퇴"};
            var selection = JOptionPane.showOptionDialog(null, "무엇을 수정하시겠습니까?", "내 정보 수정",
                    0, 3, null, options, options[0]);


            // 이름
            if (selection == 0) {
                String answer = JOptionPane.showInputDialog("수정할 이름을 입력하세요", nickName);
                String SQL = String.format("update student set name='%s' where id='%s'", answer, id);
                pstmt = con.prepareStatement(SQL);
                pstmt.executeUpdate();


                nickName = answer;
                chatNick.setText(nickName + " 님");

                con.close();
            }

            // 비밀번호
            if (selection == 1) {

                sql_query = String.format("SELECT password from student WHERE id = '%s'", id);
                stmt = con.createStatement();

                rset = stmt.executeQuery(sql_query);
                rset.next();

                String pw = rset.getString(1);


                String answer = JOptionPane.showInputDialog("수정할 비밀번호를 입력하세요", pw);

                String SQL = String.format("update student set password='%s' where id='%s'", answer, id);
                pstmt = con.prepareStatement(SQL);
                pstmt.executeUpdate();


                con.close();
            }

            // 생일
            if (selection == 2) {

                sql_query = String.format("SELECT birthday from student WHERE id = '%s'", id);
                stmt = con.createStatement();

                rset = stmt.executeQuery(sql_query);
                rset.next();

                String birth = rset.getString(1);

                String answer = JOptionPane.showInputDialog("수정할 생일을 입력하세요", birth);

                String SQL = String.format("update student set birthday='%s' where id='%s'", answer, id);
                pstmt = con.prepareStatement(SQL);
                pstmt.executeUpdate();

                con.close();
            }

            if (selection == 3) {
                String SQL = String.format("delete from student where id='%s'", id);
                pstmt = con.prepareStatement(SQL);

                String[] yes_no = {"예(Y)", "아니오(N)"};

                var unresign = JOptionPane.showOptionDialog(null, "탈퇴하시겠습니까?", "탈퇴",
                        0, 3, null, yes_no, yes_no[0]);
                if (unresign == 1){
                    new myPage();
                }
                if (unresign == 0) {
                    pstmt.executeUpdate();
                    pstmt = con.prepareStatement(SQL);
                }
                dispose();
                new login();
                l.setFrame(l);
                l.service();



            }
        }
    }

    // ----------------------------------------- Middle --------------------------------------- //
    class Middlepanel extends JPanel {

        Middlepanel() {
            setBackground(Color.white);
            setLayout(new BorderLayout());
            Middle_right MR = new Middle_right();
            add(MR, BorderLayout.EAST);
        }

        class Middle_right extends JPanel {

            Middle_right() {
                //-- 찾기 --//
                setLayout(new BorderLayout());


                JPanel Search = new JPanel();
                add(Search, BorderLayout.NORTH);


                //-----//
                setBackground(Color.blue);
                JTextField Search_T = new JTextField(10);
                Search.add(Search_T);

                JButton Search_B = new JButton("찾기");
                Search.add(Search_B);
                //-----//


                Middle_right_down MRD = new Middle_right_down();
                add(MRD, BorderLayout.CENTER);

            }

            class Middle_right_down extends JPanel {
                Middle_right_down() {
                    setBackground(Color.YELLOW);
                    add(new JLabel("대기방 접속자"));
                }
            }


        }
    }


    // ----------------------------------------- Bottom --------------------------------------- //
    class Bottompanel extends JPanel implements ActionListener, Runnable {


        Bottompanel() {
            loginNick = login.loginPanel.getNAME();
            setLayout(new BorderLayout());
            add((new Bottom_right()), BorderLayout.EAST);
            add((new Bottom_left()), BorderLayout.WEST);

        }

        class Bottom_right extends JPanel {
            Bottom_right() {
                setLayout(new BorderLayout());

                //add((new JLabel("친구")), BorderLayout.NORTH);
                add((new Online()), BorderLayout.CENTER);


            }



            public class Online extends JPanel
                    implements ListSelectionListener {
                private JList list;

                private JButton fireButton;
                private JTextField employeeName;

                public Online() {


                    super(new BorderLayout());

                    listModel = new DefaultListModel();
                    listModel.addElement(""); // list가 비었을때도 출력시키는 방법 찾아야해




                    //Create the list and put it in a scroll pane.
                    list = new JList(listModel);
                    list.setVisibleRowCount(5);
                    list.setFixedCellWidth(180);
                    JScrollPane listScrollPane = new JScrollPane(list);


                    add((new JLabel("온라인 접속자")), BorderLayout.NORTH);
                    add(listScrollPane, BorderLayout.CENTER);

                }


                @Override
                public void valueChanged(ListSelectionEvent e) {

                }
            }
        }







        class Bottom_left extends JPanel {
            Bottom_left() {
                setLayout(new BorderLayout());
                add((new Bottom_left_up()), BorderLayout.NORTH);
                add((new Bottom_left_mid()), BorderLayout.CENTER);
                add((new Bottom_left_down()), BorderLayout.SOUTH);

            }

            class Bottom_left_up extends JPanel {
                Bottom_left_up() {
                    add(new JButton("방만들기"));
                    add(new JLabel("방찾기"));
                    add(new JTextField(15));
                    add(new JButton("입력"));
                    add(new JButton("대기방 보기"));
                    add(new JButton("전체방 보기"));
                }
            }

            class Bottom_left_mid extends JPanel {
                Bottom_left_mid() {

                    output = new JTextArea(7, 50);
                    output.setEditable(false);
                    JScrollPane chat_scroll = new JScrollPane(output, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);


                    chat_scroll.setBounds(50, 50, 340, 330);
                    add(chat_scroll);


                }

            }

            class Bottom_left_down extends JPanel {
                Bottom_left_down() {
                    chatNick = new JLabel(loginNick);
                    add(chatNick);


                    input = new JTextField(55);
                    sendBtn = new JButton("입력");
                    add(input);
                    add(sendBtn);
                }
            }

        }

        //--------------------------------------UI END--------------------------------------------------//
        public void service() {

            // login 쪽에서 가져온 ServerIP&Portnum 입니다.
            String serverIP = login.getServerIp();
            int portNum = login.getPortnum();

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

            nickName = loginNick;
            chatNick.setText(loginNick + " 님");


            try {

                InfoDTO dto = new InfoDTO();
                dto.setCommand(Info.JOIN);
                dto.setNickName(nickName);

                writer.writeObject(dto);
                writer.flush();


            } catch (IOException e) {
                e.printStackTrace();
            }

            Thread t = new Thread(this);
            t.start();
            input.addActionListener(this);
            sendBtn.addActionListener(this);

        }








        //Runnable
        @Override
        public void run() {
            InfoDTO dto = null;
            while (true) {

                try {
                    dto = (InfoDTO) reader.readObject();

                    if (dto.getCommand() == Info.EXIT) {

                        listModel.removeElement(dto.getNickName());
                        reader.close();
                        writer.close();
                        socket.close();
                        System.exit(0);
                    } else if (dto.getCommand() == Info.SEND) {
                        output.append(dto.getMessage() + "\n");


                        int pos = output.getText().length();
                        output.setCaretPosition(pos);

                    } /*else if(dto.getCommand()==Info.PLUS){
                        listModel.addElement(dto.getMessage());
                    }else if(dto.getCommand()==Info.MINU){
                        listModel.removeElement(dto.getMessage());}*/



                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }


        //ActionPerformed
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String msg = input.getText();
                InfoDTO dto = new InfoDTO();

                if (msg.equals("exit")) {
                    dto.setCommand(Info.EXIT);
                } else {

                    msg = msg.trim(); //
                    if(!msg.equals("")){
                        dto.setCommand(Info.SEND);
                        dto.setMessage(msg);
                        dto.setNickName(nickName);
                    }

                }
                writer.writeObject(dto);
                writer.flush();
                input.setText("");

            } catch (IOException io) {
                io.printStackTrace();
            }
        }

    }




}
