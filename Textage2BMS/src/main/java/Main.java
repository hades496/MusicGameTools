import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yifan on 2017/4/27.
 * Main
 */
public class Main {
    private static String FILE_PATH;

    public static void main(String[] args) {
        boolean fix;
        boolean fixWithScratch;
        if (args.length == 0 || args.length > 2) {
            System.out.println("Usage:   Textage2BMS filepath [-n | -s]");
            System.out.println("         [-n] do not fix timing of charge notes for bmx2bemani");
            System.out.println("         [-s] fix timing of charge notes including scratch for bmx2bemani");
            System.out.println("Caution: the file must be a html or text file encoding with UTF-8");
            return;
        } else if (args.length == 1) {
            fix = true;
            fixWithScratch = false;
            FILE_PATH = args[0];
        } else {
            if (args[1].equalsIgnoreCase("-n")) {
                fix = false;
                fixWithScratch = false;
                FILE_PATH = args[0];
            } else if (args[1].equalsIgnoreCase("-s")) {
                fix = false;
                fixWithScratch = true;
                FILE_PATH = args[0];
            } else {
                System.out.println(String.format("Error:   Can't resolve symbol \'%s\'.", args[1]));
                System.out.println("Usage:   Textage2BMS filepath [-n | -s]");
                System.out.println("         [-n] do not fix timing of charge notes for bmx2bemani");
                System.out.println("         [-s] fix timing of charge notes including scratch for bmx2bemani");
                System.out.println("Caution: the file must be a html or text file encoding with UTF-8");
                return;
            }
        }
        try {
            List<Bar> bars = HtmlParser.read(FILE_PATH, fix, fixWithScratch);
            File file = new File(getFileName(fix, fixWithScratch));
            if (!file.exists()) {
                boolean isFileCreated = file.createNewFile();
                if (!isFileCreated) {
                    throw new Exception("file create failed");
                }
            }
            FileOutputStream out = new FileOutputStream(file, false);
            StringBuilder mainDataSb = new StringBuilder();
            StringBuilder headerSb = new StringBuilder();
            int headerCount = 1;
            List<String> bpmList = new ArrayList<String>();
            if (bars != null) {
                for (Bar bar : bars) {
                    String barNum = String.valueOf(bar.getBarNum());
                    if (barNum.length() <= 0 || barNum.length() >= 4) {
                        throw new Exception(String.format("invalid barNum of %s", barNum));
                    }
                    if (barNum.length() == 1) {
                        barNum = String.format("00%s", barNum);
                    } else if (barNum.length() == 2) {
                        barNum = String.format("0%s", barNum);
                    }
                    //Deal with bar Length
                    if (bar.getBarLength() != 768) {
                        mainDataSb.append(String.format("#%s02:%f\r\n", barNum, bar.getBarLength() / 768.0));
                    }
                    //Deal with BPM notes
                    List<BpmNote> bpmNotes = bar.getBpmNotes();
                    if (bpmNotes.size() > 0) {
                        List<BpmNote> notes03 = new ArrayList<BpmNote>();
                        List<BpmNote> notes08 = new ArrayList<BpmNote>();
                        for (BpmNote bpmNote : bpmNotes) {
                            if (bpmNote.isBpmInteger()) {
                                if (bpmNote.getBpm() >= 256) {
                                    notes08.add(bpmNote);
                                } else {
                                    notes03.add(bpmNote);
                                }
                            } else {
                                notes08.add(bpmNote);
                            }
                        }
                        if (notes03.size() > 0) {
                            int minMultiple03 = minMultipleBpmNotes(notes03);
                            String[] line03 = new String[minMultiple03];
                            for (int i = 0; i < line03.length; i++) {
                                line03[i] = "00";
                            }
                            for (BpmNote bpmNote : notes03) {
                                int num = bpmNote.getNum();
                                num *= (minMultiple03 / bpmNote.getDivider());
                                line03[num] = Integer.toHexString((int) bpmNote.getBpm()).toUpperCase();
                            }
                            mainDataSb.append(String.format("#%s03:", barNum));
                            for (String text : line03) {
                                mainDataSb.append(text);
                            }
                            mainDataSb.append("\r\n");
                        }
                        if (notes08.size() > 0) {
                            int minMultiple08 = minMultipleBpmNotes(notes08);
                            String[] line08 = new String[minMultiple08];
                            for (int i = 0; i < line08.length; i++) {
                                line08[i] = "00";
                            }
                            for (BpmNote bpmNote : notes08) {
                                int num = bpmNote.getNum();
                                num *= (minMultiple08 / bpmNote.getDivider());
                                if (bpmNote.isBpmInteger()) {
                                    String bpm = String.valueOf((int) bpmNote.getBpm());
                                    boolean isBpmFound = false;
                                    for (int i = 0; i < bpmList.size(); i++) {
                                        if (bpmList.get(i).equals(bpm)) {
                                            line08[num] = String.format("%02d", i + 1);
                                            isBpmFound = true;
                                            break;
                                        }
                                    }
                                    if (!isBpmFound) {
                                        headerSb.append(String.format("#BPM%02d %s\r\n", headerCount, String.valueOf((int) bpmNote.getBpm())));
                                        bpmList.add(String.valueOf((int) bpmNote.getBpm()));
                                        line08[num] = String.format("%02d", headerCount);
                                        headerCount++;
                                    }
                                } else {
                                    String bpm = String.valueOf(bpmNote.getBpm());
                                    boolean isBpmFound = false;
                                    for (int i = 0; i < bpmList.size(); i++) {
                                        if (bpmList.get(i).equals(bpm)) {
                                            line08[num] = String.format("%02d", i + 1);
                                            isBpmFound = true;
                                            break;
                                        }
                                    }
                                    if (!isBpmFound) {
                                        headerSb.append(String.format("#BPM%02d %s\r\n", headerCount, String.valueOf(bpmNote.getBpm())));
                                        bpmList.add(String.valueOf(bpmNote.getBpm()));
                                        line08[num] = String.format("%02d", headerCount);
                                        headerCount++;
                                    }
                                }
                            }
                            mainDataSb.append(String.format("#%s08:", barNum));
                            for (String text : line08) {
                                mainDataSb.append(text);
                            }
                            mainDataSb.append("\r\n");
                        }
                    }
                    //Deal with normal Notes
                    ArrayList<Note> notes11 = new ArrayList<Note>();
                    ArrayList<Note> notes12 = new ArrayList<Note>();
                    ArrayList<Note> notes13 = new ArrayList<Note>();
                    ArrayList<Note> notes14 = new ArrayList<Note>();
                    ArrayList<Note> notes15 = new ArrayList<Note>();
                    ArrayList<Note> notes16 = new ArrayList<Note>();
                    ArrayList<Note> notes18 = new ArrayList<Note>();
                    ArrayList<Note> notes19 = new ArrayList<Note>();
                    ArrayList<Note> notes51 = new ArrayList<Note>();
                    ArrayList<Note> notes52 = new ArrayList<Note>();
                    ArrayList<Note> notes53 = new ArrayList<Note>();
                    ArrayList<Note> notes54 = new ArrayList<Note>();
                    ArrayList<Note> notes55 = new ArrayList<Note>();
                    ArrayList<Note> notes56 = new ArrayList<Note>();
                    ArrayList<Note> notes58 = new ArrayList<Note>();
                    ArrayList<Note> notes59 = new ArrayList<Note>();
                    for (Note note : bar.getNotes()) {
                        switch (note.getTrack()) {
                            case 0:
                                notes16.add(note);
                                break;
                            case 1:
                                notes11.add(note);
                                break;
                            case 2:
                                notes12.add(note);
                                break;
                            case 3:
                                notes13.add(note);
                                break;
                            case 4:
                                notes14.add(note);
                                break;
                            case 5:
                                notes15.add(note);
                                break;
                            case 6:
                                notes18.add(note);
                                break;
                            case 7:
                                notes19.add(note);
                                break;
                            case 10:
                                notes56.add(note);
                                break;
                            case 11:
                                notes51.add(note);
                                break;
                            case 12:
                                notes52.add(note);
                                break;
                            case 13:
                                notes53.add(note);
                                break;
                            case 14:
                                notes54.add(note);
                                break;
                            case 15:
                                notes55.add(note);
                                break;
                            case 16:
                                notes58.add(note);
                                break;
                            case 17:
                                notes59.add(note);
                                break;
                        }
                        if (note.getBar() != bar.getBarNum()) {
                            throw new Exception(String.format("note num error at barNum %d with note bar %d", bar.getBarNum(), note.getBar()));
                        }
                    }
                    buildStringOfNotes(notes11, barNum, "11", mainDataSb);
                    buildStringOfNotes(notes12, barNum, "12", mainDataSb);
                    buildStringOfNotes(notes13, barNum, "13", mainDataSb);
                    buildStringOfNotes(notes14, barNum, "14", mainDataSb);
                    buildStringOfNotes(notes15, barNum, "15", mainDataSb);
                    buildStringOfNotes(notes16, barNum, "16", mainDataSb);
                    buildStringOfNotes(notes18, barNum, "18", mainDataSb);
                    buildStringOfNotes(notes19, barNum, "19", mainDataSb);
                    buildStringOfNotes(notes51, barNum, "51", mainDataSb);
                    buildStringOfNotes(notes52, barNum, "52", mainDataSb);
                    buildStringOfNotes(notes53, barNum, "53", mainDataSb);
                    buildStringOfNotes(notes54, barNum, "54", mainDataSb);
                    buildStringOfNotes(notes55, barNum, "55", mainDataSb);
                    buildStringOfNotes(notes56, barNum, "56", mainDataSb);
                    buildStringOfNotes(notes58, barNum, "58", mainDataSb);
                    buildStringOfNotes(notes59, barNum, "59", mainDataSb);
                    if (bar.getNotes().size() > 0 || bar.getBpmNotes().size() > 0) {
                        mainDataSb.append("\r\n");
                    }
                }
            }
            String output = getBmsHeader(headerSb.toString()) + mainDataSb.toString();
            out.write(output.getBytes("SHIFT-JIS"));
            out.close();
        } catch (IOException e) {
            System.out.println("Error:   file not found or reading error");
        } catch (Exception e) {
            System.out.println(String.format("Error:   %s", e.getMessage()));
        }
    }
	
