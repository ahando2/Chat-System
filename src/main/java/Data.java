import java.io.Serializable;
import java.util.ArrayList;

public class Data implements Serializable {
    String message;
    int sysMessage;
    ArrayList<Integer> recipientId;
    ArrayList<Integer> clientsId;
    Data(){
        message = "";
        sysMessage = 0;
        recipientId = new ArrayList<>();
        clientsId = new ArrayList<>();
    }

    Data( String m, ArrayList<Integer>ids){
        message = m;
        recipientId = ids;
    }

    public void reset(){
        message = "";
        sysMessage = 0;
        recipientId = new ArrayList<>();
        clientsId = new ArrayList<>();
    }
    public void dup(Data newData){
        message = newData.message;
        sysMessage = newData.sysMessage;
        recipientId = newData.recipientId;
//        clientsId = newData.clientsId;
    }
}
