import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yifan on 2017/4/27.
 * Bar
 */
class Bar {
    private int barNum;
    private int barLength;
    private List<Note> notes;
    private List<BpmNote> bpmNotes;

    Bar(int barNum, int barLength) {
        this.barNum = barNum;
        this.barLength = barLength;
        this.notes = new ArrayList<Note>();
        this.bpmNotes = new ArrayList<BpmNote>();
    }

    void addNote(Note note) {
        notes.add(note);
    }

    void addBpmNote(BpmNote bpmNote) {
        bpmNotes.add(bpmNote);
    }

    void checkBar(Bar nextBar) {
        for (int i = notes.size() - 1; i >= 0; i--) {
            if (notes.get(i).getBar() != barNum) {
                int barLength = nextBar.getBarLength();
                int marginTop = barLength - notes.get(i).getPosition() - 5;
                int status = HtmlParser.setNotePosition(notes.get(i), marginTop, barLength);
                if (status == -1) {
                    System.out.println(String.format("Info:    set Note position failed with \'renew\' marginTop %d and barLength %d checking bar %d", marginTop, barLength, nextBar.getBarNum()));
                }
                nextBar.addNote(notes.get(i));
                notes.remove(notes.get(i));
            }
        }
    }

    int getBarNum() {
        return barNum;
    }

    int getBarLength() {
        return barLength;
    }


    List<Note> getNotes() {
        return notes;
    }

    List<BpmNote> getBpmNotes() {
        return bpmNotes;
    }
}
