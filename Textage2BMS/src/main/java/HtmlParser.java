import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Yifan on 2017/4/27.
 * HtmlParser
 */
class HtmlParser {

    static List<Bar> read(String path, boolean fix, boolean fixWithScratch) throws Exception {
        File file = new File(path);
        Document doc = Jsoup.parse(file, "UTF-8");
        List<Bar> bars = new ArrayList<Bar>();
        Elements allColumns = doc.body().getElementsByTag("table").first().getElementsByTag("tbody").first().getElementsByTag("tr").first().getElementsByTag("td");
        for (Element column : allColumns) {
            Elements tables = column.getElementsByTag("table");
            for (Element table : tables) {
                Element tr = table.getElementsByTag("tbody").first().getElementsByTag("tr").first();
                int currentBar;
                Element div = tr.getElementsByTag("td").first().getElementsByTag("div").first();
                String barStyle = div.attr("style");
                int height;
                int realHeight;
                boolean isLastTable = false;
                if (table.elementSiblingIndex() == 0 && tables.size() > 1) {
                    Element infoTr = table.nextElementSibling().getElementsByTag("tbody").first().getElementsByTag("tr").first();
                    String infoBarStyle = infoTr.getElementsByTag("td").first().getElementsByTag("div").first().attr("style");
                    height = Integer.parseInt(infoBarStyle.substring(infoBarStyle.indexOf("height:") + 7, infoBarStyle.lastIndexOf("px")));
                    realHeight = Integer.parseInt(barStyle.substring(barStyle.indexOf("height:") + 7, barStyle.lastIndexOf("px")));
                    if (realHeight < height && realHeight <= 64) {
                        currentBar = Integer.parseInt(infoTr.getElementsByTag("th").first().text());
                        currentBar++;
                        isLastTable = true;
                    } else {
                        currentBar = Integer.parseInt(tr.getElementsByTag("th").first().text());
                        height = realHeight;
                    }
                } else {
                    currentBar = Integer.parseInt(tr.getElementsByTag("th").first().text());
                    height = Integer.parseInt(barStyle.substring(barStyle.indexOf("height:") + 7, barStyle.lastIndexOf("px")));
                    realHeight = height;
                }
                Bar bar = new Bar(currentBar, height);
                for (Element img : div.getElementsByTag("img")) {
                    String imgStyle = img.attr("style");
                    String imgName = img.attr("name");
                    String imgSrc = img.attr("src");
                    if (imgSrc.contains("t.gif")) {
                        int marginTop = Integer.parseInt(imgStyle.substring(4, imgStyle.indexOf("px")));
                        if (isLastTable) {
                            marginTop += (height - realHeight);
                        }
                        boolean isBpmFound = false;
                        for (Element span : div.getElementsByTag("span")) {
                            String spanStyle = span.attr("style");
                            spanStyle = spanStyle.substring(spanStyle.indexOf("top:"), spanStyle.length());
                            if (Integer.parseInt(spanStyle.substring(4, spanStyle.indexOf("px"))) == marginTop - 5) {
                                String bpm = span.text();
                                isBpmFound = true;
                                BpmNote bpmNote = new BpmNote();
                                bpmNote.setBar(currentBar);
                                try {
                                    bpmNote.setBpm(Integer.parseInt(bpm));
                                    bpmNote.setBpmInteger(true);
                                } catch (NumberFormatException e) {
                                    bpmNote.setBpm(Double.parseDouble(bpm));
                                    bpmNote.setBpmInteger(false);
                                }
                                int status = setBpmNotePosition(bpmNote, marginTop, bar.getBarLength());
                                if (status == -1) {
                                    System.out.println(String.format("Error:   set bpmNote position failed with marginTop %d and barLength %d at bar %d", marginTop, bar.getBarLength(), currentBar));
                                }
                                bar.addBpmNote(bpmNote);
                                break;
                            }
                        }
                        if (!isBpmFound) {
                            throw new Exception("bpm changed can't be found in bar " + currentBar);
                        }
                    } else if (!(imgSrc.contains("l") || imgSrc.contains("h") || imgSrc.contains("x"))) {
                        int marginTop = Integer.parseInt(imgStyle.substring(4, imgStyle.indexOf("px")));
                        if (isLastTable) {
                            marginTop += (height - realHeight);
                        }
                        int marginLeft = Integer.parseInt(imgStyle.substring(imgStyle.indexOf("left:") + 5, imgStyle.lastIndexOf("px")));
                        Note note = new Note();
                        note.setBar(currentBar);
                        if (imgName.equals("")) {
                            switch (marginLeft) {
                                case 0:
                                    note.setTrack(10);
                                    break;
                                case 37:
                                    note.setTrack(11);
                                    break;
                                case 51:
                                    note.setTrack(12);
                                    break;
                                case 65:
                                    note.setTrack(13);
                                    break;
                                case 79:
                                    note.setTrack(14);
                                    break;
                                case 93:
                                    note.setTrack(15);
                                    break;
                                case 107:
                                    note.setTrack(16);
                                    break;
                                case 121:
                                    note.setTrack(17);
                                    break;
                                default:
                                    throw new Exception(String.format("marginLeft %d error at bar %d", marginLeft, currentBar));
                            }
                        } else {
                            switch (marginLeft) {
                                case 0:
                                    note.setTrack(0);
                                    break;
                                case 37:
                                    note.setTrack(1);
                                    break;
                                case 51:
                                    note.setTrack(2);
                                    break;
                                case 65:
                                    note.setTrack(3);
                                    break;
                                case 79:
                                    note.setTrack(4);
                                    break;
                                case 93:
                                    note.setTrack(5);
                                    break;
                                case 107:
                                    note.setTrack(6);
                                    break;
                                case 121:
                                    note.setTrack(7);
                                    break;
                                default:
                                    throw new Exception(String.format("marginLeft %d error at bar %d", marginLeft, currentBar));
                            }
                        }
                        int status = setNotePosition(note, marginTop, bar.getBarLength());
                        if (status == -1) {
                            System.out.println(String.format("Error:   set Note position failed with marginTop %d and barLength %d at bar %d", marginTop, bar.getBarLength(), currentBar));
                        }
                        bar.addNote(note);
                    }
                }
                Collections.sort(bar.getNotes(), new Comparator<Note>() {
                    public int compare(Note o1, Note o2) {
                        return o2.getMarginTop() - o1.getMarginTop();
                    }
                });
                bars.add(bar);
            }
        }
        Collections.sort(bars, new Comparator<Bar>() {
            public int compare(Bar o1, Bar o2) {
                return o1.getBarNum() - o2.getBarNum();
            }
        });
        if (fix) {
            fixLongNoteEnd(bars, false);
        } else if (fixWithScratch) {
            fixLongNoteEnd(bars, true);
        }
        for (int i = 0; i < bars.size(); i++) {
            if (i != bars.size() - 1) {
                bars.get(i).checkBar(bars.get(i + 1));
            }
        }
        return bars;
    }

