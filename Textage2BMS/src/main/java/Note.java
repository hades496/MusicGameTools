/**
 * Created by Yifan on 2017/4/27.
 * Note
 */
class Note {
    private int bar;
    private int track;
    private int divider;
    private int num;
    private int marginTop;
    private int position;

    Note() {
        bar = 0;
        track = -1;
        divider = 0;
        num = -1;
    }

    int getBar() {
        return bar;
    }

    void setBar(int bar) {
        this.bar = bar;
    }

    int getTrack() {
        return track;
    }

    void setTrack(int track) {
        this.track = track;
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

    int getMarginTop() {
        return marginTop;
    }

    void setMarginTop(int marginTop) {
        this.marginTop = marginTop;
    }

    int getPosition() {
        return position;
    }

    void setPosition(int position) {
        this.position = position;
    }
}
