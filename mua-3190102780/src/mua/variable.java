package mua;

import java.util.List;
import java.util.ArrayList;

public class variable {
    public Type type;
    public String value;
    public List<Integer> spaceList = new ArrayList<Integer>();
    public variable(String value, Type type){
        this.type = type;
        this.value = value;
    }
    public variable(String value, Type type, List<Integer> spaceList){
        this.type = type;
        this.value = value;
        this.spaceList = spaceList;
    }
}
