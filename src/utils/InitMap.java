package utils;

import java.util.HashMap;
import java.util.Map;

public class InitMap {
    private Map<String, Boolean> map;

    public InitMap(){
        map = new HashMap<String, Boolean>();
    }

    public InitMap(InitMap other){
        this.map = new HashMap<String, Boolean>();
        for (Map.Entry<String, Boolean> v : other.map.entrySet()) {
            map.put(v.getKey(), v.getValue());
        }
    }

    public void addVariable(String v){
        map.put(v, false);
    }

    public static InitMap merge(InitMap map1, InitMap map2){
        InitMap res = new InitMap();
        for (Map.Entry<String, Boolean> v : map1.map.entrySet()) {
            if(v.getValue() && map2.map.get(v.getKey())) res.map.put(v.getKey(), true);
            else res.map.put(v.getKey(), false);
        }
        return res;
    }

    public boolean isInit(String v){
        return map.get(v);
    }

    public void init(String v){
        map.put(v, true);
    }

}
