import java.util.HashMap;

public class IDandPasswords {


    HashMap<String,String> logininfo = new HashMap<String,String>();
    IDandPasswords(){

        logininfo.put("Sacit","pizza");
        logininfo.put("Brometheus","PASWWORD");
        logininfo.put("Bromie","alphabet");
    }

    protected HashMap getLogininfo(){
        return logininfo;
    }
}
