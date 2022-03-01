package mua;

import java.math.BigDecimal;

public enum Type {
    WORD {
        public int compare(String a, String b) {
            return a.compareTo(b);
        }
    },
    NUMBER{
        public int compare(String a, String b) {
            BigDecimal d1 = new BigDecimal(Double.parseDouble(a));
            BigDecimal d2 = new BigDecimal(Double.parseDouble(b));

            return d1.compareTo(d2);
        }
    },
    LIST{
        public int compare(String a, String b) {
            return a.compareTo(b);
        }
    },
    BOOL{
        public int compare(String a, String b) {
            return a.compareTo(b);
        }
    },
    NOT_COMPLETE;
    public int compare(String a, String b) {
        return 0;
    }
}