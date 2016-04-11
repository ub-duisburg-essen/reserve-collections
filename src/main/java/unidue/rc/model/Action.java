package unidue.rc.model;


import unidue.rc.model.auto._Action;

public class Action extends _Action {

    public Integer getId() {
        return CayenneUtils.getID(this, ID_PK_COLUMN);
    }
}
