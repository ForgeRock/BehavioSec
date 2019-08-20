package com.behaviosec.tree.restclient;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class BehavioSecVersion {

    private String _string;
    private Map<String, String > modules;
    private String serverVersion;
    private String serverName;


    public BehavioSecVersion(String response){
        _string = response;
        this.modules = new HashMap<>();
        makeModules();
    }

    public boolean hasModule(String name){
        return modules.containsKey(name);
    }

    public String getVersion(String name){
        return modules.get(name);
    }

    public int numberOfModules(){
        return modules.size();
    }

    public String getVersion(){
        return this.serverVersion;
    }

    public String getName(){
        return this.serverName;
    }

    @Override
    public String toString(){
        return getName() + ": " + getVersion() + "\n" +
                      modules.entrySet()
                      .stream()
                      .map(e -> e.getKey() + " X " + e.getValue())
                      .collect(Collectors.joining("\n"));
    }

    private void makeModules(){
        if(_string != null){
//            String lines[] = this._string.split("\n");
//            serverName = lines[0].split(" - ")[0].strip();
//            serverVersion = lines[0].split(" - ")[1].strip();
//            for (String line: Arrays.copyOfRange(lines, 2, lines.length)) {
//                String v[] = line.split("-");
//                //Workaround for BEH-907
//                if(v.length > 1 ) {
//                    modules.put(
//                            v[0].strip(),
//                            v[1].strip()
//                    );
//                } else {
//                    if(line.startsWith("BehavioFuzzy")){
//                        String vv[] = line.split(" ");
//                        modules.put(
//                                vv[0].strip(),
//                                vv[1].strip()
//                        );
//                    }
//                }
//            }
        }
    }
}