	private static int gcd(int a, int b) {
		return b==0 ? a : gcd(b,a % b);

	private static int minMultiple(int a, int b) {
		return a*b/gcd(a,b);
		
    private static int minMultiple(int a, int b) {
        int r = a, s = a, t = b;
        if (a < b) {
            r = a;
            a = b;
            b = r;
        }
        while (r != 0) {
            r = a % b;
            a = b;
            b = r;
        }
        return s * t / a;
    }

    private static int minMultipleNotes(List<Note> notes) throws Exception {
        int i = 1;
        if (notes.size() == 0) {
            throw new Exception("minMultiple error: 0 num");
        } else if (notes.size() == 1) {
            return notes.get(0).getDivider();
        } else {
            int result = notes.get(0).getDivider();
            while (i < notes.size()) {
                result = minMultiple(result, notes.get(i).getDivider());
                i++;
            }
            return result;
        }
    }

    private static int minMultipleBpmNotes(List<BpmNote> bpmNotes) throws Exception {
        int i = 1;
        if (bpmNotes.size() == 0) {
            throw new Exception("minMultiple error: 0 num");
        } else if (bpmNotes.size() == 1) {
            return bpmNotes.get(0).getDivider();
        } else {
            int result = bpmNotes.get(0).getDivider();
            while (i < bpmNotes.size()) {
                result = minMultiple(result, bpmNotes.get(i).getDivider());
                i++;
            }
            return result;
        }
    }

    private static void buildStringOfNotes(List<Note> notes, String barNum, String track, StringBuilder sb) throws Exception {
        if (notes.size() > 0) {
            int minMultiple = minMultipleNotes(notes);
            String[] line = new String[minMultiple];
            for (int i = 0; i < line.length; i++) {
                line[i] = "00";
            }
            for (Note note : notes) {
                int num = note.getNum();
                num *= (minMultiple / note.getDivider());
                line[num] = "01";
            }
            sb.append(String.format("#%s%s:", barNum, track));
            for (String text : line) {
                sb.append(text);
            }
            sb.append("\r\n");
        }
    }

    private static String getBmsHeader(String addedHeader) throws Exception {
        return "\r\n" +
                "*---------------------- HEADER FIELD\r\n" +
                "\r\n" +
                "#PLAYER 1\r\n" +
                "#GENRE " + HtmlParser.getGenre(FILE_PATH) + "\r\n" +
                "#TITLE " + HtmlParser.getTitle(FILE_PATH) + "\r\n" +
                "#ARTIST " + HtmlParser.getArtist(FILE_PATH) + "\r\n" +
                "#BPM " + HtmlParser.getStartBpm(FILE_PATH) + "\r\n" +
                "#PLAYLEVEL " + HtmlParser.getLevel(FILE_PATH) + "\r\n" +
                "#RANK 3\r\n" +
                "\r\n" +
                "#TOTAL " + HtmlParser.getNoteCount(FILE_PATH) + "\r\n" +
                "#LNTYPE 1\r\n" +
                "\r\n" +
                "#WAV02 " + HtmlParser.getNameAttr(FILE_PATH) + ".wav\r\n" +
                addedHeader +
                "\r\n" +
                "*---------------------- EXPANSION FIELD\r\n" +
                "#BMP01 " + HtmlParser.getNameAttr(FILE_PATH) + ".wmv\r\n" +
                "\r\n" +
                "*---------------------- MAIN DATA FIELD\r\n" +
                "\r\n" +
                "#00001:02\r\n" +
                "#00004:01\r\n" +
                "\r\n";
    }

    private static String getFileName(boolean fix, boolean fixWithScratch) throws Exception {
        return HtmlParser.getTitle(FILE_PATH) + "(" + HtmlParser.getNameAttr(FILE_PATH) + ")_SP" + HtmlParser.getDiff(FILE_PATH).charAt(0) + (fix ? "" : (fixWithScratch ? "_FIX_ALL" : "_ORIGINAL")) + ".bms";
    }
}
