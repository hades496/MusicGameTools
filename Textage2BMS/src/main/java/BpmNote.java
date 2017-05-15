/**
 * Created by Yifan on 2017/4/27.
 * Bpm note
 */
class BpmNote {
    private int bar;
    private int divider;
    private int num;
    private double bpm;
    private boolean isBpmInteger;

    int getBar() {
        return bar;
    }

    void setBar(int bar) {
        this.bar = bar;
    }

    int getDivider() {
        return divider;
    }

    void setDivider(int divider) {
        this.divider = divider;
    }

    int getNum() {
        return num;
    }

    void setNum(int num) {
        this.num = num;
    }

    double getBpm() {
        return bpm;
    }

    void setBpm(double bpm) {
        this.bpm = bpm;
    }

    boolean isBpmInteger() {
        return isBpmInteger;
    }

    void setBpmInteger(boolean bpmInteger) {
        isBpmInteger = bpmInteger;
    }
}
