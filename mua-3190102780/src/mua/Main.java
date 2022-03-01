package mua;

import java.util.Scanner;

public class Main {
    public static Scanner scanner = new Scanner(System.in);
    // public static Map <String, variable> table = new HashMap<>();

    public static void main(String[] args) {
        String str;
        Interpreter interpreter = new Interpreter(scanner);
        while(scanner.hasNextLine()){
            str=scanner.nextLine();
            String[] inst;
            String delimeter =" ";
            inst = str.split(delimeter);
            Interpreter.instIndex = 0;
            variable ret = interpreter.getValue(inst);
            if(ret.type.equals(Type.NOT_COMPLETE)){
                str=str+" "+scanner.nextLine().trim();
                while(countChar(str, "[")!=countChar(str, "]")){
                    str=str+" "+scanner.nextLine().trim();
                }
                inst = str.split(delimeter);
                Interpreter.instIndex = 0;
                ret = interpreter.getValue(inst);
            }
        }
        scanner.close();
    }

    public static String[] deleteFirstNItem(String[] str, int n){
        String[] s = new String[str.length-n];
        for(int i=0; i<str.length-n; i++){
            s[i]=str[i+n];
        }
        return s;
    }

    public static boolean isNum(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static int countChar(String str, CharSequence c){
        int count = 0;
        int Length = str.length();
        str = str.replace(c, "");
        int newLength = str.length();

        count = Length - newLength;
        return count;
    }
}