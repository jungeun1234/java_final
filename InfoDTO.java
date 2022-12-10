import java.util.*;
import java.io.*;

enum Info {
    JOIN, EXIT, SEND, SENDDB, PLUS, MINU
    // SENDDB - DB 정보를 주고 받는 것
    // PLUS / MINU - 접속중인 사용자 관련 정보
}

class InfoDTO implements Serializable{
    private String nickName;
    private String message;
    private Info command;

    public String getNickName(){
        return nickName;
    }
    public Info getCommand(){
        return command;
    }
    public String getMessage(){
        return message;
    }
    public void setNickName(String nickName){
        this.nickName= nickName;
    }
    public void setCommand(Info command){
        this.command= command;
    }
    public void setMessage(String message){
        this.message= message;
    }
}