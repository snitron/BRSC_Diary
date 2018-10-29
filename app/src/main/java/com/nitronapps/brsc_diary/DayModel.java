package com.nitronapps.brsc_diary;

public class DayModel {
    private int count;
    private String[] lessons, homeworks, marks;
    private boolean isWeekend;

    DayModel(int x, String[] y, String[] z, String[] a, boolean b){
        count = x;
        lessons = y;
        homeworks = z;
        marks = a;
        isWeekend = b;
    }

    public int getCount() {
        return count;
    }

    public String[] getHomeworks() {
        return homeworks;
    }

    public String[] getLessons() {
        return lessons;
    }

    public String[] getMarks() {
        return marks;
    }

    public boolean isWeekend() {
        return isWeekend;
    }
}
