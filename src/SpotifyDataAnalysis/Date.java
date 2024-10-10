package SpotifyDataAnalysis;

import java.io.Serializable;

public class Date implements Comparable<Date>, Serializable {

    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;

    //CONSTRUCTORS
    public Date() {
        this(1970);
    }
    public Date(int year) {
        this(year,1);
    }
    public Date(int year, int month) {
        this(year,month,1);
    }
    public Date(int year, int month, int day) {
        this(year,month,day,1,0,1);
    }
    public Date(int year, int month, int day, int hour, int minute, int second) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    //MUTATORS
    public void setYear(int year) {
        this.year = year;
    }
    public void setMonth(int month) {
        this.month = month;
    }
    public void setDay(int day) {
        this.day = day;
    }
    public void setHour(int hour) {
        this.hour = hour;
    }
    public void setMinute(int minute) {
        this.minute = minute;
    }
    public void setSecond(int second) {
        this.second = second;
    }

    public void add(int year, int month, int day, int hour, int minute, int second) {
        this.year+=year;
        this.month+=month;
        this.day+=day;
        this.hour+=hour;
        this.minute+=minute;
        this.second+=second;

        this.minute+=this.second/60;
        this.hour+=this.minute/60;
        this.day+=this.hour/24;
        this.month+=this.day/30;
        this.year+=(this.month-1)/12;

        this.second=this.second%60;
        this.minute=this.minute%60;
        this.hour=this.hour%24;
        this.day=this.day%30;
        this.month=(this.month-1)%12;
    }
    public void addMonth(int m) {
        add(0,m,0,0,0,0);
    }
    //ACCESSORS
    public int getYear() {
        return year;
    }
    public int getMonth() {
        return month;
    }
    public int getDay() {
        return day;
    }
    public int getHour() {
        return hour;
    }
    public int getMinute() {
        return minute;
    }
    public int getSecond() {
        return second;
    }

    public String toString() {
        return year + "-" + toTwoDigits(month) + "-" + toTwoDigits(day) + " " +
                toTwoDigits(hour) + ":" + toTwoDigits(minute) + ":" + toTwoDigits(second);
    }
    public String simpleToString() {
        return toTwoDigits(month) + "/" + toTwoDigits(day) + "/" + year;
    }
    public int compareTo(Date d) {
        if (d==null) return 0;
        if (year!=d.getYear()) return year-d.getYear();
        else if (month!=d.getMonth()) return month-d.getMonth();
        else if (day!=d.getDay()) return day-d.getDay();
        else if (hour!=d.getHour()) return hour-d.getHour();
        else if (minute!=d.getMinute()) return minute-d.getMinute();
        else if (second!=d.getSecond()) return second-d.getSecond();
        else return 0;
    }
    public Date copy() {
        return new Date(this.year,this.month,this.day,this.hour,this.minute,this.second);
    }

    //HELPERS
    private String toTwoDigits(int n) {
        if (n>=0&n<10) return "0" + n;
        else return Integer.toString(n);
    }




}