    static String getTitle(String path) throws Exception {
        File file = new File(path);
        Document doc = Jsoup.parse(file, "UTF-8");
        return doc.body().getElementsByTag("nobr").first().getElementsByTag("b").text();
    }

    static String getGenre(String path) throws Exception {
        File file = new File(path);
        Document doc = Jsoup.parse(file, "UTF-8");
        Element nobr = doc.body().getElementsByTag("nobr").first();
        String text = nobr.text();
        int index = text.indexOf('\"');
        int endIndex = text.indexOf('\"', index + 1);
        return text.substring(index + 1, endIndex);
    }

    static String getArtist(String path) throws Exception {
        File file = new File(path);
        Document doc = Jsoup.parse(file, "UTF-8");
        Element nobr = doc.body().getElementsByTag("nobr").first();
        String title = nobr.getElementsByTag("b").text();
        String text = nobr.text();
        text = text.substring(text.indexOf(title + " / ") + title.length() + 3);
        return text.substring(0, text.indexOf("bpm:") - 1);
    }

    static String getDiff(String path) throws Exception {
        File file = new File(path);
        Document doc = Jsoup.parse(file, "UTF-8");
        String text = doc.body().getElementsByTag("nobr").first().getElementsByTag("font").first().text();
        return text.substring(4, text.length() - 1);
    }

