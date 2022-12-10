import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

class ChatHandlerObject extends Thread //처리해주는 곳(소켓에 대한 정보가 담겨있는 곳. 소켓을 처리함)
{
    private ObjectInputStream reader;
    private ObjectOutputStream writer;
    private Socket socket;
    //private InfoDTO dto;
    ///private Info command;
    private List<ChatHandlerObject> list;

    //생성자
    public ChatHandlerObject(Socket socket, List<ChatHandlerObject> list) throws IOException {

        this.socket = socket;
        this.list = list;
        writer = new ObjectOutputStream(socket.getOutputStream());
        reader = new ObjectInputStream(socket.getInputStream());
        //순서가 뒤바뀌면 값을 입력받지 못하는 상황이 벌어지기 때문에 반드시 writer부터 생성시켜주어야 함!!!!!!

    }

    public void run() {
        InfoDTO dto = null;
        String nickName;
        try {
            while (true) {
                dto = (InfoDTO) reader.readObject();
                nickName = dto.getNickName();

                //System.out.println("배열 크기:"+ar.length);
                //사용자가 접속을 끊었을 경우. 프로그램을 끝내서는 안되고 남은 사용자들에게 퇴장메세지를 보내줘야 한다.
                if (dto.getCommand() == Info.EXIT) {
                    InfoDTO sendDto = new InfoDTO();
                    // InfoDTO minuDto = new InfoDTO();

                    //나가려고 exit를 보낸 클라이언트에게 답변 보내기
                    sendDto.setCommand(Info.EXIT);
                    writer.writeObject(sendDto);
                    writer.flush();

                    reader.close();
                    writer.close();
                    socket.close();
                    //남아있는 클라이언트에게 퇴장메세지 보내기
                    list.remove(this);

                    sendDto.setCommand(Info.SEND);
                    sendDto.setMessage(nickName + "님 퇴장하였습니다");
                    broadcast(sendDto);

                    //온라인 사용자 삭제
                    /*minuDto.setCommand(Info.MINU);
                    minuDto.setMessage(nickName);
                    broadcast(minuDto);*/


                    break;
                } else if (dto.getCommand() == Info.JOIN) {
                    //모든 사용자에게 메세지 보내기
                    //모든 클라이언트에게 입장 메세지를 보내야 함
                    InfoDTO sendDto = new InfoDTO();
                    // InfoDTO plusDto = new InfoDTO();

                    sendDto.setCommand(Info.SEND);
                    sendDto.setMessage(nickName + "님 입장하였습니다");
                    broadcast(sendDto);

                    //온라인 사용자 추가
                    /*plusDto.setCommand(Info.PLUS);
                    plusDto.setMessage((nickName));
                    broadcast(plusDto);*/


                } else if (dto.getCommand() == Info.SEND) {
                    InfoDTO sendDto = new InfoDTO();
                    sendDto.setCommand(Info.SEND);
                    sendDto.setMessage("[" + nickName + "] : " + dto.getMessage());
                    broadcast(sendDto);


                    // DB를 주고 받는 부분
                } else if (dto.getCommand() == Info.SENDDB) {
                    InfoDTO sendDto = new InfoDTO();

                    // 1. DB 접속
                    login l = new login();
                    String sql_query = dto.getMessage();
                    Connection con = l.getConnection();
                    Statement stmt = con.createStatement();

                    // 2. 쿼리문 적용 (셀렉문과 업데이트 etc... 로 나누려고 함)
                    // 왜냐면 업데이트/삭제 문은 응답이 필요하지 않기 때문 T/F 응답이면 될것같아서

                    // 2-1. 셀렉문인 경우
                    if (sql_query.contains("SELECT")) {
                        ResultSet rset = stmt.executeQuery(sql_query);
                        rset.next();

                        // 3. 결과 재전송
                        sendDto.setCommand(Info.SENDDB);
                        sendDto.setMessage(rset.getString(1));
                        writer.writeObject(sendDto);
                        writer.flush();
                    }
                    // 4. DB 접속 끊기
                    con.close();

                }
            }//while

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    //다른 클라이언트에게 전체 메세지 보내주기
    public void broadcast(InfoDTO sendDto) throws IOException {
        for (ChatHandlerObject handler : list) {
            handler.writer.writeObject(sendDto); //핸들러 안의 writer에 값을 보내기
            handler.writer.flush();  //핸들러 안의 writer 값 비워주기

        }
    }
}