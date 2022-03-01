package mua;

import java.util.Map;
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;

public class Interpreter {
    public Scanner scanner;
    // public Map <String, variable> table = new HashMap<>();
    public List <String> opList = new ArrayList<>();
    public String[] operator = {"add", "sub", "mul", "div", "mod"};
    public static int instIndex;
    public static int bracketNum;
    public static int spaceIndex = 0;
    public static List<Integer> spaceList = new ArrayList<Integer>();
    public static List<Integer> varSpaceList = new ArrayList<Integer>();
    public boolean isReturn = false;
    public static List <Map <String, variable>> table = new ArrayList<Map <String, variable>>(){
        {add(new HashMap<String, variable>(){
            {put("pi", new variable("3.14159", Type.NUMBER));}
        });}
    };

    public Interpreter(Scanner scanner){
        this.scanner=scanner;
    }

    public variable getValue (String[] inst){
        variable ret = new variable("", Type.NUMBER);
        if(inst[0].equals("")){

        } else if(inst[0].equals("make")){
            String name = getValue(deleteFirstNItem(inst, 1)).value;
            instIndex+=1;
            if(inst.length==2){
                bracketNum = 0;
                ret = new variable(inst[0]+" "+inst[1], Type.NOT_COMPLETE);
            } else {
                variable value = getValue(deleteFirstNItem(inst, 2));
                if(value.type!=Type.NOT_COMPLETE) table.get(spaceIndex).put(name, value);
                ret = value;
            }
            if(ret.type==Type.LIST){
                opList.add(name);
            }
        } else if(inst[0].equals("print")){
            String[] temp = deleteFirstNItem(inst, 1);
            instIndex+=1;
            ret = getValue(temp);
            System.out.println(ret.value);
        } else if(inst[0].charAt(0)==':'){
            ret = getVariable(inst[0].substring(1));
            instIndex++;
        } else if(inst[0].charAt(0)=='"') {
            if(isNum(inst[0].substring(1))) ret = new variable(inst[0].substring(1), Type.NUMBER);
            else ret = new variable(inst[0].substring(1), Type.WORD);
            instIndex+=1;
        } else if(isNum(inst[0])){
            ret = new variable(inst[0], Type.NUMBER);
            instIndex+=1;
        } else if(inst[0].charAt(0)=='['){
            int i=-1;
            bracketNum=0;
            do{
                i++;
                if(i==inst.length) return new variable("", Type.NOT_COMPLETE);
                if(inst[i].contains("[")) bracketNum+=countChar(inst[i], "[");
                if(inst[i].contains("]")) bracketNum-=countChar(inst[i], "]");
            }while(bracketNum!=0);
            int begin = 0, end = i;
            if(inst[0].equals("[")) begin++;
            if(inst[i].equals("]")) end--;
            inst[0]=inst[0].replaceFirst("\\[", "");
            inst[i]=inst[i].replaceFirst("\\]", "");
            String list="";
            for (int k=begin; k<end; k++){
                list=list+inst[k]+" ";
            }
            list = list+inst[end];
            // instIndex+=end-begin+1;
            instIndex += i+1;
            ArrayList<Integer> mySpaceList = new ArrayList<Integer>(spaceList);
            mySpaceList.addAll(varSpaceList);
            ret = new variable(list, Type.LIST, mySpaceList);
        }else if(inst[0].equals("thing")){
            if(inst[1].charAt(0)=='"'){
                ret = getVariable(inst[1].substring(1));
                instIndex+=2;
            } else {
                ret = getVariable(getValue(deleteFirstNItem(inst, 1)).value);
                instIndex+=1;
            }
        } else if(inst[0].equals("read")){
            String input=scanner.nextLine();
            if(isNum(input)){
                ret = new variable(input, Type.NUMBER);
            } else {
                ret = new variable(input, Type.WORD);
            }
            instIndex+=1;
        } else if (Arrays.asList(operator).contains(inst[0])){
            int curIndex = instIndex;
            instIndex++;
            variable a = getValue(deleteFirstNItem(inst, 1));
            variable b = getValue(deleteFirstNItem(inst, instIndex-curIndex));
            float n1=Float.parseFloat(a.value);
            float n2=Float.parseFloat(b.value);
            switch (inst[0]) {
                case "add":
                    ret = new variable(String.valueOf(n1+n2), Type.NUMBER);
                    break;
                case "sub":
                    ret = new variable(String.valueOf(n1-n2), Type.NUMBER);
                    break;
                case "mul":
                    ret = new variable(String.valueOf(n1*n2), Type.NUMBER);
                    break;
                case "div":
                    ret = new variable(String.valueOf(n1/n2), Type.NUMBER);
                    break;
                case "mod":
                    ret = new variable(String.valueOf(n1%n2), Type.NUMBER);
                    break;
            
                default:
                    break;
            }
        } else if(inst[0].equals("erase")){
            if(inst[1].charAt(0)=='"'){
                String name = inst[1].substring(1);
                instIndex+=2;
                variable value = table.get(spaceIndex).get(name);
                table.get(spaceIndex).remove(name);
                ret = value;
            }
        } else if(inst[0].equals("isname")){
            if(inst[1].charAt(0)=='"'){
                String name = inst[1].substring(1);
                instIndex+=2;
                if(containVariable(name)){
                    ret = new variable("true", Type.BOOL);
                } else {
                    ret = new variable("false", Type.BOOL);
                }
            }
        } else if(inst[0].equals("run")){
            instIndex+=1;
            String[] list = getValue(deleteFirstNItem(inst, 1)).value.split(" "); 
            int curIndex = instIndex;
            ret = run(list);
            instIndex = curIndex;
        } else if(inst[0].equals("eq")){
            int curIndex = instIndex;
            instIndex++;
            variable a = getValue(deleteFirstNItem(inst, 1));
            variable b = getValue(deleteFirstNItem(inst, instIndex-curIndex));
            int eq = 1;
            if(a.type == b.type) eq = a.type.compare(a.value, b.value);
            if (eq==0) ret = new variable("true", Type.BOOL);
            else ret = new variable("false", Type.BOOL);
        } else if(inst[0].equals("gt")){
            int curIndex = instIndex;
            instIndex++;
            variable a = getValue(deleteFirstNItem(inst, 1));
            variable b = getValue(deleteFirstNItem(inst, instIndex-curIndex));
            int gt = a.type.compare(a.value, b.value);
            if(gt>0) ret = new variable("true", Type.BOOL);
            else ret = new variable("false", Type.BOOL);
        } else if(inst[0].equals("lt")){
            int curIndex = instIndex;
            instIndex++;
            variable a = getValue(deleteFirstNItem(inst, 1));
            variable b = getValue(deleteFirstNItem(inst, instIndex-curIndex));
            int lt = a.type.compare(a.value, b.value);
            if(lt<0) ret = new variable("true", Type.BOOL);
            else ret = new variable("false", Type.BOOL);
        } else if(inst[0].equals("and")){
            int curIndex = instIndex;
            instIndex++;
            variable a = getValue(deleteFirstNItem(inst, 1));
            variable b = getValue(deleteFirstNItem(inst, instIndex-curIndex));
            if(a.value.equals("true") && b.value.equals("true")){
                ret = new variable("true", Type.BOOL);
            } else ret = new variable("false", Type.BOOL);
        } else if(inst[0].equals("or")){
            int curIndex = instIndex;
            instIndex++;
            variable a = getValue(deleteFirstNItem(inst, 1));
            variable b = getValue(deleteFirstNItem(inst, instIndex-curIndex));
            if(a.value.equals("false") && b.value.equals("false")){
                ret = new variable("false", Type.BOOL);
            } else ret = new variable("true", Type.BOOL);
        } else if(inst[0].equals("not")){
            instIndex++;
            variable a = getValue(deleteFirstNItem(inst, 1));
            if(a.value.equals("true")) ret = new variable("false", Type.BOOL);
            else ret = new variable("true", Type.BOOL);
        } else if(inst[0].equals("isnumber")){
            instIndex++;
            variable a = getValue(deleteFirstNItem(inst, 1));
            if(a.type.equals(Type.NUMBER)){
                ret = new variable("true", Type.BOOL);
            } else ret = new variable("false", Type.BOOL);
        } else if(inst[0].equals("isword")){
            instIndex++;
            variable a = getValue(deleteFirstNItem(inst, 1));
            if(a.type.equals(Type.WORD)){
                ret = new variable("true", Type.BOOL);
            } else ret = new variable("false", Type.BOOL);
        } else if(inst[0].equals("islist")){
            instIndex++;
            variable a = getValue(deleteFirstNItem(inst, 1));
            if(a.type.equals(Type.LIST)){
                ret = new variable("true", Type.BOOL);
            } else ret = new variable("false", Type.BOOL);
        } else if(inst[0].equals("isbool")){
            instIndex++;
            variable a = getValue(deleteFirstNItem(inst, 1));
            if(a.type.equals(Type.BOOL)){
                ret = new variable("true", Type.BOOL);
            } else ret = new variable("false", Type.BOOL);
        } else if(inst[0].equals("isempty")){
            instIndex++;
            if(inst[1].charAt(0)=='"'){
                instIndex++;
                if(containVariable(inst[1].substring(1)) && getVariable(inst[1].substring(1)).value.equals("")){
                    ret = new variable("true", Type.BOOL);
                } else ret = new variable("false", Type.BOOL);
            } else {
                variable a = getValue(deleteFirstNItem(inst, 1));
                if(a.type.equals(Type.LIST) && a.value.equals("")){
                    ret = new variable("true", Type.BOOL);
                } else ret = new variable("false", Type.BOOL);
            }
        } else if(inst[0].equals("if")){
            int ifIndex = instIndex;
            instIndex++;
            variable a = getValue(deleteFirstNItem(inst, 1));
            String[] list = getValue(deleteFirstNItem(inst, instIndex-ifIndex)).value.split(" "); 
            int curIndex;
            if(a.value.equals("true")){
                curIndex = instIndex;
                ret = run(list);
                instIndex = curIndex;
            } else {
                list = getValue(deleteFirstNItem(inst, instIndex-ifIndex)).value.split(" ");
                curIndex = instIndex;
                ret = run(list);
                instIndex = curIndex;
            }
        } else if(containVariable(inst[0])){
            int curIndex=instIndex;
            instIndex++;
            variable funcvar = getVariable(inst[0]);
            String[] func = getVariable(inst[0]).value.split(" ");
            table.add(new HashMap<String,variable>());
            int funcTableIndex = table.size()-1;
            int curSpaceIndex=spaceIndex;
            List <String> arglist = new ArrayList<>();
            int i=0;

            if(func[0].charAt(0)=='['){
                while(!func[i].contains("]")) i++;
                func[0]=func[0].replaceFirst("\\[", "");
                func[i]=func[i].replaceFirst("\\]", "");
                for(int k=0; k<=i; k++){
                    if(!func[k].equals(""))
                    arglist.add(func[k]);
                }
            }

            variable a = new variable("", Type.WORD);
            for(int ii=0; ii<arglist.size(); ii++){
                a = getValue(deleteFirstNItem(inst, instIndex-curIndex));
                table.get(funcTableIndex).put(arglist.get(ii), a);
            }
            
            spaceIndex = funcTableIndex;
            spaceList.add((Integer)spaceIndex);
            ArrayList<Integer> oldVarSpaceList = new ArrayList<Integer>(varSpaceList);
            varSpaceList = funcvar.spaceList;
            curIndex=instIndex;
            String[] cmd = getValue(deleteFirstNItem(func, i+1)).value.split(" ");
            ret = run(cmd);
            // table.remove(spaceIndex);
            spaceList.remove((Integer)spaceIndex);
            varSpaceList = oldVarSpaceList;
            instIndex=curIndex;
            spaceIndex=curSpaceIndex;
            isReturn=false;
        } else if(inst[0].equals("return")){
            instIndex++;
            ret=getValue(deleteFirstNItem(inst, 1));
            isReturn=true;
        } else if(inst[0].equals("export")){
            instIndex++;
            String name=getValue(deleteFirstNItem(inst, 1)).value;
            variable tmp=table.get(spaceIndex).get(name);
            if(table.get(0).containsKey(name)) table.get(0).replace(name, tmp);
            else table.get(0).put(name, tmp);
            ret = tmp;
        } else if(inst[0].equals("readlist")){
            instIndex++;
            String input=scanner.nextLine();
            ArrayList<Integer> mySpaceList = new ArrayList<Integer>(spaceList);
            mySpaceList.addAll(varSpaceList);
            ret = new variable(input, Type.LIST, mySpaceList);
        } else if(inst[0].equals("word")){
            int curIndex = instIndex;
            instIndex++;
            variable a = getValue(deleteFirstNItem(inst, 1));
            variable b = getValue(deleteFirstNItem(inst, instIndex-curIndex));
            String word = a.value + b.value;
            ret = new variable(word, Type.WORD); 
        } else if(inst[0].equals("sentence")){
            int curIndex = instIndex;
            instIndex++;
            variable a = getValue(deleteFirstNItem(inst, 1));
            variable b = getValue(deleteFirstNItem(inst, instIndex-curIndex));
            String list = a.value + " "+ b.value;
            ArrayList<Integer> mySpaceList = new ArrayList<Integer>(spaceList);
            mySpaceList.addAll(varSpaceList);
            ret = new variable(list, Type.LIST, mySpaceList); 
        } else if(inst[0].equals("list")){
            int curIndex = instIndex;
            instIndex++;
            variable a = getValue(deleteFirstNItem(inst, 1));
            variable b = getValue(deleteFirstNItem(inst, instIndex-curIndex));
            String s1 = a.type == Type.LIST ? "[" + a.value + "]" : a.value;
            String s2 = b.type == Type.LIST ? "[" + b.value + "]" : b.value;
            String list = s1 + " " + s2;
            ArrayList<Integer> mySpaceList = new ArrayList<Integer>(spaceList);
            mySpaceList.addAll(varSpaceList);
            ret = new variable(list, Type.LIST, mySpaceList);
        } else if(inst[0].equals("join")){
            int curIndex = instIndex;
            instIndex++;
            variable a = getValue(deleteFirstNItem(inst, 1));
            variable b = getValue(deleteFirstNItem(inst, instIndex-curIndex));
            String list = a.value;
            String s2 = b.type == Type.LIST ? "[" + b.value + "]" : b.value;
            String newList = list+" "+s2;
            if(list.equals("")) newList = s2;
            ArrayList<Integer> mySpaceList = new ArrayList<Integer>(spaceList);
            mySpaceList.addAll(varSpaceList);
            ret = new variable(newList, Type.LIST, mySpaceList);
        } else if(inst[0].equals("first")){
            instIndex++;
            variable a = getValue(deleteFirstNItem(inst, 1));
            String word="";
            if(a.type != Type.LIST){
                word = a.value.substring(0, 1);
                ret = new variable(word, Type.WORD);
            } else {
                int curIndex = instIndex;
                ret = getValue(a.value.split(" "));
                instIndex = curIndex;
            }
        } else if(inst[0].equals("last")){
            instIndex++;
            variable a = getValue(deleteFirstNItem(inst, 1));
            String word="";
            if(a.type != Type.LIST){
                String s = a.value;
                word = s.substring(s.length()-1);
                ret = new variable(word, Type.WORD);
            } else {
                int curIndex = instIndex;
                instIndex = 0;
                String[] temp= a.value.split(" ");
                while(instIndex < temp.length){
                    ret = getValue(deleteFirstNItem(temp, instIndex)); 
                }
                instIndex = curIndex;
            }
        } else if(inst[0].equals("butfirst")){
            instIndex++;
            variable a = getValue(deleteFirstNItem(inst, 1));
            String word="";
            if(a.type != Type.LIST){
                word = a.value.substring(1);
                ret = new variable(word, Type.WORD);
            } else {
                int curIndex = instIndex;
                instIndex = 0;
                String[] temp= a.value.split(" ");
                getValue(temp);
                String[] s = deleteFirstNItem(temp, instIndex);
                String list=s[0];
                for(int i=1; i<s.length;i++){
                    list=list+" "+s[i];
                }
                ret = new variable(list, Type.LIST);
                instIndex = curIndex;
            }
        } else if(inst[0].equals("butlast")){
            instIndex++;
            variable a = getValue(deleteFirstNItem(inst, 1));
            String word="";
            if(a.type == Type.WORD){
                word = a.value.substring(0,a.value.length()-2);
                ret = new variable(word, Type.WORD);
            } else if(a.type == Type.LIST){
                int curIndex = instIndex;
                instIndex = 0;
                int preIndex = 0;
                String[] temp= a.value.split(" ");
                while(instIndex < temp.length){
                    preIndex = instIndex;
                    word = getValue(deleteFirstNItem(temp, instIndex)).value; 
                }
                String list=temp[0];
                for(int i=1; i<preIndex;i++){
                    list=list+" "+temp[i];
                }
                ret = new variable(list, Type.LIST);
                instIndex = curIndex;
            }
        } else if(inst[0].equals("save")){
            instIndex++;
            Map <String, variable> namespace = table.get(0);
            String filename = getValue(deleteFirstNItem(inst, 1)).value;
            File file = new File(filename);
            try {
                if(!file.exists()) {
                    file.createNewFile();
                } else {
                    file.delete();
                    file.createNewFile();
                }
    
                FileWriter fw = new FileWriter(file, true);
                BufferedWriter bw = new BufferedWriter(fw);
    
                for (Map.Entry<String, variable> entry : namespace.entrySet()) {
                    if(entry.getValue().type == Type.LIST){
                        bw.write("make "+"\""+entry.getKey()+" ["+entry.getValue().value+"]\n");
                    } else bw.write("make "+"\""+entry.getKey()+" "+entry.getValue().value+"\n");
                }
                bw.flush();
                bw.close();
                fw.close();
                ret = new variable(filename, Type.WORD);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if(inst[0].equals("erall")){
            instIndex++;
            table.get(0).clear();
            ret = new variable("true", Type.BOOL);
        } else if(inst[0].equals("load")){
            instIndex++;
            String filename = getValue(deleteFirstNItem(inst, 1)).value;
            File file = new File(filename);
            try {    
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                String str = "";
                int curIndex = instIndex;
                while((str = br.readLine())!=null){
                    instIndex = 0;
                    getValue(str.split(" "));
                }
                instIndex = curIndex;
                br.close();
                fr.close();
                ret = new variable("true", Type.BOOL);
            } catch (Exception e) {
                ret = new variable("false", Type.BOOL);
            }
        } else if(inst[0].equals("int")){
            instIndex++;
            int floor = (int)Math.floor((Double.parseDouble(getValue(deleteFirstNItem(inst, 1)).value)));
            ret = new variable(String.valueOf(floor), Type.NUMBER);
        } else if(inst[0].equals("sqrt")){
            instIndex++;
            double num = Double.parseDouble(getValue(deleteFirstNItem(inst, 1)).value);
            double sq = Math.sqrt(num);
            ret = new variable(String.valueOf(sq), Type.NUMBER);
        } else if(inst[0].equals("random")){
            int num = Integer.parseInt(getValue(deleteFirstNItem(inst, 1)).value);
            Random random = new Random();
            int ran = random.nextInt(num);
            ret = new variable(String.valueOf(ran), Type.NUMBER);
        } else {
            ret = new variable(inst[0], Type.WORD);
            instIndex++;
        }
        return ret;
    }

    public static String[] deleteFirstNItem(String[] str, int n){
        if(str.length<=n){
            String[] s = new String[1];
            s[0]="";
            return s;
        }
        String[] s = new String[str.length-n];
        for(int i=0; i<str.length-n; i++){
            s[i]=str[i+n];
        }
        return s;
    }

    public static boolean isNum(String str) {
        if(str.matches("\\d+\\.\\d*")||str.matches("\\d+")) return true;
        else return false;
    }

    public variable run(String[] list){
        variable ret = new variable("", Type.WORD);
        if(list[0].equals("")){
            ret = new variable("", Type.LIST);
        } else {
            instIndex = 0;
            while(instIndex<list.length){
                ret = getValue(deleteFirstNItem(list, instIndex));
                if(isReturn){
                    break;
                }
            }
        }
        return ret;
    }

    public static int countChar(String str, CharSequence c){
        int count = 0;
        int Length = str.length();
        str = str.replace(c, "");
        int newLength = str.length();

        count = Length - newLength;
        return count;
    }

    public static variable getVariable(String name){
        if(table.get(spaceIndex).containsKey(name)) return table.get(spaceIndex).get(name);
        for (int i=varSpaceList.size()-1; i>=0; i--){
            if(table.get(varSpaceList.get(i)).containsKey(name)){
                return table.get(varSpaceList.get(i)).get(name);
            }
        }
        for (int i=spaceList.size()-1; i>=0; i--){
            if(table.get(spaceList.get(i)).containsKey(name)){
                return table.get(spaceList.get(i)).get(name);
            }
        }
        return table.get(0).get(name);
    }

    public static boolean containVariable(String name){
        if(table.get(spaceIndex).containsKey(name)) return true;
        for (int i=varSpaceList.size()-1; i>=0; i--){
            if(table.get(varSpaceList.get(i)).containsKey(name)){
                return true;
            }
        }
        for (int i=spaceList.size()-1; i>=0; i--){
            if(table.get(spaceList.get(i)).containsKey(name)){
                return true;
            }
        }
        if(table.get(0).containsKey(name)) return true;
        else return false;
    }
}