    static String getLevel(String path) throws Exception {
        File file = new File(path);
        Document doc = Jsoup.parse(file, "UTF-8");
        String text = doc.body().getElementsByTag("nobr").first().text();
        text = text.substring(text.indexOf(" - ★") + 4, text.indexOf(" Notes:"));
        String[] full = {"１", "２", "３", "４", "５", "６", "７", "８", "９"};
        String[] half = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};
        for (int i = 0; i < full.length; i++) {
            if (text.equals(full[i])) {
                return half[i];
            }
        }
        return text;
    }

    static String getStartBpm(String path) throws Exception {
        File file = new File(path);
        Document doc = Jsoup.parse(file, "UTF-8");
        String text = doc.body().getElementsByTag("nobr").first().text();
        text = text.substring(text.indexOf(" bpm:") + 5, text.indexOf(" - ★"));
        if (text.contains("～")) {
            Elements spans = doc.body().getElementsByTag("table").first().getElementsByTag("tbody").first().getElementsByTag("tr").first().getElementsByTag("td").first().getElementsByTag("table").last().getElementsByTag("tbody").first().getElementsByTag("tr").first().getElementsByTag("td").first().getElementsByTag("div").first().getElementsByTag("span");
            int max = -1;
            int j = -1;
            for (int i = 0; i < spans.size(); i++) {
                String style = spans.get(i).attr("style");
                style = style.substring(style.indexOf("top:"));
                style = style.substring(4, style.indexOf("px"));
                if (Integer.parseInt(style) > max) {
                    max = Integer.parseInt(style);
                    j = i;
                }
            }
            return spans.get(j).text();
        } else {
            return text;
        }
    }

    static String getNameAttr(String path) throws Exception {
        File file = new File(path);
        Document doc = Jsoup.parse(file, "UTF-8");
        String text = doc.body().getElementsByTag("img").last().attr("src");
        if (text.contains("?tag=_")) {
            return text.substring(text.indexOf("?tag=_") + 6);
        } else {
            return text.substring(text.indexOf("?tag=") + 5);
        }
    }

    static String getNoteCount(String path) throws Exception {
        File file = new File(path);
        Document doc = Jsoup.parse(file, "UTF-8");
        String text = doc.body().getElementsByTag("nobr").first().text();
        return text.substring(text.indexOf(" Notes:") + 7);
    }

    static int setNotePosition(Note note, int marginTop, int barLength) {
        int position = marginTop + 5;
        position = barLength - position;
        note.setMarginTop(marginTop);
        note.setPosition(position);
        if (position == 0) {
            note.setDivider(1);
            note.setNum(0);
            return 0;
        } else if (position == barLength) {
            note.setDivider(1);
            note.setNum(0);
            note.setBar(note.getBar() + 1);
            note.setPosition(0);
            return 0;
        } else if (position > barLength) {
            position = position - barLength;
            note.setBar(note.getBar() + 1);
            note.setPosition(position);
            return 0;
        }
        int[] selectLen;
        if (barLength == 768) { //16
            selectLen = new int[]{1, 2, 3, 4, 6, 8, 12, 16, 24, 32, 48, 64, 96, 128, 192, 256};
        } else if (barLength == 576) { //12
            selectLen = new int[]{1, 2, 3, 4, 6, 8, 9, 12, 16, 18, 24, 32, 36, 48, 64, 72, 96, 144, 192};
        } else if (barLength == 384) { //8
            selectLen = new int[]{1, 2, 3, 4, 6, 8, 12, 16, 24, 32, 48, 64, 96, 128};
        } else if (barLength == 192) { //4
            selectLen = new int[]{1, 2, 3, 4, 6, 8, 12, 16, 24, 32, 48, 64};
        } else {
            try {
                selectLen = getSelectLen(barLength);
            } catch (Exception e) {
                return -1;
            }
        }
        double bestDis = 0.5;
        boolean isSet = false;
        for (int aSelectLen : selectLen) {
            double perLen = barLength / (double) aSelectLen;
            for (int j = 0; j < aSelectLen; j++) {
                double tempHeight = j * perLen - position;
                if (tempHeight == 0) {
                    note.setDivider(aSelectLen);
                    note.setNum(j);
                    return 0;
                } else if (tempHeight >= -0.5 && tempHeight < 0.5) {
                    if (bestDis > Math.abs(tempHeight)) {
                        bestDis = Math.abs(tempHeight);
                        note.setDivider(aSelectLen);
                        note.setNum(j);
                        isSet = true;
                    }
                }
            }
        }
        if (isSet) {
            System.out.println("Info:    Detected guess.");
        }
        return isSet ? 0 : -1;
    }

    private static int setNotePosition(Note note, int marginTop, int barLength, boolean isBarAdded) {
        if (isBarAdded) {
            note.setPosition(note.getPosition() + 12);
            return 0;
        } else {
            return setNotePosition(note, marginTop, barLength);
        }
    }

    private static int setBpmNotePosition(BpmNote bpmNote, int marginTopOfLine, int barLength) {
        int position = marginTopOfLine + 2;
        position = barLength - position;
        if (position == 0) {
            bpmNote.setDivider(1);
            bpmNote.setNum(0);
            return 0;
        } else {
            double max = barLength / 2;
            int i = 2;
            while (i <= max) {
                if (barLength % i == 0) {
                    if (position % (barLength / i) == 0) {
                        bpmNote.setDivider(i);
                        bpmNote.setNum(position / (barLength / i));
                        return 0;
                    }
                }
                i++;
            }
        }
        int[] selectLen;
        if (barLength == 768) { //16
            selectLen = new int[]{1, 2, 3, 4, 6, 8, 12, 16, 24, 32, 48, 64, 96, 128, 192, 256};
        } else if (barLength == 576) { //12
            selectLen = new int[]{1, 2, 3, 4, 6, 8, 9, 12, 16, 18, 24, 32, 36, 48, 64, 72, 96, 144, 192};
        } else if (barLength == 384) { //8
            selectLen = new int[]{1, 2, 3, 4, 6, 8, 12, 16, 24, 32, 48, 64, 96, 128};
        } else if (barLength == 192) { //4
            selectLen = new int[]{1, 2, 3, 4, 6, 8, 12, 16, 24, 32, 48, 64};
        } else {
            try {
                selectLen = getSelectLen(barLength);
            } catch (Exception e) {
                return -1;
            }
        }
        double bestDis = 1.0;
        boolean isSet = false;
        for (int aSelectLen : selectLen) {
            double perLen = barLength / (double) aSelectLen;
            for (int j = 0; j < aSelectLen; j++) {
                double tempHeight = j * perLen - position;
                if (tempHeight == 0) {
                    bpmNote.setDivider(aSelectLen);
                    bpmNote.setNum(j);
                    return 0;
                } else if (tempHeight > 0 && tempHeight < 1) {
                    if (bestDis > tempHeight) {
                        bestDis = tempHeight;
                        bpmNote.setDivider(aSelectLen);
                        bpmNote.setNum(j);
                        isSet = true;
                    }
                }
            }
        }
        if (isSet) {
            System.out.println("Info:    Detected guess.");
        }
        return isSet ? 0 : -1;
    }

    private static int[] getSelectLen(int barLength) throws Exception {
        List<Integer> list = new ArrayList<Integer>();
        int divNum1;
        int divNum2;
        if (barLength % 4 == 0 && barLength % 3 == 0) {
            divNum1 = barLength / 4;
            divNum2 = barLength / 3;
        } else {
            throw new Exception();
        }
        int maxTry = divNum1;
        for (int i = 1; i < maxTry; i++) {
            if (divNum1 % i == 0) {
                list.add(i);
                int k = divNum1 / i;
                if (k != i) {
                    list.add(k);
                }
                maxTry = k;
            }
        }
        maxTry = divNum2;
        for (int i = 1; i < maxTry; i++) {
            if (divNum2 % i == 0) {
                if (!list.contains(i)) {
                    list.add(i);
                }
                int k = divNum2 / i;
                if (k != i) {
                    if (!list.contains(k)) {
                        list.add(k);
                    }
                }
                maxTry = k;
            }
        }
        Collections.sort(list);
        int[] result = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    private static void fixLongNoteEnd(List<Bar> bars, boolean isScratchFix) throws Exception {
        boolean[] isInLongNote = new boolean[]{false, false, false, false, false, false, false, false};
        for (Bar bar : bars) {
            for (Note note : bar.getNotes()) {
                switch (note.getTrack()) {
                    case 10:
                        if (isScratchFix) {
                            if (isInLongNote[0]) {
                                int status = setNotePosition(note, note.getMarginTop() - 12, bar.getBarLength(), note.getBar() != bar.getBarNum());
                                if (status == -1) {
                                    System.out.println(String.format("Error:   fix charge Note end position failed with marginTop %d and barLength %d at bar %d", note.getMarginTop() - 12, bar.getBarLength(), bar.getBarNum()));
                                }
                                isInLongNote[0] = false;
                            } else {
                                isInLongNote[0] = true;
                            }
                        }
                        break;
                    case 11:
                        if (isInLongNote[1]) {
                            int status = setNotePosition(note, note.getMarginTop() - 12, bar.getBarLength(), note.getBar() != bar.getBarNum());
                            if (status == -1) {
                                System.out.println(String.format("Error:   fix charge Note end position failed with marginTop %d and barLength %d at bar %d", note.getMarginTop() - 12, bar.getBarLength(), bar.getBarNum()));
                            }
                            isInLongNote[1] = false;
                        } else {
                            isInLongNote[1] = true;
                        }
                        break;
                    case 12:
                        if (isInLongNote[2]) {
                            int status = setNotePosition(note, note.getMarginTop() - 12, bar.getBarLength(), note.getBar() != bar.getBarNum());
                            if (status == -1) {
                                System.out.println(String.format("Error:   fix charge Note end position failed with marginTop %d and barLength %d at bar %d", note.getMarginTop() - 12, bar.getBarLength(), bar.getBarNum()));
                            }
                            isInLongNote[2] = false;
                        } else {
                            isInLongNote[2] = true;
                        }
                        break;
                    case 13:
                        if (isInLongNote[3]) {
                            int status = setNotePosition(note, note.getMarginTop() - 12, bar.getBarLength(), note.getBar() != bar.getBarNum());
                            if (status == -1) {
                                System.out.println(String.format("Error:   fix charge Note end position failed with marginTop %d and barLength %d at bar %d", note.getMarginTop() - 12, bar.getBarLength(), bar.getBarNum()));
                            }
                            isInLongNote[3] = false;
                        } else {
                            isInLongNote[3] = true;
                        }
                        break;
                    case 14:
                        if (isInLongNote[4]) {
                            int status = setNotePosition(note, note.getMarginTop() - 12, bar.getBarLength(), note.getBar() != bar.getBarNum());
                            if (status == -1) {
                                System.out.println(String.format("Error:   fix charge Note end position failed with marginTop %d and barLength %d at bar %d", note.getMarginTop() - 12, bar.getBarLength(), bar.getBarNum()));
                            }
                            isInLongNote[4] = false;
                        } else {
                            isInLongNote[4] = true;
                        }
                        break;
                    case 15:
                        if (isInLongNote[5]) {
                            int status = setNotePosition(note, note.getMarginTop() - 12, bar.getBarLength(), note.getBar() != bar.getBarNum());
                            if (status == -1) {
                                System.out.println(String.format("Error:   fix charge Note end position failed with marginTop %d and barLength %d at bar %d", note.getMarginTop() - 12, bar.getBarLength(), bar.getBarNum()));
                            }
                            isInLongNote[5] = false;
                        } else {
                            isInLongNote[5] = true;
                        }
                        break;
                    case 16:
                        if (isInLongNote[6]) {
                            int status = setNotePosition(note, note.getMarginTop() - 12, bar.getBarLength(), note.getBar() != bar.getBarNum());
                            if (status == -1) {
                                System.out.println(String.format("Error:   fix charge Note end position failed with marginTop %d and barLength %d at bar %d", note.getMarginTop() - 12, bar.getBarLength(), bar.getBarNum()));
                            }
                            isInLongNote[6] = false;
                        } else {
                            isInLongNote[6] = true;
                        }
                        break;
                    case 17:
                        if (isInLongNote[7]) {
                            int status = setNotePosition(note, note.getMarginTop() - 12, bar.getBarLength(), note.getBar() != bar.getBarNum());
                            if (status == -1) {
                                System.out.println(String.format("Error:   fix charge Note end position failed with marginTop %d and barLength %d at bar %d", note.getMarginTop() - 12, bar.getBarLength(), bar.getBarNum()));
                            }
                            isInLongNote[7] = false;
                        } else {
                            isInLongNote[7] = true;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
