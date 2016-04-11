package unidue.rc.model;


import unidue.rc.model.auto._Setting;

public class Setting extends _Setting implements Comparable<Setting> {

    @Override
    public int compareTo(Setting setting) {
       return this.getKey().compareTo(setting.getKey());
    }


}
